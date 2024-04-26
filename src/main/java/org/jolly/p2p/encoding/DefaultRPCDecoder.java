package org.jolly.p2p.encoding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jolly.p2p.RPC;
import org.jolly.p2p.SocketChannel;

import java.io.IOException;

public class DefaultRPCDecoder implements Decoder<RPC> {

    private static final Logger log = LogManager.getLogger(DefaultRPCDecoder.class);

    /**
     * Constructs a new RPC object by reading data
     * from the given socket connection.
     *
     * @param s a socket connection object
     * @throws IOException if I/O error occurs
     */
    @Override
    public RPC decode(SocketChannel s) throws IOException {
        byte[] type = new byte[4];
        if (s.read(type) != 4) {
            throw new IOException("EOF in RPC constructor: type");
        }
        byte[] payloadByteLen = new byte[4];
        if (s.read(payloadByteLen) != 4) {
            throw new IOException("EOF in RPC constructor: payloadLen");
        }

        int payloadLen = byteArrayToInt(payloadByteLen);
        byte[] data = new byte[payloadLen];

        if (s.read(data) != payloadLen) {
            throw new IOException("EOF in RPC constructor: Unexpected message payload length");
        }

        byte[] fromByteLen = new byte[4];
        if (s.read(fromByteLen) != 4) {
            throw new IOException("EOF in RPC constructor: fromLen");
        }

        int fromLen = byteArrayToInt(fromByteLen);
        byte[] from = new byte[fromLen];

        if (s.read(from) != fromLen) {
            throw new IOException("EOF in RPC constructor: Unexpected message from length");
        }

        return RPC.of(type, data, from);
    }

    private static int byteArrayToInt(byte[] b) {
        int integer = 0;
        for (int n = 0; n < 4; n++) {
            integer = (integer << 8) | (b[n] & 0xff);
        }

        if (integer > 1000) {
            log.debug("{} {} {} {} ==> {}", b[0], b[1], b[2], b[3], integer);
        }

        return integer;
    }
}
