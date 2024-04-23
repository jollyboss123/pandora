package org.jolly.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.Function;

// CASTransformPath transforms a key into a structured path suitable for Content-Addressable Storage (CAS).
// It computes the SHA-1 hash of the key, converts it to hexadecimal, partitions it into blocks of characters,
// and constructs a path using these blocks, separated by slashes ("/").
// The resulting path structure helps in distributing and accessing data in a content-addressable manner.
public class CASTransformPath implements Function<String, PathKey> {

    private static final Logger log = LogManager.getLogger(CASTransformPath.class);
    private static final String ALGORITHM = "SHA-1";

    @Override
    public PathKey apply(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(s.getBytes(StandardCharsets.UTF_8));
            byte[] hash = md.digest();
            String hashStr = HexFormat.of().formatHex(hash);

            int blockSize = 5;
            int sliceLen = hashStr.length() / blockSize;
            String[] path = new String[sliceLen];

            for (int i = 0; i < sliceLen; i++) {
                int from = i * blockSize;
                int to = (i * blockSize) + blockSize;
                path[i] = hashStr.substring(from, to);
            }

            return new PathKey(String.join(File.separator, path), hashStr);

        } catch (NoSuchAlgorithmException e) {
            log.error("no such algorithm: {}", ALGORITHM, e);
            throw new PathTransformException(e);
        }
    }
}
