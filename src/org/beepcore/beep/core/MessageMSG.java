/*
 * MessageMSG.java  $Revision: 1.3 $ $Date: 2001/05/10 04:43:52 $
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
 * @version $Revision: 1.3 $, $Date: 2001/05/10 04:43:52 $
 *
 */
class MessageMSG extends Message
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
        Message m;

        synchronized (this) {
            // reusing ansno (initialized to -1) from Message since
            // this is a MSG
            ++ansno;

            m = new Message(this.channel, this.msgno, this.ansno, stream);
        }

        return this.channel.sendMessage(m);
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
        DataStream stream = new StringDataStream(error.createErrorMessage());
        Message m =
            new Message(this.channel, this.msgno, stream, MESSAGE_TYPE_ERR);
        return this.channel.sendMessage(m);
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
        String error = BEEPError.createErrorMessage(code, diagnostic);
        Message m = new Message(this.channel, this.msgno,
                                new StringDataStream(error), MESSAGE_TYPE_ERR);
        return this.channel.sendMessage(m);
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
        String error = BEEPError.createErrorMessage(code, diagnostic, xmlLang);
        Message m = new Message(this.channel, this.msgno,
                                new StringDataStream(error), MESSAGE_TYPE_ERR);
        return this.channel.sendMessage(m);
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
        Message m =
            new Message(this.channel, this.msgno, null, MESSAGE_TYPE_NUL);
        return this.channel.sendMessage(m);
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
        Message m =
            new Message(this.channel, this.msgno, stream, MESSAGE_TYPE_RPY);
        return this.channel.sendMessage(m);
    }
}
