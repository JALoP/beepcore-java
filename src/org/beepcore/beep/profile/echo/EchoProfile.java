/*
 * EchoProfile.java    $Revision: 1.4 $ $Date: 2001/04/26 16:31:25 $
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
package org.beepcore.beep.profile.echo;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.beepcore.beep.core.*;
import org.beepcore.beep.lib.MessageQueue;
import org.beepcore.beep.profile.*;
import org.beepcore.beep.util.*;


/**
 * This is the Echo profile implementation
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.4 $, $Date: 2001/04/26 16:31:25 $
 */
public class EchoProfile
    implements Profile, StartChannelListener, FrameListener
{

    public static final String ECHO_URI =
        "http://xml.resource.org/profiles/NULL/ECHO";

    private ProfileConfiguration config;
    private MessageQueue messages = new MessageQueue();

    public void init(ProfileConfiguration config) throws BEEPException
    {
        this.config = config;
    }

    public ProfileConfiguration getConfiguration()
    {
        return this.config;
    }


    public StartChannelListener getStartChannelListener()
    {
        return this;
    }

    public String getURI()
    {
        return ECHO_URI;
    }

    /**
     * Method startChannel
     *
     *
     * @param channel
     * @param encoding
     * @param data
     *
     * @throws StartChannelException
     *
     */
    public void startChannel(Channel channel, String encoding, String data)
            throws StartChannelException
    {
        Log.logEntry(Log.SEV_DEBUG, "EchoCCL StartChannel Callback");
        channel.setDataListener(this);
    }

    /**
     * Method closeChannel
     *
     *
     * @param channel
     *
     * @throws CloseChannelException
     *
     */
    public void closeChannel(Channel channel) throws CloseChannelException
    {
        Log.logEntry(Log.SEV_DEBUG, "EchoCCL CloseChannel Callback");
        channel.setDataListener(null);
        channel.setAppData(null);
    }

    public void receiveFrame(Frame frame) throws BEEPException
    {
        FrameDataStream ds =
            (FrameDataStream) frame.getChannel().getAppData();

        // The MessageType is guaranteed to be MSG since this
        // peer won't be sending MSGs on a channel with this profile.

        if (ds == null) {
            ds = new FrameDataStream();
            frame.getChannel().setAppData(ds);
        }

        ds.add(frame);

        if (frame.isLast() == false) {
            return;
        }

        frame.getChannel().setAppData(null);

        try {
            InputStream is = ds.getInputStream();

            byte buf[] = new byte[is.available()];

            is.read(buf, 0, buf.length);

            new ReplyThread(frame.getChannel(), new ByteDataStream(buf)).start();
        } catch (IOException e) {
            Log.logEntry(Log.SEV_ERROR, e);
        }
    }

    private class ReplyThread extends Thread {
        private Channel channel;
        private DataStream reply;

        ReplyThread(Channel channel, DataStream reply) {
            this.channel = channel;
            this.reply = reply;
        }

        public void run() {
            try {
                channel.sendRPY(reply);
            } catch (BEEPException e) {
                Log.logEntry(Log.SEV_ERROR, e);
            }
        }
    }
}
