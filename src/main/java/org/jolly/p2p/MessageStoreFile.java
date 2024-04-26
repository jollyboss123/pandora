package org.jolly.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;

public class MessageStoreFile implements Serializable {
    @Serial
    private static final long serialVersionUID = -5261472589235681855L;

    private final String key;
    private final int size;

    private MessageStoreFile(String key, int size) {
        this.key = key;
        this.size = size;
    }

    public static MessageStoreFile of(String key, int size) {
        return new MessageStoreFile(key, size);
    }

    public String getKey() {
        return key;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "MessageStoreFile{key=%s, size=%d}".formatted(key, size);
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }
}
