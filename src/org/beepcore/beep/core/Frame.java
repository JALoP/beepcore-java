/*
 * Frame.java  $Revision: 1.21 $ $Date: 2003/04/23 15:23:04 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2001,2002 Huston Franklin.  All rights reserved.
 *
 * The contents of this file are subject to the Blocks Public License (the
 * "License"); You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.beepcore.org/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 */
package org.beepcore.beep.core;


import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.beepcore.beep.util.BufferSegment;
import org.beepcore.beep.util.HeaderParser;
import org.beepcore.beep.util.StringUtil;

/**
 * Frame encapsulates a BEEP protocol frame for MSG, RPY, ERR, ANS and NUL
 * BEEP message types.
 * Contains a the <code>Channel</code> this frame belongs to, the BEEP Frame
 * Payload which holds the BEEP Frames's Header, Trailer, and the message
 * payload.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.21 $, $Date: 2003/04/23 15:23:04 $
 *
 * @see BufferSegment
 */
public class Frame {

    private static final String CRLF = "\r\n";

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
                                               + CRLF.length());
    public static final int MIN_HEADER_SIZE = (3        // msg type
                                               + 1      // space
                                               + 1      // channel number
                                               + 1      // space
                                               + 1      // msgno
                                               + 1      // space
                                               + 1      // more
                                               + 1      // space
                                               + 1      // seqno
                                               + 1      // space
                                               + 1      // size
                                               + CRLF.length());
    public static final int MIN_FRAME_SIZE = MIN_HEADER_SIZE +
        Frame.TRAILER.length();
    public static final int MAX_ANS_NUMBER = Integer.MAX_VALUE; // 2147483647;
    public static final int MAX_CHANNEL_NUMBER = Integer.MAX_VALUE;
    public static final int MAX_MESSAGE_NUMBER = Integer.MAX_VALUE;
    public static final long MAX_SEQUENCE_NUMBER = 4294967295L;
    public static final int MAX_SIZE = Integer.MAX_VALUE;

    private static final BufferSegment trailerBufferSegment =
        new BufferSegment(TRAILER.getBytes());

    private Log log = LogFactory.getLog(this.getClass());

    /** BEEP message type of  <code>Frame</code>. */
    private int messageType;

    /** <code>Channel</code> to which <code>Frame</code> belongs. */
    private ChannelImpl channel;

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
     * Size of the payload of a BEEP message.  <code>size</code> is equal to
     * the 'size' of the BEEP message header.
     */
    private int size;

    /**
     * Specifies whether this is the final frame of the message (i.e. the
     * continuation indicator 'more' is equal to a '.' and not a '*').
     */
    private boolean last;

    /**
     * The payload of a BEEP message.
     */
    private LinkedList payload = new LinkedList();

    Frame(int messageType, ChannelImpl channel, int msgno, boolean last,
          long seqno, int size, int ansno)
    {
        this.messageType = messageType;
        this.channel = channel;
        this.msgno = msgno;
        this.last = last;
        this.seqno = seqno;
        this.size = size;
        this.ansno = ansno;
    }

    /**
     * Adds the <code>BufferSegment</code> to the list representing the
     * payload for this frame.
     */
    public void addPayload(BufferSegment buf)
    {
        this.payload.add(buf);
    }

    /**
     * Returns an <code>iterator</code> to iterate over a collection of
     * <code>BufferSegment</code> objects.
     *
     */
    public BufferSegment[] getBytes()
    {
        BufferSegment[] b = new BufferSegment[this.payload.size() + 2];
        this.size = 0;

        int j=1;
        Iterator i = this.payload.iterator();
        while (i.hasNext()) {
            b[j] = (BufferSegment) i.next();
            this.size += b[j].getLength();
            ++j;
        }

        b[0] = new BufferSegment(buildHeader());
        b[j] = trailerBufferSegment;

        return b;
    }

    /**
     * Returns the <code>payload</code> of a <code>Frame</code>.
     * A <code>BufferSegment</code> contains a BEEP Frames Payload.
     *
     * @see BufferSegment
     */
    public Iterator getPayload()
    {
        return this.payload.iterator();
    }

    /**
     * Returns the message type of this <code>Frame</code>.
     */
    public int getMessageType()
    {
        return this.messageType;
    }

    /**
     * Returns the message type of this <code>Frame</code>.
     */
    public String getMessageTypeString()
    {
        return MessageType.getMessageType(this.messageType);
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
     * Returns the <code>size</code> of the payload for this
     * <code>Frame</code>.
     */
    public int getSize()
    {
        return this.size;
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

    void setLast()
    {
        this.last = true;
    }

    /**
     * Builds a BEEP Header from the given <code>Frame</code> and returns it
     * as a byte array.
     *
     * @param f <code>Frame</code> from which we derive the BEEP Header.
     */
    byte[] buildHeader()
    {
        StringBuffer header = new StringBuffer(Frame.MAX_HEADER_SIZE);

        // Create header
        header.append(MessageType.getMessageType(this.messageType));
        header.append(' ');
        header.append(this.channel.getNumberAsString());
        header.append(' ');
        header.append(this.msgno);
        header.append(' ');
        header.append((this.last ? '.' : '*'));
        header.append(' ');
        header.append(this.seqno);
        header.append(' ');
        header.append(this.size);

        if (this.messageType == Message.MESSAGE_TYPE_ANS) {
            header.append(' ');
            header.append(this.ansno);
        }

        header.append(Frame.CRLF);

        if (log.isTraceEnabled()) {
            log.trace(header);
        }
        return StringUtil.stringBufferToAscii(header);
    }

    static Frame parseHeader(SessionImpl session, byte[] headerBuffer, int length)
        throws BEEPException
    {
        HeaderParser header = new HeaderParser(headerBuffer, length);

        int msgType =
            MessageType.getMessageType(new String(header.parseType()));
        int channelNum = header.parseInt();
        int msgNum = header.parseInt();
        boolean last = header.parseLast();
        long seqNum = header.parseUnsignedInt();
        int size = header.parseInt();

        int ansNum = -1;
        if (header.hasMoreTokens()) {
            ansNum = header.parseInt();
        }

        if (header.hasMoreTokens()) {
            throw new BEEPException("Malformed BEEP Header");
        }

        return new Frame(msgType, session.getValidChannel(channelNum), msgNum,
                         last, seqNum, size, ansNum);
    }

    private static class MessageType {

        public static final String MESSAGE_TYPE_UNK = "UNK";
        public static final String MESSAGE_TYPE_MSG = "MSG";
        public static final String MESSAGE_TYPE_RPY = "RPY";
        public static final String MESSAGE_TYPE_ERR = "ERR";
        public static final String MESSAGE_TYPE_ANS = "ANS";
        public static final String MESSAGE_TYPE_NUL = "NUL";

        /**
         * BEEP message type for utility only.
         */
        private static final int MESSAGE_TYPE_MAX = 6;

        //    public static LinkedList types = new LinkedList();
        public static String[] types = new String[MESSAGE_TYPE_MAX];

        private MessageType() {}

        static {
            types[Message.MESSAGE_TYPE_UNK] = MessageType.MESSAGE_TYPE_UNK;
            types[Message.MESSAGE_TYPE_ANS] = MessageType.MESSAGE_TYPE_ANS;
            types[Message.MESSAGE_TYPE_MSG] = MessageType.MESSAGE_TYPE_MSG;
            types[Message.MESSAGE_TYPE_ERR] = MessageType.MESSAGE_TYPE_ERR;
            types[Message.MESSAGE_TYPE_RPY] = MessageType.MESSAGE_TYPE_RPY;
            types[Message.MESSAGE_TYPE_NUL] = MessageType.MESSAGE_TYPE_NUL;
        }

        static int getMessageType(String type)
                throws IndexOutOfBoundsException
        {
            int ret = 0;
            int i = 0;

            for (i = 0; i < MESSAGE_TYPE_MAX; i++) {
                if (type.equals(types[i])) {
                    ret = i;

                    break;
                }
            }

            return ret;
        }

        static String getMessageType(int type)
                throws IndexOutOfBoundsException
        {
            return types[type];
        }
    }
}
