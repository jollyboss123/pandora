package org.jolly.p2p.encoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ObjectDecoder<T extends Serializable> {

    public T decode(byte[] buf) {
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new EncodingException(e);
        }
    }
}
