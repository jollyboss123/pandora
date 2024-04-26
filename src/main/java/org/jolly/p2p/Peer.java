package org.jolly.p2p;

import org.jolly.p2p.encoding.Decoder;
import org.jolly.p2p.encoding.Encoder;

import java.net.SocketAddress;

/**
 * Peer represents a remote node.
 */
public interface Peer extends AutoCloseable {
    void send(RPC msg, Encoder<RPC> encoder);
    RPC receive(Decoder<RPC> decoder);
    SocketAddress getRemoteAddress();
}
