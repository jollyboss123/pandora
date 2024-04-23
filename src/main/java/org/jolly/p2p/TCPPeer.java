package org.jolly.p2p;

import java.net.Socket;

/**
 * TCPPeer represents the remote node over a TCP established connection.
 */
public class TCPPeer implements Peer {
    /**
     * conn is the underlying connection of the peer.
     */
    private final Socket conn;
    /**
     * outbound is true, if we dial and retrieve a connection;
     * false, if we accept and retrieve a connection.
     */
    private final boolean outbound;

    private TCPPeer(Socket conn, boolean outbound) {
        this.conn = conn;
        this.outbound = outbound;
    }

    public static Peer ofOutbound(Socket conn) {
        return new TCPPeer(conn, true);
    }

    public static Peer ofInbound(Socket conn) {
        return new TCPPeer(conn, false);
    }

    public static Peer of(Socket conn, boolean outbound) {
        return new TCPPeer(conn, outbound);
    }

    @Override
    public String toString() {
        return "TCPPeer{conn=%s, outbound=%s}".formatted(conn.getRemoteSocketAddress().toString(), Boolean.toString(outbound));
    }
}
