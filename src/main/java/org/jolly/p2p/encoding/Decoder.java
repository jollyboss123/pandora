package org.jolly.p2p.encoding;

import java.io.Serializable;

public interface Decoder<T extends Serializable> {
    T decode(byte[] buf);
}
