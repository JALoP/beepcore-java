/*
 * ReplyListener.java            $Revision: 1.3 $ $Date: 2002/09/07 14:58:33 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2002 Huston Franklin.  All rights reserved.
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
 * Provided to allow for the registration of a <code>ReplyListener</code>
 * per request.
 * <p>
 * A <code>ReplyListener</code> receives replies (RPY, ERR, ANS, NUL)
 * corresponding to an MSG sent with <code>sendMSG</code>.
 *
 * @see org.beepcore.beep.core.Channel#sendMSG(OutputDataStream, ReplyListener)
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision, $Date: 2002/09/07 14:58:33 $
 */
public interface ReplyListener {

    /**
     * Called when the underlying BEEP framework receives a reply of
     * type RPY.
     *
     * @param message BEEP message
     *
     * @see org.beepcore.beep.core.Message
     *
     * @throws AbortChannelException
     */
    public void receiveRPY(Message message) throws AbortChannelException;

    /**
     * Called when the underlying BEEP framework receives a reply of
     * type ERR.
     *
     * @param message BEEP message
     *
     * @see org.beepcore.beep.core.Message
     *
     * @throws AbortChannelException
     */
    public void receiveERR(Message message) throws AbortChannelException;

    /**
     * Called when the underlying BEEP framework receives a reply of
     * type ANS.
     *
     * @param message BEEP message
     *
     * @see org.beepcore.beep.core.Message
     *
     * @throws AbortChannelException
     */
    public void receiveANS(Message message) throws AbortChannelException;

    /**
     * Called when the underlying BEEP framework receives a reply of
     * type NUL.
     *
     * @param message BEEP message
     *
     * @see org.beepcore.beep.core.Message
     *
     * @throws AbortChannelException
     */
    public void receiveNUL(Message message) throws AbortChannelException;

    // @todo do any of the above need to throw BEEPException???
}
