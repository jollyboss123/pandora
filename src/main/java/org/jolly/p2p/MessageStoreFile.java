package org.jolly.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

public class MessageStoreFile implements Serializable {
    @Serial
    private static final long serialVersionUID = -5261472589235681855L;

    private final String key;
    private final int size;
    private final byte[] data;

    private MessageStoreFile(String key, int size, byte[] data) {
        this.key = key;
        this.size = size;
        this.data = data;
    }

    public static MessageStoreFile of(String key, int size, byte[] data) {
        return new MessageStoreFile(key, size, data);
    }

    public String getKey() {
        return key;
    }

    public int getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "MessageStoreFile{key=%s, size=%d, data=%s}".formatted(key, size, Arrays.toString(data));
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }
}
