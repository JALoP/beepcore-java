/*
 * TestDataStream.java  $Revision: 1.1 $ $Date: 2001/10/31 00:32:38 $
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

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.beepcore.beep.util.BufferSegment;

import junit.framework.*;

public class TestInputDataStreamAdapter extends TestCase {
    protected InputDataStream data;
    protected static byte[] message;
    protected static int dataOffset = 0;
    protected static Hashtable headers = new Hashtable();

    public TestInputDataStreamAdapter(String name) {
        super(name);
    }

     public void testAvailable() throws IOException {
         InputStream is = data.getInputStream();
         assertEquals("is.available()",
                      message.length - dataOffset, is.available());
     }

    public void testRead() throws IOException {
        InputStream is = data.getInputStream();

        for (int i=0; i < message.length - dataOffset; ++i) {
            assertEquals("is.available()",
                         is.available(), message.length - dataOffset - i);
            assertEquals(//"Byte number: " + i + " did not match",
                         ((byte) is.read()), message[dataOffset + i]);
        }

        assertEquals("is.available() != 0", 0, is.available());
    }

    public void testReadArray() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[is.available()];
        int count = is.read(b);
        assertEquals("count == message.length - dataOffset",
                     message.length - dataOffset, count);
        for (int i=0; i < message.length - dataOffset; ++i) {
            assertEquals(message[dataOffset + i], b[i]);
        }
        assertEquals("is.available() == 0", 0, is.available());
    }

    public void testReadArrayWithOffsetAndLength() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[is.available() + 10];
        int count = is.read(b, 5, is.available());
        assertEquals("count == message.length - dataOffset",
                     message.length - dataOffset, count);
        for (int i=0; i < message.length - dataOffset; ++i) {
            assertEquals(//"Byte number: " + i + " did not match",
                         message[dataOffset + i], b[i+5]);
        }
        assertEquals("is.available() == 0", 0, is.available());
    }

    public void testPartialReadArray() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[is.available()/2];
        int count = is.read(b, 0, b.length);
        if (count != b.length) {
            assertEquals("count == message.length - dataOffset",
                         message.length - dataOffset, count);
        }
        for (int i=0; i < count; ++i) {
            assertEquals(//"Byte number: " + i + " did not match",
                         message[dataOffset + i], b[i]);
        }
        assertEquals("is.available() == message.length - dataOffset - count",
                     message.length - dataOffset - count, is.available());
    }

    public void testTwoPartialReadArray() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[40];
        int count = is.read(b, 0, Math.min(is.available(), b.length)/2);
        count += is.read(b, count, b.length - count);
        if (count != b.length) {
            assertEquals("count == message.length (" + message.length +
                         ") - dataOffset (" + dataOffset + ")",
                         message.length - dataOffset, count);
        }
        for (int i=0; i < b.length - dataOffset; ++i) {
            assertEquals("Byte number: " + i + " did not match",
                         message[dataOffset + i], b[i]);
        }
        assertEquals("is.available() == message.length - dataOffset - count",
                     message.length - dataOffset - count, is.available());
    }

    public void testPartialReadArrayWithOffset() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[is.available()/2];
        int count = is.read(b, 5, b.length - 5);
        if (count != b.length - 5) {
            assertEquals("count == message.length - dataOffset - 5",
                         message.length - dataOffset - 5, count);
        }
        for (int i=0; i < count - 5; ++i) {
            assertEquals(//"Byte number: " + i + " did not match",
                         message[dataOffset + i], b[i+5]);
        }
        assertEquals("is.available() == message.length - dataOffset - count",
                     message.length - dataOffset - count, is.available());
    }

    public void testMarkAndReset() throws IOException {
        InputStream is = data.getInputStream();

        if( is.markSupported() )
        {
          byte[] b = new byte[20];

          is.mark(b.length);
          int count = is.read(b);
          is.reset();

          for (int i=0; i<count; ++i) {
              assertEquals("Byte number: " + i + " did not match",
                           (byte) is.read(), b[i]);
          }

          for (int i=0; is.available() != 0; ++i) {
              assertEquals("Byte number: " + i + " did not match",
                           message[dataOffset + b.length + i],
                           (byte) is.read());
          }
        }
    }

    public void testSkip() throws IOException {
        final int SKIP_AMOUNT = (message.length - dataOffset) / 2;
        InputStream is = data.getInputStream();
        is.skip(SKIP_AMOUNT);
        byte[] b = new byte[20];
        int count = is.read(b);
        for (int i=0; i < count; ++i) {
            assertEquals("Byte number: " + i + " did not match",
                         message[dataOffset + SKIP_AMOUNT + i], b[i]);
        }
    }

    public void testHeaders() throws IOException, BEEPException {
        Enumeration e = data.getInputStream().getHeaderNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String header = data.getInputStream().getHeaderValue(name);
            String origHeader = (String)headers.get(name);
            assertEquals("Header: " + name + " values do not match (" +
                         header + "!=" + origHeader + ")", origHeader, header);
        }

        if (!headers.containsKey(MimeHeaders.CONTENT_TYPE)) {
          assertEquals("Unexpected content type",
                       MimeHeaders.DEFAULT_CONTENT_TYPE,
                       data.getInputStream().getContentType());
        }
        if (!headers.containsKey(MimeHeaders.CONTENT_TRANSFER_ENCODING)) {
          assertEquals("Unexpected transfer encoding",
                       MimeHeaders.DEFAULT_CONTENT_TRANSFER_ENCODING,
                       data.getInputStream().getTransferEncoding());
        }
    }

    protected void setUp() throws UnsupportedEncodingException {
        int j = 0;
        int frameSize = 1400;

        data = new InputDataStream();

        do {
            int len = Math.min(message.length -j, frameSize);

            data.add(new BufferSegment(message, j, len));

            j += len;
        } while (j < message.length);

        data.setComplete();
    }

    public static Test suite() {
        return new TestSuite(TestInputDataStreamAdapter.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

    static {
        headers.put(MimeHeaders.CONTENT_TYPE,
                    MimeHeaders.DEFAULT_CONTENT_TYPE);
        headers.put(MimeHeaders.CONTENT_TRANSFER_ENCODING,
                    MimeHeaders.DEFAULT_CONTENT_TRANSFER_ENCODING);

        char[] c = new char[60000];

        c[0] = '\r';
        c[1] = '\n';
        c[2] = 'a';

        for (int i = 3; i < c.length; ++i) {
            c[i] = (char) (c[i - 1] + 1);

            if (c[i] > 'z') {
                c[i] = 'a';
            }
        }

        dataOffset = 2; // CRLF

        try {
            message = new String(c).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {}
    }
}

