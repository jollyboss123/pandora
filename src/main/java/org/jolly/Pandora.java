package org.jolly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.p2p.TCPTransport;
import org.jolly.p2p.TCPTransportConfig;
import org.jolly.server.FileServer;
import org.jolly.server.FileServerConfig;
import org.jolly.storage.CASTransformPath;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pandora {
    private static final Logger log = LogManager.getLogger(Pandora.class);

    public static void main(String[] args) {
        try (FileServer fs1 = make(3000, new int[]{});
             FileServer fs2 = make(4000, new int[]{3000});
            ExecutorService executor = Executors.newCachedThreadPool()) {
            executor.submit(fs1::start);
            executor.submit(fs2::start);

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

            ByteArrayInputStream in = new ByteArrayInputStream("big data file".getBytes());
            fs1.store("privatedata", in);
//            Thread.sleep(5000);
//            fs.stop();
        } catch (Exception e) {
            log.error("File server error", e);
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }

    static FileServer make(int port, int[] nodes) {
        TCPTransportConfig tcpTransportConfig = TCPTransportConfig.of(port);
        TCPTransport t = (TCPTransport) TCPTransport.of(tcpTransportConfig);
        FileServerConfig fileServerConfig = FileServerConfig.of(t, "%d_network".formatted(port), new CASTransformPath(), nodes);
        return FileServer.of(fileServerConfig);
    }
}
