/*
 * AbortChannelException.java   $Revision: 1.2 $ $Date: 2001/11/08 05:51:34 $
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
package org.beepcore.beep.core;

/**
 * This exception is thrown by listeners to cause the session to terminate.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/11/08 05:51:34 $
 *
 */
public class AbortChannelException extends BEEPException
{
    private static final String MESSAGE =
        "Peer detected terminate condition\n";
    public AbortChannelException(String message)
    {
        super(MESSAGE + message);
    }
}
