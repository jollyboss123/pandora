package org.jolly.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class PathKey {
    private final String pathName;
    private final String filename;

    public PathKey(String pathName, String filename) {
        Objects.requireNonNull(pathName);
        Objects.requireNonNull(filename);
        requireNonEmpty(pathName);
        requireNonEmpty(filename);

        this.pathName = pathName;
        this.filename = filename;
    }

    public String getPathName() {
        return pathName;
    }

    public String getFilename() {
        return filename;
    }

    public Path getFullPath() {
        return Paths.get(pathName, filename);
    }

    public Path getParent() {
        return Paths.get(pathName.split("/")[0]);
    }

    private static void requireNonEmpty(String obj) {
        if (obj.isEmpty() || obj.isBlank())
            throw new NullPointerException();
    }
}
