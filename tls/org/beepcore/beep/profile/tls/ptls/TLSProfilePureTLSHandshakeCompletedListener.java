
/*
 * TLSProfilePureTLSHandshakeCompletedListener.java  $Revision: 1.3 $ $Date: 2003/06/03 02:51:12 $
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
package org.beepcore.beep.profile.tls.ptls;


import org.beepcore.beep.core.*;

import java.util.Vector;


/**
 * receives handshake completed events.  This allows the application to
 * examine the trust of the authentication and take appropriate action as
 * necessary, such as alter the profiles on the session, or simply close it.
 * This is a specific interface that only works with PureTLS.  Other TLS
 * profile implementations will use something else if they use anything at all.
 * @see TLSProfilePureTLS
 * @see org.beepcore.beep.core.Session
 * @see COM.claymoresystems.cert.X509Cert
 */
public interface TLSProfilePureTLSHandshakeCompletedListener {

    /**
     * called after the SSL handshake has completed to allow verification
     * and fine tuning by the application.  The cert chain is a Vector where
     *     each certificate is a COM.claymoresystems.cert.X509Cert, reaching back from
     *     user to root.
     * @param session The BEEP Session on which the TLS profile channel was initiated
     * @param certChain the certficate chain that authenticates this peer
     *     @param cipher the encryption protocol chosen for this session (as defined
     *     in Appendex A of the <a href="http://www.ietf.org">TLS specification</a>.
     * @see org.beepcore.beep.core.Session
     *     @see COM.claymoresystems.cert.X509Cert
     *     @see java.util.Vector
     */
    public void handshakeCompleted(Session session, Vector certChain, int cipher)
        throws BEEPException;
}
