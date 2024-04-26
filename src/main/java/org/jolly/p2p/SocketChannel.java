package org.jolly.p2p;

import java.io.IOException;
import java.net.SocketAddress;

public interface SocketChannel {
    void write(byte[] b) throws IOException;
    int read() throws IOException;
    int read(byte[] b) throws IOException;
    void close() throws IOException;
    SocketAddress getRemoteAddr();
}
