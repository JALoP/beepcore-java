/*
 * UserDatabaseImpl.java            $Revision: 1.4 $ $Date: 2003/04/23 15:23:05 $
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
package org.beepcore.beep.profile.sasl.otp.database;


import org.beepcore.beep.profile.sasl.SASLException;
import org.beepcore.beep.profile.sasl.otp.SASLOTPProfile;


/**
 * This class is an implementation of UserDatabase interface.
 * It uses the java.util.Property class, which uses the local
 * filesystem, to store SASL OTP user databases.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.4 $, $Date: 2003/04/23 15:23:05 $
 *
 */
public class UserDatabaseImpl 
    implements UserDatabase
{
    // Data
    private String authenticator;
    private String algorithm;
    private String lastHash;
    private String seed;
    private int sequence;
 
    /**
     * This constructor is the only one available, 
     * and it requires everything.
     *
     * @param String algorithm indicates the Algorithm to be used by
     * SASL OTP.  This library supports otp-md5 and otp-sha1.
     * @param String authenticator the identity of the user wishing 
     * to authenticate via SASL OTP.
     * @param String lastHash the hexidecimal representation of the 
     * sequence + 1 hash of the seed, passphrase combination.
     * @param String seed the string that's prepended to the passphrase
     * prior to the initial hash calculation.
     * @param int sequence the current OTP sequence, which represents 
     * the number of times the hash function is called with its own 
     * product.  This value is decremented each time authentication
     * occurs.
     *
     */
    UserDatabaseImpl(String algorithm, String lastHash, String seed,
                     String authenticator, int sequence)
    {
        this.algorithm = algorithm;
        this.authenticator = authenticator;
        this.lastHash = lastHash;
        this.seed = seed;
        this.sequence = sequence;
    }
    
    /**
     * Method getAlgorithmName
     *
     * @return String the algorithm employed by the user of this database
     * for SASL OTP authentication.
     *
     */
    public String getAlgorithmName()
    {
        return algorithm;
    }

    /**
     * Method getLastHash
     *
     * @return byte[] the lastHash value generated the last time the
     * user of this database performed SASL OTP authentication.  The
     * hash value is represented in binary form.
     *
     */
    public byte[] getLastHash() throws SASLException
    {
        return SASLOTPProfile.convertHexToBytes(lastHash);
    }

    /**
     * Method getLastHash
     *
     * @return String the lastHash value generated the last time the
     * user of this database performed SASL OTP authentication.  The
     * hash is represented in hexadecimal form.
     *
     */
    public String getLastHashAsString()
    {
        return lastHash;
    }

    /**
     * Method getLastHash
     *
     * @return String the seed used by the 
     * user of this database for SASL OTP authentication.
     *
     */
    public String getSeed()
    {
        return seed;
    }

    /**
     * Method getLastHash
     *
     * @return int the sequence that should be used by the 
     * user of this database for their next SASL OTP authentication.
     *
     */
    public int getSequence()
    {
        return sequence;
    }

    /**
     * Method getLastHash
     *
     * @return String the last hash value used by the 
     * user of this database for SASL OTP authentication.
     *
     */
    public String getAuthenticator()
    {
        return authenticator;
    }

    /**
     * Method updateLastHash
     * 
     * @param hash String is the new hash value to be stored
     * in the user database, for use in comparison the next time
     * they try to authenticate.
     * 
     * @throws SASLException in the event that the update causes
     * an exception to be thrown during the OTP database update.
     */
    public void updateLastHash(String hash) 
        throws SASLException
    {
        sequence--;

        lastHash = hash;
    }
}
