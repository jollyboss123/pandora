package org.jolly.p2p;

/**
 * Transport handles the communication between the nodes in the network.
 * This can be in the form of TCP, UDP, etc.
 */
public interface Transport {
    void listen();
}
