package org.jolly.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.p2p.PeerHandler;
import org.jolly.p2p.Peer;
import org.jolly.p2p.RPC;
import org.jolly.storage.Store;
import org.jolly.storage.StoreConfig;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileServer implements AutoCloseable, PeerHandler {
    private static final Logger log = LogManager.getLogger(FileServer.class);

    private final FileServerConfig cfg;
    private final Store store;
    private volatile boolean running = false;
    private final ExecutorService executor;
    private final ReentrantReadWriteLock peerLock;
    private final Map<String, Peer> peers;

    private FileServer(FileServerConfig cfg) {
        StoreConfig storeCfg = StoreConfig.of(cfg.getStorageRoot(), cfg.getTransformPath());
        cfg.getTransport().getCfg().setOnPeer(this);
        this.cfg = cfg;
        this.store = Store.of(storeCfg);
        this.executor = Executors.newCachedThreadPool();
        this.peers = new ConcurrentHashMap<>();
        this.peerLock = new ReentrantReadWriteLock();
    }

    public static FileServer of(FileServerConfig cfg) {
        return new FileServer(cfg);
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

    @Override
    public void onPeer(Peer peer) {
        log.info("updating peers");
        peerLock.writeLock().lock();
        try {
            peers.put(peer.getRemoteAddress().toString(), peer);
            log.info("connected with remote {}", peer.getRemoteAddress());
        } finally {
            peerLock.writeLock().unlock();
        }
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
            log.error("file server listen interrupted", e);
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
