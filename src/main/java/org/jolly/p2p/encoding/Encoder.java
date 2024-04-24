package org.jolly.p2p.encoding;

import java.io.Serializable;

public interface Encoder<T extends Serializable> {
    byte[] encode(T obj);
}
