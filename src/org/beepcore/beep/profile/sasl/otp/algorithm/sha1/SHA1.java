
/*
 * SHA1.java            $Revision: 1.3 $ $Date: 2003/04/23 15:23:00 $
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
package org.beepcore.beep.profile.sasl.otp.algorithm.sha1;


import org.beepcore.beep.profile.sasl.InvalidParameterException;
import org.beepcore.beep.profile.sasl.otp.algorithm.AlgorithmImpl;


/**
 * This class does the 'funky OTP stuff' to the SHA1 hash
 * (basically folds it to 64 bits and makes it little endian).
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.3 $, $Date: 2003/04/23 15:23:00 $
 *
 */
public class SHA1 extends AlgorithmImpl {

    private static final String SHA1_INTERNAL = "SHA";
    private static final String SHA1_NAME = "otp-sha1";

    public SHA1()
    {
        super(SHA1_INTERNAL);
    }

    public String getName()
    {
        return getAlgorithmName();
    }

    public static String getAlgorithmName()
    {
        return SHA1_NAME;
    }

    public byte[] generateHash(byte hash[]) 
        throws InvalidParameterException
    {
        if (hash == null) {
            throw new InvalidParameterException();
        }

        hash = super.generateHash(hash);
        return foldHash(hash);
    }
    
    protected byte[] foldHash(byte hash[])
        throws InvalidParameterException
    {
        if (hash == null) {
            throw new InvalidParameterException();
        }

        for (int i = 0; i < 4; i++) {
            hash[i] ^= (0xff & hash[i + 8]);
        }

        for (int i = 4; i < 8; i++) {
            hash[i] ^= (0xff & hash[i + 8]);
        }

        for (int i = 0; i < 4; i++) {
            hash[i] ^= (0xff & hash[i + 16]);
        }

        byte newHash[] = new byte[8];

        for (int i = 0; i < 4; i++) {
            newHash[3 - i] = hash[i];
            newHash[7 - i] = hash[i + 4];
        }

        return (newHash);
    }
}
