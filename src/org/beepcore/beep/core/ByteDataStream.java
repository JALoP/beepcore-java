
/*
 * ByteDataStream.java            $Revision: 1.3 $ $Date: 2001/04/24 22:52:02 $
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
 * @version $Revision: 1.3 $, $Date: 2001/04/24 22:52:02 $
 */
public class ByteDataStream extends InputStreamDataStream {

    ByteDataStream(String contentType, String transferEncoding)
    {
      super( contentType, transferEncoding, null );
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code> with a
     * content type of <code>DEFAULT_CONTENT_TYPE</code> and a transfer encoding
     * of <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param data  A <code>byte[]</code> representing a message's payload.
     */
    public ByteDataStream(byte[] data)
    {
        super(new ByteArrayInputStream(data));
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code>
     * with a specified content type and a transfer encoding
     * of <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param contentType Content type of <code>data</code>.
     * @param data  A <code>byte[]</code> representing a message's payload.
     */
    public ByteDataStream(String contentType, byte[] data)
    {
        super(contentType, new ByteArrayInputStream(data));
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code>
     * with a specified content type and a specified transfer encoding.
     *
     * @param contentType Content type of <code>data</code>.
     * @param transferEncoding Encoding Transfer encoding type of
     * <code>data</code>.
     * @param data  A <code>byte[]</code> representing a message's payload.
     */
    public ByteDataStream(String contentType, String transferEncoding,
                          byte[] data)
    {
        super(contentType, transferEncoding, new ByteArrayInputStream(data));
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code> using
     * the specified length and offset
     * with a content type of <code>DEFAULT_CONTENT_TYPE</code> and a transfer
     * encoding of <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param data  A <code>byte[]</code> representing a message's payload.
     * @param offset  The start offset in array <code>data</code> at which the
     * data is written.
     * @param length The maximum number of bytes to read.
     */
    public ByteDataStream(byte[] data, int offset, int length)
    {
        super(new ByteArrayInputStream(data, offset, length));
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code> using
     * the specified length and offset and with a specified content type and a
     * transfer encoding of <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
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
        super(contentType, new ByteArrayInputStream(data, offset, length));
    }

    /**
     * Creates a <code>ByteDataStream</code> from a <code>byte[]</code> using
     * the specified length and offset and with a specified content type and a
     * specified transfer encoding.
     *
     * @param contentType Content type of <code>byte[]</code>.
     * @param transferEncoding Encoding Transfer encoding type of
     * <code>data</code>.
     * @param data  A <code>byte[]</code> representing a message's payload.
     * @param offset  The start offset in array <code>data</code> at which the
     * data is written.
     * @param length The maximum number of bytes to read.
     */
    public ByteDataStream(String contentType, String transferEncoding,
                          byte[] data, int offset, int length)
    {
        super(contentType, transferEncoding,
              new ByteArrayInputStream(data, offset, length));
    }

    void setData( byte[] data )
    {
        this.data = new ByteArrayInputStream(data);
    }

    /**
     * Returns this data stream as an <code>InputStream</code>
     */
    public InputStream getInputStream()
    {
        return this.data;
    }

    /**
     * Returns <code>true</code> if no more bytes will be added to those
     * currently available, if any, on this stream.  Returns
     * <code>false</code> if more bytes are expected.
     */
    public boolean isComplete()
    {
      return true;
    }
}
