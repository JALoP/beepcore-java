/*
 * TestStringDataStream.java  $Revision: 1.3 $ $Date: 2001/11/08 05:51:35 $
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

public class TestStringDataStream extends TestCase {
    protected StringDataStream data;
    protected byte[] message;
    protected int dataOffset = 0;
    protected Hashtable headers = new Hashtable();

    public TestStringDataStream(String name) {
        super(name);
    }

    public void testGetNextSegment() {
        int i=0;
        while (data.isComplete() == false || data.availableSegment()) {
            BufferSegment b = data.getNextSegment();
            for (int j=b.getOffset(); j < b.getOffset() + b.getLength(); ++j) {
                assertEquals((char)message[i++], (char)(b.getData()[j]));
            }
        }
    }

    protected void setUp() throws UnsupportedEncodingException, BEEPException {
        String s = "1234567890";

        String EH1 = "EntityHeader1";
        String h1 = "header1";
        String EH2 = "EntityHeader2";
        String h2 = "header2";
        String te = "TransferEncoding1";

        data = new StringDataStream(s);

        data.setHeader(EH1, h1);
        data.setHeader(EH2, h2);
        data.setTransferEncoding(te);
        data.setContentType(MimeHeaders.BEEP_XML_CONTENT_TYPE);

        headers.put(MimeHeaders.CONTENT_TYPE,
                    MimeHeaders.BEEP_XML_CONTENT_TYPE);
        headers.put(MimeHeaders.CONTENT_TRANSFER_ENCODING, te);
        headers.put(EH1, h1);
        headers.put(EH2, h2);

        StringBuffer messageBuffer = new StringBuffer();

        messageBuffer.append(serializeHeaders(data.getHeaderNames(), headers));

        dataOffset = messageBuffer.length();

        messageBuffer.append(s);
        message = messageBuffer.toString().getBytes("UTF8");
    }

    private String serializeHeaders(Enumeration names, Hashtable headers)
        throws BEEPException
    {
        StringBuffer buf = new StringBuffer();
        //        Enumeration names = data.getHeaderNames();

        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = (String) headers.get(name);

            if ((name.equals(MimeHeaders.CONTENT_TYPE) &&
                 value.equals(MimeHeaders.DEFAULT_CONTENT_TYPE)) ||
                (name.equals(MimeHeaders.CONTENT_TRANSFER_ENCODING) &&
                 value.equals(MimeHeaders.DEFAULT_CONTENT_TRANSFER_ENCODING)))
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

    public static Test suite() {
        return new TestSuite(TestStringDataStream.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
}
