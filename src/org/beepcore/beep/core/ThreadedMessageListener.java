/*
 * ThreadedMessageListener.java $Revision: 1.2 $ $Date: 2002/10/05 15:30:11 $
 *
 * Copyright (c) 2002 Huston Franklin.  All rights reserved.
 *
 */
package org.beepcore.beep.core;

import edu.oswego.cs.dl.util.concurrent.PooledExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


class ThreadedMessageListener implements MessageListener, Runnable {
    ThreadedMessageListener(Channel channel, MessageListener listener) {
        this.channel = channel;
        this.listener = listener;
    }

    public void receiveMSG(Message message)
        throws BEEPError, AbortChannelException
    {
        this.message = (MessageMSG)message;
        try {
            callbackQueue.execute(this);
        } catch (InterruptedException e) {
            throw new BEEPError(BEEPError.CODE_REQUESTED_ACTION_ABORTED);
        }
    }

    public void run() {
        try {
            listener.receiveMSG(message);
        } catch (BEEPError e) {
            try {
                message.sendERR(e);
            } catch (BEEPException e2) {
                log.error("Error sending ERR", e2);
            }
        } catch (AbortChannelException e) {
            try {
                channel.close();
            } catch (BEEPException e2) {
                log.error("Error closing channel", e2);
            }
        }
    }

    public MessageListener getMessageListener()
    {
        return listener;
    }

    private Channel channel;
    private MessageListener listener;
    private MessageMSG message;
        
    private static final PooledExecutor callbackQueue =
        new PooledExecutor();

    private Log log = LogFactory.getLog(this.getClass());
}

