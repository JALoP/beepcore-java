/*
 * TLSProfileJSSE.java  $Revision: 1.12 $ $Date: 2003/11/16 00:17:14 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
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
package org.beepcore.beep.profile.tls.jsse;


import java.net.Socket;

import java.util.*;

import org.beepcore.beep.core.*;
import org.beepcore.beep.profile.*;
import org.beepcore.beep.profile.tls.TLSProfile;
import org.beepcore.beep.transport.tcp.*;

import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.KeyManagerFactory;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import com.sun.net.ssl.TrustManagerFactory;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.security.KeyStore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * TLS provides encrypted, and optionally authenticated, communication over
 * a session.  TLS is a tuning profile, a special set of profiles that affect
 * an entire session.  As a result, only one channel with the profile of TLS
 * may be open per session.  As with all tuning profiles, TLS may be configured
 * using properties passed into the init method.
 *
 * @see #init
 * @see org.beepcore.beep.profile.Profile
 * @see org.beepcore.beep.core.Channel
 * @see org.beepcore.beep.profile.tls.jsse.TLSProfileJSSEHandshakeCompletedListener
 */
public class TLSProfileJSSE extends TLSProfile
        implements Profile, StartChannelListener, RequestHandler {

    // Constants
    public static final String PROCEED1 = "<proceed/>";
    public static final String PROCEED2 = "<proceed />";
    public static final String READY1 = "<ready/>";
    public static final String READY2 = "<ready />";

    // error messages thrown in exceptions
    static final String ERR_SERVER_MUST_HAVE_KEY =
        "Listener must be anonymous if no keys are specified.";
    static final String ERR_EXPECTED_PROCEED = "Error receiving <proceed />";
    static final String ERR_ILLEGAL_KEY_STORE =
        "Illegal Key Store Type property value";
    static final String ERR_ILLEGAL_TRUST_STORE =
        "Illegal Trust Store Type property value";
    static final String ERR_TLS_NOT_SUPPORTED_BY_SESSION =
        "TLS not supported by this session";
    static final String ERR_TLS_SOCKET = "TLS not supported by this session";
    static final String ERR_TLS_HANDSHAKE_WAIT =
        "Error waiting for TLS handshake to complete";
    static final String ERR_TLS_NO_AUTHENTICATION =
        "Authentication failed for this TLS negotiation";

    // property names
    static final String PROPERTY_KEY_MANAGER_ALGORITHM = "Key Algorithm";
    static final String PROPERTY_KEY_MANAGER_PROVIDER = "Key Provider";
    static final String PROPERTY_TRUST_MANAGER_ALGORITHM = "Trust Algorithm";
    static final String PROPERTY_TRUST_MANAGER_PROVIDER = "Trust Provider";
    static final String PROPERTY_KEYSTORE_PASSPHRASE = "Key Store Passphrase";
    static final String PROPERTY_KEYSTORE_TYPE = "Key Store Data Type";
    static final String PROPERTY_KEYSTORE_NAME = "Key Store";
    static final String PROPERTY_KEYSTORE_FORMAT = "Key Store Format";
    static final String PROPERTY_KEYSTORE_PROVIDER = "Key Store Provider";
    static final String PROPERTY_TRUSTSTORE_PASSPHRASE =
        "Trust Store Passphrase";
    static final String PROPERTY_TRUSTSTORE_TYPE = "Trust Store Data Type";
    static final String PROPERTY_TRUSTSTORE_NAME = "Trust Store";
    static final String PROPERTY_TRUSTSTORE_FORMAT = "Trust Store Format";
    static final String PROPERTY_TRUSTSTORE_PROVIDER = "Trust Store Provider";
    static final String PROPERTY_CIPHER_SUITE = "Cipher Suite";
    static final String PROPERTY_CLIENT_AUTHENTICATION =
        "Initiator Authentication Required";
    static final String PROPERTY_SERVER_ANONYMOUS = "Listener Anonymous";

    // properties set from the configuration
    static boolean needClientAuth = false;
    static boolean serverAnonymous = true;
    static String keyAlgorithm = null;
    static String keyProvider = null;
    static String trustAlgorithm = null;
    static String trustProvider = null;
    static String keyPassphrase = null;
    static String keyStoreType = null;
    static String keyStoreName = null;
    static String keyStoreFormat = null;
    static String keyStoreProvider = null;
    static String trustPassphrase = null;
    static String trustStoreType = null;
    static String trustStoreName = null;
    static String trustStoreFormat = null;
    static String trustStoreProvider = null;

    // socket factory that creates/wraps SSL connections
    static SSLSocketFactory socketFactory = null;

    private Log log = LogFactory.getLog(this.getClass());

    // listeners to update when an SSL handshake completes
    //      static List handshakeListeners = null;
    //      static Map pendingHandshakes = null;
    boolean notifiedHandshake = false;
    boolean waitingForHandshake = false;
    boolean abortSession = false;
    String uri = TLSProfile.URI;
    static List handshakeListeners = null;

    class TLSHandshake implements HandshakeCompletedListener {

        Session session;
        SessionCredential cred;
        boolean notifiedHandshake = false;
        boolean waitingForHandshake = false;

        public void handshakeCompleted(HandshakeCompletedEvent event)
        {
            log.debug("HandshakeCompleted");
            synchronized (handshakeListeners) {
                Iterator i = TLSProfileJSSE.handshakeListeners.iterator();

                while (i.hasNext()) {
                    TLSProfileJSSEHandshakeCompletedListener l =
                        (TLSProfileJSSEHandshakeCompletedListener) i.next();

                    if (l.handshakeCompleted(session, event) == false) {
                        abortSession = true;

                        break;
                    }
                }
            }

            Hashtable h = new Hashtable();

            try {
                h.put(SessionCredential.AUTHENTICATOR,
                      event.getPeerCertificateChain()[0].getSubjectDN().getName());
                h.put(SessionCredential.REMOTE_CERTIFICATE,
                      event.getPeerCertificateChain());
            } catch (SSLPeerUnverifiedException e) {
                h.put(SessionCredential.AUTHENTICATOR, "");
                h.put(SessionCredential.REMOTE_CERTIFICATE, "");
            }

            cred = new SessionCredential(h);

            synchronized (this) {
                if (waitingForHandshake) {
                    notify();
                }

                notifiedHandshake = true;
            }
        }
    }

    class BeepListenerHCL implements HandshakeCompletedListener {

        Channel channel;
        boolean notifiedHandshake = false;
        boolean waitingForHandshake = false;
        
        BeepListenerHCL(Channel tuningChannel)
        {
            this.channel = tuningChannel;
        }

        public void handshakeCompleted(HandshakeCompletedEvent event)
        {
            Session oldSession = channel.getSession();
            
            log.debug("HandshakeCompleted");
            synchronized (handshakeListeners) {
                Iterator i = TLSProfileJSSE.handshakeListeners.iterator();

                while (i.hasNext()) {
                    TLSProfileJSSEHandshakeCompletedListener l =
                        (TLSProfileJSSEHandshakeCompletedListener) i.next();

                    if (l.handshakeCompleted(oldSession, event) == false) {
                        BEEPError e =
                            new BEEPError(BEEPError.CODE_REQUESTED_ACTION_ABORTED,
                                          ERR_TLS_NO_AUTHENTICATION);
                        TLSProfileJSSE.this.abort(e, channel);

                        return;
                    }
                }
            }

            Hashtable h = new Hashtable();

            try {
                h.put(SessionCredential.AUTHENTICATOR,
                      event.getPeerCertificateChain()[0].getSubjectDN().getName());
                h.put(SessionCredential.REMOTE_CERTIFICATE,
                      event.getPeerCertificateChain());
            } catch (SSLPeerUnverifiedException e) {
                h.put(SessionCredential.AUTHENTICATOR, "");
                h.put(SessionCredential.REMOTE_CERTIFICATE, "");
            }

            ProfileRegistry preg = oldSession.getProfileRegistry();

            preg.removeStartChannelListener(uri);

            Hashtable hash = new Hashtable();

            hash.put(SessionTuningProperties.ENCRYPTION, "true");

            SessionTuningProperties tuning =
                new SessionTuningProperties(hash);

            // Cause the session to be recreated and reset
            try {
                TLSProfileJSSE.this.complete(channel, generateCredential(),
                                             new SessionCredential(h), tuning,
                                             preg, event.getSocket());
            } catch (BEEPException e) {
                BEEPError error =
                    new BEEPError(BEEPError.CODE_REQUESTED_ACTION_ABORTED,
                                  ERR_TLS_NO_AUTHENTICATION);
                TLSProfileJSSE.this.abort(error, channel);
            }
        }
    }

    /**
     * TLS provides encryption and optionally authentication for a session
     * by opening a channel with this profile.  The default action is to
     * set up for a channel with encryption only, no authentication.  To
     * mandate authentication, set the configuration via <code>init</code>.<p>
     * @see org.beepcore.beep.profile.Profile
     */
    public TLSProfileJSSE()
    {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");

            ctx.init(null, null, null);

            socketFactory = (SSLSocketFactory) ctx.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        } catch (KeyManagementException e) {
            log.error(e.getMessage());
        }

        if (handshakeListeners == null) {
            handshakeListeners =
                Collections.synchronizedList(new LinkedList());
        }
    }

    /**
     * init sets the criteria for which an SSL connection is made when
     * a TLS channel is started for a profile.  It should only be
     * called once.  For the properties, the initiator is defined as
     * the peer who starts the channel for the TLS profile, the
     * listener is the peer that receives the the channel start
     * request, irregardless of which actually started the session.<p>
     *
     * @param config <code>ProfileConfiguration</code> object that
     * contains key value pairs to initialize the TLS layer.  None of
     * these are mandatory, but if you wish communication to be
     * anonymous with no authentication, (i.e., the listener to not
     * send back a certificate), you must set "Listener Anonymous" to
     * "true" and "Initiator Authentication Required" to "false".
     * The meaningful properties that can be set are these:<p>
     * <table>
     * <tr>
     * <td>Listener Anonymous</td><td>(true|false) must be set to false if the
     *   listener will not authenticate itself</td>
     * </tr><tr>
     * <td>Initiator Authentication Required</td><td>(true|false) set if the
     *       initiator should send a certificate and the listener expects a
     *       certificate.</td>
     * </tr><tr>
     * <td>Cipher Suite</td><td><i>not yet implemented.</i>the algorithms that
     *       can be used for encryption, authentication, and key exchange.</td>
     * </tr><tr>
     * <td>Key Algorithm</td><td>key management algorithm. See
     *       {@link com.sun.net.ssl.KeyManagerFactory#getInstance}</td>
     * </tr><tr>
     * <td>Key Provider</td><td>provider of the key management
     *       algorithm.  Defaults to
     *    <code>com.sun.net.ssl.internal.ssl.Provider</code> See
     *       {@link com.sun.net.ssl.KeyManagerFactory#getInstance}</td>
     * </tr><tr>
     * <td>Trust Algorithm</td><td>algorithm to be used by the trust
     *       manager.  See
     *       {@link com.sun.net.ssl.TrustManagerFactory#getInstance}</td>
     * </tr><tr>
     * <td>Trust Provider</td><td>provider of the trust manager.  Defaults to
     *    <code>com.sun.net.ssl.internal.ssl.Provider</code>.  See
     *    {@link com.sun.net.ssl.TrustManagerFactory#getInstance}</td>
     * </tr><tr>
     * <td>Key Store Passphrase</td><td>pass phrase used to encrypt the key
     *    store.  See {@link java.security.KeyStore#load}</td>
     * </tr><tr>
     * <td>Key Store Data Type</td><td>data type of the key store passed in.
     *     "file" is currently the only value accepted, meaning Key Store
     *     is the name of a file containing keys.  See
     *     {@link java.security.KeyStore#load}</td>
     * </tr><tr>
     * <td>Key Store</td><td>value of the key store, dependent on the type in
     *     Key Store Data Type.  See {@link java.security.KeyStore#load}</td>
     * </tr><tr>
     * <td>Key Store Format</td><td>format of the keys within the key store.
     *  Default is "JKS".  See {@link java.security.KeyStore#getInstance}</td>
     * </tr><tr>
     * <td>Key Store Provider</td><td>provider for the key stores.  See
     *     {@link java.security.KeyStore#getInstance}</td>
     * </tr><tr>
     * <td>Trust Store Passphrase</td><td>pass phrase used to encrypt the trust
     *     store.  See {@link java.security.KeyStore#load}</td>
     * </tr><tr>
     * <td>Trust Store Data Type</td><td>data type of the certificates in the
     * trust store.  "file" is currently th only value accepted,
     * meaning the trust store is a file on the local disk.  See
     *     {@link java.security.KeyStore#load}</td>
     * </tr><tr>
     * <td>Trust Store</td><td>value of the trust store, dependent on the type
     *     in Trust
     *     Store Data Type  See {@link java.security.KeyStore#load}</td>
     * </tr><tr>
     * <td>Trust Store Format</td><td>format of the certificates within the
     *     trust store.
     * Default is "JKS".  See {@link java.security.KeyStore#getInstance}</td>
     * </tr><tr>
     * <td>Trust Store Provider</td><td>provider for the trust stores.  See
     *     {@link java.security.KeyStore#getInstance}</td>
     * </tr><tr>
     * </table>
     * @throws BEEPException For any error in the profile configuration, a
     * negative response in the form of a BEEP error will be sent back to the
     * requesting peer.  The session will continue to be open and usable, at
     * least from the standpoint of this peer.
     *
     * @see com.sun.net.ssl.KeyManagerFactory
     * @see com.sun.net.ssl.TrustManagerFactory
     * @see java.security.KeyStore
     * @see com.sun.net.ssl.SSLContext
     */
    public StartChannelListener init(String uri, ProfileConfiguration config)
            throws BEEPException
    {
        KeyManagerFactory kmf = null;
        KeyManager[] km = null;
        KeyStore ks = null;
        TrustManagerFactory tmf = null;
        TrustManager[] tm = null;
        KeyStore ts = null;
        SSLContext ctx;

        // set the URI of this instance of the profile
        this.uri = uri;

        try {

            // create an SSL context object
            ctx = SSLContext.getInstance("TLS");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new BEEPException("TLS Algorithm Not Found. Probable "
                                    + "cause is the JSSE provider has not "
                                    + "been added to the java.security file.");
        }

        try {

            // initialize the key managers, trust managers, and
            keyAlgorithm = config.getProperty(PROPERTY_KEY_MANAGER_ALGORITHM);
            keyProvider = config.getProperty(PROPERTY_KEY_MANAGER_PROVIDER);
            trustAlgorithm =
                config.getProperty(PROPERTY_TRUST_MANAGER_ALGORITHM);
            trustProvider =
                config.getProperty(PROPERTY_TRUST_MANAGER_PROVIDER);
            keyPassphrase = config.getProperty(PROPERTY_KEYSTORE_PASSPHRASE);
            keyStoreType = config.getProperty(PROPERTY_KEYSTORE_TYPE);
            keyStoreName = config.getProperty(PROPERTY_KEYSTORE_NAME);
            keyStoreFormat = config.getProperty(PROPERTY_KEYSTORE_FORMAT,
                                                "JKS");
            keyStoreProvider = config.getProperty(PROPERTY_KEYSTORE_PROVIDER);
            trustPassphrase =
                config.getProperty(PROPERTY_TRUSTSTORE_PASSPHRASE);
            trustStoreType = config.getProperty(PROPERTY_TRUSTSTORE_TYPE);
            trustStoreName = config.getProperty(PROPERTY_TRUSTSTORE_NAME);
            trustStoreFormat = config.getProperty(PROPERTY_TRUSTSTORE_FORMAT,
                                                  "JKS");
            trustStoreProvider =
                config.getProperty(PROPERTY_TRUSTSTORE_PROVIDER);

            // determine if the client must authenticate or if the server can
            // 
            needClientAuth =
                new Boolean(config.getProperty(PROPERTY_CLIENT_AUTHENTICATION,
                                               "false")).booleanValue();
            serverAnonymous =
                new Boolean(config.getProperty(PROPERTY_SERVER_ANONYMOUS,
                                               "true")).booleanValue();

            if (keyAlgorithm != null) {
                if (keyProvider != null) {
                    kmf = KeyManagerFactory.getInstance(keyAlgorithm,
                                                        keyProvider);
                } else {
                    kmf = KeyManagerFactory.getInstance(keyAlgorithm);
                }

                // add support for a default type of key manager factory?
                if (keyStoreProvider != null) {
                    ks = KeyStore.getInstance(keyStoreFormat,
                                              keyStoreProvider);
                } else {
                    ks = KeyStore.getInstance(keyStoreFormat);
                }

                if (keyStoreType.equals("file")) {
                    ks.load(new FileInputStream(keyStoreName),
                            keyPassphrase.toCharArray());
                } else {
                    throw new BEEPException(ERR_ILLEGAL_KEY_STORE);
                }

                // initialize the key factory manager 
                kmf.init(ks, keyPassphrase.toCharArray());

                km = kmf.getKeyManagers();
            } else {
                km = null;
            }

            if (trustAlgorithm != null) {
                if (trustProvider != null) {
                    tmf = TrustManagerFactory.getInstance(trustAlgorithm,
                                                          trustProvider);
                } else {
                    tmf = TrustManagerFactory.getInstance(trustAlgorithm);
                }

                // add support for a default type of trust manager factory?
                if (trustStoreProvider != null) {
                    ts = KeyStore.getInstance(trustStoreFormat,
                                              trustStoreProvider);
                } else {
                    ts = KeyStore.getInstance(trustStoreFormat);
                }

                if (trustStoreType.equals("file")) {
                    ts.load(new FileInputStream(trustStoreName),
                            trustPassphrase.toCharArray());
                } else {
                    throw new BEEPException(ERR_ILLEGAL_TRUST_STORE);
                }

                // initialize the trust factory manager 
                tmf.init(ts);

                tm = tmf.getTrustManagers();
            } else {
                tm = null;
            }

            // create a socket factory from the key factories and
            // trust factories created for the algorithms and stores
            // specfied.  No option is given to change the secure
            // random number generator
            ctx.init(km, tm, null);

            socketFactory = ctx.getSocketFactory();

            return this;
        } catch (Exception e) {
            log.error(e);

            throw new BEEPException(e);
        }
    }

    /**
     * advertiseProfile
     */
    public boolean advertiseProfile(Session session, SessionTuningProperties tuning)
            throws BEEPException
    {
        return true;
    }

    /**
     * Called when the underlying BEEP framework receives
     * a "start" element for the TLS profile.<p>
     *
     * @param channel A <code>Channel</code> object which represents a channel
     * in this <code>Session</code>.
     * @param data The content of the "profile" element selected for this
     * channel (may be <code>null</code>).
     * @param encoding specifies whether the content of the "profile" element
     * selected for this channel is represented as a base64-encoded string.
     * The <code>encoding</code> is only valid if <code>data</code> is not
     * <code>null</code>.
     *
     * @throws StartChannelException Throwing this exception will cause an
     * error to be returned to the BEEP peer requesting to start a channel.
     * The channel is then discarded.
     */
    public void startChannel(Channel channel, String encoding, String data)
            throws StartChannelException
    {
        channel.setRequestHandler(this, true);
    }

    public void receiveMSG(MessageMSG msg)
    {
        Channel channel = msg.getChannel();

        InputDataStreamAdapter is = msg.getDataStream().getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String data;

        try {
            try {
                data = reader.readLine();
            } catch (IOException e) {
                msg.sendERR(BEEPError.CODE_PARAMETER_ERROR,
                            "Error reading data");
                return;
            }

            if (data.equals(READY1) == false && data.equals(READY2) == false) {
                msg.sendERR(BEEPError.CODE_PARAMETER_INVALID,
                            "Expected READY element");
            }

            this.begin(channel);

            msg.sendRPY(new StringOutputDataStream(PROCEED2));
        } catch (BEEPException e1) {
            channel.getSession().terminate("unable to send ERR");
            return;
        }

        try {
            Socket oldSocket = ((TCPSession) channel.getSession()).getSocket();
            /** @TODO add support for serverName */
            SSLSocket newSocket =
                (SSLSocket) socketFactory.createSocket(oldSocket,
                                                       oldSocket.getInetAddress().getHostName(),
                                                       oldSocket.getPort(),
                                                       true);

            BeepListenerHCL l = new BeepListenerHCL(channel);

            newSocket.addHandshakeCompletedListener(l);
            newSocket.setUseClientMode(false);
            newSocket.setNeedClientAuth(needClientAuth);
            newSocket.setEnabledCipherSuites(newSocket.getSupportedCipherSuites());

            newSocket.startHandshake();
        } catch (IOException e) {
            channel.getSession().terminate("TLS error: " + e.getMessage());
            return;
        }
    }
    
    /**
     * Called when the underlying BEEP framework receives
     * a "close" element.<p>
     *
     * @param channel <code>Channel</code> which received the close request.
     *
     * @throws CloseChannelException Throwing this exception will return an
     * error to the BEEP peer requesting the close.  The channel will remain
     * open.
     */
    public void closeChannel(Channel channel) throws CloseChannelException
    {
        log.debug("Closing TLS channel.");
    }

    /**
     * Default implementation of advertiseProfile.  Just returns TRUE that
     * the TLS profile should be advertised.
     */
    public boolean advertiseProfile(Session session)
            throws BEEPException
    {
        return true;
    }

    /**
     * start a channel for the TLS profile.  Besides issuing the
     * channel start request, it also performs the initiator side
     * chores necessary to begin encrypted communication using TLS
     * over a session.  Parameters regarding the type of encryption
     * and whether or not authentication is required are specified
     * using the profile configuration passed to the <code>init</code>
     * method Upon returning, all traffic over the session will be
     * entrusted as per these parameters.<p>
     *
     * @see #init init - profile configuration
     * @param session session - the session to encrypt communcation for
     * @param arg arg - not used currently
     *
     * @return new <code>Session</code> with TLS negotiated.
     * @throws BEEPException an error occurs during the channel start
     * request or the TLS handshake (such as trying to negotiate an
     * anonymous connection with a peer that doesn't support an
     * anonymous cipher suite).
     */
    public TCPSession startTLS(TCPSession session) throws BEEPException
    {
        Channel ch = startChannel(session, uri, false, READY2, null);

        // See if we got start data back
        String data = ch.getStartData();

        if (log.isDebugEnabled()) {
            log.debug("Got start data of " + data);
        }

        // Consider the data (see if it's proceed)
        if ((data == null)
                || (!data.equals(PROCEED1) &&!data.equals(PROCEED2)))
        {
            log.error("Invalid reply: " + data);
            throw new BEEPException(ERR_EXPECTED_PROCEED);
        }

        // Freeze IO and get the socket and reset it to TLS
        Socket oldSocket = session.getSocket();
        SSLSocket newSocket = null;
        TLSHandshake l = new TLSHandshake();

        // create the SSL Socket
        try {
            newSocket =
                (SSLSocket) socketFactory.createSocket(oldSocket,
                                                       oldSocket.getInetAddress().getHostName(),
                                                       oldSocket.getPort(),
                                                       true);

            newSocket.addHandshakeCompletedListener(l);
            newSocket.setUseClientMode(true);
            newSocket.setNeedClientAuth(needClientAuth);
            newSocket.setEnabledCipherSuites(newSocket.getSupportedCipherSuites());

            // set up so the handshake listeners will be called
            l.session = session;

            log.debug("Handshake starting");
            newSocket.startHandshake();
            log.debug("Handshake returned");

            synchronized (l) {
                if (!l.notifiedHandshake) {
                    l.waitingForHandshake = true;

                    l.wait();

                    l.waitingForHandshake = false;
                }
            }
            log.debug("Handshake done waiting");
        } catch (javax.net.ssl.SSLException e) {
            log.error(e);
            throw new BEEPException(e);
        } catch (java.io.IOException e) {
            log.error(e);
            throw new BEEPException(ERR_TLS_SOCKET);
        } catch (InterruptedException e) {
            log.error(e);
            throw new BEEPException(ERR_TLS_HANDSHAKE_WAIT);
        }

        // swap it out for the new one with TLS enabled.
        if (abortSession) {
            session.close();

            throw new BEEPException(ERR_TLS_NO_AUTHENTICATION);
        } else {
            Hashtable hash = new Hashtable();

            hash.put(SessionTuningProperties.ENCRYPTION, "true");

            SessionTuningProperties tuning =
                new SessionTuningProperties(hash);

            return (TCPSession) reset(session, generateCredential(), l.cred,
                                      tuning, session.getProfileRegistry(),
                                      newSocket);
        }
    }

    /**
     * return the default credentials for the new session to use after a TLS
     * negotiation is complete.
     *
     */
    public static SessionCredential generateCredential()
    {
        Hashtable ht = new Hashtable(4);

        // @todo Could this matter since we're using TLSProfile here
        // and everywhere else we're using the instance variable uri?  Perhaps.
        // I'll have to look into it.
        ht.put(SessionCredential.AUTHENTICATOR, TLSProfile.URI);

        return new SessionCredential(ht);
    }

    /**
     * add a listener for completed handshakes.
     * @param TLSProfileJSSEHandshakeCompletedListener receives handshake complete
     *    events
     *
     * @param x
     */
    public void addHandshakeCompletedListener(TLSProfileJSSEHandshakeCompletedListener x)
    {
        removeHandshakeCompletedListener(x);
        handshakeListeners.add(x);
    }

    /**
     * remove a listener for completed handshakes.
     * @param TLSProfileJSSEHandshakeCompletedListener receives handshake complete
     *    events
     *
     * @param x
     */
    public void removeHandshakeCompletedListener(TLSProfileJSSEHandshakeCompletedListener x)
    {
        handshakeListeners.remove(x);
    }
}
