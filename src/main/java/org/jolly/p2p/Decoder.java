package org.jolly.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public interface Decoder<T extends Serializable> {
    T decode(InputStream in) throws IOException, ClassNotFoundException;
}
