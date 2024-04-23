package org.jolly.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class Store {
    private static final Logger log = LogManager.getLogger(Store.class);

    private final Function<String, PathKey> transformPath;
    private final Set<PosixFilePermission> filePermissions;
    private static final String DEFAULT_PERMISSIONS = "rwxr-xr-x";
    // root is the root directory for the system containing stored directories/files.
    private final String root;
    private static final String DEFAULT_ROOT = "default";

    private Store(Function<String, PathKey> transformPath, Set<PosixFilePermission> filePermissions, String root) {
        this.transformPath = transformPath;
        this.filePermissions = filePermissions;
        this.root = root;
    }

    private Store(Function<String, PathKey> transformPath) {
        this(transformPath, PosixFilePermissions.fromString(DEFAULT_PERMISSIONS), DEFAULT_ROOT);
    }

    private Store() {
        this(new DefaultTransformPath(), PosixFilePermissions.fromString(DEFAULT_PERMISSIONS), DEFAULT_ROOT);
    }

    public static Store create() {
        return new Store();
    }

    public static Store create(Function<String, PathKey> transformPath) {
        return new Store(transformPath);
    }

    public static Store create(Function<String, PathKey> transformPath, String root) {
        return new Store(transformPath, PosixFilePermissions.fromString(DEFAULT_PERMISSIONS), root);
    }

    public InputStream read(String key) throws IOException {
        try (InputStream in = readStream(key)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n = copy(in, out);
            log.info("read {} bytes from disk", n);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public void delete(String key) throws IOException {
        PathKey pathKey = transformPath.apply(key);
        Path parent = prependRoot(pathKey.getParent());

        if (Files.exists(parent)) {
            try (Stream<Path> fileStream = Files.walk(parent)) {
                fileStream.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                if (!Files.deleteIfExists(path)) {
                                    log.warn("delete failed, file: {} does not exist", path);
                                }
                            } catch (IOException e) {
                                log.error("file delete error for: {}", path, e);
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
        log.info("deleted: {} from disk", pathKey.getFullPath());
    }

    public boolean has(String key) {
        PathKey pathKey = transformPath.apply(key);
        Path fullPath = prependRoot(pathKey.getFullPath());
        return Files.exists(fullPath);
    }

    InputStream readStream(String key) throws IOException {
        PathKey pathKey = transformPath.apply(key);
        Path fullPath = prependRoot(pathKey.getFullPath());
        return Files.newInputStream(fullPath);
    }

    void writeStream(String key, InputStream in) throws IOException {
        PathKey pathKey = transformPath.apply(key);
        Path fullPath = prependRoot(pathKey.getFullPath());

        if (!Files.exists(fullPath.getParent())) {
            Files.createDirectories(fullPath.getParent(), PosixFilePermissions.asFileAttribute(filePermissions));
            log.info("created directory: {}", fullPath.getParent());
        }
        if (!Files.exists(fullPath)) {
            Files.createFile(fullPath);
            log.info("created file: {}", fullPath.getFileName());
        }

        try (OutputStream out = new FileOutputStream(fullPath.toFile())) {
            int n = copy(in, out);
            log.info("written {} bytes to disk: {}", n, fullPath);
        }
    }

    private static int copy(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(in);
        Objects.requireNonNull(out);

        byte[] buf = new byte[8192];
        int count = 0;
        int n;

        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
            count += n;
        }
        return count;
    }

    private Path prependRoot(Path path) {
        return Paths.get(root, path.toString());
    }
}
