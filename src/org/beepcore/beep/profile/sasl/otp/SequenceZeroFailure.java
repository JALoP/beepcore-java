
/*
 * SequenceZeroFailure.java            $Revision: 1.2 $ $Date: 2001/11/08 05:51:34 $
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
package org.beepcore.beep.profile.sasl.otp;


import org.beepcore.beep.profile.sasl.AuthenticationFailureException;

/**
 * This exception is used when a user attempts to authenticate via
 * SASL OTP and it is determined that the user has no more valid
 * hashes left in their OTP database.  It's distinct from the error
 * used when an OTP database isn't found in that it allows the receiver
 * to programmatically prompt the user to do an init-hex or init-word
 * command to re-establish an OTP database.
 * 
 * @todo make sure the OTP db gets wiped clean off the disk after this
 * gets thrown once - or something like that.  Think about that anyway.
 * 
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/11/08 05:51:34 $
 *
 */
public class SequenceZeroFailure extends AuthenticationFailureException {

    // Constants
    private static final String MSG =
        "Authentication unable to proceed because the user's SASL OTP Sequence is 0.";

    public SequenceZeroFailure()
    {
        super(MSG);
    }
}
