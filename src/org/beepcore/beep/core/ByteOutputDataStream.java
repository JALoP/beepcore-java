/*
 * ByteOutputDataStream.java  $Revision: 1.2 $ $Date: 2002/09/07 15:00:30 $
 *
 * Copyright (c) 2001 Huston Franklin.  All rights reserved.
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


import java.util.Enumeration;

import org.beepcore.beep.util.BufferSegment;


/**
 * <code>ByteOutputDataStream</code> represents a BEEP message's
 * payload.  Allows the implementor to treat a <code>byte[]</code> as
 * a <code>DataStream</code>.  <p> <b>Note that this implementation is
 * not synchronized.</b> If multiple threads access a
 * <code>ByteOutputDataStream</code> concurrently, data may be
 * inconsistent or lost.
 *
 * @see org.beepcore.beep.core.OutputDataStream
 *
 * @author Huston Franklin
 * @version $Revision: 1.2 $, $Date: 2002/09/07 15:00:30 $
 */
public class ByteOutputDataStream extends OutputDataStream {

    /**
     * Creates a <code>ByteOutputDataStream</code> from a
     * <code>byte[]</code> with a content type of
     * <code>DEFAULT_CONTENT_TYPE</code> and a transfer encoding of
     * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param data  A <code>byte[]</code> representing a message's payload.
     */
    public ByteOutputDataStream(byte[] data)
    {
        this(data, 0, data.length);
    }

    /**
     * Creates a <code>ByteOutputDataStream</code> from a <code>byte[]</code>
     * with a specified content type and a transfer encoding
     * of <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param contentType Content type of <code>data</code>.
     * @param data  A <code>byte[]</code> representing a message's payload.
     */
    public ByteOutputDataStream(String contentType, byte[] data)
    {
        this(contentType, data, 0, data.length);
    }

    /**
     * Creates a <code>ByteOutputDataStream</code> from a <code>byte[]</code>
     * with a specified content type and a specified transfer encoding.
     *
     * @param contentType Content type of <code>data</code>.
     * @param transferEncoding Encoding Transfer encoding type of
     * <code>data</code>.
     * @param data  A <code>byte[]</code> representing a message's payload.
     */
    public ByteOutputDataStream(String contentType, String transferEncoding,
                                byte[] data)
    {
        this(contentType, transferEncoding, data, 0, data.length);
    }

    /**
     * Creates a <code>ByteOutputDataStream</code> from a
     * <code>byte[]</code> using the specified length and offset with
     * a content type of <code>DEFAULT_CONTENT_TYPE</code> and a
     * transfer encoding of
     * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param data  A <code>byte[]</code> representing a message's payload.
     * @param offset  The start offset in array <code>data</code> at which the
     * data is written.
     * @param length The maximum number of bytes to read.
     */
    public ByteOutputDataStream(byte[] data, int offset, int length)
    {
        super(new MimeHeaders(),
              new BufferSegment(data, offset, length));
    }

    /**
     * Creates a <code>ByteOutputDataStream</code> from a
     * <code>byte[]</code> using the specified length and offset and
     * with a specified content type and a transfer encoding of
     * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param contentType Content type of <code>byte[]</code>.
     * @param data  A <code>byte[]</code> representing a message's payload.
     * @param offset  The start offset in array <code>data</code> at which the
     * data is written.
     * @param length The maximum number of bytes to read.
     */
    public ByteOutputDataStream(String contentType, byte[] data, int offset,
                                int length)
    {
        super(new MimeHeaders(contentType),
              new BufferSegment(data, offset, length));
    }

    /**
     * Creates a <code>ByteOutputDataStream</code> from a
     * <code>byte[]</code> using the specified length and offset and
     * with a specified content type and a specified transfer
     * encoding.
     *
     * @param contentType Content type of <code>byte[]</code>.
     * @param transferEncoding Encoding Transfer encoding type of
     * <code>data</code>.
     * @param data  A <code>byte[]</code> representing a message's payload.
     * @param offset  The start offset in array <code>data</code> at which the
     * data is written.
     * @param length The maximum number of bytes to read.
     */
    public ByteOutputDataStream(String contentType, String transferEncoding,
                                byte[] data, int offset, int length)
    {
        super(new MimeHeaders(contentType, transferEncoding),
              new BufferSegment(data, offset, length));
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

    /**
     * Returns the value of the MIME entity header <code>Content-Type</code>.
     */
    public String getContentType() throws BEEPException
    {
        return this.mimeHeaders.getContentType();
    }

    /**
     * Sets the content type of a <code>DataStream</code>.
     *
     * @param contentType
     */
    public void setContentType(String contentType)
    {
        this.mimeHeaders.setContentType(contentType);
    }

    /**
     * Returns the value of the MIME entity header
     * <code>Content-Transfer-Encoding</code>.
     */
    public String getTransferEncoding() throws BEEPException
    {
        return this.mimeHeaders.getTransferEncoding();
    }

    /**
     * Sets the content transfer encoding of a <code>DataStream</code>
     *
     * @param transferEncoding
     */
    public void setTransferEncoding(String transferEncoding)
    {
        this.mimeHeaders.setTransferEncoding(transferEncoding);
    }

    /**
     * Returns an <code>Enumeration</code> of all the names of the MIME entity
     * headers in this data stream.
     * Use this call in conjunction with <code>getHeaderValue</code> to iterate
     * through all the corresponding MIME entity header <code>value</code>(s)
     * in this data stream.
     *
     * @return An <code>Enumeration</code> of all the MIME entity header
     * names.
     *
     * @throws BEEPException
     */
    public Enumeration getHeaderNames() throws BEEPException
    {
        return this.mimeHeaders.getHeaderNames();
    }

    /**
     * Retrieves the correspoding <code>value</code> to a given a MIME entity
     * header <code>name</code>.
     *
     * @param name Name of the MIME entity header.
     * @return The <code>value</code> of the MIME entity header.
     *
     * @throws BEEPException
     */
    public String getHeaderValue(String name) throws BEEPException
    {
        return this.mimeHeaders.getHeaderValue(name);
    }

    /**
     * Adds a MIME entity header to this data stream.
     *
     * @param name  Name of the MIME enitity header.
     * @param value Value of the MIME entity header.
     */
    public void setHeaderValue(String name, String value)
    {
        this.mimeHeaders.setHeader(name, value);
    }

    /**
     * Removes the <code>name</code> and <code>value</code> of a MIME entity
     * header from the data stream.  Returns <code>true</code> if the
     * <code>name</code> was successfully removed.
     *
     * @param name Name of the header to be removed from the data stream.
     *
     * @return Returns </code>true<code> if header was removed.  Otherwise,
     * returns <code>false</code>.
     */
    public boolean removeHeader(String name)
    {
        return this.mimeHeaders.removeHeader(name);
    }
}
