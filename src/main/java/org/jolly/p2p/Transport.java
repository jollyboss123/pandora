package org.jolly.p2p;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Transport handles the communication between the nodes in the network.
 * This can be in the form of TCP, UDP, etc.
 */
public interface Transport extends AutoCloseable {
    TransportConfig getCfg();
    void listen();
    void dial(int port) throws IOException;
    BlockingQueue<RPC> consume();
}
