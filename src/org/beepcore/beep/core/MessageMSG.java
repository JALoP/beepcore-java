/*
 * MessageMSG.java  $Revision: 1.10 $ $Date: 2003/06/03 16:38:35 $
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
 * This interface represents the operations available for messages of type MSG.
 *
 * @author Huston Franklin
 * @version $Revision: 1.10 $, $Date: 2003/06/03 16:38:35 $
 *
 */
public interface MessageMSG extends Message
{
    /**
     * Sends an ANS reply to this MSG message.
     *
     * @param stream Payload to be sent.
     *
     * @see OutputDataStream
     * @see MessageStatus
     * @see #sendNUL
     */
    public MessageStatus sendANS(OutputDataStream stream) throws BEEPException;

    /**
     * Sends an ERR reply to this MSG message.
     *
     * @param error Error to send in the form of <code>BEEPError</code>.
     *
     * @see BEEPError
     * @see MessageStatus
     */
    public MessageStatus sendERR(BEEPError error) throws BEEPException;

    /**
     * Sends an ERR reply to this MSG message.
     *
     * @param code <code>code</code> attibute in <code>error</code> element.
     * @param diagnostic Message for <code>error</code> element.
     *
     * @see MessageStatus
     */
    public MessageStatus sendERR(int code, String diagnostic)
        throws BEEPException;

    /**
     * Sends an ERR reply to this MSG message.
     *
     * @param code <code>code</code> attibute in <code>error</code> element.
     * @param diagnostic Message for <code>error</code> element.
     * @param xmlLang <code>xml:lang</code> attibute in <code>error</code>
     *                element.
     *
     * @see MessageStatus
     *
     * @return MessageStatus
     */
    public MessageStatus sendERR(int code, String diagnostic, String xmlLang)
        throws BEEPException;

    /**
     * Sends a reply of type NUL to this MSG message. This is sent as the
     * completion to a MSG/ANS/NUL message exchange.
     *
     * @see MessageStatus
     * @see #sendANS
     *
     * @return MessageStatus
     */
    public MessageStatus sendNUL() throws BEEPException;

    /**
     * Sends a RPY reply to this MSG message.
     *
     * @param stream Payload to be sent.
     *
     * @see OutputDataStream
     * @see MessageStatus
     *
     * @return MessageStatus
     */
    public MessageStatus sendRPY(OutputDataStream stream) throws BEEPException;
}
