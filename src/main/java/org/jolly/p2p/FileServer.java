package org.jolly.p2p;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.io.IOUtils;
import org.jolly.io.TeeInputStream;
import org.jolly.p2p.encoding.*;
import org.jolly.storage.Store;
import org.jolly.storage.StoreConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileServer implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(FileServer.class);

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
        log.info("storing {}", key);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        TeeInputStream tee = TeeInputStream.of(in, buf);
        int size = store.write(key, tee);

        MessageStoreFile payload = MessageStoreFile.of(key, size);
        ObjectEncoder<MessageStoreFile> encoder = new ObjectEncoder<>();
        byte[] encodedPayload = encoder.encode(payload);
//        broadcast(RPC.of("stor", encodedPayload, null));
        broadcast(RPC.of("stop", buf.toByteArray(), null));

        //TODO: fix this
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//        broadcast(RPC.of("stop", buf.toByteArray(), null));
    }

    private void bootstrapNetwork() {
        log.info("bootstrapping network");
        for (int addr : cfg.getBootstrapNodes()) {
            try {
                cfg.getTransport().dial(addr);
            } catch (UnknownHostException e) {
                log.warn("dial {} (UnknownHostEx): ", addr,e);
            } catch (IOException e) {
                log.warn("dial {} (IOEx): ", addr, e);
            } catch (InvalidHandshakeException e) {
                log.warn("dial {} (InvalidHandshakeEx)): ", addr, e);
            }
        }
    }

    private OnPeer onPeer() {
        return peer -> {
            log.info("updating peers");
            peerLock.writeLock().lock();
            try {
                peers.put(peer.getRemoteAddress().toString(), peer);
                log.info("connected with remote {}", peer.getRemoteAddress());
            } finally {
                peerLock.writeLock().unlock();
            }
        };
    }

    private void listen() {
        cfg.getTransport().consume().stream()
                .peek(rpc -> log.info("message received: {}", rpc))
                .forEach(rpc -> {
                    log.info("do something here with: {}", rpc);
//                    ObjectDecoder<MessageStoreFile> decoder = new ObjectDecoder<>();
//                    MessageStoreFile msg = decoder.decode(rpc.getPayloadBytes());
//                    log.info("store file msg: {}", msg);

                    log.info("here");

                    try {
                        store.write("stor", new ByteArrayInputStream(rpc.getPayloadBytes()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void broadcast(RPC rpc) {
        Encoder<RPC> encoder = new DefaultRPCEncoder();
        for (TCPPeer peer : peers.values()) {
            log.info("sending to peer [{}]", peer);
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
