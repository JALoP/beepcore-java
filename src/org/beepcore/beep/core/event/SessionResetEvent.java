/*
 * SessionResetEvent.java  $Revision: 1.1 $ $Date: 2003/05/20 16:06:16 $
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
package org.beepcore.beep.core.event;


import org.beepcore.beep.core.Session;

/**
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2003/05/20 16:06:16 $
 */
public class SessionResetEvent extends SessionEvent {

    public final static int SESSION_RESET = 4;

    public SessionResetEvent(Session session, Session newSession) {
        super(session);
        this.newSession = newSession;
    }

    public Session getNewSession() {
        return this.newSession;
    }

    private Session newSession;
}
