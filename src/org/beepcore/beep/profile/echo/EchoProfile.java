/*
 * EchoProfile.java    $Revision: 1.16 $ $Date: 2003/04/23 15:23:07 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2002 Huston Franklin.  All rights reserved.
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
package org.beepcore.beep.profile.echo;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.beepcore.beep.core.*;
import org.beepcore.beep.profile.*;
import org.beepcore.beep.util.BufferSegment;


/**
 * This is the Echo profile implementation
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.16 $, $Date: 2003/04/23 15:23:07 $
 */
public class EchoProfile
    implements Profile, StartChannelListener, MessageListener
{

    public static final String ECHO_URI =
        "http://xml.resource.org/profiles/NULL/ECHO";

    private Log log = LogFactory.getLog(this.getClass());

    public StartChannelListener init(String uri, ProfileConfiguration config)
        throws BEEPException
    {
        return this;
    }

    public void startChannel(Channel channel, String encoding, String data)
            throws StartChannelException
    {
        log.debug("EchoCCL StartChannel Callback");
        channel.setMessageListener(this);
    }

    public void closeChannel(Channel channel) throws CloseChannelException
    {
        log.debug("EchoCCL CloseChannel Callback");
        channel.setMessageListener(null);
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

            while (true) {
                try {
                    BufferSegment b = ds.waitForNextSegment();
                    if (b == null) {
                        break;
                    }
                    data.add(b);
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
