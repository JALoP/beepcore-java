
/*
 * SessionEvent.java            $Revision: 1.2 $ $Date: 2001/04/16 17:05:20 $
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
 * This is a bit of smart data corresponding to various Session
 * Events..such as.
 * Channel opened, closed
 * Greeting sent, received
 * Session terminated
 * Debate - to subclass or to do stupid types here...
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/04/16 17:05:20 $
 */
public class SessionEvent {

    // Constants
    public final static int UNKNOWN_EVENT_CODE = 0;
    public final static int GREETING_SENT_EVENT_CODE = 1;
    public final static int GREETING_RECEIVED_EVENT_CODE = 2;
    public final static int CHANNEL_OPENED_EVENT_CODE = 3;
    public final static int CHANNEL_CLOSED_EVENT_CODE = 4;
    public final static int SESSION_CLOSED_EVENT_CODE = 5;
    public final static int SESSION_TERMINATED_EVENT_CODE = 6;

    // Data
    protected Object data;
    protected int event;

    // Constructor

    /**
     * Constructor SessionEvent
     *
     *
     * @param c
     * @param arg
     *
     */
    SessionEvent(int event, Object arg)
    {
        this.event = event;
        this.data = arg;
    }

    /**
     * Returns the event assigned to this object.
     *
     *
     */
    public int getEvent()
    {
        return this.event;
    }

    /**
     * Returns the data associated with this event.
     *
     *
     */
    public Object getData()
    {
        return this.data;
    }
}
