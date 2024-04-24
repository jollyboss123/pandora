package org.jolly.p2p.encoding;

import java.io.Serial;

public class EncodingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -1794614528153528639L;

    public EncodingException() {
        super();
    }

    public EncodingException(String msg) {
        super(msg);
    }

    public EncodingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public EncodingException(Throwable cause) {
        super(cause);
    }
}
