package org.jolly.p2p.encoding;

import java.io.*;

public class ObjectEncoder<T extends Serializable> implements Encoder<T> {

    @Override
    public byte[] encode(T obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
            objectOutputStream.writeObject(obj);
            return out.toByteArray();
        } catch (IOException e) {
            throw new EncodingException(e);
        }
    }
}
