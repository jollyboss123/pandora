package org.jolly.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Payload implements Serializable {
    @Serial
    private static final long serialVersionUID = 456894349201583492L;

    /**
     * Key. Must be non-null.
     * @serial
     */
    private final String key;
    /**
     * Data. Must be non-null.
     * @serial
     */
    private final byte[] data;

    public Payload(String key, byte[] data) {
        this.key = key;
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public byte[] getData() {
        return data;
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }

    @Override
    public String toString() {
        return "Payload{key=%s, data=%s}".formatted(key, new String(data, StandardCharsets.UTF_8));
    }
}
