package org.jolly.storage;

import java.util.function.Function;

public class DefaultTransformPath implements Function<String, PathKey> {

    @Override
    public PathKey apply(String s) {
        return new PathKey(s, s);
    }
}
