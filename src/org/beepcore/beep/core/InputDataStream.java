/*
 * InputDataStream.java  $Revision: 1.6 $ $Date: 2003/04/23 15:23:04 $
 *
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


import java.util.LinkedList;

import org.beepcore.beep.util.BufferSegment;

/**
 * <code>InputDataStream</code> holds a stream of
 * <code>BufferSegments</code>(s) and provides accessor methods to
 * that stream.
 * <p>
 * <b>Note that this implementation is not synchronized.</b> If
 * multiple threads access a <code>InputDataStream</code>
 * concurrently, data may be inconsistent or lost.
 *
 * @see org.beepcore.beep.util.BufferSegment
 *
 * @author Huston Franklin
 * @version $Revision: 1.6 $, $Date: 2003/04/23 15:23:04 $
 */
public class InputDataStream {

    /**
     * Creates a <code>InputDataStream</code>.
     */
    InputDataStream()
    {
    }

    /**
     * Creates a <code>InputDataStream</code>.
     * For use with in <code>Channel</code>.
     *
     * @param channel Is notified as BufferSegments are read this allows the
     *                Channel to update the receive window.
     */
    InputDataStream(ChannelImpl channel)
    {
        this.channel = channel;
    }

    /**
     * Creates a <code>InputDataStream</code>.
     *
     * @param buf The first buffer in the data stream.
     */
    InputDataStream(BufferSegment buf)
    {
        this.add(buf);
    }

    /**
     * Creates a <code>InputDataStream</code>.
     *
     * @param buf The first buffer in the data stream.
     */
    InputDataStream(BufferSegment buf, boolean complete)
    {
        this(buf);
        if (complete) {
            this.setComplete();
        }
    }

    void add(BufferSegment segment)
    {
        if( this.closed) {
            if (this.channel != null) {
                this.channel.freeReceiveBufferBytes(segment.getLength());
            }
            return;
        }

        synchronized (this.buffers) {
            this.buffers.addLast(segment);
            this.buffers.notify();
        }
        this.availableBytes += segment.getLength();
    }

    public int available()
    {
        return this.availableBytes;
//         int bytesAvailable = 0;

//         synchronized (this.buffers) {
//             Iterator i = this.buffers.iterator();
//             while (i.hasNext()) {
//                 bytesAvailable += ((BufferSegment) i.next()).getLength();
//             }
//         }

//         return bytesAvailable;
    }

    /**
     * Returns <code>true</code> if a <code>BufferSegment</code> is available
     * to receive.
     */
    public boolean availableSegment() {
        return (this.buffers.isEmpty() == false);
    }

    /**
     * Indicates that the application is finished receiving data from this
     * stream. If there is more data available the data will be discarded.
     */
    public void close() {
        this.closed = true;
        while (this.availableSegment()) {
            this.getNextSegment();
        }
    }

    /**
     * Returns an <code>InputStream</code> for reading the data in this stream.
     */
    public InputDataStreamAdapter getInputStream()
    {
        if (stream == null) {
            stream = new InputDataStreamAdapter(this);
        }

        return stream;
    }

    /**
     * Returns the next <code>BufferSegment</code> in this stream.
     *
     */
    public BufferSegment getNextSegment() {
        BufferSegment b;

        synchronized (buffers) {
            b = (BufferSegment) buffers.removeFirst();
        }

        if (this.channel != null) {
            this.channel.freeReceiveBufferBytes(b.getLength());
        }

        this.availableBytes -= b.getLength();

        return b;
    }

    /**
     *
     * @return null if isComplete() is true.
     */
    public BufferSegment waitForNextSegment() throws InterruptedException {
        synchronized (buffers) {
            while (availableSegment() == false) {
                if (isComplete() == true) {
                    return null;
                }
                buffers.wait();
            }
            return getNextSegment();
        }
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Returns <code>true</code> if no more bytes will be added to
     * those currently available on this stream.  Returns
     * <code>false</code> if more bytes are expected.
     */
    public boolean isComplete() {
        return this.complete;
    }

    void setComplete() {
        this.complete = true;
        synchronized (this.buffers) {
            this.buffers.notify();
        }
    }

    LinkedList buffers = new LinkedList();
    private int availableBytes = 0;
    private ChannelImpl channel = null;
    private boolean closed = false;
    private boolean complete = false;
    private InputDataStreamAdapter stream = null;
}
