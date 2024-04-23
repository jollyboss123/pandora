package org.jolly.p2p;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TCPTransportTest {

    @Test
    void connectTCP() {
        int port = 4444;
        TCPTransportConfig cfg = TCPTransportConfig.of(port);
        TCPTransport t = (TCPTransport) TCPTransport.of(cfg);

        assertEquals(port, t.getCfg().getPort());
        assertDoesNotThrow(t::listen);
    }
}
