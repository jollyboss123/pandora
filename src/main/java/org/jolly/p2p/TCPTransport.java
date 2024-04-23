package org.jolly.p2p;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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
            } catch (ClassNotFoundException e) {
                log.error("tcp dial decode input stream error on: {}", socket.getPort(), e);
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
                } catch (ClassNotFoundException e) {
                    log.error("tcp accept decode input stream error on: {}", socket.getPort(), e);
                    throw new IllegalStateException(e);
                }
            });
        }
    }

    private void handleConn(Socket socket, boolean outbound) throws InvalidHandshakeException, IOException, ClassNotFoundException {
        TCPPeer peer = (TCPPeer) TCPPeer.of(socket, outbound);
        log.info("tcp peer created: {}", peer);

        cfg.getHandshake().perform(peer);

        if (cfg.getOnPeer() != null) {
            cfg.getOnPeer().apply(peer);
        }

        try (InputStream in = socket.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;

            while ((n = in.read(buf)) != -1) {
                buffer.write(buf, 0, n);
                byte[] data = buffer.toByteArray();

                int eolIndex;
                while ((eolIndex = indexOf(data, (byte) '\n')) != -1) {
                    byte[] messageBytes = Arrays.copyOf(data, eolIndex);
                    RPC msg = cfg.getDecoder().decode(new ByteArrayInputStream(messageBytes));
                    RPC finalMsg = new RPC(msg.getPayload(), socket.getRemoteSocketAddress());
                    log.info("tcp received message: {}", finalMsg);
                    rpcChannel.put(finalMsg);

                    // move the remaining bytes to the beginning of the buffer
                    buffer.reset();
                    buffer.write(data, eolIndex + 1, data.length - eolIndex - 1);
                    data = buffer.toByteArray();
                }
            }
        } catch (InterruptedException e) {
            log.error("rpc channel put error for peer: [{}]", peer, e);
            Thread.currentThread().interrupt();
        }
    }

    private static int indexOf(byte[] array, byte target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public TCPTransportConfig getCfg() {
        return cfg;
    }

    @Override
    public void close() {
        log.info("quitting tcp transport");
        running = false;
    }
}
