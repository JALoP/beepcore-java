/*
 * StringUtil.java  $Revision: 1.1 $ $Date: 2001/11/27 17:37:22 $
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
package org.beepcore.beep.util;


/**
 *
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2001/11/27 17:37:22 $
 */
public class StringUtil
{
    private StringUtil() {}
    
    public static String asciiToString(byte[] b, int off, int len)
    {
        char[] c = new char[len];

        for (int i=0; i<len; ++i) {
            c[i] = (char)(b[off + i] & 0xff);
        }

        return new String(c);
    }

    public static byte[] stringToAscii(String s)
    {
        byte[] b = new byte[s.length()];

        for (int i=0; i<b.length; ++i) {
            b[i] = (byte)s.charAt(i);
        }

        return b;
    }

    public static byte[] stringBufferToAscii(StringBuffer s)
    {
        byte[] b = new byte[s.length()];

        for (int i=0; i<b.length; ++i) {
            b[i] = (byte)s.charAt(i);
        }

        return b;
    }
}
