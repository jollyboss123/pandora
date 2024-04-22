package org.jolly.p2p;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TCPTransportTest {

    @Test
    void connectTCP() {
        int port = 4444;
        TCPTransport t = (TCPTransport) TCPTransport.create(port);

        assertEquals(port, t.getPort());
        assertDoesNotThrow(t::listen);
    }
}
