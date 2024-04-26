package org.jolly.p2p;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * SocketWrapper is a wrapper class to encapsulate {@link Socket} and expose convenience methods
 * for a {@link Peer} to utilize.
 */
public class SocketWrapper extends Socket implements SocketChannel {
    private final InputStream in;
    private final OutputStream out;
    private final SocketAddress addr;

    private SocketWrapper(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    private SocketWrapper(Socket socket) throws IOException {
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
        addr = socket.getRemoteSocketAddress();
    }

    public static SocketWrapper of(String host, int port) throws IOException {
        return new SocketWrapper(host, port);
    }

    public static SocketWrapper of(Socket socket) throws IOException {
        return new SocketWrapper(socket);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        out.flush();
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public SocketAddress getRemoteAddr() {
        return addr;
    }
}
