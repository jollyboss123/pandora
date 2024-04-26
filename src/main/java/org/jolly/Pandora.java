package org.jolly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.p2p.FileServer;
import org.jolly.p2p.FileServerConfig;
import org.jolly.p2p.TCPTransport;
import org.jolly.p2p.TCPTransportConfig;
import org.jolly.storage.CASTransformPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pandora {

    private static final Logger log = LogManager.getLogger(Pandora.class);

    public static void main(String[] args) {
        try (ExecutorService executor = Executors.newCachedThreadPool();
             FileServer fs1 = make(3000, new int[]{});
             FileServer fs2 = make(4000, new int[]{3000})) {

            executor.submit(fs1::start);
            executor.submit(fs2::start);

            Thread.sleep(2000);
            ByteArrayInputStream in = new ByteArrayInputStream("big data file".getBytes());
            fs1.store("privatedata", in);
        } catch (IOException e) {
            log.error(e);
        } catch (InterruptedException e) {
            log.error(e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    private static FileServer make(int port, int[] nodes) {
        TCPTransportConfig tcpTransportConfig = TCPTransportConfig.of(port);
        TCPTransport t = TCPTransport.of(tcpTransportConfig);
        FileServerConfig fileServerConfig = FileServerConfig.of(t, "%d_network".formatted(port), new CASTransformPath(), nodes);
        return FileServer.of(fileServerConfig);
    }
}
