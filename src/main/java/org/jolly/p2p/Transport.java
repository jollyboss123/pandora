package org.jolly.p2p;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Transport handles the communication between the nodes in the network.
 * This can be in the form of TCP/IP, UDP, websockets, etc.
 */
public interface Transport extends AutoCloseable {
    void listen();
    void dial(int port) throws IOException, InvalidHandshakeException;
    InfiniteBlockingQueue<RPC> consume();
    TransportConfig getConfig();
}
