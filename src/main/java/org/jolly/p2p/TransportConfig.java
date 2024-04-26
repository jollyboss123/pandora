package org.jolly.p2p;

public interface TransportConfig {
    void setOnPeer(OnPeer onPeer);
    int getPort();
}
