package org.jolly.p2p.encoding;

import java.io.*;

public class ObjectDecoder<T extends Serializable> implements Decoder<T> {

    @Override
    public T decode(byte[] buf) {
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new EncodingException(e);
        }
    }
}
