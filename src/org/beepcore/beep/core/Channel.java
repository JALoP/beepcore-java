/*
 * Channel.java  $Revision: 1.32 $ $Date: 2003/04/21 15:09:10 $
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
 * This interface represents the operations available for all BEEP Channels.
 *
 * @author Huston Franklin
 * @version $Revision: 1.32 $, $Date: 2003/04/21 15:09:10 $
 *
 */
public interface Channel {

    /**
     * Closes the channel.
     *
     * @throws BEEPException
     */
    public void close() throws BEEPException;

    /**
     * Returns application context data previously set using
     * <code>setAppData()</code>.
     *
     * @see #setAppData
     */
    public Object getAppData();

    /**
     * Set the application context data.
     *
     * @see #getAppData
     */
    public void setAppData(Object applicationData);

    /**
     * Return the number of this <code>Channel</code>.
     *
     */
    public int getNumber();

    /**
     * Sets the <code>MessageListener</code> for this channel.
     *
     * @param listener
     * @return The previous MessageListener or null if none was set.
     */
    public MessageListener setMessageListener(MessageListener listener);

    /**
     * Returns the message listener for this channel.
     */
    public MessageListener getMessageListener();

    /**
     * Returns the session for this channel.
     *
     */
    public Session getSession();

    /**
     * Sends a MSG message.
     *
     * @param stream Data contents of the MSG message to be sent.
     * @param replyListener A listener to be notified when a reply to this
     *                       MSG is received.
     *
     * @see OutputDataStream
     * @see MessageStatus
     *
     * @return MessageStatus
     *
     * @throws BEEPException if an error is encoutered.
     */
    public MessageStatus sendMSG(OutputDataStream stream,
                                 ReplyListener replyListener)
            throws BEEPException;

    public void setStartData(String data);

    public String getStartData();

    public String getProfile();
}
