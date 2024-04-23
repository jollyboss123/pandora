package org.jolly.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {
    private Store s;

    @BeforeEach
    void setUp() {
        StoreConfig cfg = StoreConfig.of(null, new CASTransformPath());
        s = Store.of(cfg);
    }

    @AfterEach
    void tearDown() {
        try {
            s.clear();
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void storeAndRetrieve() {
        String key = "somepicture";
        byte[] data = "random bytes".getBytes();

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try {
            s.writeStream(key, in);
        } catch (IOException e) {
            fail();
        }

        try (InputStream inputStream = s.read(key)) {
            assertArrayEquals(data, inputStream.readAllBytes());
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void delete() {
        String key = "somepicture";
        byte[] data = "random bytes".getBytes();

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try {
            s.writeStream(key, in);
        } catch (IOException e) {
            fail();
        }

        assertTrue(s.has(key));

        try {
            s.delete(key);
        } catch (IOException e) {
            fail();
        }

        assertFalse(s.has(key));
    }
}
