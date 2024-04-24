package org.jolly.p2p;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class TCPTransport implements Transport {
    private static final Logger log = LogManager.getLogger(TCPTransport.class);

    private final TCPTransportConfig cfg;
    private final ArrayBlockingQueue<RPC> rpcChannel;
    private final ExecutorService executor;

    private volatile boolean running = true;

    private TCPTransport(TCPTransportConfig cfg, ArrayBlockingQueue<RPC> rpcChannel) {
        this.cfg = cfg;
        this.rpcChannel = rpcChannel;
        this.executor = Executors.newCachedThreadPool();
    }

    private TCPTransport(TCPTransportConfig cfg) {
        this(cfg, new ArrayBlockingQueue<>(1024));
    }

    public static Transport of(TCPTransportConfig cfg) {
        return new TCPTransport(cfg);
    }

    @Override
    public void listen() {
        try (ServerSocket serverSocket = new ServerSocket(this.cfg.getPort())) {

            accept(serverSocket);
        } catch (IOException e) {
            log.error("failed to start server");
            System.exit(1);
        }
    }

    @Override
    public void dial(int port) throws IOException {
        Socket socket = new Socket((String) null, port);
        executor.submit(() -> {
            try {
                log.info("tcp dial client connected on: {}", socket.getPort());
                handleConn(socket, true);

            } catch (IOException e) {
                log.error("tcp dial accept error on: {}", socket.getPort(), e);
                throw new IllegalStateException(e);
            } catch (InvalidHandshakeException e) {
                log.error("tcp dial handshake error on: {}", socket.getPort(), e);
                throw new IllegalStateException(e);
            }
        });
    }

    @Override
    public BlockingQueue<RPC> consume() {
        return this.rpcChannel;
    }

    private void accept(ServerSocket serverSocket) throws IOException {
        while (running) {
            log.info("waiting for new tcp client connection");
            Socket socket = serverSocket.accept();
            executor.submit(() -> {
                try {
                    log.info("tcp client connected on: {}", socket.getPort());
                    handleConn(socket, false);

                } catch (IOException e) {
                    log.error("tcp accept error on: {}", socket.getPort(), e);
                    throw new IllegalStateException(e);
                } catch (InvalidHandshakeException e) {
                    log.error("tcp accept handshake error on: {}", socket.getPort(), e);
                    throw new IllegalStateException(e);
                }
            });
        }
    }

    private void handleConn(Socket socket, boolean outbound) throws InvalidHandshakeException, IOException {
        TCPPeer peer = (TCPPeer) TCPPeer.of(socket, outbound);
        log.info("tcp peer created: {}", peer);

        cfg.getHandshake().perform(peer);

        if (cfg.getPeerHandler() != null) {
            cfg.getPeerHandler().onPeer(peer);
        }

        try (InputStream in = socket.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];

            for (int n; (n = in.read(buf)) != -1; ) {
                buffer.write(buf, 0, n);
                byte[] data = buffer.toByteArray();

                RPC msg = cfg.getDecoder().decode(data);
                RPC finalMsg = new RPC(msg.getPayload(), socket.getRemoteSocketAddress());
                log.info("tcp received message: {}", finalMsg);
                rpcChannel.put(finalMsg);
            }
        } catch (InterruptedException e) {
            log.error("rpc channel put error for peer: [{}]", peer, e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public TCPTransportConfig getCfg() {
        return cfg;
    }

    @Override
    public void close() {
        log.info("quitting tcp transport");
        running = false;
    }
}
