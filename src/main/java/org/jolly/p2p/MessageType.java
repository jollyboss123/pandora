package org.jolly.p2p;

import java.util.Arrays;
import java.util.Optional;

public enum MessageType {
    STORE("stor"), FETCH("fetc");

    private final String name;

    MessageType(String s) {
        this.name = s;
    }

    public String getName() {
        return name;
    }

    public static MessageType from(String name) {
        return Arrays.stream(MessageType.values())
                .filter(t -> t.name.equals(name))
                .findFirst()
                .orElse(null);
    }
}
