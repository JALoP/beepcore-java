/*
 * TCPSessionCreator.java  $Revision: 1.9 $ $Date: 2003/11/18 14:03:10 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
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
package org.beepcore.beep.transport.tcp;


import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.util.Hashtable;

import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.ProfileRegistry;


/**
 * This class provides a means for applications or other libraries to create
 * a TCP-based BEEP Session with another BEEP peer.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.9 $, $Date: 2003/11/18 14:03:10 $
 */
public class TCPSessionCreator {

    // Constants
    private static final int DEFAULT_TABLE_SIZE = 4;
    private static final int DEFAULT_BACKLOG_SIZE = 100;
    private static final String ERR_TCP_SOCKET_FAILURE =
        "Unable to create a TCP socket";
    private static final String ERR_BIND_FAILURE = "Bind Failed";
    private static final String ERR_CONNECT_FAILURE = "Connect Failed";
    private static final String ERR_LISTEN_FAILURE = "Accept Failed";

    // Data
    private static Hashtable listenerSockets = null;

    /**
     * Method initiate
     *
     *
     * @param host
     * @param port
     *
     * @throws BEEPException
     *
     */
    public static TCPSession initiate(InetAddress host, int port)
            throws BEEPException
    {
        try {
            return TCPSession.createInitiator(new Socket(host, port),
                                              new ProfileRegistry());
        } catch (IOException x) {
            throw new BEEPException(x);
        }
    }

    /**
     * Method initiate
     *
     *
     * @param host
     * @param port
     * @param registry
     * @param servername
     *
     * @throws BEEPException
     *
     */
    public static TCPSession initiate(InetAddress host, int port,
                                      ProfileRegistry registry,
                                      String servername)
            throws BEEPException
    {
        try {
            return TCPSession.createInitiator(new Socket(host, port),
                                              registry, servername);
        } catch (IOException x) {
            throw new BEEPException(x);
        }
    }

    /**
     * Method initiate
     *
     *
     * @param host
     * @param port
     * @param registry
     *
     * @throws BEEPException
     *
     */
    public static TCPSession initiate(InetAddress host, int port,
                                      ProfileRegistry registry)
        throws BEEPException
    {
        return initiate(host, port, registry, null);
    }
    
    /**
     * Method initiate
     *
     *
     * @param host
     * @param port
     *
     * @throws BEEPException
     *
     */
    public static TCPSession initiate(String host, int port)
        throws BEEPException
    {
        try {
            return initiate(InetAddress.getByName(host), port);
        } catch (UnknownHostException x) {
            throw new BEEPException("Unable to connect, unkown host");
        }
    }

    /**
     * Method initiate
     *
     *
     * @param host
     * @param port
     * @param registry
     *
     * @throws BEEPException
     *
     */
    public static TCPSession initiate(String host, int port,
                                      ProfileRegistry registry)
        throws BEEPException
    {
        try {
            return initiate(InetAddress.getByName(host), port, registry);
        } catch (UnknownHostException x) {
            throw new BEEPException("Unable to connect, unkown host");
        }
    }

    public static TCPSession initiate(String host, int port,
                                      ProfileRegistry registry,
                                      String servername)
        throws BEEPException
    {
        try {
            return initiate(InetAddress.getByName(host), port, registry,
                            servername);
        } catch (UnknownHostException x) {
            throw new BEEPException("Unable to connect, unkown host");
        }
    }

    /**
     * Method listen
     *
     *
     * @param port
     * @param registry
     *
     * @throws BEEPException
     *
     */
    public static TCPSession listen(int port, ProfileRegistry registry)
            throws BEEPException
    {
        InetAddress temp = null;

        return listen(temp, port, registry);
    }

    /**
     * Method listen
     *
     *
     * @param localInterface
     * @param port
     * @param registry
     *
     * @throws BEEPException
     *
     */
    public static TCPSession listen(InetAddress localInterface, int port,
                                    ProfileRegistry registry)
            throws BEEPException
    {
        ServerSocket socket = null;
        Socket peer = null;

        if (listenerSockets == null) {
            listenerSockets = new Hashtable(DEFAULT_TABLE_SIZE);
        }

        socket = (ServerSocket) listenerSockets.get(Integer.toString(port));

        // Bind if we're not listening on this port
        if (socket == null) {

            // Bind to interface/port pair
            try {
                if (localInterface == null) {
                    socket = new ServerSocket(port, DEFAULT_BACKLOG_SIZE);
                } else {
                    socket = new ServerSocket(port, DEFAULT_BACKLOG_SIZE,
                                              localInterface);
                }

                listenerSockets.put(Integer.toString(port), socket);
            } catch (Exception x) {
                throw new BEEPException(x);
            }
        }

        // Listen
        try {
            peer = socket.accept();

            return TCPSession.createListener(peer, registry);
        } catch (Exception e) {
            throw new BEEPException(e);
        }
    }

    /**
     * Method listen
     *
     *
     * @param port
     * @param registry
     * @param localInterface
     *
     * @throws BEEPException
     *
     */
    public static TCPSession listen(String localInterface, int port,
                                    ProfileRegistry registry)
            throws BEEPException
    {
        try {
            InetAddress addr = null;

            if (localInterface != null) {
                addr = InetAddress.getByName(localInterface);
            }

            return listen(addr, port, registry);
        } catch (UnknownHostException x) {
            throw new BEEPException(x);
        }
    }
}
