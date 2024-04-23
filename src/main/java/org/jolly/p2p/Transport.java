package org.jolly.p2p;

import java.util.concurrent.BlockingQueue;

/**
 * Transport handles the communication between the nodes in the network.
 * This can be in the form of TCP, UDP, etc.
 */
public interface Transport {
    void listen();
    BlockingQueue<RPC> consume();
}
