/*
 * TestByteDataStream.java  $Revision: 1.2 $ $Date: 2001/11/08 05:51:35 $
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

import junit.framework.*;

public class TestByteDataStream extends TestDataStream {

    public TestByteDataStream(String name) {
        super(name);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

    protected void setUp() throws UnsupportedEncodingException, BEEPException {
        Channel channel = new Channel("0", "0", null, null);

        String s = "12345678901234567890x2345678901234567890";

        String EH1 = "EntityHeader1";
        String h1 = "header1";
        String EH2 = "EntityHeader2";
        String h2 = "header2";

        data = new ByteDataStream(s.getBytes("UTF-8"));

        data.setHeader( EH1, h1 );
        data.setHeader( EH2, h2 );

        headers.put( EH1, h1 );
        headers.put( EH2, h2 );

        headers.put( DataStream.CONTENT_TYPE,
                      DataStream.DEFAULT_CONTENT_TYPE );
        headers.put( DataStream.CONTENT_TRANSFER_ENCODING,
                      DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING );

        StringBuffer messageBuffer = new StringBuffer();

        messageBuffer.append(serializeHeaders());

        dataOffset = messageBuffer.length();

        messageBuffer.append(s);
        message = messageBuffer.toString().getBytes("UTF8");
    }

    public static Test suite() {
        return new TestSuite(TestByteDataStream.class);
    }

}
