/*
 * TCPSessionCreator.java $Revision: 1.1.1.1 $ $Date: 2001/04/02 08:45:53 $
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
package org.beepcore.beep.transport.tcp;


import java.net.Socket;

import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.ProfileRegistry;
import org.beepcore.beep.core.SessionCredential;


/**
 * This class provides a means for applications or other libraries to create
 * a TCP-based BEEP Session with another BEEP peer.
 *
 * The Socket-based initiate and listen calls are in the
 * <code>AutomatedTCPSessionFactory</code>.
 *
 * Opportunities for polymorphic factory methods are rampant here, depending
 * upon how one wants to represent hosts, and whether helper objects
 * (such as the dreaded profile manager concept) might be used as parameters
 * in classes that extend this one.
 * It is assumed that the callpath started here in Session Factory handles the
 * Greeting exchange and so on, done either here or in the constructors.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.1.1.1 $, $Date: 2001/04/02 08:45:53 $
 */
public class TCPSessionCreator {

    protected static final int CHANNEL_START_ODD = 1;
    protected static final int CHANNEL_START_EVEN = 2;

    /**
     * Method initiate
     *
     *
     * @param sock
     * @param registry
     *
     * @throws BEEPException
     *
     */
    public static TCPSession initiate(Socket sock, ProfileRegistry registry)
            throws BEEPException
    {
        return new TCPSession(sock, (ProfileRegistry) registry.clone(),
                              CHANNEL_START_ODD, null, null);
    }

    /**
     * Accessible only from TCPSession (kinda slick)
     */
    static TCPSession initiate(Socket sock, ProfileRegistry registry,
                               SessionCredential localCred,
                               SessionCredential peerCred)
            throws BEEPException
    {
        return new TCPSession(sock, (ProfileRegistry) registry.clone(),
                              CHANNEL_START_ODD, localCred, peerCred);
    }

    /**
     * Method listen
     *
     *
     * @param sock
     * @param registry
     *
     * @throws BEEPException
     *
     */
    public static TCPSession listen(Socket sock, ProfileRegistry registry)
            throws BEEPException
    {
        return new TCPSession(sock, (ProfileRegistry) registry.clone(),
                              CHANNEL_START_EVEN, null, null);
    }

    /**
     * Accessible only from TCPSession (reset() specifically)
     */
    static TCPSession listen(Socket sock, ProfileRegistry registry,
                               SessionCredential localCred,
                               SessionCredential peerCred)
            throws BEEPException
    {
        return new TCPSession(sock, (ProfileRegistry) registry.clone(),
                              CHANNEL_START_EVEN, localCred, peerCred);
    }
}
