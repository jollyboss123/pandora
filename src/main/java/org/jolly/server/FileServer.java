package org.jolly.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.p2p.RPC;
import org.jolly.storage.Store;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileServer implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(FileServer.class);
    private final FileServerConfig cfg;
    private final Store store;
    private volatile boolean running = true;

    private FileServer(FileServerConfig cfg, Store store) {
        this.cfg = cfg;
        this.store = store;
    }

    public static FileServer of(FileServerConfig cfg) {
        return new FileServer(cfg, Store.of());
    }

    public void start() {
        try (ExecutorService executor = Executors.newCachedThreadPool()) {
            executor.submit(() -> cfg.getTransport().listen());
            executor.submit(this::listen);
        }
    }

    private void listen() {
        log.info("waiting for transport message");
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                RPC rpc = cfg.getTransport().consume().take();
                log.info("file server received message: {}", rpc);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() throws Exception {
        running = false;
    }
}
