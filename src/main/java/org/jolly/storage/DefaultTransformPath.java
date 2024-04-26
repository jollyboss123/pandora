package org.jolly.storage;

public class DefaultTransformPath implements TransformPath {

    @Override
    public PathKey apply(String s) {
        return new PathKey(s, s);
    }
}
