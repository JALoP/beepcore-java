/*
 * HeaderParser.java            $Revision: 1.1 $ $Date: 2001/11/27 16:04:59 $
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


import org.beepcore.beep.core.BEEPException;

/**
 *
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2001/11/27 16:04:59 $
 */
public class HeaderParser {
    static final int MAX_INT_STRING_LENGTH = 10;
    static final long UNSIGNED_INT_MAX_VALUE = 4294967295L;

    private byte[] buf;
    private int off = 0;
    private int len;

    public HeaderParser(byte[] buf, int len) {
        this.buf = buf;
        this.len = len;
    }

    public boolean hasMoreTokens() {
        return off < len;
    }

    public boolean parseLast() throws BEEPException {
        if (hasMoreTokens() == false) {
            throw new BEEPException("Malformed BEEP Header");
        }

        int tl = tokenLength();
        if (tl != 1) {
            throw new BEEPException("Malformed BEEP Header");
        }

        char c = (char)(buf[off] & 0x7f);

        if (c != '*' && c != '.') {
            throw new BEEPException("Malformed BEEP Header");
        }

        findNextToken(tl);

        return c == '.';
    }

    public int parseInt() throws BEEPException {
        if (hasMoreTokens() == false) {
            throw new BEEPException("Malformed BEEP Header");
        }

        int tl = tokenLength();

        int i = atoi(buf, off, tl);

        findNextToken(tl);

        return i;
    }

    public long parseUnsignedInt() throws BEEPException {
        if (hasMoreTokens() == false) {
            throw new BEEPException("Malformed BEEP Header");
        }

        int tl = tokenLength();

        long l = atoui(buf, off, tl);

        findNextToken(tl);

        return l;
    }

    public char[] parseType() throws BEEPException {
        if (hasMoreTokens() == false) {
            throw new BEEPException("Malformed BEEP Header");
        }

        int tl = tokenLength();
        if (tl != 3) {
            throw new BEEPException("Malformed BEEP Header");
        }

        char[] c = new char[3];

        c[0] = (char) (buf[off] & 0xff);
        c[1] = (char) (buf[off+1] & 0xff);
        c[2] = (char) (buf[off+2] & 0xff);

        findNextToken(tl);

        return c;
    }

    private void findNextToken(int previousLength) throws BEEPException {
            
        off += previousLength + 1;

        if (off > len)
            return;

        if (off == len ||
            buf[off - 1] != ' ' || buf[off] == ' ')
        {
            throw new BEEPException("Malformed BEEP Header");
        }
    }

    private int tokenLength()
    {
        for (int i=off; i<len; ++i) {
            if ((buf[i] & 0xff) == ' ') {
                return i - off;
            }
        }
        return len - off;
    }

    private static int atoi(byte[] b, int off, int len)
        throws NumberFormatException
    {
        long res = atoui(b, off, len);
        if (res > Integer.MAX_VALUE) {
            throw new NumberFormatException();
        }

        return (int) res;
    }

    private static long atoui(byte[] b, int off, int len)
        throws NumberFormatException
    {
        if (len > MAX_INT_STRING_LENGTH) {
            throw new NumberFormatException();
        }

        long res = 0;
        for (int i=off; i<len + off; ++i) {
            if (b[i] < (byte) '0' || b[i] > (byte) '9') {
                System.out.println("b[" + i + "] = " + b[i]);
                throw new NumberFormatException();
            }

            res *= 10;
            res += (b[i] - (byte) '0');
        }

        if (res > UNSIGNED_INT_MAX_VALUE) {
            throw new NumberFormatException();
        }

        return res;
    }
}
