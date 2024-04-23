package org.jolly.server;

import org.jolly.p2p.RPC;
import org.jolly.p2p.Transport;
import org.jolly.storage.TransformPath;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class FileServerConfig {
    private final String storageRoot;
    private final TransformPath transformPath;
    private final Transport transport;

    private FileServerConfig(String storageRoot, TransformPath transformPath, Transport transport) {
        Objects.requireNonNull(transport);
        this.storageRoot = storageRoot;
        this.transformPath = transformPath;
        this.transport = transport;
    }

    public static FileServerConfig of(Transport transport) {
        return new FileServerConfig(null, null, transport);
    }

    public static FileServerConfig of(Transport transport, String storageRoot, TransformPath transformPath) {
        return new FileServerConfig(storageRoot, transformPath, transport);
    }

    public String getStorageRoot() {
        return storageRoot;
    }

    public TransformPath getTransformPath() {
        return transformPath;
    }

    public Transport getTransport() {
        return transport;
    }

    public BlockingQueue<RPC> getRPCChannel() {
        return transport.consume();
    }
}
