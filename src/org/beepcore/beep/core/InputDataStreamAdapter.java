/*
 * InputDataStreamAdapter.java  $Revision: 1.2 $ $Date: 2002/10/05 15:28:49 $
 *
 * Copyright (c) 2002 Huston Franklin.  All rights reserved.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.beepcore.beep.util.BufferSegment;

public class InputDataStreamAdapter extends java.io.InputStream {

    InputDataStreamAdapter(InputDataStream ids)
    {
        this.ids = ids;
    }

    public int available()
    {
        if (this.state != STATE_HEADERS_PARSED) {
            parseHeaders();
            if (this.state != STATE_HEADERS_PARSED) {
                return 0;
            }
        }

        if (this.pos == this.curBuf.getLength()) {
            setNextBuffer();
        }
        return (this.curBuf.getLength() - this.pos) + this.ids.available();
    }

    public void close()
    {
        ids.close();
    }

    /**
     * Returns the content type of a <code>FrameDataStrea</code>.  If the
     * <code>Frame</code> containing the content type hasn't been received yet,
     * the method blocks until it is received.
     *
     * @return Content type.
     *
     * @throws BEEPException
     */
    public String getContentType() throws BEEPException
    {
        return this.getHeaderValue(MimeHeaders.CONTENT_TYPE);
    }

    /**
     * Returns the value of the MIME entity header which corresponds
     * to the given <code>name</code>. If the <code>Frame</code>
     * containing the content type hasn't been received yet, the
     * method blocks until it is received.
     *
     * @param name Name of the entity header.
     * @return String Value of the entity header.
     *
     * @throws BEEPException
     */
    public String getHeaderValue(String name) throws BEEPException
    {
        waitAvailable();

        return (String) this.mimeHeaders.get(name);
    }

    /**
     * Returns an <code>Enumeration</code> of all the MIME entity header names
     * belonging to this <code>FrameDataStream</code>.  If the
     * <code>Frame</code> containing the content type hasn't been received yet,
     * the method blocks until it is received.
     *
     * @throws BEEPException
     */
    public Enumeration getHeaderNames() throws BEEPException
    {
        waitAvailable();

        return this.mimeHeaders.keys();
    }

    /**
     * Returns the transfer encoding of a <code>FrameDataStrea</code>.  If the
     * <code>Frame</code> containing the content type hasn't been received yet,
     * the method blocks until it is received.
     *
     * @return Content type.
     *
     * @throws BEEPException
     */
    public String getTransferEncoding() throws BEEPException
    {
        return this.getHeaderValue(MimeHeaders.CONTENT_TRANSFER_ENCODING);
    }

    public int read() throws IOException
    {
        if (waitAvailable() == -1) {
            return -1;
        }

        return internalRead();
    }

    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }


    public int read(byte[] b, int off, int len) throws IOException
    {
        if (off == -1 || len == -1 || off + len > b.length)
            throw new IndexOutOfBoundsException();

        int bytesRead = 0;
        while (bytesRead < len) {
            if (waitAvailable() == -1) {
                if (bytesRead == 0) {
                    return -1;
                } else {
                    break;
                }
            }
            int n = internalRead(b, off + bytesRead, len - bytesRead);
            if (n == 0 && ids.isClosed()) {
                return bytesRead != 0 ? bytesRead : -1;
            }
            bytesRead += n;
        }

        return bytesRead;
    }

    public long skip(long n) throws IOException
    {
        if (n > Integer.MAX_VALUE) {
            return 0;
        }

        int bytesSkipped = 0;
        while (bytesSkipped < n && ids.isClosed() == false) {
            if (waitAvailable() == -1) {
                break;
            }
            int i = Math.min(((int) n) - bytesSkipped,
                             curBuf.getLength() - pos);
            pos += i;
            bytesSkipped += i;
        }

        return bytesSkipped;
    }

    /**
     * This version of read() does not block if there are no bytes
     * available.
     */
    private int internalRead()
    {
        if (setNextBuffer() == false) {
            return -1;
        }

        int b = curBuf.getData()[curBuf.getOffset() + pos] & 0xff;

        pos++;

        return b;
    }

    /**
     * This version of read(byte[], int, int) does not block if
     * there are no bytes available.
     */
    private int internalRead(byte[] b, int off, int len)
    {
        int bytesRead = 0;

        while (bytesRead < len) {
            if (setNextBuffer() == false) {
                break;
            }

            int n = Math.min(len - bytesRead, curBuf.getLength() - pos);
            System.arraycopy(curBuf.getData(), curBuf.getOffset() + pos,
                             b, off + bytesRead, n);

            pos += n;
            bytesRead += n;
        }

        return bytesRead;
    }

    private void parseHeaders()
    {
        while (true) {
            switch (state) {
            case STATE_INIT:
                state = STATE_PARSING_NAME;
                break;
            case STATE_PARSING_NAME:
                parseName();
                if (state == STATE_PARSING_NAME) {
                    return;
                }
                break;
            case STATE_PARSING_NAME_TERMINATOR:
                parseNameTerminator();
                if (state == STATE_PARSING_NAME_TERMINATOR) {
                    return;
                }
                break;
            case STATE_PARSING_VALUE:
                parseValue();
                if (state == STATE_PARSING_VALUE) {
                    return;
                }
                break;
            case STATE_PARSING_VALUE_TERMINATOR:
                parseValueTerminator();
                if (state == STATE_PARSING_VALUE_TERMINATOR) {
                    return;
                }
                break;
            case STATE_PARSING_HEADERS_TERMINATOR:
                parseHeadersTerminator();
                if (state == STATE_PARSING_HEADERS_TERMINATOR) {
                    return;
                }
                break;
            case STATE_HEADERS_PARSED:
                return;
            }
        }
    }

    private void parseHeadersTerminator()
    {
        int b = internalRead(); // move off of the LF
        if (b == -1) {
            return;
        }

        if (this.mimeHeaders.get(MimeHeaders.CONTENT_TYPE) == null) {
            this.mimeHeaders.put(MimeHeaders.CONTENT_TYPE,
                                 MimeHeaders.DEFAULT_CONTENT_TYPE);
        }

        if (mimeHeaders.get(MimeHeaders.CONTENT_TRANSFER_ENCODING) == null) {
            mimeHeaders.put(MimeHeaders.CONTENT_TRANSFER_ENCODING,
                            MimeHeaders.DEFAULT_CONTENT_TRANSFER_ENCODING);
        }

        state = STATE_HEADERS_PARSED;
    }

    private void parseName()
    {
        int b = internalRead();
        if (b == -1) {
            return;
        }

        if (b == CR) {
            state = STATE_PARSING_HEADERS_TERMINATOR;
            return;
        }

        name.write((byte) b);

        // find the ':'
        while (true) {
            b = internalRead();
            if (b == -1) {
                return;
            }

            if (b == COLON) {
                state = STATE_PARSING_NAME_TERMINATOR;
                return;
            }

            name.write((byte) b);
        }
    }

    private void parseNameTerminator()
    {
        int b = internalRead(); // we are pointing to ':' move off of ' '
        if (b == -1) {
            return;
        }
        state = STATE_PARSING_VALUE;
    }

    private void parseValue()
    {
        while (true) {
            int b = internalRead();
            if (b == -1) {
                return;
            }

            if (b == CR) {
                state = STATE_PARSING_VALUE_TERMINATOR;
                return;
            }

            value.write((byte) b);
        }
    }

    private void parseValueTerminator()
    {
        int b = internalRead(); // move off of LF
        if (b == -1) {
            return;
        }

        try {
            this.mimeHeaders.put(name.toString("UTF-8"),
                                 value.toString("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported");
        }
        name.reset();
        value.reset();
        state = STATE_PARSING_NAME;
    }

    /**
     * If there are no bytes remaining in the current buffer move to
     * the next one if it exists.
     */
    private boolean setNextBuffer() {
        while (pos == curBuf.getLength()) {
            if (ids.availableSegment() == false) {
                return false;
            }

            curBuf = ids.getNextSegment();
            pos = 0;
        }

        return true;
    }

    /**
     * Wait until available() != 0
     */
    private int waitAvailable() {
        int n;

        if ((n = available()) > 0) {
            return n;
        }

        synchronized (ids.buffers) {
            while ((n = available()) == 0) {

                if (ids.isComplete() == true) {

                    // no more bytes to read() and none are
                    // expected, return -1
                    return -1;
                }

                // no bytes available to read, but more are
                // expected... block
                try {
                    ids.buffers.wait();
                } catch (InterruptedException e) {
                    log.error("waiting for buffer", e);
                }
            }

            return n;
        }
    }

    private static final BufferSegment zeroLength =
        new BufferSegment(new byte[0]);
    private int pos = 0;
    private BufferSegment curBuf = zeroLength;

    private static final int LF = 10;
    private static final int CR = 13;
    private static final int COLON = 58;

    private static final int STATE_INIT = 0;
    private static final int STATE_PARSING_NAME = 1;
    private static final int STATE_PARSING_NAME_TERMINATOR = 2;
    private static final int STATE_PARSING_VALUE = 3;
    private static final int STATE_PARSING_VALUE_TERMINATOR = 4;
    private static final int STATE_PARSING_HEADERS_TERMINATOR = 5;
    private static final int STATE_HEADERS_PARSED = 6;

    private Log log = LogFactory.getLog(this.getClass());

    private int state = STATE_INIT;
    private ByteArrayOutputStream name = new ByteArrayOutputStream(32);
    private ByteArrayOutputStream value = new ByteArrayOutputStream(32);
    private static final int DEFAULT_HEADER_TABLE_SIZE = 2;
    private Hashtable mimeHeaders =
        new Hashtable(DEFAULT_HEADER_TABLE_SIZE);
    private InputDataStream ids;
}
