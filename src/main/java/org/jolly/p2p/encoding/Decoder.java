package org.jolly.p2p.encoding;

import org.jolly.p2p.SocketChannel;

import java.io.IOException;

public interface Decoder<T> {
    T decode(SocketChannel socketChannel) throws IOException;
}
