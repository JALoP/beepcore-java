/*
 * TCPSessionCreator.java  $Revision: 1.3 $ $Date: 2001/10/31 02:03:41 $
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


import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.util.Hashtable;

import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.ProfileRegistry;
import org.beepcore.beep.core.SessionCredential;
import org.beepcore.beep.core.SessionTuningProperties;


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
 * @version $Revision: 1.3 $, $Date: 2001/10/31 02:03:41 $
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

    private static final int CHANNEL_START_ODD = 1;
    private static final int CHANNEL_START_EVEN = 2;

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
                              CHANNEL_START_ODD, null, null, null);
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

        // Connect and create TCPSession with the socket
        Socket socket;
        try {
            socket = new Socket(host, port);
        } catch (IOException x) {
            throw new BEEPException(x.getMessage());
        }

        if (socket == null) {
            throw new BEEPException(ERR_TCP_SOCKET_FAILURE);
        }

        TCPSession t = TCPSessionCreator.initiate(socket, registry);

        return t;
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
            throw new BEEPException("Unable to connect, unkown host");
        }
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
                              CHANNEL_START_EVEN, null, null, null);
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
                throw new BEEPException(x.getMessage());
            }
        }

        // Listen
        try {
            peer = socket.accept();

            return TCPSessionCreator.listen(peer, registry);
        } catch (Exception e) {
            throw new BEEPException(e.getMessage());
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
            TCPSession temp = null;
            InetAddress addr = null;

            if (localInterface != null) {
                addr = InetAddress.getByName(localInterface);
            }

            temp = listen(addr, port, registry);

            return temp;
        } catch (UnknownHostException x) {
            throw new BEEPException(x.getMessage());
        }
    }

    /**
     * Accessible only from TCPSession (kinda slick)
     */
    static TCPSession initiate(Socket sock, ProfileRegistry registry,
                               SessionCredential localCred,
                               SessionCredential peerCred,
                               SessionTuningProperties tuning)
            throws BEEPException
    {
        return new TCPSession(sock, (ProfileRegistry) registry.clone(),
                              CHANNEL_START_ODD, localCred, peerCred, tuning);
    }

    /**
     * Accessible only from TCPSession (reset() specifically)
     */
    static TCPSession listen(Socket sock, ProfileRegistry registry,
                             SessionCredential localCred,
                             SessionCredential peerCred,
                             SessionTuningProperties tuning)
            throws BEEPException
    {
        return new TCPSession(sock, (ProfileRegistry) registry.clone(),
                              CHANNEL_START_EVEN, localCred, peerCred,
                              tuning);
    }
}
