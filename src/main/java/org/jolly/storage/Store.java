package org.jolly.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private Store(Function<String, PathKey> transformPath, Set<PosixFilePermission> filePermissions) {
        this.transformPath = transformPath;
        this.filePermissions = filePermissions;
    }

    private Store(Function<String, PathKey> transformPath) {
        this(transformPath, PosixFilePermissions.fromString("rwxr-xr-x"));
    }

    private Store() {
        this(new DefaultTransformPath(), PosixFilePermissions.fromString("rwxr-xr-x"));
    }

    public static Store create() {
        return new Store();
    }

    public static Store create(Function<String, PathKey> transformPath) {
        return new Store(transformPath);
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

        if (Files.exists(pathKey.getParent())) {
            try (Stream<Path> fileStream = Files.walk(pathKey.getParent())) {
                fileStream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    InputStream readStream(String key) throws IOException {
        PathKey pathKey = transformPath.apply(key);
        return Files.newInputStream(pathKey.getFullPath());
    }

    void writeStream(String key, InputStream in) throws IOException {
        PathKey pathKey = transformPath.apply(key);
        Path fullPath = pathKey.getFullPath();

        if (!Files.exists(fullPath.getParent())) {
            Files.createDirectories(pathKey.getFullPath().getParent(), PosixFilePermissions.asFileAttribute(filePermissions));
            log.info("created directory: {}", fullPath.getParent());
        }
        if (!Files.exists(fullPath)) {
            Files.createFile(pathKey.getFullPath());
            log.info("created file: {}", fullPath.getFileName());
        }

        try (OutputStream out = new FileOutputStream(fullPath.toFile())) {
            int n = copy(in, out);
            log.info("written {} bytes to disk", n);
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
}
