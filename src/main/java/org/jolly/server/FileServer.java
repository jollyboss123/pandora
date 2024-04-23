package org.jolly.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.p2p.RPC;
import org.jolly.storage.Store;

import java.io.IOException;
import java.util.concurrent.*;

public class FileServer implements AutoCloseable {
    private static final Logger log = LogManager.getLogger(FileServer.class);

    private final FileServerConfig cfg;
    private final Store store;
    private volatile boolean running = false;
    private final ExecutorService executor;

    private FileServer(FileServerConfig cfg, Store store) {
        this.cfg = cfg;
        this.store = store;
        this.executor = Executors.newCachedThreadPool();
    }

    public static FileServer of(FileServerConfig cfg) {
        return new FileServer(cfg, Store.of());
    }

    public void start() {
        running = true;
        executor.submit(() -> cfg.getTransport().listen());
        executor.submit(this::listen);
        bootstrapNetwork();
    }

    public void stop() throws Exception {
        log.info("sending quit signal");
        close();
    }

    private void bootstrapNetwork() {
        for (int addr : cfg.getBootstrapNodes()) {
            executor.submit(() -> {
                log.info("trying to connect on: {}", addr);
                try {
                    cfg.getTransport().dial(addr);
                } catch (IOException e) {
                    log.warn("failed to connect on: {}", addr, e);
                }
            });
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
        log.info("quitting file server");
        running = false;
        cfg.getTransport().close();
        executor.shutdown();
    }
}
