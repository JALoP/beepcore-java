/*
 * Session.java  $Revision: 1.37 $ $Date: 2003/11/18 14:03:07 $
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
package org.beepcore.beep.core;


import java.util.Collection;

import org.beepcore.beep.core.event.ChannelListener;
import org.beepcore.beep.core.event.SessionListener;


/**
 * This interface represents the operations available for all BEEP Sessions.
 * <p>
 * A BEEP Session encapsulates a connection for a given transport.
 *
 * @author Huston Franklin
 * @version $Revision: 1.37 $, $Date: 2003/11/18 14:03:07 $
 *
 * @see Channel
 */
public interface Session {

    // Constants
    public static final int SESSION_STATE_INITIALIZED = 0;
    public static final int SESSION_STATE_GREETING_SENT = 1;
    public static final int SESSION_STATE_ACTIVE = 2;
    public static final int SESSION_STATE_TUNING_PENDING = 3;
    public static final int SESSION_STATE_TUNING = 4;
    public static final int SESSION_STATE_CLOSE_PENDING = 5;
    public static final int SESSION_STATE_CLOSING = 6;
    public static final int SESSION_STATE_CLOSED = 7;
    public static final int SESSION_STATE_ABORTED = 8;

    /**
     * Adds a listener to be notified of Channel events (start, close, etc.).
     *
     * @see #removeChannelListener
     */
    public void addChannelListener(ChannelListener listener);

    /**
     * Adds a listener to be notified of Session events (greeting, close, etc.).
     *
     * @see #removeSessionListener
     */
    public void addSessionListener(SessionListener listener);

    /**
     * Request to close this <code>Session</code> gracefully. The profiles of
     * the open <code>Channels</code> on this <code>Session</code> may veto
     * the close request.
     *
     * @throws BEEPException
     */
    public void close() throws BEEPException;

    /**
     * Get the <code>SessionCredential</code> used to authenticate this peer
     * of this Session.
     *
     * @return <code>null</code> if this session has not been authenticated
     */
    public SessionCredential getLocalCredential();

    /**
     * Get the <code>SessionCredential</code> used to authenticate the remote
     * peer of this Session.
     *
     * @return <code>null</code> if this session has not been authenticated
     */
    public SessionCredential getPeerCredential();

    /**
     * Get the profiles supported by the remote peer.
     */
    public Collection getPeerSupportedProfiles();

    /**
     * Get the <code>ProfileRegistry</code> for this <code>Session</code>.
     *
     * @see ProfileRegistry
     */
    public ProfileRegistry getProfileRegistry();

    /**
     * Returns the state of <code>Session</code>.
     *
     * @return Session state (see the SESSION_STATE_xxx constants defined
     *          in this interface).
     */
    public int getState();

    /**
     * Indicates whehter or not this peer is the initiator of this Session.
     */
    public boolean isInitiator();

    /**
     * Removes the listener from the list of listeners to be notified
     * of future events. Note that the listener will be notified of
     * events which have already happened and are in the process of
     * being dispatched.
     *
     * @see #addChannelListener
     */
    public void removeChannelListener(ChannelListener l);

    /**
     * Removes the listener from the list of listeners to be notified
     * of future events. Note that the listener will be notified of
     * events which have already happened and are in the process of
     * being dispatched.
     *
     * @see #addSessionListener
     */
    public void removeSessionListener(SessionListener l);

    /**
     * Sends a request to start a new Channel on this Session for the
     * specified profile.
     *
     * @param profile The URI of the profile for the new Channel.
     *
     * @throws BEEPError Thrown if the remote peer is unable or refuses to
     *                    start a new Channel for the requested profile.
     * @throws BEEPException Thrown for errors other than those defined by
     *                        the BEEP protocol (e.g. the Session is not in a
     *                        state to create a new Channel).
     */
    public Channel startChannel(String profile)
            throws BEEPException, BEEPError;

    /**
     * Sends a request to start a new Channel on this Session for the
     * specified profile. This version of <code>startChannel</code> allows a
     * <code>MessageListener</code> to be specified to be registered once the
     * Channel is started. This is useful for profiles that are peer-to-peer in
     * nature.
     *
     * @param profile The URI of the profile for the new Channel.
     * @param listener A <code>MessageListener</code> to receive MSG messages
     *                  sent by the remote peer of this Session.
     *
     * @throws BEEPError Thrown if the remote peer is unable or refuses to
     *                    start a new Channel for the requested profile.
     * @throws BEEPException Thrown for errors other than those defined by
     *                        the BEEP protocol (e.g. the Session is not in a
     *                        state to create a new Channel).
     * @see MessageListener
     * @deprecated
     */
    public Channel startChannel(String profile, MessageListener listener)
            throws BEEPException, BEEPError;

    /**
     * Sends a request to start a new Channel on this Session for the
     * specified profile. This version of <code>startChannel</code> allows a
     * <code>RequestHandler</code> to be specified to be registered once the
     * Channel is started. This is useful for profiles that are peer-to-peer in
     * nature.
     *
     * @param profile The URI of the profile for the new Channel.
     * @param handler A <code>RequestHandler</code> to receive MSG messages
     *                sent by the remote peer of this Session.
     *
     * @throws BEEPError Thrown if the remote peer is unable or refuses to
     *                   start a new Channel for the requested profile.
     * @throws BEEPException Thrown for errors other than those defined by
     *                       the BEEP protocol (e.g. the Session is not in a
     *                       state to create a new Channel).
     */
    public Channel startChannel(String profile, RequestHandler handler)
            throws BEEPException, BEEPError;

    /**
     * Sends a request to start a new Channel on this Session for the
     * specified profile. This version of <code>startChannel</code> allows a
     * <code>MessageListener</code> to be specified to be registered once the
     * Channel is started. This is useful for profiles that are peer-to-peer in
     * nature.
     *
     * @param profile The URI of the profile for the new Channel.
     * @param base64Encoding Indicates whether or not <code>data</code> is
     *                        base64 encoded. <code>data</code> must be base64
     *                        encoded if it is not valid XML CDATA.
     * @param data An initial request to be sent piggyback'd along with the
     *              request to start the Channel. This request can be at most
     *              4K in size.
     *
     * @throws BEEPError Thrown if the remote peer is unable or refuses to
     *                    start a new Channel for the requested profile.
     * @throws BEEPException Thrown for errors other than those defined by
     *                        the BEEP protocol (e.g. the Session is not in a
     *                        state to create a new Channel).
     * @see MessageListener
     */
    public Channel startChannel(String profile, boolean base64Encoding,
                                String data)
            throws BEEPException, BEEPError;

    /**
     * Sends a request to start a new Channel on this Session for the
     * specified profile. This version of <code>startChannel</code> allows a
     * <code>MessageListener</code> to be specified to be registered once the
     * Channel is started. This is useful for profiles that are peer-to-peer in
     * nature.
     *
     * @param profile The URI of the profile for the new Channel.
     * @param base64Encoding Indicates whether or not <code>data</code> is
     *                        base64 encoded. <code>data</code> must be base64
     *                        encoded if it is not valid XML CDATA.
     * @param data An initial request to be sent piggyback'd along with the
     *              request to start the Channel. This request can be at most
     *              4K in size.
     * @param listener A <code>MessageListener</code> to receive MSG messages
     *                  sent by the remote peer of this Session.
     *
     * @throws BEEPError Thrown if the remote peer is unable or refuses to
     *                    start a new Channel for the requested profile.
     * @throws BEEPException Thrown for errors other than those defined by
     *                        the BEEP protocol (e.g. the Session is not in a
     *                        state to create a new Channel).
     * @see MessageListener
     * @deprecated
     */
    public Channel startChannel(String profile, boolean base64Encoding,
                                String data, MessageListener listener)
            throws BEEPException, BEEPError;

    /**
     * Sends a request to start a new Channel on this Session for the
     * specified profile. This version of <code>startChannel</code> allows a
     * <code>RequestHandler</code> to be specified to be registered once the
     * Channel is started.
     *
     * @param profile
     * @param handler A <code>RequestHandler</code> to receive MSG messages
     *                sent by the remote peer of this Session.
     *
     * @throws BEEPError Thrown if the remote peer is unable or refuses to
     *                   start a new Channel for the requested profile.
     * @throws BEEPException Thrown for errors other than those defined by
     *                       the BEEP protocol (e.g. the Session is not in a
     *                       state to create a new Channel).
     */
    public Channel startChannel(StartChannelProfile profile, RequestHandler handler)
            throws BEEPException, BEEPError;

    /**
     * Sends a start channel request using the given list of profiles.
     *
     * @param profiles A collection of <code>StartChannelProfile</code>(s).
     * @param listener A <code>MessageListener</code> to receive MSG messages
     *                  sent by the remote peer of this Session.
     *
     * @throws BEEPError Thrown if the remote peer is unable or refuses to
     *                    start a new Channel for the requested profile.
     * @throws BEEPException Thrown for errors other than those defined by
     *                        the BEEP protocol (e.g. the Session is not in a
     *                        state to create a new Channel).
     * @see StartChannelProfile
     * @see MessageListener
     * @deprecated
     */
    public Channel startChannel(Collection profiles, MessageListener listener)
        throws BEEPException, BEEPError;

    /**
     * Sends a start channel request using the given list of profiles.
     *
     * @param profiles A collection of <code>StartChannelProfile</code>(s).
     * @param handler A <code>RequestHandler</code> to receive MSG messages
     *                sent by the remote peer of this Session.
     *
     * @throws BEEPError Thrown if the remote peer is unable or refuses to
     *                   start a new Channel for the requested profile.
     * @throws BEEPException Thrown for errors other than those defined by
     *                       the BEEP protocol (e.g. the Session is not in a
     *                       state to create a new Channel).
     * @see StartChannelProfile
     * @see RequestHandler
     */
    public Channel startChannel(Collection profiles, RequestHandler handler)
        throws BEEPException, BEEPError;

    /**
     * This method is used to terminate the session when there is an
     * non-recoverable error.
     *
     * @param reason
     *
     */
    public void terminate(String reason);

    public SessionTuningProperties getTuningProperties();
}
