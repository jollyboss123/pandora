package org.jolly.p2p;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Peer represents a remote node.
 */
public interface Peer {
    /**
     * getRemoteAddress returns the remote address of this peer's
     * underlying connection.
     *
     * @return {@link SocketAddress} the remote address.
     */
    SocketAddress getRemoteAddress();
    void send(byte[] data) throws IOException;
}
