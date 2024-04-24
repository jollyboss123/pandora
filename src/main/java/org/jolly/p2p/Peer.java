package org.jolly.p2p;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Peer represents a remote node.
 */
public interface Peer {
    SocketAddress getRemoteAddress();
    void send(byte[] data) throws IOException;
}
