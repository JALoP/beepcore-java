/*
 * TestFileDataStream.java  $Revision: 1.4 $ $Date: 2003/06/03 02:53:28 $
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

/*
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import junit.framework.*;
*/

public class TestFileDataStream /*extends TestDataStream*/ {
/*
    static public String filename;

    public TestFileDataStream(String name) {
        super(name);
    }

    public static void main (String[] args) {
        if( args.length == 0 )
        {
          System.out.println("Missing command line parameter: <filename>");
          return;
        }
        filename = args[0];
        junit.textui.TestRunner.run (suite());
    }

    protected void setUp()
        throws UnsupportedEncodingException, BEEPException, IOException {

        Channel channel = new Channel("0", "0", null, null);

        File f = new File( filename );
        int size = (int)f.length();
        char[] chars = new char[size];
        FileReader fr = new FileReader(f);
        fr.read( chars );
        String s = new String( chars );
        data = new FileDataStream(f);

        String EH1 = "EntityHeader1";
        String h1 = "header1";
        String EH2 = "EntityHeader2";
        String h2 = "header2";
        String ct = "ContentType1";
        String te = "TransferEncoding1";

        data.setHeader( EH1, h1 );
        data.setHeader( EH2, h2 );
        data.setContentType( ct );
        data.setTransferEncoding( te );

        headers.put( EH1, h1 );
        headers.put( EH2, h2 );
        headers.put( DataStream.CONTENT_TYPE, ct );
        headers.put( DataStream.CONTENT_TRANSFER_ENCODING, te );

        StringBuffer messageBuffer = new StringBuffer();

        messageBuffer.append(serializeHeaders());

        dataOffset = messageBuffer.length();

        messageBuffer.append(s);
        message = messageBuffer.toString().getBytes("UTF8");
    }

    public static Test suite() {
        return new TestSuite(TestFileDataStream.class);
    }
*/
}
