/*
 * UserDatabase.java            $Revision: 1.4 $ $Date: 2003/04/23 15:23:05 $
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
 * @version $Revision: 1.4 $, $Date: 2003/04/23 15:23:05 $
 *
 */
public interface UserDatabase 
{
    /**
     * Method getAlgorithmName returns the algorithm associated with this
     * particular user and OTP database.  
     *
     * @return String the algorithm employed by the user of this database
     * for SASL OTP authentication.
     *
     */
    public String getAlgorithmName();

    /**
     * Method getLastHash
     *
     * @return byte[] the lastHash value generated the last time the
     * user of this database performed SASL OTP authentication.  The
     * hash value is represented in binary form.
     *
     */
    public byte[] getLastHash() throws SASLException;
    
    /**
     * Method getLastHashAsString
     *
     * @return String the lastHash value generated the last time the
     * user of this database performed SASL OTP authentication.  The
     * hash is represented in hexadecimal form.
     *
     */
    public String getLastHashAsString();

    /**
     * Method getSeed
     *
     * @return String the seed used by the 
     * user of this database for SASL OTP authentication.
     *
     */
    public String getSeed();
    
    /**
     * Method getSequence
     *
     * @return int the sequence to be used by the 
     * user of this database for SASL OTP authentication.
     *
     */
    public int getSequence();

    /**
     * Method getAuthenticator
     *
     * @return String the user of this database.
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
