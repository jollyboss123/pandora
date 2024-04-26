package org.jolly.p2p;

import java.net.SocketAddress;

/**
 * RPC holds any arbitrary data that is being sent over the transport
 * between two nodes in the network.
 */
public class RPC {
    private final byte[] type;
    private final byte[] payload;
    private final byte[] from;

    private RPC(byte[] type, byte[] payload, byte[] from) {
        this.type = type.clone();
        this.payload = payload.clone();
        this.from = from.clone();
    }

    public static RPC of(byte[] type, byte[] data, byte[] from) {
        return new RPC(type, data, from.clone());
    }

    public static RPC of(String type, String data, String from) {
        if (from == null) {
            from = "unknown";
        }
        return new RPC(type.getBytes(), data.getBytes(), from.getBytes());
    }

    public static RPC of(String type, byte[] data, String from) {
        if (from == null) {
            from = "unknown";
        }
        return new RPC(type.getBytes(), data, from.getBytes());
    }

    public String getType() {
        return new String(type);
    }

    public byte[] getTypeBytes() {
        return type.clone();
    }

    public String getPayload() {
        return new String(payload);
    }

    public byte[] getPayloadBytes() {
        return payload.clone();
    }

    public String getFrom() {
        return new String(from);
    }

    public byte[] getFromBytes() {
        return from.clone();
    }

    @Override
    public String toString() {
        return "RPC{type=%s, payload=%s, from=%s}".formatted(getType(), getPayload(), getFrom());
    }
}
