package org.jolly.p2p;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Arrays;

/**
 * RPC holds any arbitrary data that is being sent over each transport between two
 * nodes in the network.
 */
public class RPC implements Serializable {
    private final byte[] payload;
    private final SocketAddress from;

    public RPC(byte[] payload, SocketAddress from) {
        this.payload = payload;
        this.from = from;
    }

    public RPC(byte[] payload) {
        this.payload = payload;
        this.from = null;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "RPC{payload=%s, from=%s".formatted(Arrays.toString(payload), from != null ? from.toString() : null);
    }
}
