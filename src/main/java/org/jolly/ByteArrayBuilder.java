package org.jolly;

import java.nio.charset.StandardCharsets;

public class ByteArrayBuilder {
    public static final int INITIAL_CAPACITY = 13;
    private byte[] backingArray;
    private int size;

    /**
     * Constructs a new ByteArrayBuilder initializing the internal array with a default capacity.
     */
    public ByteArrayBuilder() {
        backingArray = new byte[INITIAL_CAPACITY];
    }

    /**
     * Appends a single byte to the builder.
     * @param b The byte to append.
     * @return This ByteArrayBuilder instance to enable method chaining.
     */
    public ByteArrayBuilder append(byte b) {
        if (size == backingArray.length) {
            extendCapacity();
        }
        backingArray[size] = b;
        size++;
        return this;
    }

    /**
     * Appends an integer after converting it to its string representation and then to bytes.
     * @param i The integer to append.
     * @return This ByteArrayBuilder instance to enable method chaining.
     */
    public ByteArrayBuilder append(int i) {
        return append(String.valueOf(i));
    }

    /**
     * Appends a string after converting it to bytes using UTF-8 encoding.
     * @param str The string to append.
     * @return This ByteArrayBuilder instance to enable method chaining.
     */
    public ByteArrayBuilder append(String str) {
        return append(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Appends an entire array of bytes to the builder.
     * @param bytes The byte array to append.
     * @return This ByteArrayBuilder instance to enable method chaining.
     */
    public ByteArrayBuilder append(byte[] bytes) {
        if (bytes.length > (backingArray.length - size)) {
            extendCapacity();
        }
        System.arraycopy(bytes, 0, backingArray, size, bytes.length);
        size += bytes.length;
        return this;
    }

    public void append(byte[] bytes, int offset, int length) {
        if (size + length > backingArray.length) {
            extendCapacity();
        }
        System.arraycopy(bytes, offset, backingArray, size, length);
        size += length;
    }

    public void clear() {
        size = 0;
        backingArray = new byte[0];
    }

    /**
     * Retrieves the internal backing array. Note that this array may contain additional unused bytes.
     * @return The backing byte array.
     */
    public byte[] getBackingArray() {
        return backingArray;
    }

    /**
     * Copies the internal backing array to its size.
     * @return The copy of the trimmed backing byte array.
     */
    public byte[] getBytes() {
        byte[] result = new byte[size];
        System.arraycopy(backingArray, 0, result, 0, size);
        return result;
    }

    /**
     * Returns the number of bytes that have been appended to the builder.
     * @return The size of the valid part of the backing array.
     */
    public int size() {
        return size;
    }

    /**
     * Doubles the capacity of the backing array when more space is needed.
     * This involves creating a new array and copying the existing bytes into it.
     */
    private void extendCapacity() {
        int cap = backingArray.length * 2;
        byte[] arr = new byte[cap];
        System.arraycopy(backingArray, 0, arr, 0, backingArray.length);
        backingArray = arr;
    }
}
