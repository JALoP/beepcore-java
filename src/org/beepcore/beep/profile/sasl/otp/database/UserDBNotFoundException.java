
/*
 * UserDBNotFoundException.java            $Revision: 1.3 $ $Date: 2003/11/18 14:03:10 $
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
 * This class represents errors associated with either the
 * non-existence of a SASL OTP database for a given user,
 * or errors associated with reading or parsing of 
 * the said database.
 * 
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.3 $, $Date: 2003/11/18 14:03:10 $
 *
 */
public class UserDBNotFoundException extends SASLException {

    // Constants    
    public static final String MSG = "User OTP database not found for user ";

    /**
     * Default Constructor
     * 
     * @param username Denotes the identity of the user that had
     * been attempting to authenticate via SASL OTP.
     *
     */
    public UserDBNotFoundException(String username)
    {
        super(MSG + username);
    }
}
