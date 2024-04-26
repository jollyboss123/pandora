package org.jolly.p2p.encoding;

import org.jolly.p2p.RPC;

public class DefaultRPCEncoder implements Encoder<RPC> {

    /**
     * Returns a packed representation of this message as an
     * array of bytes.
     *
     * @return byte array of message data
     */
    @Override
    public byte[] encode(RPC rpc) {
        byte[] bytes = new byte[4 + 4 + rpc.getPayloadBytes().length + 4 + rpc.getFromBytes().length];
        byte[] payloadBytesLen = intToByteArray(rpc.getPayloadBytes().length);
        byte[] fromBytesLen = intToByteArray(rpc.getFromBytes().length);

        System.arraycopy(rpc.getTypeBytes(), 0, bytes, 0, 4);
        System.arraycopy(payloadBytesLen, 0, bytes, 4, 4);
        System.arraycopy(rpc.getPayloadBytes(), 0, bytes, 8, rpc.getPayloadBytes().length);
        System.arraycopy(fromBytesLen, 0, bytes, 8 + rpc.getPayloadBytes().length, 4);
        System.arraycopy(rpc.getFromBytes(), 0, bytes, 8 + rpc.getPayloadBytes().length + 4, rpc.getFromBytes().length);

        return bytes;
    }

    /**
     * Returns a byte array containing the two's-complement representation of the integer.<br>
     * The byte array will be in big-endian byte-order with a fixes length of 4
     * (the least significant byte is in the 4th element).<br>
     * <br>
     * <b>Example:</b><br>
     * <code>intToByteArray(258)</code> will return { 0, 0, 1, 2 },<br>
     * <code>BigInteger.valueOf(258).toByteArray()</code> returns { 1, 2 }.
     *
     * @param integer The integer to be converted.
     * @return The byte array of length 4.
     */
    private static byte[] intToByteArray(int integer) {
        int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
        byte[] bytes = new byte[4];

        for (int n = 0; n < byteNum; n++) {
            bytes[3 - n] = (byte) (integer >>> (n * 8));
        }

        return bytes;
    }
}
