package org.jolly.p2p;

public class NOPHandshake implements Handshake {
    @Override
    public void perform(TCPPeer peer) throws InvalidHandshakeException {
        // not operational
    }
}
