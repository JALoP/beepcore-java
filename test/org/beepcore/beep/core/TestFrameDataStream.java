/*
 * TestFrameDataStream.java  $Revision: 1.1 $ $Date: 2001/05/16 19:42:41 $
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

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import junit.framework.*;

public class TestFrameDataStream extends TestDataStream {

    public TestFrameDataStream(String name) {
        super(name);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

    protected void setUp() {
        Channel channel = new Channel("0", "0", null, null);
        data = new FrameDataStream();
//        int max = 11;
//        String[] s = new String[max];
//
//        s[0] = "EntityHeader1: header1\r\n";
//        s[1] = "EntityHeader2: header2\r\n";
//        s[2] = "\r\n123456789012";
//        dataOffset = s[0].length() + s[1].length() + 2; // 24 + 24 + 2
//        s[3] = "34567890";
//        s[4] = "x234";
//        s[5] = "5";
//        s[6] = "6";
//        s[7] = "7890123";
//        s[8] = "4567";
//        s[9] = "89";
//        s[10] = "0";
//
//        try {
//            for (int i=0; i < max; i++) {
//                Frame f = new Frame(Message.MESSAGE_TYPE_MSG, channel, 1,
//                                    (i == max-1), i, -1,
//                                    new Frame.BufferSegment(s[i].getBytes("UTF-8")));
//                ((FrameDataStream)data).add(f);
//            }
//            String temp = "";
//            for (int i=0; i < max; i++) {
//                temp += s[i];
//            }
//            message = temp.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException e) {}
//
//        String EH1 = "EntityHeader1";
//        String h1 = "header1";
//        String EH2 = "EntityHeader2";
//        String h2 = "header2";
//
//        headers.put( EH1, h1 );
//        headers.put( EH2, h2 );
//
        headers.put( DataStream.CONTENT_TYPE,
                      DataStream.DEFAULT_CONTENT_TYPE );
        headers.put( DataStream.CONTENT_TRANSFER_ENCODING,
                      DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING );

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

        int j = 0;
        int frameSize = 1400;
        do {
            if (message.length - j < frameSize) {
                frameSize = message.length - j;
            }
            Frame f = new Frame(Message.MESSAGE_TYPE_MSG, channel, 1,
                                (message.length - j == frameSize), j, -1,
                                new Frame.BufferSegment(message, j,
                                                        frameSize));
            ((FrameDataStream)data).add(f);

            j += 1400;
        } while (j < message.length);
    }

    public static Test suite() {
        return new TestSuite(TestFrameDataStream.class);
    }
}
