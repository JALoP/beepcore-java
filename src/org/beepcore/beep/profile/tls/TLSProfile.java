
/*
 * TLSProfile.java  $Revision: 1.10 $ $Date: 2001/07/09 05:57:04 $
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
package org.beepcore.beep.profile.tls;


import org.beepcore.beep.core.*;
import org.beepcore.beep.profile.*;
import org.beepcore.beep.transport.tcp.*;
import org.beepcore.beep.util.*;


/**
 * TLS provides encrypted and authenticated communication over
 * a session.  TLS is a tuning profile, a special set of profiles that affect
 * an entire session.  As a result, only one channel with the profile of TLS
 * may be open per session.  As with all tuning profiles, TLS may be configured
 * using properties passed into the init method.  You may wish considering
 * adding your own methods to your subclass that allows you to set the
 * parameters necessary rather than using the init method.  The init method
 * for configurations that can be set by a user/administrator.  Developers
 * would most likely want to use a set of set/get methods.  See
 * {@link TLSProfilePureTLS} and {@link TLSProfilePureTLSPemInit} for
 * differences between the two.
 *
 * @see #init
 * @see org.beepcore.beep.profile.Profile
 * @see org.beepcore.beep.core.Channel
 */
public abstract class TLSProfile extends TuningProfile implements Profile {

    /**
     * default URI of the channel to start to start a TLS channel
     */
    public static final String URI = "http://iana.org/beep/TLS";

    /**
     * list of known providers
     */
    public static final String[] providers = { "TLSProfilePureTLS",
                                               "TLSProfileJSSE" };

    /**
     * init sets the criteria for which an TLS connection is made when
     * a TLS channel is started for a profile.  It should only be
     * called once.  For the properties, the initiator is defined as
     * the peer who starts the channel for the TLS profile, the
     * listener is the peer that receives the the channel start
     * request, irregardless of which actually started the session.<p>
     * Each subclass that encapsulates an implementation has its own
     * properties as to what it needs to initialise.  See the individual
     * implementations for their properties.
     *
     * @param uri used to start a channel with TLS protection
     * @param config used to specify the parameters for sessions protected
     * by this profile's version of TLS.  In other words, if you want another
     * set of paramters, you must either recall this method or create another
     * instance of a <code>TLSProfile</code>.
     */
    abstract public StartChannelListener init(String uri, ProfileConfiguration config)
        throws BEEPException;

    /**
     * start a channel for the TLS profile.  Besides issuing the
     * channel start request, it also performs the initiator side
     * chores necessary to begin encrypted communication using TLS
     * over a session.  Parameters regarding the type of encryption
     * and authentication are specified using the profile
     * configuration passed to the <code>init</code> method Upon
     * returning, all traffic over the session will be entrusted as
     * per these parameters.<p>
     *
     * @see #init profile configuration
     * @param session the session to encrypt communcation for
     * @return new {@link TCPSession} with TLS negotiated.
     * @throws BEEPException an error occurs during the channel start
     * request or the TLS handshake (such as trying to negotiate an
     * anonymous connection with a peer that doesn't support an
     * anonymous cipher suite).
     */
    abstract public TCPSession startTLS(TCPSession session)
        throws BEEPException;

    /**
     * factory method that returns an instance the default
     * implementation.  As of now, that is a {@link TLSProfilePureTLS}
     * class.
     */
    public static TLSProfile getDefaultInstance() throws BEEPException
    {
        return
            getInstance("org.beepcore.beep.profile.tls.ptls.TLSProfilePureTLS");
    }

    /**
     * factory method that returns an instance of the implementation
     * given in the parameter.  As of now, only one is supported,
     * "PureTLS" returns an instance of TLSProfilePureTLS.  If the
     * provider is unknown, it tries to load a class of that name and
     * returns.
     * @param provider implementation to use.
     */
    public static TLSProfile getInstance(String provider) throws BEEPException
    {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            Class whatever = cl.loadClass(provider);

            return (TLSProfile) whatever.newInstance();
        } catch (ClassNotFoundException e) {
            throw new BEEPException("Provider '" + provider + "' not found.");
        } catch (Exception e) {
            throw new BEEPException(e.getMessage());
        }
    }
}
