/*
 * EchoProfile.java    $Revision: 1.18 $ $Date: 2003/09/14 04:17:39 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2002,2003 Huston Franklin.  All rights reserved.
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
 * @version $Revision: 1.18 $, $Date: 2003/09/14 04:17:39 $
 */
public class EchoProfile
    implements Profile, StartChannelListener, RequestHandler
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
        channel.setRequestHandler(this);
    }

    public void closeChannel(Channel channel) throws CloseChannelException
    {
        log.debug("EchoCCL CloseChannel Callback");
        channel.setRequestHandler(null);
    }

    public boolean advertiseProfile(Session session)
    {
        return true;
    }

    public void receiveMSG(MessageMSG message)
    {
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
