package org.jolly;

import org.jolly.p2p.TCPTransport;

public class Pandora {
    public static void main(String[] args) {
        TCPTransport t = (TCPTransport) TCPTransport.create(3000);
        t.listen();
    }
}
