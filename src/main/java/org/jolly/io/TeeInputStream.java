package org.jolly.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TeeInputStream allows reading from an InputStream and simultaneously writing the read data
 * to an OutputStream, mirroring the functionality of the Unix/Linux "tee" command. This class
 * is useful for scenarios where data needs to be both processed and logged or copied elsewhere.
 * <p>
 * Each successful read operation from the underlying InputStream results in the data being
 * written to the associated OutputStream. When TeeInputStream is closed, it also closes both
 * the input and output streams.
 */
public class TeeInputStream extends InputStream {
    private final InputStream in;
    private final OutputStream tee;

    private TeeInputStream(InputStream in, OutputStream tee) {
        this.in = in;
        this.tee = tee;
    }

    public static TeeInputStream of(InputStream in, OutputStream tee) {
        return new TeeInputStream(in, tee);
    }

    @Override
    public int read() throws IOException {
        final int retVal = this.in.read();
        if (retVal >= 0) {
            tee.write(retVal);
        }
        return retVal;
    }

    @Override
    public int read(byte[] b) throws IOException {
        final int retVal = this.in.read(b);
        if (retVal >= 0) {
            tee.write(b, 0, retVal);
        }
        return retVal;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        final int retVal = this.in.read(b, off, len);
        if (retVal >= 0) {
            tee.write(b, off, retVal);
        }
        return retVal;
    }

    @Override
    public void close() throws IOException {
        try {
            this.in.close();
        } finally {
            this.tee.close();
        }
    }
}
