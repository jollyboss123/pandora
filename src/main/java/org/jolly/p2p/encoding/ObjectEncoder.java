package org.jolly.p2p.encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectEncoder<T extends Serializable> implements Encoder<T> {

    @Override
    public byte[] encode(T obj) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {

            objectOutputStream.writeObject(obj);
            return out.toByteArray();
        } catch (IOException e) {
            throw new EncodingException(e);
        }
    }
}
