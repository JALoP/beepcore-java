
/*
 * StringDataStream.java            $Revision: 1.4 $ $Date: 2001/04/26 17:42:53 $
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


import java.util.MissingResourceException;

import java.io.UnsupportedEncodingException;
import java.io.IOException;


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
 * @version $Revision: 1.4 $, $Date: 2001/04/26 17:42:53 $
 */
public class StringDataStream extends ByteDataStream {
    /**
     * The default <code>StringDataStream</code> String encoding
     * ("UTF-8").
     */
    private static final String DEFAULT_STRING_ENCODING = "UTF-8";

    private String enc;

    /**
     * Creates a <code>StringDataStream</code> with a <code>String</code> and
     * a <code>BEEP_XML_CONTENT_TYPE</code> content type and a transfer encoding
     * of <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     *
     * @param data  A <code>String</code> representing a message's payload.
     */
    public StringDataStream(String data)
    {
        super(BEEP_XML_CONTENT_TYPE,
              DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING);

        try {
            setData(data.getBytes(DEFAULT_STRING_ENCODING));

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
        super(contentType, DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING);
        try {
            setData(data.getBytes(DEFAULT_STRING_ENCODING));

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
        super(contentType, transferEncoding);
        try {
            setData(data.getBytes(DEFAULT_STRING_ENCODING));

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
    {
        super(contentType, transferEncoding);
        try {
            setData(data.getBytes(enc));

            this.enc = enc;
        } catch (UnsupportedEncodingException e) {
            throw new MissingResourceException("Encoding " + enc + 
					       "not supported",
                                               "StringDataStream", enc);
        }
    }

    /**
     * Returns the encoding used to convert the <code>String</code> to a
     * <code>bytes[]</code>.
     */
    public String getEncoding()
    {
        return this.enc;
    }
}
