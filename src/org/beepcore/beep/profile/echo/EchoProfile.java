/*
 * EchoProfile.java    $Revision: 1.10 $ $Date: 2001/11/08 04:00:11 $
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
import org.beepcore.beep.profile.*;
import org.beepcore.beep.util.*;


/**
 * This is the Echo profile implementation
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.10 $, $Date: 2001/11/08 04:00:11 $
 */
public class EchoProfile
    implements Profile, StartChannelListener, MessageListener
{

    public static final String ECHO_URI =
        "http://xml.resource.org/profiles/NULL/ECHO";

    public StartChannelListener init(String uri, ProfileConfiguration config)
        throws BEEPException
    {
        return this;
    }

    public void startChannel(Channel channel, String encoding, String data)
            throws StartChannelException
    {
        Log.logEntry(Log.SEV_DEBUG, "EchoCCL StartChannel Callback");
        channel.setMessageListener(this);
    }

    public void closeChannel(Channel channel) throws CloseChannelException
    {
        Log.logEntry(Log.SEV_DEBUG, "EchoCCL CloseChannel Callback");
        channel.setMessageListener(null);
        channel.setAppData(null);
    }

    public boolean advertiseProfile(Session session)
    {
        return true;
    }

    public void receiveMSG(Message message) throws BEEPError
    {
        new ReplyThread(message).start();
    }

    private class ReplyThread extends Thread {
        private Message message;

        ReplyThread(Message message) {
            this.message = message;
        }

        public void run() {
            OutputDataStream data = new OutputDataStream();
            InputDataStream ds = message.getDataStream();

            while (ds.isComplete() == false || ds.availableSegment()) {
                try {
                    data.add(ds.waitForNextSegment());
                } catch (InterruptedException e) {
                    message.getChannel().getSession().terminate(e.getMessage());
                    return;
                }
            }

            data.setComplete();

            try {
                message.sendRPY(data);
            } catch (BEEPException e) {
                try {
                    message.sendERR(BEEPError.CODE_REQUESTED_ACTION_ABORTED,
                                    "Error sending RPY");
                } catch (BEEPException x) {
                    message.getChannel().getSession().terminate(x.getMessage());
                }
                return;
            }
        }
    }
}
