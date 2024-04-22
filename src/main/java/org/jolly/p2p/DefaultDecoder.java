package org.jolly.p2p;

import java.io.IOException;
import java.io.InputStream;

public class DefaultDecoder implements Decoder<Message> {

    @Override
    public Message decode(InputStream in) throws IOException, ClassNotFoundException {
        byte[] buf = new byte[1024];
        int n = in.read(buf);
        byte[] payload = new byte[n];
        System.arraycopy(buf, 0, payload, 0, n);
        return new Message(payload);
    }
}
