package org.jolly;

import org.jolly.p2p.TCPTransport;
import org.jolly.p2p.TCPTransportConfig;
import org.jolly.server.FileServer;
import org.jolly.server.FileServerConfig;

public class Pandora {
    public static void main(String[] args) {
        TCPTransportConfig cfg = TCPTransportConfig.of(3000);
        TCPTransport t = (TCPTransport) TCPTransport.of(cfg);
        FileServerConfig fileServerConfig = FileServerConfig.of(t);
        FileServer fs = FileServer.of(fileServerConfig);
        fs.start();
    }
}
