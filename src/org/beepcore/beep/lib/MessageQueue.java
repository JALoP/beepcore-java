/*
 * MessageQueue.java            $Revision: 1.5 $ $Date: 2002/10/05 15:31:19 $
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
package org.beepcore.beep.lib;


import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.beepcore.beep.core.Message;
import org.beepcore.beep.core.MessageListener;


/**
 * Is a convience class that is registered with a <code>Channel</code> as a
 * <code>MessageListener</code>. This receives messages and places them on a
 * queue to be retrieved by calling <code>getNextMessage</code>. The same
 * instance of <code>MessageQueue</code> can be registered with more than one
 * <code>Channel</code> providing an easy mechanism to service the requests on
 * several <code>Channel</code>s with the same thread(s).
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.5 $, $Date: 2002/10/05 15:31:19 $
 */
public class MessageQueue implements MessageListener {

    private Log log = LogFactory.getLog(this.getClass());

    private LinkedList queue = new LinkedList();

    /**
     * Gets the next message on the queue blocking if none are available.
     */
    public Message getNextMessage() throws InterruptedException
    {
        log.trace("getNextMessage: entry");
        synchronized (this) {
            if (queue.size() == 0) {
                this.wait();
            }
            return (Message) queue.removeFirst();
        }
    }

    public void receiveMSG(Message message)
    {
        log.trace("receiveMSG: entry");
        synchronized (this) {
            queue.addLast(message);
            this.notify();
        }
    }
}
