

/*
 * AuthenticationFailureException.java            $Revision: 1.2 $ $Date: 2001/11/08 05:51:34 $
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
package org.beepcore.beep.profile.sasl;


/**
 * This is the general superclass for all exceptions pertaining
 * to the failure of SASL Authentication.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/11/08 05:51:34 $
 *
 */
public class AuthenticationFailureException extends SASLException {

    public AuthenticationFailureException(String message)
    {
        super(message);
    }
}
