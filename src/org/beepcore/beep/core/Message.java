/*
 * Message.java            $Revision: 1.12 $ $Date: 2003/06/03 16:38:35 $
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
 * @version $Revision: 1.12 $, $Date: 2003/06/03 16:38:35 $
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

    /**
     * @deprecated use method on MessageMSG instead.
     */
    public MessageStatus sendANS(OutputDataStream stream) throws BEEPException;

    /**
     * @deprecated use method on MessageMSG instead.
     */
    public MessageStatus sendERR(BEEPError error) throws BEEPException;

    /**
     * @deprecated use method on MessageMSG instead.
     */
    public MessageStatus sendERR(int code, String diagnostic)
        throws BEEPException;

    /**
     * @deprecated use method on MessageMSG instead.
     */
    public MessageStatus sendERR(int code, String diagnostic, String xmlLang)
        throws BEEPException;

    /**
     * @deprecated use method on MessageMSG instead.
     */
    public MessageStatus sendNUL() throws BEEPException;

    /**
     * @deprecated use method on MessageMSG instead.
     */
    public MessageStatus sendRPY(OutputDataStream stream) throws BEEPException;
}
