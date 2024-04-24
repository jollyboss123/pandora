package org.jolly.p2p.encoding;

import org.jolly.p2p.RPC;

import java.util.Objects;

public class DefaultRPCDecoder implements Decoder<RPC> {

    @Override
    public RPC decode(byte[] buf) {
        Objects.requireNonNull(buf, "byte data");
        return new RPC(buf);
    }
}
