package org.jolly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.p2p.TCPTransport;
import org.jolly.p2p.TCPTransportConfig;
import org.jolly.server.FileServer;
import org.jolly.server.FileServerConfig;
import org.jolly.storage.CASTransformPath;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pandora {
    private static final Logger log = LogManager.getLogger(Pandora.class);

    public static void main(String[] args) {
        TCPTransportConfig cfg = TCPTransportConfig.of(3000);
        TCPTransport t = (TCPTransport) TCPTransport.of(cfg);
        FileServerConfig fileServerConfig = FileServerConfig.of(t, "testing", new CASTransformPath(), new int[]{4000, 3000});

        try (FileServer fs = FileServer.of(fileServerConfig);
            ExecutorService executor = Executors.newCachedThreadPool()) {
            executor.submit(fs::start);

            CountDownLatch latch = new CountDownLatch(1);

            // Keep the application running until interrupted or the latch is released
            executor.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    log.error("Latch await interrupted", e);
                    Thread.currentThread().interrupt();
                }
            });

//            Thread.sleep(5000);
//            fs.stop();
        } catch (Exception e) {
            log.error("File server error", e);
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }
}
