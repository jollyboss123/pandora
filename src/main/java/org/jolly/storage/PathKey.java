package org.jolly.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class PathKey {
    private static final Logger log = LogManager.getLogger(PathKey.class);
    private final String pathName;
    private final String filename;

    public PathKey(String pathName, String filename) {
        Objects.requireNonNull(pathName, "pathName");
        Objects.requireNonNull(filename, "fileName");
        requireNonEmpty(pathName, "pathName");
        requireNonEmpty(filename, "filename");

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
        String parent = pathName.split("/")[0];
        requireNonEmpty(parent, "parent");
        return Paths.get(parent);
    }

    private static void requireNonEmpty(String str, String msg) {
        if (str.isEmpty() || str.isBlank()) {
            log.error("should not be empty or blank");
            throw new IllegalArgumentException("%s should not be empty or blank".formatted(msg));
        }
    }
}
