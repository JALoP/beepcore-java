
/*
 * MD5.java            $Revision: 1.4 $ $Date: 2003/04/23 15:23:06 $
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
package org.beepcore.beep.profile.sasl.otp.algorithm.md5;


import org.beepcore.beep.profile.sasl.InvalidParameterException;
import org.beepcore.beep.profile.sasl.otp.algorithm.AlgorithmImpl;


/**
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.4 $, $Date: 2003/04/23 15:23:06 $
 *
 */
public class MD5 extends AlgorithmImpl {

    private static final String MD5_INTERNAL = "MD5";
    public static final String MD5_NAME = "otp-md5";

    public MD5()
    {
        super(MD5_INTERNAL);
    }

    public String getName()
    {
        return getAlgorithmName();
    }

    public static String getAlgorithmName()
    {
        return MD5_NAME;
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
        byte[] newHash = new byte[8];

        for(int i=0; i<8; i++)
        {
            newHash[i] = hash[i];
            newHash[i] ^= hash[i+8];
        }
        return newHash;
    }
}
