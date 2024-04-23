package org.jolly.storage;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {

    @Test
    void storeAndRetrieve() {
        Store s = Store.create(new CASTransformPath());
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

        try {
            s.delete(key);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void delete() {
        Store s = Store.create(new CASTransformPath());
        String key = "somepicture";
        byte[] data = "random bytes".getBytes();

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try {
            s.writeStream(key, in);
        } catch (IOException e) {
            fail();
        }

        try {
            s.delete(key);
        } catch (IOException e) {
            fail();
        }
    }
}
