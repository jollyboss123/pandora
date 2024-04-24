package org.jolly.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.stream.Stream;

public class Store {
    private static final Logger log = LogManager.getLogger(Store.class);

    private final StoreConfig cfg;

    private Store(StoreConfig cfg) {
        this.cfg = cfg;
    }

    public static Store of(StoreConfig cfg) {
        return new Store(cfg);
    }

    public static Store of() {
        return new Store(StoreConfig.of());
    }

    public InputStream read(String key) throws IOException {
        try (InputStream in = readStream(key)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n = IOUtils.copy(in, out);
            log.info("read {} bytes from disk", n);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public void write(String key, InputStream in) throws IOException {
        writeStream(key, in);
    }

    public void delete(String key) throws IOException {
        PathKey pathKey = cfg.getTransformPath().apply(key);
        Path parent = prependRoot(pathKey.getParent());

        log.info("deleting key: {} from disk", key);
        recursiveDelete(parent);
    }

    public boolean has(String key) {
        PathKey pathKey = cfg.getTransformPath().apply(key);
        Path fullPath = prependRoot(pathKey.getFullPath());
        return Files.exists(fullPath);
    }

    InputStream readStream(String key) throws IOException {
        PathKey pathKey = cfg.getTransformPath().apply(key);
        Path fullPath = prependRoot(pathKey.getFullPath());
        return Files.newInputStream(fullPath);
    }

    void writeStream(String key, InputStream in) throws IOException {
        PathKey pathKey = cfg.getTransformPath().apply(key);
        Path fullPath = prependRoot(pathKey.getFullPath());

        if (!Files.exists(fullPath.getParent())) {
            Files.createDirectories(fullPath.getParent(), PosixFilePermissions.asFileAttribute(cfg.getFilePermissions()));
            log.info("created directory: {}", fullPath.getParent());
        }
        if (!Files.exists(fullPath)) {
            Files.createFile(fullPath);
            log.info("created file: {}", fullPath.getFileName());
        }

        try (OutputStream out = new FileOutputStream(fullPath.toFile())) {
            int n = IOUtils.copy(in, out);
            log.info("written {} bytes to disk: {}", n, fullPath);
        }
    }

    void clear() throws IOException {
        log.info("deleting root: {} from disk", cfg.getRoot());
        recursiveDelete(Paths.get(cfg.getRoot()));
    }

    private static void recursiveDelete(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> fileStream = Files.walk(path)) {
                fileStream.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                if (!Files.deleteIfExists(p)) {
                                    log.warn("delete failed, file: {} does not exist", p);
                                }
                            } catch (IOException e) {
                                log.error("file delete error for: {}", p, e);
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
        log.info("deleted: {} from disk", path);
    }

    private Path prependRoot(Path path) {
        return Paths.get(cfg.getRoot(), path.toString());
    }

    public StoreConfig getCfg() {
        return cfg;
    }
}
