/*
 * StringDataStream.java  $Revision: 1.6 $ $Date: 2001/11/08 05:51:34 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
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
import java.util.MissingResourceException;

import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.beepcore.beep.util.BufferSegment;


/**
 * <code>StringDataStream</code> represents a BEEP message's payload.
 * Allows implementors to treat a
 * <code>String</code> as a <code>DataSream</code>. The <code>String</code>
 * is stored as a <code>byte[]</code> using UTF-8 encoding.
 * <p>
 * <b>Note that this implementation
 * is not synchronized.</b> If multiple threads access a
 * <code>StringDataStream</code> concurrently, data may be inconsistent or lost.
 *
 * @see org.beepcore.beep.core.DataStream
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.6 $, $Date: 2001/11/08 05:51:34 $
 */
public class StringDataStream extends OutputDataStream {
    /**
     * The default <code>StringDataStream</code> String encoding
     * ("UTF-8").
     */
    private static final String DEFAULT_STRING_ENCODING = "UTF-8";

    private String enc;

    /**
     * Creates a <code>StringDataStream</code> with a
     * <code>String</code> and a <code>BEEP_XML_CONTENT_TYPE</code>
     * content type and a transfer encoding of
     * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param data  A <code>String</code> representing a message's payload.
     */
    public StringDataStream(String data)
    {
        super(new MimeHeaders());
        try {
            add(new BufferSegment(data.getBytes(DEFAULT_STRING_ENCODING)));

            this.enc = DEFAULT_STRING_ENCODING;
        } catch (UnsupportedEncodingException e) {
            throw new MissingResourceException("Encoding " +
					       DEFAULT_STRING_ENCODING +
					       " not supported",
                                               "StringDataStream", 
					       DEFAULT_STRING_ENCODING);
        }
    }

    /**
     * Creates a <code>StringDataStream</code> with a <code>String</code> and
     * a specified content type and a transfer encoding of
     * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param contentType Content type of <code>data</code>
     * @param data  A <code>String</code> representing a message's payload.
     */
    public StringDataStream(String contentType, String data)
    {
        super(new MimeHeaders(contentType));
        try {
            add(new BufferSegment(data.getBytes(DEFAULT_STRING_ENCODING)));

            this.enc = DEFAULT_STRING_ENCODING;
        } catch (UnsupportedEncodingException e) {
            throw new MissingResourceException("Encoding " +
					       DEFAULT_STRING_ENCODING +
					       " not supported",
                                               "StringDataStream", 
					       DEFAULT_STRING_ENCODING);
        }
    }

    /**
     * Creates a <code>StringDataStream</code> with a <code>String</code> and
     * a specified content type and a transfer encoding of
     * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param contentType Content type of <code>data</code>
     * @param transferEncoding Encoding Transfer encoding type of
     * <code>data</code>.
     * @param data  A <code>String</code> representing a message's payload.
     */
    public StringDataStream(String contentType, String transferEncoding,
                            String data)
    {
        super(new MimeHeaders(contentType, transferEncoding));
        try {
            add(new BufferSegment(data.getBytes(DEFAULT_STRING_ENCODING)));

            this.enc = DEFAULT_STRING_ENCODING;
        } catch (UnsupportedEncodingException e) {
            throw new MissingResourceException("Encoding " +
					       DEFAULT_STRING_ENCODING +
					       " not supported",
                                               "StringDataStream", 
					       DEFAULT_STRING_ENCODING);
        }
    }

    /**
     * Creates a <code>StringDataStream</code> with a <code>String</code> and
     * a specified content type and encoding.
     *
     * @param contentType Content type of <code>data</code>
     * @param transferEncoding Encoding Transfer encoding type of
     * <code>data</code>.
     * @param data  A <code>String</code> representing a message's payload.
     * @param enc The encoding used when converting <code>data</code> to a
     * <code>bytes[]</code>.
     */
    public StringDataStream(String contentType, String transferEncoding,
                            String data, String enc)
        throws UnsupportedEncodingException
    {
        super(new MimeHeaders(contentType, transferEncoding));
        add(new BufferSegment(data.getBytes(DEFAULT_STRING_ENCODING)));
        this.enc = DEFAULT_STRING_ENCODING;
    }

    /**
     * Returns the encoding used to convert the <code>String</code> to a
     * <code>bytes[]</code>.
     */
    public String getEncoding()
    {
        return this.enc;
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
     * @deprecated
     */
    public void setContentType(String contentType)
    {
        this.mimeHeaders.setContentType(contentType);
    }

    /**
     * @deprecated
     */
    public String getContentType() throws BEEPException
    {
        return this.mimeHeaders.getContentType();
    }

    /**
     * @deprecated
     */
    public void setTransferEncoding(String transferEncoding)
    {
        this.mimeHeaders.setTransferEncoding(transferEncoding);
    }

    /**
     * @deprecated
     */
    public String getTransferEncoding() throws BEEPException
    {
        return this.mimeHeaders.getTransferEncoding();
    }

    /**
     * Adds a MIME entity header to this data stream.
     *
     * @param name  Name of the MIME enitity header.
     * @param value Value of the MIME entity header.
     * @deprecated
     */
    public void setHeader(String name, String value)
    {
        this.mimeHeaders.setHeader(name, value);
    }

    /**
     * Retrieves the correspoding <code>value</code> to a given a MIME entity
     * header <code>name</code>.
     *
     * @param name Name of the MIME entity header.
     * @return The <code>value</code> of the MIME entity header.
     *
     * @throws BEEPException
     * @deprecated
     */
    public String getHeaderValue(String name) throws BEEPException
    {
        return this.mimeHeaders.getHeaderValue(name);
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
