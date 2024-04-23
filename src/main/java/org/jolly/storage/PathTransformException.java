package org.jolly.storage;

import java.io.Serial;

public class PathTransformException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7082522122914655535L;

    public PathTransformException() {
        super();
    }

    public PathTransformException(String msg) {
        super(msg);
    }

    public PathTransformException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public PathTransformException(Throwable cause) {
        super(cause);
    }
}
