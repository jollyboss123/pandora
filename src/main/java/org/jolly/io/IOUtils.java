package org.jolly.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class IOUtils {
    private IOUtils() {}

    public static int copy(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(in, "inputStream");
        Objects.requireNonNull(out, "outputStream");

        byte[] buf = new byte[8192];
        int count = 0;
        int n;

        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
            count += n;
        }
        return count;
    }
}
