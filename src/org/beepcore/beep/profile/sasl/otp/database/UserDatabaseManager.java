/*
 * UserDatabasePool.java            $Revision: 1.5 $ $Date: 2003/11/18 14:03:10 $
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
 * This class provides several routines through which one
 * can retrieve implementations of SASL OTP User Databases.
 * Implementors of any other UserDatabase implementations 
 * will want to expose them through this, or provide similar
 * functionality elsewhere.
 * 
 */
public interface UserDatabaseManager
{
    // Constants
    public static final String OTP_ALGO = "OTP Algorithm";
    public static final String OTP_AUTHENTICATOR = "OTP Authenticator";
    public static final String OTP_HEADER = "OTP Properties Header";
    public static final String OTP_LAST_HASH = "OTP Last Hash";
    public static final String OTP_MECH = "OTP Mechanism";
    public static final String OTP_SEED = "OTP Seed";
    public static final String OTP_SEQUENCE = "OTP Sequence";
    public static final String OTP_SUFFIX = ".otp";
    public static final String ERR_DB_PARSE = "Unable to parse OTP DB";

    /**
     * Method getUser This method is provided as a means
     * for users of the OTP databases to retrieve the information
     * contained in them, in the form of an instance of
     * UserDatabase.  Please note that ALGORITHM should in time be
     * added - to be part of how one looks up an OTP database (using
     * both the username and the algorithm).  The init-word and init-hex
     * commands, in their nature, don't really allow for it, so this'll
     * do for now, but in time it should be that way.  It certainly
     * wouldn't be a difficult thing to do.  This would also entail
     * evolving the way init-hex/word are processed, as well...which
     * is slightly trickier than doing a dual parameter lookup.
     * 
     * @param username Indicates which OTP database should 
     * be retrieved, based on who wishes to authenticate using it.
     *
     * @return UserDatabase the OTP database for the user specified.
     * 
     * @throws SASLException is thrown if the parameter is null or 
     * some error is encountered during the reading or processing
     * of the user's OTP database file.
     *
     */
    public UserDatabase getUser(String username)
        throws SASLException;

    /**
     * Method addUser
     *
     * @param username The identity of the user for whom this OTP
     *                 database is used.
     *
     */
    public void addUser(String username, String algorithm,
                        String hash, String seed, String sequence)
        throws SASLException;

    /**
     * Method updateUserDB causes the long-term representation
     * (e.g. file) of the user's OTP database to be updated
     * after a successful authentication.  This entails a
     * decrementation of the sequence, and a storage of a new
     * 'last hash' value.
     *
     *
     * @param ud The updated form of the OTP database.
     *
     * @throws SASLException if any issues are encountered during the
     * storage of the user's OTP DB.
     *
     */
    public void updateUserDB(UserDatabase ud)
            throws SASLException;

    /**
     * Method purgeUserDatabase is a routine designed to allow
     * for the removal of a user db.
     * 
     * @param username The username associated with a given db.
     * 
     * @throws SASLException if any errors are encountered in the
     * removal of the data (such as it not being there in the first place
     * or encountering some rights issue, it can't be removed right now
     * cuz it's being used etc.)
     */
    public void removeUserDB(String username)
        throws SASLException;
}
