/*
 * SessionAdapter.java  $Revision: 1.2 $ $Date: 2003/05/20 21:44:56 $
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


/**
 * This is class provides a default nop implementation for each method
 * in <code>SessionListener</code>.
 *
 * @author Huston Franklin
 * @version $Revision: 1.2 $, $Date: 2003/05/20 21:44:56 $
 */
public class SessionAdapter implements SessionListener {
    public void greetingReceived(SessionEvent e) {}
    public void sessionClosed(SessionEvent e) {}
    public void sessionReset(SessionResetEvent e) {}
}
