package org.beepcore.beep.util;

/**
 * A <code>BufferSegment</code> represents a BEEP Frame payload and holds
 * the BEEP Frames's Header, Trailer and the message payload.
 *
 * It contains a byte array an offset into the array and the
 * length from the offset.
 *
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2001/10/31 00:32:38 $
 */
public class BufferSegment {
    /**
     * Constructor BufferSegment
     *
     * @param data A byte array containing a BEEP Frame payload.
     */
    public BufferSegment(byte[] data)
    {
        this.data = data;
        this.offset = 0;
        this.length = data.length;
    }

    /**
     * Constructor BufferSegment
     *
     * @param data A byte array containing a BEEP Frame payload.
     * @param offset Indicates the begining position of the BEEP Frame
     * payload in the byte array <code>data</code>.
     * @param length Number of valid bytes in the byte array starting from
     * <code>offset</code>.
     */
    public BufferSegment(byte[] data, int offset, int length)
    {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    public byte[] getData()
    {
        return this.data;
    }

    public int getOffset()
    {
        return this.offset;
    }

    public int getLength()
    {
        return this.length;
    }

    private byte[] data;
    private int offset;
    private int length;
}

