
/*
 * AlgorithmImpl.java            $Revision: 1.4 $ $Date: 2003/09/13 21:19:10 $
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
package org.beepcore.beep.profile.sasl.otp.algorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.beepcore.beep.profile.sasl.InvalidParameterException;

/**
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.4 $, $Date: 2003/09/13 21:19:10 $
 *
 */
public abstract class AlgorithmImpl implements Algorithm {

    // Data
    // We use two types of algorithm names - one for challenges
    // and one for the java.security.MessageDigest
    private String internalAlgorithmName;

    /**
     * Method AlgorithmImpl
     *
     * @param String internal is the data used by the JVM internally
     * to represent a certain MessageDigest hash algorithm.   This
     * is defined in JVM documentation and in constants in SASLOTPProfile.,
     *
     */
    public AlgorithmImpl(String internal)
    {
        internalAlgorithmName = internal;
    }

    /**
     * Method getName
     *
     *
     * @param String the algorithm's name.
     *
     */
    public abstract String getName();

    /**
     * Method generateHash generate a hash value using the appropriate
     * hash function.
     *
     * @param String s is the data to be hashed
     * @return byte[] the hash value in binary form.
     *
     * @throws SASLException if an error is encountered during the 
     * generation of hte hash.
     *
     */
    public byte[] generateHash(String s) 
        throws InvalidParameterException
    {
        return generateHash(s.toLowerCase().getBytes()); ///@TODO use encoding
    }

    /**
     * Method generateHash generate a hash value using the appropriate
     * hash function.
     *
     * @param byte[] data is the data to be hashed
     * @return byte[] the hash value in binary form.
     *
     * @throws SASLException if an error is encountered during the 
     * generation of hte hash.
     *
     */
    public byte[] generateHash(byte data[]) 
        throws InvalidParameterException
    {
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance(internalAlgorithmName);
        } catch (NoSuchAlgorithmException x) {
            throw new RuntimeException(internalAlgorithmName
                                       + " hash algorithm not found");
        }
        return digest.digest(data);
    }

    /**
     * Method foldHash is provided for implementations, as the value
     * of the message digest hash must be folding into 64 bits before 
     * it can be used by the SASLOTPProfile and its supporting classes.
     *
     * @param byte[] data the hash value to be folded
     * @return byte[] is the folded hash.
     *
     * @throws InvalidParameterException of the has provided is
     * somehow improper or invalid.
     *
     */
    protected abstract byte[] foldHash(byte hash[])
        throws InvalidParameterException;
}
