package org.jolly.server;

import org.jolly.InfiniteBlockingQueue;
import org.jolly.p2p.RPC;
import org.jolly.p2p.Transport;
import org.jolly.storage.TransformPath;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class FileServerConfig {
    private final String storageRoot;
    private final TransformPath transformPath;
    private final Transport transport;
    private final int[] bootstrapNodes;

    private FileServerConfig(String storageRoot, TransformPath transformPath, Transport transport, int[] bootstrapNodes) {
        Objects.requireNonNull(transport, "transport");
        this.storageRoot = storageRoot;
        this.transformPath = transformPath;
        this.transport = transport;
        this.bootstrapNodes = bootstrapNodes;
    }

    public static FileServerConfig of(Transport transport) {
        return new FileServerConfig(null, null, transport, new int[]{});
    }

    public static FileServerConfig of(Transport transport, String storageRoot, TransformPath transformPath) {
        return new FileServerConfig(storageRoot, transformPath, transport, new int[]{});
    }

    public static FileServerConfig of(Transport transport, String storageRoot, TransformPath transformPath, int[] bootstrapNodes) {
        return new FileServerConfig(storageRoot, transformPath, transport, bootstrapNodes);
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

    public InfiniteBlockingQueue<RPC> getRPCChannel() {
        return transport.consume();
    }

    public int[] getBootstrapNodes() {
        return bootstrapNodes;
    }
}
