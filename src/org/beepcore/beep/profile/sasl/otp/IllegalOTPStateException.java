
/*
 * IllegalOTPStateException.java            $Revision: 1.2 $ $Date: 2001/11/08 05:51:34 $
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


import org.beepcore.beep.profile.sasl.SASLException;

/**
 * This object is used to represent exceptions and errors relative
 * to the order in which OTP authentication is supposed to take
 * place, such as messages being sent out of order, that sort of thing.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/11/08 05:51:34 $
 *
 */
public class IllegalOTPStateException extends SASLException {

    public static final String MSG = "Out of Sequence SASL-OTP Message";

    public IllegalOTPStateException()
    {
        super(MSG);
    }
}
