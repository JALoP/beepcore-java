/*
 * Frame.java            $Revision: 1.2 $ $Date: 2001/04/02 21:39:45 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 *
 * The contents of this file are subject to the Blocks Public License (the
 * "License"); You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.invisible.net/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 */
package org.beepcore.beep.core;


/**
 * Frame encapsulates the MSG, RPY, ERR, ANS and NUL BEEP message types.
 * Contains a the <code>Channel</code> this frame belongs to, the BEEP Frame
 * Payload which holds the BEEP Frames's Header, Trailer, and the message
 * payload.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision, $Date: 2001/04/02 21:39:45 $
 *
 * @see FrameDataStream
 * @see BufferSegment
 */
public class Frame {

    public static final String TRAILER = "END\r\n";
    public static final int MAX_HEADER_SIZE = (3        // msg type
                                               + 1      // space
                                               + 10     // channel number
                                               + 1      // space
                                               + 10     // msgno
                                               + 1      // space
                                               + 1      // more
                                               + 1      // space
                                               + 10     // seqno
                                               + 1      // space
                                               + 10     // size
                                               + 1      // space
                                               + 10     // ansno
                                               + 2);    // CRLF
    private static final String CRLF = "\r\n";

    /** BEEP message type of  <code>Frame</code>. */
    private int messageType;

    /** <code>Channel</code> to which <code>Frame</code> belongs. */
    private Channel channel;

    /** Message number of <code>Frame</code>. */
    private int msgno;

    /** Answer number of this BEEP <code>Frame</code>. */
    private int ansno;

    /**
     * Sequence number of a BEEP message.  <code>seqno</code> is equal to
     * the 'seqno' of the BEEP message header.
     */
    private long seqno;

    /**
     * Specifies whether this is the final frame of the message (i.e. the
     * continuation indicator 'more' is equal to a '.' and not a '*').
     */
    private boolean last;

    /**
     * The payload of a BEEP message.
     */
    private BufferSegment payload;

    /**
     * Initializes a new <code>Frame</code> representing a BEEP MSG, RPY, ERR
     * or NUL frame.
     *
     * @param messageType indicates whether a <code>Frame</code> is a MSG,
     *    RPY, ERR, ANS or NUL.
     * @param channel <code>Channel</code> on which the <code>Frame</code> was
     *    sent.
     * @param msgno Message number of the <code>Frame</code>.
     * @param seqno Sequence number of the <code>Frame</code>.
     * @param payload Payload of the <code>Frame</code>.
     * @param last  Indicates if this is the last <code>Frame</code> sent in a
     *    sequence of frames.
     *
     * @see Payload
     */
    Frame(int messageType, Channel channel, int msgno, long seqno,
            byte[] payload, boolean last)
    {
        this.messageType = messageType;
        this.channel = channel;
        this.msgno = msgno;
        this.seqno = seqno;
        this.payload = new BufferSegment(payload, 0, payload.length);
        this.last = last;
        this.ansno = -1;    // not valid for this type of frame
    }

    /**
     * Initializes a new <code>Frame</code> representing a BEEP MSG, RPY, ERR
     * or NUL frame.
     *
     * @param messageType indicates whether a <code>Frame</code> is a MSG,
     *    RPY, ERR, ANS or NUL.
     * @param channel <code>Channel</code> on which the <code>Frame</code> was
     *    sent.
     * @param msgno Message number of the <code>Frame</code>.
     * @param seqno Sequence number of the <code>Frame</code>.
     * @param payload Payload of the <code>Frame</code>.
     * @param offset  Starting byte in the payload.
     * @param length Number of bytes in the payload.
     * @param last  Indicates if this is the last <code>Frame</code> sent in a
     *    sequence of frames.
     *
     * @see BufferSegment
     */
    Frame(int messageType, Channel channel, int msgno, long seqno,
            byte[] payload, int offset, int length, boolean last)
    {
        this.messageType = messageType;
        this.channel = channel;
        this.msgno = msgno;
        this.seqno = seqno;
        this.payload = new BufferSegment(payload, offset, length);
        this.last = last;
        this.ansno = -1;    // not valid for this type of frame
    }

    /**
     * Initializes a new <code>Frame</code> representing a BEEP ANS frame.
     *
     * @param messageType indicates whether a <code>Frame</code> is a MSG,
     *    RPY, ERR, ANS or NUL.
     * @param channel <code>Channel</code> on which the <code>Frame</code> was
     *    sent.
     * @param msgno Message number of the <code>Frame</code>.
     * @param seqno Sequence number of the <code>Frame</code>.
     * @param ansno Answer number of the <code>Frame</code>.
     * @param payload Payload of the <code>Frame</code>.
     * @param last  Indicates if this is the last <code>Frame</code> sent in a
     *    sequence of frames.
     *
     * @see BufferSegment
     */
    Frame(int messageType, Channel channel, int msgno, long seqno, int ansno,
            byte[] payload, boolean last)
    {
        this.messageType = messageType;
        this.channel = channel;
        this.msgno = msgno;
        this.seqno = seqno;
        this.ansno = ansno;
        this.payload = new BufferSegment(payload, 0, payload.length);
        this.last = last;
    }

    /**
     * Initializes a new <code>Frame</code> representing a BEEP MSG, RPY, ERR
     * or NUL frame.
     *
     * @param messageType indicates whether a <code>Frame</code> is a MSG,
     *    RPY, ERR, ANS or NUL.
     * @param channel <code>Channel</code> on which the <code>Frame</code> was
     *    sent.
     * @param msgno Message number of the <code>Frame</code>.
     * @param seqno Sequence number of the <code>Frame</code>.
     * @param ansno Answer number of the <code>Frame</code>.
     * @param payload Payload of the <code>Frame</code>.
     * @param offset  Starting byte in the payload.
     * @param count Number of bytes in the payload.
     * @param last  Indicates if this is the last <code>Frame</code> sent in a
     *    sequence of frames.
     *
     * @see BufferSegment
     */
    Frame(int messageType, Channel channel, int msgno, long seqno, int ansno,
            byte[] payload, int offset, int length, boolean last)
    {
        this.messageType = messageType;
        this.channel = channel;
        this.msgno = msgno;
        this.seqno = seqno;
        this.payload = new BufferSegment(payload, offset, length);
        this.last = last;
        this.ansno = ansno;
    }

    /**
     * Builds a BEEP Header from the given <code>Frame</code> and returns it
     * as a byte array.
     *
     * @param f <code>Frame</code> from which we derive the BEEP Header.
     */
    public byte[] buildHeader()
    {
        // @todo throw an exception if a malformed header results
        StringBuffer header = new StringBuffer(Frame.MAX_HEADER_SIZE);

        // Create header
        header.append(Message.MessageType.getMessageType(this.messageType));
        header.append(' ');
        header.append(this.channel.getNumberAsString());
        header.append(' ');
        header.append(this.msgno);
        header.append(' ');
        header.append((this.last ? '.' : '*'));
        header.append(' ');
        header.append(this.seqno);
        header.append(' ');
        header.append(this.payload.getLength());

        if (this.messageType == Message.MESSAGE_TYPE_ANS) {
            header.append(' ');
            header.append(this.ansno);
        }

        header.append(this.CRLF);

        return header.toString().getBytes();
    }


    /**
     * Returns the <code>payload</code> of a <code>Frame</code>.
     * A <code>BufferSegment</code> contains a BEEP Frames Payload.
     *
     * @see BufferSegment
     */
    public BufferSegment getPayload()
    {
        return this.payload;
    }

    /**
     * Returns the message type of this <code>Frame</code>.
     */
    public int getMessageType()
    {
        return this.messageType;
    }

    /**
     * Returns the <code>Channel</code> to which this <code>Frame</code>
     * belongs.
     *
     * @see Channel
     */
    public Channel getChannel()
    {
        return this.channel;
    }

    /**
     * Returns the message number of this <code>Frame</code>.
     */
    public int getMsgno()
    {
        return this.msgno;
    }

    /**
     * Returns the <code>seqno</code> of this <code>Frame</code>.
     */
    public long getSeqno()
    {
        return this.seqno;
    }

    /**
     * Returns the answer number of this <code>Frame</code>.
     */
    public int getAnsno()
    {
        return this.ansno;
    }

    /**
     * Indicates if this is the last <code>Frame</code> in a sequence of frames
     */
    public boolean isLast()
    {
        return this.last;
    }

    /**
     * A <code>BufferSegment</code> represents a BEEP Frame payload and holds
     * the BEEP Frames's Header, Trailer and the message payload.
     *
     * It contains a byte array an offset into the array and the
     * length from the offset.
     *
     * @author Eric Dixon
     * @author Huston Franklin
     * @author Jay Kint
     * @author Scott Pead
     * @version $Revision: 1.2 $, $Date: 2001/04/02 21:39:45 $
     */
    public static class BufferSegment {

        private byte[] data;    // the byte array
        private int offset;     // starting offset
        private int length;     // number of bytes from offset

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

        /**
         * Method <code>getBytes</code> returns a byte array.
         */
        public byte[] getBytes()
        {
            return this.data;
        }

        /**
         * Method <code>getOffset</code> returns the starting offset into the
         * <code>BufferSegment</code>s byte array.
         */
        public int getOffset()
        {
            return this.offset;
        }

        /**
         * Method <code>getLength</code> returns the number of valid bytes
         * from the starting offset for the byte array.
         */
        public int getLength()
        {
            return this.length;
        }
    }
}
