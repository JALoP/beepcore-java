
/*
 * TLSProfileHandshakeCompletedListener.java  $Revision: 1.4 $ $Date: 2003/04/23 15:23:06 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
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
package org.beepcore.beep.profile.tls;


import org.beepcore.beep.core.*;

import javax.net.ssl.HandshakeCompletedEvent;


/**
 * receives handshake completed events.  This allows the application to
 * examine the trust of the authentication and take appropriate action as
 * necessary, such as alter the profiles on the session, or simply close it.
 * @see TLSProfile
 * @see org.beepcore.beep.core.Session
 * @see javax.net.ssl.HandshakeCompletedListener
 */
public interface TLSProfileHandshakeCompletedListener {

    /**
     * called after the SSL handshake has completed to allow verification
     * and fine tuning by the application.
     * @param session The BEEP Session on which the TLS profile channel was initiated
     * @param event Event passed by the <code>SSLSocket</code> when the handshake was completed.
     * @see javax.net.ssl.HandshakeCompletedEvent
     * @see org.beepcore.beep.core.Session
     */
    public boolean handshakeCompleted(Session session,
                                      HandshakeCompletedEvent event);
}
