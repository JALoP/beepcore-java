/*
 * SessionEvent.java  $Revision: 1.1 $ $Date: 2001/11/22 15:25:29 $
 *
 * Copyright (c) 2001 Huston Franklin.  All rights reserved.
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
package org.beepcore.beep.core.event;


import org.beepcore.beep.core.Session;

/**
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2001/11/22 15:25:29 $
 */
public class SessionEvent extends java.util.EventObject {

    public final static int GREETING_RECEIVED = 1;
    public final static int SESSION_ABORTED = 2;
    public final static int SESSION_CLOSED = 3;

    public SessionEvent(Session session) {
        super(session);
    }
}
