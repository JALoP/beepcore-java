/*
 * Message.java            $Revision: 1.14 $ $Date: 2004/01/01 16:40:38 $
 *
 * Copyright (c) 2003 Huston Franklin.  All rights reserved.
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
 * This interface represents the operations available for all types of
 * messages.
 *
 * @author Huston Franklin
 * @version $Revision: 1.14 $, $Date: 2004/01/01 16:40:38 $
 */
public interface Message {

    /**
     * Uninitialized <code>Message</code>.
     */
    public static final int MESSAGE_TYPE_UNK = 0;

    /**
     * BEEP MSG message.
     */
    public static final int MESSAGE_TYPE_MSG = 1;

    /**
     * BEEP RPY message.
     */
    public static final int MESSAGE_TYPE_RPY = 2;

    /**
     * BEEP ERR message.
     */
    public static final int MESSAGE_TYPE_ERR = 3;

    /**
     * BEEP ANS message.
     */
    public static final int MESSAGE_TYPE_ANS = 4;

    /**
     * BEEP NUL message.
     */
    public static final int MESSAGE_TYPE_NUL = 5;

	public static final int PIGGYBACKED_MSGNO = -1;
    /**
     * Returns <code>InputDataStream</code> containing the payload for this
     * <code>Message</code>.
     *
     * @see InputDataStream
     */
    public InputDataStream getDataStream();

    /**
     * Returns the <code>Channel</code> on which this <code>Message</code>
     * was received.
     *
     * @see Channel
     */
    public Channel getChannel();

    /**
     * Returns the message number of this <code>Message</code>.
     */
    public int getMsgno();

    /**
     * Returns the answer number of this <code>Message</code>.
     */
    public int getAnsno();

    /**
     * Returns the message type of this <code>Message</code>.
     */
    public int getMessageType();
}
