/*
 * TuningProfile.java  $Revision: 1.7 $ $Date: 2002/08/20 03:04:26 $
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
package org.beepcore.beep.core;


import java.util.*;

import org.beepcore.beep.profile.*;
import org.beepcore.beep.profile.tls.*;
import org.beepcore.beep.util.*;


/**
 * TuningProfiles change the security of a Session, either by
 * negotiating a security layer (such as encryption or some integrity
 * enhancing frame checksum), or by authenticating one or more of the
 * individual peers involved in the session.
 *
 * The TuningProfile class provides a nice place for constant data,
 * shared routines used by its subclasses (blob manipulation, base64
 * encoding), and so on.
 *
 * SASL Authentication goes through about 4 states, two of them
 * which are terminal
 * (abort)    (terminal state)
 * (complete) (terminal state)
 * (begin)
 * (continue)
 *
 */
public abstract class TuningProfile {

    // Constants
    private static final String TLS_URI = "http://iana.org/beep/TLS";

    // Data
    // If the channel has begun tuning, it's in the table.
    // If it's been aborted or gets completed, it's removed.
    private HashSet tuningChannels;

    /**
     * Constructor TuningProfile
     *
     *
     */
    public TuningProfile()
    {
        tuningChannels = new HashSet();
    }

    /**
     * Method abort
     *
     *
     * @param error
     * @param channel
     *
     * @throws BEEPException
     *
     */
    public void abort(BEEPError error, Channel channel) throws BEEPException
    {
        tuningChannels.remove(channel);
        Log.logEntry(Log.SEV_DEBUG, "TuningProfile.abort");

        // Log entry or something - throw an exception???
    }

    // Default Profile Methods

    /**
     * Method begin
     *
     *
     * @param channel
     * @param profile
     * @param data
     *
     * @throws BEEPException
     *
     */
    public void begin(Channel channel, String profile, String data)
            throws BEEPException
    {
        Log.logEntry(Log.SEV_DEBUG, "TuningProfile.begin");

        Session session = channel.getSession();

        try {
            tuningChannels.add(channel);
            session.sendProfile(profile, data, channel);
            session.disableIO();
        } catch (Exception x) {

            // If we're here, the profile didn't succesfully send, so
            // send an error
            BEEPError error = new BEEPError(451,
                                            "UnknownError" + x.getMessage());

            session.sendProfile(profile, error.createErrorMessage(), channel);
            abort(error, channel);
        }
    }

    /**
     * Method complete
     *
     *
     * @param channel
     * @param cred
     * @param registry
     * @param argument
     *
     * @throws BEEPException
     *
     */
    public void complete(Channel channel,
                         SessionCredential localCred,
                         SessionCredential peerCred,
                         SessionTuningProperties tuning,
                         ProfileRegistry registry,
                         Object argument)
            throws BEEPException
    {
        try {
            Log.logEntry(Log.SEV_DEBUG, "TuningProfile.complete");

            Session s = channel.getSession();

            s.reset(localCred, peerCred, tuning, registry, argument);
            tuningChannels.remove(channel);
        } catch (Exception x) {
            abort(new BEEPError(451, "TuningProfile.complete failure\n"
                                + x.getMessage()), channel);
        }
    }

    // Session calls exposed only through the tuning profile

    /**
     * Method disableIO
     *
     *
     * @param session
     *
     */
    protected static void disableIO(Session session)
    {
        session.disableIO();
    }

    /**
     * Method enableIO
     *
     *
     * @param session
     *
     */
    protected static void enableIO(Session session)
    {
        session.enableIO();
    }

    /**
     * Method setLocalCredential
     *
     *
     * @param session
     * @param credential
     *
     */
    protected static void setLocalCredential(Session session,
                                             SessionCredential credential)
    {
        session.setLocalCredential(credential);
    }

    /**
     * Method setPeerCredential
     *
     *
     * @param session
     * @param credential
     *
     */
    protected static void setPeerCredential(Session session,
                                            SessionCredential credential)
    {
        session.setPeerCredential(credential);
    }

    /**
     * Method reset
     *
     *
     * @param session
     * @param cred
     * @param registry
     * @param argument
     *
     * @throws BEEPException
     *
     */
    protected static Session reset(Session session,
                                   SessionCredential localCred,
                                   SessionCredential peerCred,
                                   SessionTuningProperties tuning,
                                   ProfileRegistry registry,
                                   Object argument)
            throws BEEPException
    {
        return session.reset(localCred, peerCred, tuning, registry, argument);
    }

    /**
     * Method sendProfile
     *
     *
     * @param session
     * @param uri
     * @param data
     * @param channel
     *
     * @throws BEEPException
     *
     */
    protected static void sendProfile(Session session, String uri, String data,
                                      Channel channel)
            throws BEEPException
    {
        session.sendProfile(uri, data, channel);
    }

    public Channel startChannel(Session session, String profile,
                                boolean base64Encoding, String data,
                                MessageListener listener)
            throws BEEPException, BEEPError
    {
        StartChannelProfile p = new StartChannelProfile(profile,
                                                        base64Encoding, data);
        LinkedList l = new LinkedList();

        l.add(p);

        return session.startChannelRequest(l, listener, true);
    }
}
