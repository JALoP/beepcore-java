/*
 * InputDataStream.java  $Revision: 1.1 $ $Date: 2001/10/31 00:32:37 $
 *
 * Copyright (c) 2001 Huston Franklin.  All rights reserved.
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


import java.util.Enumeration;
import java.util.Iterator;
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
 * @version $Revision: 1.1 $, $Date: 2001/10/31 00:32:37 $
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
    InputDataStream(Channel channel)
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
     * @todo doc
     */
    public boolean availableSegment() {
        return (this.buffers.isEmpty() == false);
    }

    /**
     * @todo doc
     */
    public void close() {
        this.closed = true;
        while (this.availableSegment()) {
            this.getNextSegment();
        }
    }

    /**
     * @todo doc
     */
    public InputDataStreamAdapter getInputStream()
    {
        if (stream == null) {
            stream = new InputDataStreamAdapter(this);
        }

        return stream;
    }

    /**
     * @todo doc
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

    public BufferSegment waitForNextSegment() throws InterruptedException {
        synchronized (buffers) {
            if (availableSegment() == false) {
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
        // @todo notify objects waiting for more buffers.
    }

    LinkedList buffers = new LinkedList();
    private int availableBytes = 0;
    private Channel channel = null;
    private boolean closed = false;
    private boolean complete = false;
    private InputDataStreamAdapter stream = null;
}
    /**
     * Returns the content type of a <code>InputDataStrea</code>.  If
     * the <code>BufferSegment</code> containing the content type
     * hasn't been received yet, the method blocks until it is
     * received.
     *
     * @return Content type.
     *
     * @throws BEEPException
     * @deprecated
     */
//     public String getContentType() throws BEEPException
//     {
//         return this.getInputStream().getContentType();
//     }

    /**
     * Returns the value of the MIME entity header which corresponds
     * to the given <code>name</code>. If the <code>BufferSegment</code>
     * containing the content type hasn't been received yet, the
     * method blocks until it is received.
     *
     * @param name Name of the entity header.
     * @return String Value of the entity header.
     *
     * @throws BEEPException
     * @deprecated
     */
//     public String getHeaderValue(String name) throws BEEPException
//     {
//         return this.getInputStream().getHeaderValue(name);
//     }

    /**
     * Returns an <code>Enumeration</code> of all the MIME entity
     * header names belonging to this <code>InputDataStream</code>.
     * If the <code>BufferSegment</code> containing the content type
     * hasn't been received yet, the method blocks until it is
     * received.
     *
     * @throws BEEPException
     * @deprecated
     */
//     public Enumeration getHeaderNames() throws BEEPException
//     {
//         return this.getInputStream().getHeaderNames();
//     }

    /**
     * Returns the transfer encoding of a <code>InputDataStrea</code>.
     * If the <code>BufferSegment</code> containing the content type
     * hasn't been received yet, the method blocks until it is
     * received.
     *
     * @throws BEEPException
     * @deprecated
     */
//     public String getTransferEncoding() throws BEEPException
//     {
//         return this.getInputStream().getTransferEncoding();
//     }

