/*
 * SharedChannel.java            $Revision: 1.6 $ $Date: 2002/08/22 05:28:41 $
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
package org.beepcore.beep.lib;


import org.beepcore.beep.core.BEEPError;
import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.Channel;
import org.beepcore.beep.core.Message;
import org.beepcore.beep.core.MessageListener;
import org.beepcore.beep.core.MessageStatus;
import org.beepcore.beep.core.OutputDataStream;
import org.beepcore.beep.core.ReplyListener;
import org.beepcore.beep.core.Session;

import java.util.LinkedList;
import java.util.Date;


/**
 * <code>SharedChannel</code> references a <code>Channel</code>.  A
 * <code>ChannelPool</code> holds a collection of <code>SharedChannel</code>s.
 * Call <code>ChannelPoll</code>'s <code>getSharedChannel</code> to create
 * a <code>SharedChannel</code>.
 *
 * @see ChannelPool
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.6 $, $Date: 2002/08/22 05:28:41 $
 */
public class SharedChannel extends Channel {

    private Channel channel = null;    // Channel this SharedChannel references
    private ChannelPool pool =
        null;                       // the ChannelPool this SharedChannel belongs to
    private long timeStamp = -1;    // time SharedChannel became available

    /**
     * Creates a <code>SharedChannel</code> with given <code>channel</code>
     * and <code>pool</code>
     *
     * @param channel The channel this object references.
     * @param pool  The <code>ChannelPool</code> this object belongs to.
     * @see ChannelPool
     */
    SharedChannel(Channel channel, ChannelPool pool)
    {
        super(channel.getProfile(), String.valueOf(channel.getNumber()),
              channel.getMessageListener(), true, channel.getSession());

        this.channel = channel;
        this.pool = pool;
    }

    /**
     * Marks this <code>SharedChannel</code> as available for reuse.
     */
    public void release()
    {
        Date now = new Date();

        this.timeStamp = now.getTime();

        this.pool.releaseSharedChannel(this);
    }

    long getTTL()
    {
        return this.timeStamp;
    }

    /**
     * Send a message of type MSG. Sends <code>stream</code> as message's
     * payload.
     * Note: If the stream is not complete, then the send will block
     * until it is finished (a <code>read</code> returns a -1).
     *
     * @param stream <code>DataStream</code> that is read to send data.
     * @param replyListener
     * @return MessageStutas Can be queried to get status information about the
     * message.
     *
     * @see DataStream
     * @see MessageStatus
     *
     * @throws BEEPException
     * @deprecated
     */
    public MessageStatus sendMSG(OutputDataStream stream,
                                 ReplyListener replyListener)
            throws BEEPException
    {
        return channel.sendMSG(stream, replyListener);
    }

    /**
     * Sets the <code>DataListener</code> for this <code>SharedChannel</code>.
     *
     *
     * @param ml A listener of type <code>DataListener</code>
     *
     */
    public MessageListener setMessageListener(MessageListener dl)
    {
        return channel.setMessageListener(dl);
    }

    public void setReceiveBufferSize( int size ) throws BEEPException
    {
        channel.setReceiveBufferSize( size );
    }

    public int getBufferSize()
    {
        return channel.getBufferSize();
    }

    public int getBufferUsed()
    {
        return channel.getBufferUsed();
    }

    public MessageListener getMessageListener()
    {
        return channel.getMessageListener();
    }

    public int getNumber()
    {
        return channel.getNumber();
    }

    public Session getSession()
    {
        return channel.getSession();
    }

    /**
     * Returns the profile used to create this <code>SharedChannel</code>.
     *
     */
    public String getProfile()
    {
        return channel.getProfile();
    }

    /**
     * Closes this <code>SharedChannel</code>.
     *
     * @throws BEEPException
     */
    public void close() throws BEEPException
    {
        channel.close();
    }

    /**
     * Sends a 'synchronous' request on this <code>SharedChannel</code>.
     *
     * @param ds <code>DataStream</code> to send as this request's payload.
     *
     * @return Reply Caller may block using this object to retrieve the reply
     * to this request.
     *
     * @throws BEEPException
     *
     */
    public Reply sendRequest(OutputDataStream ds) throws BEEPException
    {
        Reply r = new Reply();
        channel.sendMSG(ds, r);
        return r;
    }
}
