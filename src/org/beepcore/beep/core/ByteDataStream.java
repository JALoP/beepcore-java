
/*
 * ByteDataStream.java            $Revision: 1.1 $ $Date: 2001/04/02 08:56:06 $
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


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * <code>ByteDataStream</code> represents a BEEP message's payload.
 * Allows the implementor to treat a
 * <code>byte[]</code> as a <code>DataStream</code>.
 * <p>
 * <b>Note that this implementation
 * is not synchronized.</b> If multiple threads access a
 * <code>ByteDataStream</code> concurrently, data may be inconsistent or lost.
 *
 * @see org.beepcore.beep.core.DataStream
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.1 $, $Date: 2001/04/02 08:56:06 $
 */
public class ByteDataStream extends DataStream {

    private ByteArrayInputStream data = null;

    /**
     * Constructor ByteDataStream
     *
     *
     * @param contentType
     *
     */
    protected ByteDataStream(String contentType)
    {
        super(contentType, DEFAULT_CONTENT_TRANSFER_ENCODING);
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code> with a
     * content type of <code>DEFAULT_CONTENT_TYPE</code>.
     *
     * @param data  A <code>byte[]</code> representing a message's payload.
     */
    public ByteDataStream(byte[] data)
    {
        super();

        this.data = new ByteArrayInputStream(data);
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code>
     * with a specified content type.
     *
     * @param contentType Content type of <code>data</code>.
     * @param data  A <code>byte[]</code> representing a message's payload.
     */
    public ByteDataStream(String contentType, byte[] data)
    {
        super(contentType, DEFAULT_CONTENT_TRANSFER_ENCODING);

        this.data = new ByteArrayInputStream(data);
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code> using
     * the specified length and offset
     * with a content type of <code>DEFAULT_CONTENT_TYPE</code>.
     *
     * @param data  A <code>byte[]</code> representing a message's payload.
     * @param offset  The start offset in array <code>data</code> at which the
     * data is written.
     * @param len The maximum number of bytes to read.
     * @param length
     */
    public ByteDataStream(byte[] data, int offset, int length)
    {
        super();

        this.data = new ByteArrayInputStream(data, offset, length);
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code> using
     * the specified length and offset
     * with a specified content type.
     *
     * @param contentType Content type of <code>byte[]</code>.
     * @param data  A <code>byte[]</code> representing a message's payload.
     * @param offset  The start offset in array <code>data</code> at which the
     * data is written.
     * @param length The maximum number of bytes to read.
     */
    public ByteDataStream(String contentType, byte[] data, int offset,
                          int length)
    {
        super(contentType, DEFAULT_CONTENT_TRANSFER_ENCODING);

        this.data = new ByteArrayInputStream(data, offset, length);
    }

    /**
     * Method setData
     *
     *
     * @param data
     *
     */
    protected void setData(byte[] data)
    {
        this.data = new ByteArrayInputStream(data);
    }

    /**
     * Returns this data stream as an <code>InputStream</code>.
     *
     */
    public InputStream getInputStream()
    {
        return this.data;
    }

    int available()
    {
        return data.available();
    }

    int read()
    {
        return this.data.read();
    }

    int read(byte[] buf) throws BEEPException
    {
        try {
            return this.data.read(buf);
        } catch (IOException e) {
            throw new BEEPException(e.getMessage());
        }
    }

    int read(byte[] buf, int off, int len) throws BEEPException
    {
        return this.data.read(buf, off, len);
    }

    long skip(long n)
    {
        return this.data.skip(n);
    }

    boolean markSupported()
    {
        return this.data.markSupported();
    }

    void mark(int readlimit)
    {
        this.data.mark(readlimit);
    }

    void reset() throws BEEPException
    {
        this.data.reset();
    }

    void close() throws BEEPException
    {
        try {
            this.data.close();
        } catch (IOException e) {
            throw new BEEPException(e.getMessage());
        }
    }
}
