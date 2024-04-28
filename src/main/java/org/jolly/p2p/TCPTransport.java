package org.jolly.p2p;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.p2p.encoding.Decoder;
import org.jolly.p2p.encoding.DefaultRPCDecoder;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class TCPTransport implements Transport {
    private static final Logger log = LogManager.getLogger(TCPTransport.class);
    private volatile boolean shutdown = false;
    private final ExecutorService executor;
    private final InfiniteBlockingQueue<RPC> msgChan;
    private final TCPTransportConfig cfg;
    private final Phaser phaser = new Phaser(1);

    private TCPTransport(TCPTransportConfig cfg) {
        this.cfg = cfg;
        this.executor = Executors.newCachedThreadPool();
        this.msgChan = new InfiniteBlockingQueue<>(1024);
    }

    public static TCPTransport of(TCPTransportConfig cfg) {
        return new TCPTransport(cfg);
    }

    @Override
    public void listen() {
        try {
            ServerSocket s = new ServerSocket(cfg.getPort());
//            s.setSoTimeout(2000);
            s.setSoTimeout(0);

            accept(s);

            s.close();
        } catch (SocketException e) {
            log.info(cfg.getPort());
            log.error("stopping accept loop (SocketEx): ", e);
            System.exit(1);
        } catch (IOException e) {
            log.error("stopping accept loop (IOEx): ", e);
            System.exit(1);
        }

        shutdown = true;
    }

    @Override
    public void dial(int port) throws IOException, InvalidHandshakeException {
        Socket socket = new Socket((String) null, port);
        handleConn(socket);
    }

    /**
     * Consume will return a read-only channel for reading
     * incoming messages received from another peer in the network.
     *
     * @return a message channel.
     */
    @Override
    public InfiniteBlockingQueue<RPC> consume() {
        return this.msgChan;
    }

    public Phaser getPhaser() {
        return this.phaser;
    }

    @Override
    public TransportConfig getConfig() {
        return this.cfg;
    }

    private void accept(ServerSocket s) throws IOException {
        do {
            log.info("listening...");
            try {
                Socket clientSocket = s.accept();
                clientSocket.setSoTimeout(0);

                executor.submit(() -> {
                    try {
                        handleConn(clientSocket);
                    } catch (IOException e) {
                        log.error("handle conn (IOEx): ", e);
                    } catch (InvalidHandshakeException e) {
                        log.error("handle conn (InvalidHandshakeEx): ", e);
                    }
                });
            } catch (SocketTimeoutException e) {
                log.info(e);
            }
        } while (!shutdown);
    }

    private void handleConn(Socket socket) throws IOException, InvalidHandshakeException {
        try (SocketWrapper sw = SocketWrapper.of(socket);
             TCPPeer peer = TCPPeer.of(sw);) {
            log.info("tcp peer created: {}", peer);

            cfg.getHandshake().perform(peer);

            if (cfg.getOnPeer() != null) {
                cfg.getOnPeer().perform(peer);
            }

            RPC msg = peer.receive(cfg.getDecoder());
            log.info("peer [{}] message received: {}", peer, msg);
            try {
                msgChan.put(msg);
    //            boolean offer = msgChan.offer(msg);
    //            log.info("offer: {}", offer);
                if (msg.getType().equals(MessageType.STORE.getName())) {
                    int i = 0;
                    while (!msg.getType().equals("done")) {
                        i++;
                        log.info("here {}", i);
                        log.info("sock: {}", sw);
                        msg = peer.receive(cfg.getDecoder());
                        log.info("here 2: {}", msg);
                        msgChan.put(msg);
    //                    offer = msgChan.offer(msg);
    //                    log.info("offer: {}", offer);
                        log.info("done {}", i);
                    }
                }
    //            log.info("producer queue size: {}", msgChan.size());
                log.info("done done");
            } catch (InterruptedException ignored) {
                log.error("msg channel put error for peer: [{}]", peer, ignored);
            }
        }
//        SocketWrapper sw = SocketWrapper.of(socket);
//        log.info("tcp client connected on: {}", socket.getPort());
//
//        TCPPeer peer = TCPPeer.of(sw);
//        log.info("tcp peer created: {}", peer);
//
//        cfg.getHandshake().perform(peer);
//
//        if (cfg.getOnPeer() != null) {
//            cfg.getOnPeer().perform(peer);
//        }
//
//        RPC msg = peer.receive(cfg.getDecoder());
//        log.info("peer [{}] message received: {}", peer, msg);
////        try {
////            msgChan.put(msg);
//            boolean offer = msgChan.offer(msg);
//            log.info("offer: {}", offer);
//            if (msg.getType().equals(MessageType.STORE.getName())) {
//                int i = 0;
//                while (msg != null) {
//                    i++;
//                    log.info("here {}", i);
//                    msg = peer.receive(cfg.getDecoder());
//                    log.info("here 2: {}", msg);
////                    msgChan.put(msg);
//                    offer = msgChan.offer(msg);
//                    log.info("offer: {}", offer);
//                    log.info("done {}", i);
//                }
//            }
//            log.info("producer queue size: {}", msgChan.size());
////        } catch (InterruptedException ignored) {
////            log.error("msg channel put error for peer: [{}]", peer, ignored);
////        }
//
//        peer.close();
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
    }
}
