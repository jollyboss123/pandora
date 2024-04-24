package org.jolly.p2p;

public class TCPTransportConfig implements TransportConfig {
    private final int port;
    private final Handshake handshake;
    private final Decoder<RPC> decoder;
    private PeerHandler peerHandler;

    private static final int DEFAULT_PORT = 3000;
    private static final Handshake DEFAULT_HANDSHAKE = new NOPHandshake();
    private static final Decoder<RPC> DEFAULT_DECODER = new DefaultDecoder();

    private TCPTransportConfig(int port, Handshake handshake, Decoder<RPC> decoder, PeerHandler peerHandler) {
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
        this.peerHandler = peerHandler;
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

    public static TCPTransportConfig of(int port, Handshake handshake, Decoder<RPC> decoder, PeerHandler peerHandler) {
        return new TCPTransportConfig(port, handshake, decoder, peerHandler);
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

    public PeerHandler getOnPeer() {
        return peerHandler;
    }

    @Override
    public void setOnPeer(PeerHandler peerHandler) {
        this.peerHandler = peerHandler;
    }
}
