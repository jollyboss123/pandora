package org.jolly.p2p;

import java.util.function.UnaryOperator;

public class TCPTransportConfig {
    private final int port;
    private final Handshake handshake;
    private final Decoder<RPC> decoder;
    private final UnaryOperator<Peer> onPeer;

    private static final int DEFAULT_PORT = 3000;
    private static final Handshake DEFAULT_HANDSHAKE = new NOPHandshake();
    private static final Decoder<RPC> DEFAULT_DECODER = new DefaultDecoder();

    private TCPTransportConfig(int port, Handshake handshake, Decoder<RPC> decoder, UnaryOperator<Peer> onPeer) {
        if (port == 0) {
            port = DEFAULT_PORT;
        }
        if (handshake == null) {
            handshake = DEFAULT_HANDSHAKE;
        }
        if (decoder == null) {
            decoder = DEFAULT_DECODER;
        }

        this.port = port;
        this.handshake = handshake;
        this.decoder = decoder;
        this.onPeer = onPeer;
    }

    public static TCPTransportConfig of() {
        return new TCPTransportConfig(DEFAULT_PORT, DEFAULT_HANDSHAKE, DEFAULT_DECODER, null);
    }

    public static TCPTransportConfig of(int port) {
        return new TCPTransportConfig(port, DEFAULT_HANDSHAKE, DEFAULT_DECODER, null);
    }

    public static TCPTransportConfig of(int port, Handshake handshake, Decoder<RPC> decoder) {
        return new TCPTransportConfig(port, handshake, decoder, null);
    }

    public static TCPTransportConfig of(int port, Handshake handshake, Decoder<RPC> decoder, UnaryOperator<Peer> onPeer) {
        return new TCPTransportConfig(port, handshake, decoder, onPeer);
    }

    public int getPort() {
        return port;
    }

    public Handshake getHandshake() {
        return handshake;
    }

    public Decoder<RPC> getDecoder() {
        return decoder;
    }

    public UnaryOperator<Peer> getOnPeer() {
        return onPeer;
    }
}
