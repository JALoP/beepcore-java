/*
 * Channel.java  $Revision: 1.34 $ $Date: 2003/11/07 17:39:21 $
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
 * @version $Revision: 1.34 $, $Date: 2003/11/07 17:39:21 $
 *
 */
public interface Channel {

    public static final int STATE_INITIALIZED = 0;
    public static final int STATE_STARTING = 1;
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_TUNING_PENDING = 3;
    public static final int STATE_TUNING = 4;
    public static final int STATE_CLOSE_PENDING = 5;
    public static final int STATE_CLOSING = 6;
    public static final int STATE_CLOSED = 7;
    public static final int STATE_ABORTED = 8;

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
     * @deprecated
     */
    public MessageListener setMessageListener(MessageListener listener);

    /**
     * Returns the message listener for this channel.
     * @deprecated
     */
    public MessageListener getMessageListener();

    /**
     * Returns the <code>RequestHandler</code> registered with this channel.
     */
    public RequestHandler getRequestHandler();
    
    /**
     * Sets the MSG handler for this <code>Channel</code>.
     * 
     * @param handler <code>RequestHandler</code> to handle received
     *                MSG messages.
     * @return The previous <code>RequestHandler</code> or <code>null</code> if
     *         one wasn't set.
     */
    public RequestHandler setRequestHandler(RequestHandler handler);
    
    /**
     * Sets the MSG handler for this <code>Channel</code>.
     * 
     * @param handler <code>RequestHandler</code> to handle received
     *                MSG messages.
     * @param tuningReset flag indicating that the profile will request a
     *                    tuning reset.
     * 
     * @return The previous <code>RequestHandler</code> or <code>null</code> if
     *         one wasn't set.
     */
    public RequestHandler setRequestHandler(RequestHandler handler,
                                            boolean tuningReset);

    /**
     * Returns the session for this channel.
     *
     */
    public Session getSession();

    /**
     * Returns the state of this channel.
     */
    public int getState();

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

    /**
     * @deprecated
     */
    public void setStartData(String data);

    /**
     * @deprecated
     */
    public String getStartData();

    public String getProfile();
}
