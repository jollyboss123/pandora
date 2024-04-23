package org.jolly.storage;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class StoreConfig {
    private final String root;
    private final TransformPath transformPath;
    private final Set<PosixFilePermission> filePermissions;

    private static final String DEFAULT_ROOT = "default";
    private static final String DEFAULT_PERMISSIONS = "rwxr-xr-x";

    private StoreConfig(String root, TransformPath transformPath, Set<PosixFilePermission> filePermissions) {
        if (root == null || root.isEmpty() || root.isBlank()) {
            root = DEFAULT_ROOT;
        }
        if (transformPath == null) {
            transformPath = new DefaultTransformPath();
        }
        if (filePermissions == null || filePermissions.isEmpty()) {
            filePermissions = PosixFilePermissions.fromString(DEFAULT_PERMISSIONS);
        }

        this.root = root;
        this.transformPath = transformPath;
        this.filePermissions = filePermissions;
    }

    public static StoreConfig of() {
        return new StoreConfig(DEFAULT_ROOT, new DefaultTransformPath(), PosixFilePermissions.fromString(DEFAULT_PERMISSIONS));
    }

    public static StoreConfig of(String root) {
        return new StoreConfig(root, new DefaultTransformPath(), PosixFilePermissions.fromString(DEFAULT_PERMISSIONS));
    }

    public static StoreConfig of(String root, TransformPath transformPath) {
        return new StoreConfig(root, transformPath, PosixFilePermissions.fromString(DEFAULT_PERMISSIONS));
    }

    public static StoreConfig of(String root, TransformPath transformPath, Set<PosixFilePermission> filePermissions) {
        return new StoreConfig(root, transformPath, filePermissions);
    }

    public String getRoot() {
        return root;
    }

    public TransformPath getTransformPath() {
        return transformPath;
    }

    public Set<PosixFilePermission> getFilePermissions() {
        return filePermissions;
    }
}
