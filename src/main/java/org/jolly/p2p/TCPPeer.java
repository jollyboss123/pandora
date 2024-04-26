package org.jolly.p2p;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.p2p.encoding.Decoder;
import org.jolly.p2p.encoding.Encoder;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * TCPPeer represents the remote node over a TCP established connection.
 */
public class TCPPeer implements Peer {
    private static final Logger log = LogManager.getLogger(TCPPeer.class);
    /**
     * The underlying connection of the peer, which in this case is a
     * TCP connection.
     */
    private SocketChannel s;

    private TCPPeer(SocketChannel socket) {
        s = socket;
    }

    public static TCPPeer of(SocketChannel socket) {
        return new TCPPeer(socket);
    }

    @Override
    public void send(RPC msg, Encoder<RPC> encoder) {
        try {
            s.write(encoder.encode(msg));
        } catch (IOException e) {
            log.error("error sending message: ", e);
        }
    }

    @Override
    public RPC receive(Decoder<RPC> decoder) {
        try {
            return decoder.decode(s);
        } catch (IOException e) {
            if (!e.getMessage().equals("EOF in Message constructor: type")) {
                log.warn("error receiving message: ", e);
            } else {
                log.info("error receiving message: ", e);
            }
            return null;
        }
    }

    @Override
    public void close() {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                log.warn("error closing: ", e);
            }
            s = null;
        }
    }

    /**
     * RemoteAddress is the address of the endpoint this socket is connected to,
     * or null if it is unconnected.
     *
     * @return the underlying connection's remote address.
     */
    @Override
    public SocketAddress getRemoteAddress() {
        return s.getRemoteAddr();
    }

    @Override
    public String toString() {
        return "Peer{addr=%s}".formatted(getRemoteAddress());
    }
}
