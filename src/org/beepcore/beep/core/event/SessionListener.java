/*
 * SessionListener.java  $Revision: 1.2 $ $Date: 2003/05/20 17:09:31 $
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


import java.util.EventListener;

/**
 * This is an interface defining the methods that must be implemented
 * to listen for Session Events.
 *
 * @author Huston Franklin
 * @version $Revision: 1.2 $, $Date: 2003/05/20 17:09:31 $
 */
public interface SessionListener extends EventListener {

    /**
     * Invoked when the greeting has been received for a session.
     */
    public void greetingReceived(SessionEvent e);

    /**
     * Invoked when the session is closed.
     */
    public void sessionClosed(SessionEvent e);

    /**
     * Invoked when the session is reset.
     */
    public void sessionReset(SessionResetEvent e);
}
