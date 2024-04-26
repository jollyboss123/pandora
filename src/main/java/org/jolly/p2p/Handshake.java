package org.jolly.p2p;

public interface Handshake {
    void perform(TCPPeer peer) throws InvalidHandshakeException;
}
