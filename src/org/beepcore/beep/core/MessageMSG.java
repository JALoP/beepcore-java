/*
 * MessageMSG.java  $Revision: 1.1 $ $Date: 2001/05/07 19:21:57 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 *
 * The contents of this file are subject to the Blocks Public License (the
 * "License"); You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.invisible.net/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 */
package org.beepcore.beep.core;

/**
 * Represents received BEEP MSG messages. Provides methods to reply to
 * the MSG.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.1 $, $Date: 2001/05/07 19:21:57 $
 *
 */
public class MessageMSG extends Message
{
    MessageMSG(Channel channel, int msgno, DataStream data) {
        super(channel, msgno, data, MESSAGE_TYPE_MSG);
    }

    /**
     * Sends a message of type ANS.
     *
     * @param stream Data to send in the form of <code>DataStream</code>.
     *
     * @see DataStream
     * @see MessageStatus
     * @see #sendNUL
     *
     * @return MessageStatus
     *
     * @throws BEEPException if an error is encoutered.
     */
    public MessageStatus sendANS(DataStream stream) throws BEEPException
    {
        return this.getChannel().sendANS(stream);
    }

    /**
     * Sends a message of type ERR.
     *
     * @param error Error to send in the form of <code>BEEPError</code>.
     *
     * @see BEEPError
     * @see MessageStatus
     *
     * @return MessageStatus
     *
     * @throws BEEPException if an error is encoutered.
     */
    public MessageStatus sendERR(BEEPError error) throws BEEPException
    {
        return this.getChannel().sendERR(new StringDataStream(error.createErrorMessage()));
    }

    /**
     * Sends a message of type ERR.
     *
     * @param code <code>code</code> attibute in <code>error</code> element.
     * @param diagnostic Message for <code>error</code> element.
     *
     * @see MessageStatus
     *
     * @return MessageStatus
     *
     * @throws BEEPException if an error is encoutered.
     */
    public MessageStatus sendERR(int code, String diagnostic)
        throws BEEPException
    {
        return this.getChannel().sendERR(new StringDataStream(BEEPError.createErrorMessage(code, diagnostic)));
    }

    /**
     * Sends a message of type ERR.
     *
     * @param code <code>code</code> attibute in <code>error</code> element.
     * @param diagnostic Message for <code>error</code> element.
     * @param xmlLang <code>xml:lang</code> attibute in <code>error</code>
     *                element.
     *
     * @see MessageStatus
     *
     * @return MessageStatus
     *
     * @throws BEEPException if an error is encoutered.
     */
    public MessageStatus sendERR(int code, String diagnostic, String xmlLang)
        throws BEEPException
    {
        return this.getChannel().sendERR(new StringDataStream(BEEPError.createErrorMessage(code, diagnostic, xmlLang)));
    }

    /**
     * Sends a message of type NUL.
     *
     * @see MessageStatus
     * @see #sendANS
     *
     * @return MessageStatus
     *
     * @throws BEEPException if an error is encoutered.
     */
    public MessageStatus sendNUL() throws BEEPException
    {
        return this.getChannel().sendNUL();
    }

    /**
     * Sends a message of type RPY.
     *
     * @param stream Data to send in the form of <code>DataStream</code>.
     *
     * @see DataStream
     * @see MessageStatus
     *
     * @return MessageStatus
     *
     * @throws BEEPException if an error is encoutered.
     */
    public MessageStatus sendRPY(DataStream stream) throws BEEPException
    {
        return this.getChannel().sendRPY(stream);
    }

    private int ansno = -1;         // counter of the answer
}
