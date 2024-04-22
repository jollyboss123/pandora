package org.jolly.p2p;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TCPTransport implements Transport {
    private static final Logger log = LogManager.getLogger(TCPTransport.class);

    private final int port;
    private final Handshake handshake;
    private final Decoder<Message> decoder;
    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Map<SocketAddress, Peer> peers;

    private final AtomicBoolean running = new AtomicBoolean(true);

    private TCPTransport(int port, Handshake handshake, Decoder<Message> decoder, Map<SocketAddress, Peer> peers) {
        this.port = port;
        this.handshake = handshake;
        this.decoder = decoder;
        this.peers = peers;
    }

    private TCPTransport(int port) {
        this(port, new NOPHandshake(), new DefaultDecoder(), new ConcurrentHashMap<>());
    }

    public static Transport create(int port) {
        return new TCPTransport(port);
    }

    public static Transport create(int port, Handshake handshake, Decoder<Message> decoder) {
        return new TCPTransport(port, handshake, decoder, new ConcurrentHashMap<>());
    }

    @Override
    public void listen() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            accept(serverSocket);
        } catch (IOException e) {
            log.error("failed to start server");
            System.exit(1);
        }
    }

    private void accept(ServerSocket serverSocket) {
        while (running.get()) {
            log.info("waiting for new tcp client connection");
            try (Socket socket = serverSocket.accept()) {

                handleConn(socket);
                log.info("tcp client connected on: {}", socket.getPort());

            } catch (IOException e) {
                log.error("tcp accept error");
                throw new IllegalStateException(e);
            } catch (InvalidHandshakeException e) {
                log.error("tcp handshake error");
                throw new IllegalStateException(e);
            } catch (ClassNotFoundException e) {
                log.error("tcp decode input stream error");
                throw new IllegalStateException(e);
            }
        }
    }

    private void handleConn(Socket socket) throws InvalidHandshakeException, IOException, ClassNotFoundException {
        TCPPeer peer = (TCPPeer) TCPPeer.createOutbound(socket);
        log.info("tcp peer created: {}", peer);

        handshake.perform(peer);

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
                    Message msg = decoder.decode(new ByteArrayInputStream(messageBytes));
                    Message finalMsg = new Message(msg.getPayload(), socket.getRemoteSocketAddress());
                    log.info("tcp received message: {}", finalMsg);

                    // move the remaining bytes to the beginning of the buffer
                    buffer.reset();
                    buffer.write(data, eolIndex + 1, data.length - eolIndex - 1);
                    data = buffer.toByteArray();
                }
            }
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

    int getPort() {
        return port;
    }
}
