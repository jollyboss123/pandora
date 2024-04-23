package org.jolly.p2p;

import java.io.IOException;
import java.io.InputStream;

public class DefaultDecoder implements Decoder<RPC> {

    @Override
    public RPC decode(InputStream in) throws IOException, ClassNotFoundException {
        byte[] buf = new byte[1024];
        int n = in.read(buf);
        byte[] payload = new byte[n];
        System.arraycopy(buf, 0, payload, 0, n);
        return new RPC(payload);
    }
}
