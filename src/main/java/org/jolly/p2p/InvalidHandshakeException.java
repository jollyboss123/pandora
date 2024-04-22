package org.jolly.p2p;

import java.io.Serial;

public class InvalidHandshakeException extends Exception {
    @Serial
    private static final long serialVersionUID = 6209430154449045655L;

    public InvalidHandshakeException() {
        super();
    }

    public InvalidHandshakeException(String msg) {
        super(msg);
    }
}
