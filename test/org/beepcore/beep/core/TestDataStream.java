/*
 * TestDataStream.java  $Revision: 1.2 $ $Date: 2001/11/08 05:51:35 $
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

import junit.framework.*;

abstract public class TestDataStream extends TestCase {
    protected DataStream data;
    protected byte[] message;
    protected int dataOffset = 0;
    protected Hashtable headers = new Hashtable(4);

    public TestDataStream(String name) {
        super(name);
    }

    protected String serializeHeaders() throws BEEPException {
        StringBuffer buf = new StringBuffer();
        Enumeration names = data.getHeaderNames();

        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = (String) headers.get(name);

            if ((name.equals(DataStream.CONTENT_TYPE) &&
                 value.equals(DataStream.DEFAULT_CONTENT_TYPE))
                || (name.equals(DataStream.CONTENT_TRANSFER_ENCODING)
                    && value.equals(DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING)))
            {

                // these are default values that don't need to be added
                // to the payload
                continue;
            }


            buf.append(name);
            buf.append(": ");
            buf.append(value);
            buf.append("\r\n");
        }

        buf.append("\r\n");

        return buf.toString();
    }

    public void testAvailable() throws IOException {
        InputStream is = data.getInputStream();
        assert("is.available()",
               is.available() == message.length - dataOffset);
    }

    public void testRead() throws IOException {
        InputStream is = data.getInputStream();

        for (int i=0; i < message.length - dataOffset; ++i) {
            assert("is.available()",
                   is.available() == message.length - dataOffset - i);
            assert("Byte number: " + i + " did not match",
                   ((byte) is.read()) == message[dataOffset + i]);
        }

        assert("is.available() == 0", is.available() == 0);
    }

    public void testReadArray() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[is.available()];
        int count = is.read(b);
        assert("count == message.length - dataOffset",
               count == message.length - dataOffset);
        for (int i=0; i < message.length - dataOffset; ++i) {
            assert("Byte number: " + i + " did not match",
                    b[i] == message[dataOffset + i]);
        }
        assert("is.available() == 0", is.available() == 0);
    }

    public void testReadArrayWithOffsetAndLength() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[is.available() + 10];
        int count = is.read(b, 5, is.available());
        assert("count == message.length - dataOffset",
               count == message.length - dataOffset);
        for (int i=0; i < message.length - dataOffset; ++i) {
            assert("Byte number: " + i + " did not match",
                    b[i+5] == message[dataOffset + i]);
        }
        assert("is.available() == 0", is.available() == 0);
    }

    public void testPartialReadArray() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[is.available()/2];
        int count = is.read(b, 0, b.length);
        if (count != b.length) {
            assert("count == message.length - dataOffset",
                   count == message.length - dataOffset);
        }
        for (int i=0; i < count; ++i) {
            assert("Byte number: " + i + " did not match",
                    b[i] == message[dataOffset + i]);
        }
        assert("is.available() == message.length - dataOffset - count",
               is.available() == message.length - dataOffset - count);
    }

    public void testTwoPartialReadArray() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[40];
        int count = is.read(b, 0, Math.min(is.available(), b.length)/2);
        count += is.read(b, count, b.length - count);
        if (count != b.length) {
            assert("count (" + count + ") == message.length (" +
                   message.length + ") - dataOffset (" + dataOffset + ")",
                   count == message.length - dataOffset);
        }
        for (int i=0; i < b.length - dataOffset; ++i) {
            assert("Byte number: " + i + " did not match",
                    b[i] == message[dataOffset + i]);
        }
        assert("is.available() == message.length - dataOffset - count",
               is.available() == message.length - dataOffset - count);
    }

    public void testPartialReadArrayWithOffset() throws IOException {
        InputStream is = data.getInputStream();
        byte[] b = new byte[is.available()/2];
        int count = is.read(b, 5, b.length - 5);
        if (count != b.length - 5) {
            assert("count == message.length - dataOffset - 5",
                   count == message.length - dataOffset - 5);
        }
        for (int i=0; i < count - 5; ++i) {
            assert("Byte number: " + i + " did not match",
                    b[i+5] == message[dataOffset + i]);
        }
        assert("is.available() == message.length - dataOffset - count",
               is.available() == message.length - dataOffset - count);
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
              assert("Byte number: " + i + " did not match",
                      b[i] == (byte) is.read());
          }

          for (int i=0; is.available() != 0; ++i) {
              assert("Byte number: " + i + " did not match",
                      ((byte) is.read()) == message[dataOffset + b.length + i]);
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
            assert("Byte number: " + i + " did not match",
                    b[i] == message[dataOffset + SKIP_AMOUNT + i]);
        }
    }

    public void testHeaders() throws IOException, BEEPException {
        Enumeration e = data.getHeaderNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String header = data.getHeaderValue(name);
            String origHeader = (String)headers.get(name);
            assert( "Header: " + name + " values do not match (" + header + "!="
                    + origHeader + ")", header.equals(origHeader) );
        }

        if( !headers.containsKey( DataStream.CONTENT_TYPE ) )
        {
          assert( "Unexpected content type",
                  data.getContentType().equals(DataStream.DEFAULT_CONTENT_TYPE) );
        }
        if( !headers.containsKey( DataStream.CONTENT_TRANSFER_ENCODING ) )
        {
          assert( "Unexpected transfer encoding",
                  data.getTransferEncoding().equals(DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING) );
        }
    }

    public void testReadHeadersAndData() throws BEEPException {
        byte[] b = new byte[data.availableHeadersAndData()];

        int count = data.readHeadersAndData(b, 0, b.length);

        for (int i=0; i < b.length; ++i) {
            assert("(i = " + i + ") " + (char)b[i] + " == " + (char)message[i],
                   b[i] == message[i]);
        }
    }

    public void testPartialReadHeadersAndData() throws BEEPException {
        byte[] b = new byte[data.availableHeadersAndData()];

        int count = data.readHeadersAndData(b, 0, dataOffset / 2);

        count = data.readHeadersAndData(b, count, b.length - count);

        for (int i=0; i < b.length; ++i) {
            assert("(i = " + i + ") " + (char)b[i] + " == " + (char)message[i],
                   b[i] == message[i]);
        }
    }
}

