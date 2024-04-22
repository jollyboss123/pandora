package org.jolly.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ObjectDecoder<T extends Serializable> implements Decoder<T> {

    @Override
    public T decode(InputStream in) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
            return (T) objectInputStream.readObject();
        }
    }
}
