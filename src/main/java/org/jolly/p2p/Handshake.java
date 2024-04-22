package org.jolly.p2p;

public interface Handshake {
    void perform(Peer peer) throws InvalidHandshakeException;
}
