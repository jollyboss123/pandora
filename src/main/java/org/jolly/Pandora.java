package org.jolly;

import org.jolly.p2p.TCPTransport;
import org.jolly.p2p.TCPTransportConfig;

public class Pandora {
    public static void main(String[] args) {
        TCPTransportConfig cfg = TCPTransportConfig.of(3000);
        TCPTransport t = (TCPTransport) TCPTransport.of(cfg);
        t.listen();
    }
}
