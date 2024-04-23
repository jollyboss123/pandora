package org.jolly.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CASTransformPathTest {

    private static final Logger log = LogManager.getLogger(CASTransformPathTest.class);

    @Test
    void transform() {
        String key = "random";
        PathKey pathKey = new CASTransformPath().apply(key);
        log.info(pathKey.getFullPath());

        assertEquals("a415a/b5cc1/7c8c0/93c01/5ccdb/7e552/aee79/11aa4/a415ab5cc17c8c093c015ccdb7e552aee7911aa4", pathKey.getFullPath().toString());
        assertEquals("a415ab5cc17c8c093c015ccdb7e552aee7911aa4", pathKey.getFilename());
    }

}
