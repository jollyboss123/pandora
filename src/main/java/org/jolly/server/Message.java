package org.jolly.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 11341112876419323L;

    private final byte[] payload;

    private Message(byte[] payload) {
        this.payload = payload;
    }

    public static Message of(byte[] payload) {
        Objects.requireNonNull(payload, "payload");
        return new Message(payload);
    }

    public byte[] getPayload() {
        return payload;
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }

    @Override
    public String toString() {
        return "Message{payload=%s}".formatted(new String(payload, StandardCharsets.UTF_8));
    }
}
