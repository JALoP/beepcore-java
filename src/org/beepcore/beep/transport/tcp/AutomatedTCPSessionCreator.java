
/*
 * AutomatedTCPSessionCreator.java            $Revision: 1.1.1.1 $ $Date: 2001/04/02 08:45:51 $
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


import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.util.Hashtable;

import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.ProfileRegistry;


/**
 * This class provides a simple means for applications or other libraries to
 * create a TCP-based BEEP Session.  It provides some rudimentary socket
 * management logic to make the creation of TCPSessions simpler.  It's too
 * impl-specific to belong in the library however.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision, $Date: 2001/04/02 08:45:51 $
 */
public class AutomatedTCPSessionCreator {

    // Constants
    public final static int DEFAULT_TABLE_SIZE = 4;
    public final static int DEFAULT_BACKLOG_SIZE = 100;
    private static final String ERR_TCP_SOCKET_FAILURE =
        "Unable to create a TCP socket";
    private static final String ERR_BIND_FAILURE = "Bind Failed";
    private static final String ERR_CONNECT_FAILURE = "Connect Failed";
    private static final String ERR_LISTEN_FAILURE = "Accept Failed";

    // Data
    protected static Hashtable listenerSockets = null;

    // Creator Methods

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
        try {

            // Connect and create TCPSession with the socket
            Socket socket = new Socket(host, port);

            if (socket == null) {
                throw new BEEPException(ERR_TCP_SOCKET_FAILURE);
            }

            TCPSession t = TCPSessionCreator.initiate(socket, registry);

            return t;
        } catch (Exception x) {
            throw new BEEPException(ERR_CONNECT_FAILURE);
        }
    }

    /**
     * Method initiate
     *
     *
     * @param host
     * @param port
     * @param profiles
     * @param ccls
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
            throw new BEEPException(ERR_CONNECT_FAILURE);
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
                throw new BEEPException(ERR_BIND_FAILURE);
            }
        }

        // Listen
        try {
            peer = socket.accept();

            return TCPSessionCreator.listen(peer, registry);
        } catch (Exception e) {
            throw new BEEPException(ERR_LISTEN_FAILURE);
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
    public static TCPSession listen(int port, ProfileRegistry registry,
                                    String localInterface)
            throws BEEPException
    {
        try {
            TCPSession temp = null;
            InetAddress addr = null;

            if (localInterface != null) {
                addr = InetAddress.getByName(localInterface);
            }

            temp = listen(addr, port, registry);

            return temp;
        } catch (UnknownHostException x) {
            throw new BEEPException(ERR_LISTEN_FAILURE);
        }
    }
}
