/*
 * UserDatabase.java            $Revision: 1.1 $ $Date: 2001/04/02 21:38:14 $
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
package org.beepcore.beep.profile.sasl.otp.database;

import org.beepcore.beep.profile.sasl.SASLException;
import org.beepcore.beep.profile.sasl.otp.SASLOTPProfile;
import org.beepcore.beep.profile.sasl.otp.algorithm.md5.MD5;
import org.beepcore.beep.profile.sasl.otp.algorithm.sha1.SHA1;


/**
 * This interface represents all the information associated
 * with a given user for SASL OTP, including password hashes, seeds,
 * and the sequence number.  It is designed to be implemented by 
 * classes that provide real-world storage for this information.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.1 $, $Date: 2001/04/02 21:38:14 $
 *
 */
public interface UserDatabase 
{
    /**
     * Method getAlgorithmName returns the algorithm associated with this
     * particular user and OTP database.  
     *
     * @returns String the algorithm employed by the user of this database
     * for SASL OTP authentication.
     *
     */
    public String getAlgorithmName();

    /**
     * Method getLastHash
     *
     * @returns byte[] the lastHash value generated the last time the
     * user of this database performed SASL OTP authentication.  The
     * hash value is represented in binary form.
     *
     */
    public byte[] getLastHash() throws SASLException;
    
    /**
     * Method getLastHashAsString
     *
     * @returns String the lastHash value generated the last time the
     * user of this database performed SASL OTP authentication.  The
     * hash is represented in hexadecimal form.
     *
     */
    public String getLastHashAsString();

    /**
     * Method getSeed
     *
     * @returns String the seed used by the 
     * user of this database for SASL OTP authentication.
     *
     */
    public String getSeed();
    
    /**
     * Method getSequence
     *
     * @returns int the sequence to be used by the 
     * user of this database for SASL OTP authentication.
     *
     */
    public int getSequence();

    /**
     * Method getAuthenticator
     *
     * @returns String the user of this database.
     *
     */
    public String getAuthenticator();

    /**
     * Method updateLastHash
     * 
     * @param hash String is the new hash value to be stored
     * in the user database.
     * 
     */
    public void updateLastHash(String hash) throws SASLException;
}