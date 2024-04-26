package org.jolly.p2p.encoding;

public interface Encoder<T> {
    byte[] encode(T obj);
}
