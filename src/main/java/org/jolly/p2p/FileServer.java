package org.jolly.p2p;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jolly.io.TeeInputStream;
import org.jolly.p2p.encoding.*;
import org.jolly.storage.Store;
import org.jolly.storage.StoreConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileServer implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(FileServer.class);

    private static final ObjectDecoder<MessageStoreFile> msfDecoder = new ObjectDecoder<>();
    private static final ObjectEncoder<MessageStoreFile> msfEncoder = new ObjectEncoder<>();
    private final Marker marker;
    private final ExecutorService executor;
    private final ReadWriteLock peerLock;
    private final Map<String, TCPPeer> peers;
    private final FileServerConfig cfg;
    private final Store store;

    private FileServer(FileServerConfig cfg) {
        StoreConfig storeCfg = StoreConfig.of(cfg.getStorageRoot(), cfg.getTransformPath());
        cfg.getTransport().getConfig().setOnPeer(onPeer());
        this.cfg = cfg;
        this.store = Store.of(storeCfg);
        this.executor = Executors.newCachedThreadPool();
        this.peers = new ConcurrentHashMap<>();
        this.peerLock = new ReentrantReadWriteLock();
        this.marker = MarkerManager.getMarker("file-server-%d".formatted(cfg.getTransport().getConfig().getPort()));
    }

    public static FileServer of(FileServerConfig cfg) {
        return new FileServer(cfg);
    }

    public void start() {
        executor.submit(() -> cfg.getTransport().listen());
        bootstrapNetwork();
        executor.submit(this::listen);
    }

    public void store(String key, InputStream in) throws IOException {
        log.info(marker, "storing {}", key);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        TeeInputStream tee = TeeInputStream.of(in, buf);
        int size = store.write(key, tee);

        MessageStoreFile payload = MessageStoreFile.of(key, size, buf.toByteArray());
        byte[] encodedPayload = msfEncoder.encode(payload);
        broadcast(RPC.of(MessageType.STORE.getName(), encodedPayload, null));
//        payload = MessageStoreFile.of(key, 10, buf.toByteArray());
//        encodedPayload = msfEncoder.encode(payload);
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        broadcast(RPC.of("smtg", "random bytes".getBytes(), null));
        broadcast(RPC.of("give", "random".getBytes(), null));
        broadcast(RPC.of("done", "nothing".getBytes(), null));

        //TODO: fix this
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // check if exist locally
    // broadcast and get reply from peers in network if file exist there
    // store locally if file does exist in other nodes
    public InputStream fetch(String key) throws IOException {
        if (store.has(key)) {
            log.info(marker, "file {} stored in local", key);
            return store.read(key);
        }

        log.info(marker, "file {} not stored in local, fetching from network", key);

        return null;
    }

    private void bootstrapNetwork() {
        log.info(marker, "bootstrapping network");
        for (int addr : cfg.getBootstrapNodes()) {
            try {
                cfg.getTransport().dial(addr);
            } catch (UnknownHostException e) {
                log.warn(marker, "dial {} (UnknownHostEx): ", addr,e);
            } catch (IOException e) {
                log.warn(marker, "dial {} (IOEx): ", addr, e);
            } catch (InvalidHandshakeException e) {
                log.warn(marker, "dial {} (InvalidHandshakeEx)): ", addr, e);
            }
        }
    }

    private OnPeer onPeer() {
        return peer -> {
            log.info(marker, "updating peers");
            peerLock.writeLock().lock();
            try {
                peers.put(peer.getRemoteAddress().toString(), peer);
                log.info(marker, "connected with remote {}", peer.getRemoteAddress());
            } finally {
                peerLock.writeLock().unlock();
            }
        };
    }

    private void listen() {
//        for (;;) {
//            try {
//                log.info("producer queue size: {}", cfg.getRPCChannel().size());
//
//                RPC rpc = cfg.getRPCChannel().take();
//                log.info(marker, "message received: {}", rpc);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
        cfg.getRPCChannel().stream()
                .peek(rpc -> log.info(marker, "message received: {}", rpc))
                .forEach(rpc -> {
                    log.info(marker, "handle message: {}", rpc);

//                    Peer peer = peers.get("localhost/127.0.0.1:3000");
//                    log.info(marker, "peer: {} from: {}", peer, rpc.getFrom());
//                    log.info(marker, "peers: {}", peers);
//                    RPC next = peer.receive(new DefaultRPCDecoder());
//                    log.info(marker, "next: {}", next);
//                    log.info(marker, "phaser: {}", ((TCPTransport) cfg.getTransport()).getPhaser().arrive());
//
//                    MessageType type = MessageType.from(rpc.getType());
//                    switch (type) {
//                        case STORE -> handleStoreFileMessage(rpc);
//                        case FETCH -> {
//                            //TODO:
//                        }
//                        case null, default -> log.warn(marker, "unsupported message type: {}", rpc.getType());
//                    }
                });
    }

    private void handleStoreFileMessage(RPC rpc) {
        MessageStoreFile msg = msfDecoder.decode(rpc.getPayloadBytes());
        log.info(marker, "store file msg: {}", msg);

        try {
            store.write(msg.getKey(), new ByteArrayInputStream(msg.getData(), 0, msg.getSize()));
            ((TCPTransport) cfg.getTransport()).getPhaser().arriveAndDeregister();
        } catch (IOException e) {
            //TODO: improve
            throw new RuntimeException(e);
        }
    }

    private void broadcast(RPC rpc) {
        Encoder<RPC> encoder = new DefaultRPCEncoder();
        for (TCPPeer peer : peers.values()) {
            log.info(marker, "sending to peer [{}]", peer);
            RPC newRPC = RPC.of(rpc.getType(), rpc.getPayloadBytes(), peer.getRemoteAddress().toString());
            peer.send(newRPC, encoder);
        }
    }

    @Override
    public void close() throws Exception {
        cfg.getTransport().close();
        executor.shutdown();
    }
}
