/*
 * TLSProfilePureTLS.java  $Revision: 1.9 $ $Date: 2003/11/07 23:01:35 $
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
package org.beepcore.beep.profile.tls.ptls;


import java.net.Socket;

import java.util.*;

import java.security.PrivateKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.beepcore.beep.core.*;
import org.beepcore.beep.profile.tls.TLSProfile;
import org.beepcore.beep.transport.tcp.*;

import COM.claymoresystems.ptls.*;
import COM.claymoresystems.sslg.*;
import COM.claymoresystems.cert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * TLS provides encrypted and authenticated communication over a
 * session using the PureTLS library.  TLS is a tuning profile, a
 * special set of profiles that affect an entire session.  As a
 * result, only one channel with the profile of TLS may be open per
 * session.  As with all tuning profiles, TLS may be configured using
 * properties passed into the init method, though TLSProfilePureTLS
 * requires some <p>
 *
 * This profile uses the PureTLS library from <a
 * href="http://www.rtfm.com/puretls">www.rtfm.com/puretls</a> by
 * Claymore Systems and Eric Rescorla to implement the TLS protocol
 * for a session.<p> For now, this profile for TLS will <b>NOT</b> use
 * anonymous ciphers.  All ciphers must have a private key and
 * certificate chain.
 *
 * @see #init
 * @see org.beepcore.beep.profile.Profile
 * @see org.beepcore.beep.core.Channel
 * @see org.beepcore.beep.profile.tls.ptls.TLSProfilePureTLSHandshakeCompletedListener
 * @see java.util.List
 */
public class TLSProfilePureTLS extends TuningProfile
    implements StartChannelListener, RequestHandler
{

    // Constants
    public static final String PROCEED1 = "<proceed/>";
    public static final String PROCEED2 = "<proceed />";
    public static final String READY1 = "<ready/>";
    public static final String READY2 = "<ready />";

    /**
     * use this as the uri for the channel to open to encrypt a
     * session using TLS.
     */
    public static final String URI = "http://iana.org/beep/TLS";

    // taken from the SSLPolicyInt of PureTLS
    static final int MAX_CIPHERS = 0x67;

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
    //      public static final String PROPERTY_PEER_AUTHENTICATION_REQUIRED =
    //         "Peer Authentication Required";
    public static final String PROPERTY_CLIENT_AUTH_REQUIRED = 
        "Client Authenticaton Required";
    public static final String PROPERTY_CIPHER_SUITE = "Cipher Suite";
    public static final String PROPERTY_CERTIFICATES = "Certificates";
    public static final String PROPERTY_PRIVATE_KEY = "Private Key";
    public static final String PROPERTY_TRUSTED_CERTS =
        "Trusted Certificates";
    public static final String PROPERTY_PRIVATE_KEY_ALGORITHM =
        "Private Key Type";

    private Log log = LogFactory.getLog(this.getClass());

    // properties set from the configuration
    boolean needPeerAuth = true;
    short[] cipherSuite = new short[MAX_CIPHERS];
    String uri = URI;
    SSLPolicyInt policy = null;
    SSLContext context = null;
    boolean abortSession = false;
    private TLSProfilePureTLSHandshakeCompletedListener handshakeListener =
        null;

    // these might be useful for doing anonymous cipehrs.  If so, these
    // would be the certificates used to "verify" the peers.
    private static String defaultPrivateKey =
        "-----BEGIN RSA PRIVATE KEY-----\n" + "Proc-Type: 4,ENCRYPTED\n"
        + "DEK-Info: DES-EDE3-CBC,376827D42B068D3C\n" + "\n"
        + "FAyWxidmVeHJBv9IWjp3NLtmnsLML92XJfVOT134C5IFez/PxHrkieuzHYv79m0u\n"
        + "QAuySeIccNgdSQA/zcHLFUJjzxx7NjFtj3+80zredcXW5SyGd8F8Y9EpWV6rd6sa\n"
        + "h3BJ2BnYNr3hTBoIlj/xnaSvfW0LrjcI6vaPw4sZ1gcNjfNzOVTUCgqNf6O+AIlI\n"
        + "uMXNF+Lurp/aK6CV1LABhbsc5/CqmfOlWRvydiQiUFyGhJ5ub3yjgH0EejTUQpjC\n"
        + "t2dPyKS97+2RJZE650VZDP37DVKOEdnf4OF1jmsoGQzxv33J8DoSGqNb1u4z4uXn\n"
        + "icbhDI6ZxM53xUW6Oseu290+rKPUUIeZrWYWk8+SrMeV3KZq01K+paKAjA7CqfmW\n"
        + "B7sO1mhiwRefIyj89NbXFZKMxMl95Th8A3aiONP0NtY=\n"
        + "-----END RSA PRIVATE KEY-----\n";
    private static String defaultPassphrase = "stupid";
    private static String defaultCertificate =
        "-----BEGIN CERTIFICATE-----\n"
        + "MIIDJDCCAs6gAwIBAgIBATANBgkqhkiG9w0BAQQFADCBkzELMAkGA1UEBhMCVVMx\n"
        + "CzAJBgNVBAgTAlVUMRYwFAYDVQQHEw1BbWVyaWNhbiBGb3JrMRkwFwYDVQQKExBJ\n"
        + "bnZpc2libGUgV29ybGRzMQ0wCwYDVQQLEwRVdGFoMREwDwYDVQQDEwhKYXkgS2lu\n"
        + "dDEiMCAGCSqGSIb3DQEJARYTamtpbnRAaW52aXNpYmxlLm5ldDAeFw0wMTA1MjIy\n"
        + "MjI2MzBaFw0wMjA1MjIyMjI2MzBaMH4xCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJV\n"
        + "VDEZMBcGA1UEChMQSW52aXNpYmxlIFdvcmxkczENMAsGA1UECxMEVXRhaDETMBEG\n"
        + "A1UEAxMKRXJpYyBEaXhvbjEjMCEGCSqGSIb3DQEJARYUZWRpeG9uQGludmlzaWJs\n"
        + "ZS5uZXQwXDANBgkqhkiG9w0BAQEFAANLADBIAkEAvlFYMlFSrVwYtQqClXow5Fln\n"
        + "ywGiddbtuKDmOYXVmkhMijiz5FJEE9Og+4hMHqkpY7ls2pgHAp2ojVk2mUc4MQID\n"
        + "AQABo4IBHzCCARswCQYDVR0TBAIwADAsBglghkgBhvhCAQ0EHxYdT3BlblNTTCBH\n"
        + "ZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYEFAsXsSPS9ygE+KOzXcDJhWWq\n"
        + "l+Q1MIHABgNVHSMEgbgwgbWAFDpO2dz71wbN86ypUTtDVJ16LvXooYGZpIGWMIGT\n"
        + "MQswCQYDVQQGEwJVUzELMAkGA1UECBMCVVQxFjAUBgNVBAcTDUFtZXJpY2FuIEZv\n"
        + "cmsxGTAXBgNVBAoTEEludmlzaWJsZSBXb3JsZHMxDTALBgNVBAsTBFV0YWgxETAP\n"
        + "BgNVBAMTCEpheSBLaW50MSIwIAYJKoZIhvcNAQkBFhNqa2ludEBpbnZpc2libGUu\n"
        + "bmV0ggEAMA0GCSqGSIb3DQEBBAUAA0EAWysxYjfYZK3QMTAaI/sKIZaPtwUaWhHp\n"
        + "KoWUMF6WZxe2iFwts0AoyLo6F7oMjvxNc2yNn4gi/WrPfZWkHncU3g==\n"
        + "-----END CERTIFICATE-----\n";
    private static String defaultRootCertificate =
        "-----BEGIN CERTIFICATE-----\n"
        + "MIIDDTCCAregAwIBAgIBADANBgkqhkiG9w0BAQQFADCBkzELMAkGA1UEBhMCVVMx\n"
        + "CzAJBgNVBAgTAlVUMRYwFAYDVQQHEw1BbWVyaWNhbiBGb3JrMRkwFwYDVQQKExBJ\n"
        + "bnZpc2libGUgV29ybGRzMQ0wCwYDVQQLEwRVdGFoMREwDwYDVQQDEwhKYXkgS2lu\n"
        + "dDEiMCAGCSqGSIb3DQEJARYTamtpbnRAaW52aXNpYmxlLm5ldDAeFw0wMTA1MjIy\n"
        + "MjIzMTZaFw0wMTA2MjEyMjIzMTZaMIGTMQswCQYDVQQGEwJVUzELMAkGA1UECBMC\n"
        + "VVQxFjAUBgNVBAcTDUFtZXJpY2FuIEZvcmsxGTAXBgNVBAoTEEludmlzaWJsZSBX\n"
        + "b3JsZHMxDTALBgNVBAsTBFV0YWgxETAPBgNVBAMTCEpheSBLaW50MSIwIAYJKoZI\n"
        + "hvcNAQkBFhNqa2ludEBpbnZpc2libGUubmV0MFwwDQYJKoZIhvcNAQEBBQADSwAw\n"
        + "SAJBANvFvkyq94iwrEvA4AprtJyfpznGfE2ibG5OrzeGWgZ1FqPsfhkU4qt0xbRL\n"
        + "Fdgb438SZCJ0bFWdK//P7Z1flI8CAwEAAaOB8zCB8DAdBgNVHQ4EFgQUOk7Z3PvX\n"
        + "Bs3zrKlRO0NUnXou9egwgcAGA1UdIwSBuDCBtYAUOk7Z3PvXBs3zrKlRO0NUnXou\n"
        + "9eihgZmkgZYwgZMxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJVVDEWMBQGA1UEBxMN\n"
        + "QW1lcmljYW4gRm9yazEZMBcGA1UEChMQSW52aXNpYmxlIFdvcmxkczENMAsGA1UE\n"
        + "CxMEVXRhaDERMA8GA1UEAxMISmF5IEtpbnQxIjAgBgkqhkiG9w0BCQEWE2praW50\n"
        + "QGludmlzaWJsZS5uZXSCAQAwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQQFAANB\n"
        + "AMmJY0I24Qx9RNi6GdF75hblGsmt+W4oBnlWv4WI7qDcSzeSO8M2xGom95mE1+Hu\n"
        + "czaRiitRTKis54e1d3h2HVs=\n" + "-----END CERTIFICATE-----\n";

    /**
     * TLS provides encryption and optionally authentication for a session
     * by opening a channel with this profile.  The default action is to
     * set up for a channel with encryption only, no authentication.  To
     * mandate authentication, set the configuration via <code>init</code>.<p>
     * @see org.beepcore.beep.profile.Profile
     */
    public TLSProfilePureTLS()
    {
        context = new SSLContext();
        policy = new SSLPolicyInt();
    }

    public boolean advertiseProfile(Session session,
                                    SessionTuningProperties tuning)
            throws BEEPException
    {
        return true;
    }

    /**
     * init sets the criteria for which an TLS connection is made when
     * a TLS channel is started for a profile.  It should only be
     * called once.  For the properties, the initiator is defined as
     * the peer who starts the channel for the TLS profile, the
     * listener is the peer that receives the the channel start
     * request, irregardless of which actually started the session.<p>
     *
     * @param uri used to start a channel with TLS protection
     * @param config used to specify the parameters for sessions
     * protected by this profile's version of TLS.  In other words, if
     * you want another set of paramters, you must either recall this
     * method or create another <code>TLSProfilePureTLS</code> and
     * call this method with a new configuration.
     *
     * The meaningful properties that can be set are:
     * <table>
     * <tr>
     * <td><i>Cipher Suite</i></td><td><code>short []</code>
     * corresponding to the <a
     * href="http://www.ietf.org/rfc/rfc2246.txt">TLS spec</a> ciphers
     * (Appendix A).  By default all the ciphers (except anonymous for
     * now) are available.  Use this to restrict to a certain strength
     * of cipher if you desire to do so.</td>
     * </tr>
     * <tr>
     * <td><i>Certificates</i></td><td>{@link List} that holds the X.509
     * format certificates that verify this peer, ordered from the users to
     * the root.</td>
     * </tr>
     * <tr>
     * <td><i>Private Key</i></td><td>{@link PrivateKey} that holds the private
     * key that correspond to the certificates.</td>
     * </tr>
     * <tr>
     * <td><i>Key Type</i></td><td>{@link String} that tells what algorithm
     * generated the private key.  "RSA" or "DSA" are the two accepted private
     * key formats.</td>
     * </tr>
     * <tr>
     * <td><i>Trusted Certificates</i></td><td>{@link List} that holds
     * all trusted (or root) certificates that we can verify a peer
     * against.</td>
     * </tr>
     * </table>
     */
    public StartChannelListener init(String uri, Hashtable config)
            throws BEEPException
    {
        boolean havePrivateKey = false;

        // set the URI for this instance of the profile
        this.uri = uri;

        // set the policy and contexts
        policy = new SSLPolicyInt();
        context = new SSLContext();

        policy.negotiateTLS(true);    // we don't support SSL v3

        // set whether or not peer must send a certificate
        if (config.get(PROPERTY_CLIENT_AUTH_REQUIRED) instanceof String &&
            Boolean.valueOf((String) config.get(PROPERTY_CLIENT_AUTH_REQUIRED )).booleanValue() == false) {
            policy.acceptUnverifiableCertificates(true);
            policy.checkCertificateDates(false);
            policy.requireClientAuth(false);

            needPeerAuth = false;
        } else {
            policy.acceptUnverifiableCertificates(false);
            policy.checkCertificateDates(true);
            policy.requireClientAuth(true);

            needPeerAuth = true;
        }

        context.setPolicy(policy);

        // set the cipher suites
        if (config.get(PROPERTY_CIPHER_SUITE) != null) {
            try {
                short[] ciphers = (short[]) config.get(PROPERTY_CIPHER_SUITE);

                policy.setCipherSuites(ciphers);
            } catch (Exception e) {
                throw new BEEPException(e);
            }
        }

        // @todo support the anonymous cipher suites
        // for now, we don't support the anonymous cipher suites, meaning
        // the user must supply a private key, certificate(s), and trusted
        // certificate.
        if ((config.get(PROPERTY_PRIVATE_KEY) == null)
                || (config.get(PROPERTY_PRIVATE_KEY_ALGORITHM) == null)
                || (config.get(PROPERTY_CERTIFICATES) == null)
                || (config.get(PROPERTY_TRUSTED_CERTS) == null)) {
            throw new BEEPException("Must have a private key and " +
                                    "certificates with root certificates " +
                                    "that match the key's algorithm");
        }

        // store the private key
        PrivateKey key = (PrivateKey) config.get(PROPERTY_PRIVATE_KEY);

        PureTLSPackageBridge.setPrivateKey(context, key);

        // store the certificates
        if (!(config.get(PROPERTY_CERTIFICATES) instanceof List)) {
            throw new BEEPException("X.509 Certificates should be in a List " +
                                    "or subclass");
        }

        // iterate the list and put the certificates into the policy
        List certList = (List) config.get(PROPERTY_CERTIFICATES);
        Iterator i = certList.iterator();

        PureTLSPackageBridge.initCertificates(context);

        try {
            while (i.hasNext()) {
                byte[] c = (byte[]) i.next();

                if (c != null) {
                    PureTLSPackageBridge.addCertificate(context, c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            throw new BEEPException(e.getMessage());
        }

        // store the root certificates
        if (!(config.get(PROPERTY_TRUSTED_CERTS) instanceof List)) {
            throw new BEEPException("Must have trusted root certificates.");
        }

        List rootList = (List) config.get(PROPERTY_TRUSTED_CERTS);

        i = rootList.iterator();

        PureTLSPackageBridge.initRootCertificates(context);

        try {
            while (i.hasNext()) {
                byte[] c = (byte[]) i.next();

                PureTLSPackageBridge.addRootCertificate(context, c);
            }
        } catch (Exception e) {
            throw new BEEPException("Trusted (root) certificates must be in " +
                                    "DRE format contained in byte[]");
        }

        // return ourselves as the start channel listener
        return this;
    }

    /**
     * Called when the underlying BEEP framework receives
     * a "start" element for the TLS profile.<p>
     *
     * @param channel A <code>Channel</code> object which represents a channel
     * in this <code>Session</code>.
     * @param data The content of the "profile" element selected for this
     * channel (must be <code>&ltready /&gt</code>).
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

    /// @TODO Fix error handling in this method
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
        } catch (BEEPException e) {
            log.error("TLS Error", e);
            channel.getSession().terminate("unable to send ERR");
            return;
        }

        TCPSession oldSession = (TCPSession) channel.getSession();

        SSLSocket newSocket = null;
        SessionCredential peerCred = null;
        try {
            // negotiate TLS over a new socket
            context.setPolicy(policy);

            Socket oldSocket = oldSession.getSocket();
            newSocket =
                new SSLSocket(context, oldSocket.getInputStream(),
                              oldSocket.getOutputStream(),
                              oldSocket.getInetAddress().getHostName(),
                              oldSocket.getPort(), SSLSocket.SERVER);
        } catch (SSLThrewAlertException e) {
            log.error("TLS Error", e);
            channel.getSession().terminate(e.getMessage());
            return;
        } catch (IOException e) {
            log.error("TLS Error", e);
            channel.getSession().terminate(e.getMessage());
            return;
        }

        // get the credentials of the peer
        Vector cc = null;
        int cs;
            
        try {
            if (needPeerAuth) {
                cc = newSocket.getCertificateChain();
                if (cc == null) {
                    log.trace("No certificate chain when there should be one.");
                    msg.sendERR(BEEPError.CODE_REQUESTED_ACTION_NOT_TAKEN2,
                                "No certificate chain when there " +
                                                    "should be one. ");
                    return;
                }

                Enumeration enum = cc.elements();
                while (enum.hasMoreElements()) {
                    X509Cert cert = (X509Cert) enum.nextElement();
                    String subject = cert.getSubjectName().getNameString();
                    String issuer = cert.getIssuerName().getNameString();
                    if (log.isTraceEnabled()) {
                        log.trace("Name = " + subject + " issued by " + issuer);
                    }
                }
            } else {
                log.trace("No peer authentication needed");
            }

            cs = newSocket.getCipherSuite();
        } catch (BEEPException e) {
            log.error("TLS Error", e);
            channel.getSession().terminate("unable to send ERR");
            return;
        } catch (IOException e) {
            log.error("TLS Error", e);
            channel.getSession().terminate(e.getMessage());
            return;
        }

        try {
            // verify that this is authenticated and authorized
            if (handshakeListener != null) {

                // default of just putting the certificate and 
                handshakeListener.handshakeCompleted(oldSession, cc, cs);
            }
        } catch (BEEPException e) {
            log.error("BEEP Handshake error", e);
            channel.getSession().terminate("BEEP Handshake error");
            return;
        }

        // create the peer credential
        Hashtable ht = new Hashtable();

        ht.put(SessionCredential.AUTHENTICATOR, URI);
        ht.put(SessionCredential.ALGORITHM,
               SSLPolicyInt.getCipherSuiteName(cs));
        ht.put(SessionCredential.AUTHENTICATOR_TYPE, "TLS");

        if (cc != null) {
            ht.put(SessionCredential.REMOTE_CERTIFICATE, cc.elementAt(0));
        }

        peerCred = new SessionCredential(ht);

        // Cause the session to be recreated and reset
        Hashtable hash = new Hashtable();

        hash.put(SessionTuningProperties.ENCRYPTION, "true");

        SessionTuningProperties tuning =
            new SessionTuningProperties(hash);

        // Consider the Profile Registry
        ProfileRegistry preg = oldSession.getProfileRegistry();

        preg.removeStartChannelListener(URI);

        try {
            this.complete(channel, generateCredential(), peerCred,
                          tuning, preg, newSocket);
        } catch (BEEPException x) {
            BEEPError error =
                new BEEPError(BEEPError.CODE_REQUESTED_ACTION_ABORTED,
                              ERR_TLS_NO_AUTHENTICATION);
            abort(error, channel);
        }
    }
    

    /**
     * Called when the underlying BEEP framework receives
     * a "close" element.<p>
     *
     * As of now, it is not possible to close a TLS channel.  To cease
     * using TLS, the entire session must be closed.  This is done
     * since opening a TLS channel resets the entire session,
     * effectively closing all the previously open channels, including
     * channel 0 (hence the greetings are exchanged again).
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
     * and authentication are specified using the profile
     * configuration passed to the <code>init</code> method Upon
     * returning, all traffic over the session will be entrusted as
     * per these parameters.<p>
     *
     * @see #init profile configuration
     * @param session the session to encrypt communcation for
     *
     * @return new {@link TCPSession} with TLS negotiated.
     * @throws BEEPException an error occurs during the channel start
     * request or the TLS handshake (such as trying to negotiate an
     * anonymous connection with a peer that doesn't support an
     * anonymous cipher suite).
     */
    public TCPSession startTLS(TCPSession session) throws BEEPException
    {
        Channel ch = startChannel(session, TLSProfile.URI, false, READY2,
                                  null);

        // See if we got start data back
        String data = ch.getStartData();

        // Consider the data (see if it's proceed)
        if ((data == null)
                || (!data.equals(PROCEED1) &&!data.equals(PROCEED2))) {
            throw new BEEPException(ERR_EXPECTED_PROCEED);
        }

        log.debug("Staring TLS channel.");

        // Freeze IO and get the socket and reset it to TLS
        Socket oldSocket = session.getSocket();
        SSLSocket newSocket = null;
        SessionCredential peerCred = null;

        // create the SSL Socket
        try {

            // set the parameters
            context.setPolicy(policy);

            // create the socket and start the handshake
            newSocket =
                new SSLSocket(context, oldSocket.getInputStream(),
                              oldSocket.getOutputStream(),
                              oldSocket.getInetAddress().getHostName(),
                              oldSocket.getPort(), SSLSocket.CLIENT);

        } catch (SSLThrewAlertException e) {
            session.terminate(e.getMessage());
            throw new BEEPException(e);
        } catch (IOException e) {
            session.terminate(e.getMessage());
            throw new BEEPException(e);
        }

        try {
            // get the credentials of the peer
            Vector cc = null;

            if (needPeerAuth) {
                cc = newSocket.getCertificateChain();
                if (cc == null) {
                    log.trace("No certificate chain when there should be one.");
                    throw new BEEPException("No certificate chain when " +
                                            "there should be one. ");
                }
                Enumeration enum = cc.elements();
                while (enum.hasMoreElements()) {
                    X509Cert cert = (X509Cert) enum.nextElement();
                    String subject = cert.getSubjectName().getNameString();
                    String issuer = cert.getIssuerName().getNameString();
                    if (log.isTraceEnabled()) {
                        log.trace("Name = " + subject + " issued by " + issuer);
                    }
                }
            } else {
                log.trace("No peer authentication needed");
            }

            int cs = newSocket.getCipherSuite();

            // verify that this is authenticated and authorized
            if (handshakeListener != null) {

                // default of just putting the certificate and 
                handshakeListener.handshakeCompleted(session, cc, cs);
            }

            // create the peer credential
            Hashtable ht = new Hashtable();

            ht.put(SessionCredential.AUTHENTICATOR, URI);
            ht.put(SessionCredential.ALGORITHM,
                   SSLPolicyInt.getCipherSuiteName(cs));
            ht.put(SessionCredential.AUTHENTICATOR_TYPE, "TLS");

            if (cc != null) {
                ht.put(SessionCredential.REMOTE_CERTIFICATE, cc.elementAt(0));
            }

            peerCred = new SessionCredential(ht);
        } catch (Exception e) {
            throw new BEEPException(e);
        } finally {}

        // swap it out for the new one with TLS enabled.
        if (abortSession) {
            session.close();

            throw new BEEPException(ERR_TLS_NO_AUTHENTICATION);
        } else {
            Hashtable hash = new Hashtable();

            hash.put(SessionTuningProperties.ENCRYPTION, "true");

            SessionTuningProperties tuning =
                new SessionTuningProperties(hash);

            return (TCPSession) reset(session, generateCredential(),
                                      peerCred, tuning,
                                      session.getProfileRegistry(),
                                      newSocket);
        }
    }

    /**
     * return the default credentials for the new session to use after a TLS
     * negotiation is complete.
     *
     * @return default SessionCredential that can be added to
     */
    public static SessionCredential generateCredential()
    {
        Hashtable ht = new Hashtable(4);

        ht.put(SessionCredential.AUTHENTICATOR, URI);

        return new SessionCredential(ht);
    }

    /**
     * set a listener for completed handshakes.
     * @param x is called when a TLS handshake completes.
     */
    public void setHandshakeCompletedListener(TLSProfilePureTLSHandshakeCompletedListener x)
    {
        handshakeListener = x;
    }

    /**
     * return the maximum number of ciphers that can be set.
     */
    public int getMaxCiphersKnown()
    {
        return MAX_CIPHERS;
    }

    // set methods for initializing

    /**
     * allows an initializer class to set the private key for the profile.
     * The initializers are profile classes with a custom {@link init} method
     * that takes the private key from a given source, such as a file or
     * database, converts it to a {@link PrivateKey}, and calls this method.
     * @param PrivateKey
     */
    void setPrivateKey(PrivateKey key) throws BEEPException
    {
        try {
            PureTLSPackageBridge.setPrivateKey(context, key);
        } catch (Exception e) {
            throw new BEEPException(e);
        }
    }

    /**
     * allows an initializer class to set the certificate chain for the profile.
     * The initializers are profile classes with a custom {@link init} method
     * that takes the certificate chain (a {@link List} of byte[], each being
     * the DER format for an X.509 certificate) from a given source, such as a
     * file or database and calls this method.
     *
     * @param certs
     */
    void setCertChain(List certs) throws BEEPException
    {
        if (certs != null) {
            Iterator i = certs.iterator();

            PureTLSPackageBridge.initCertificates(context);

            try {
                while (i.hasNext()) {
                    byte[] c = (byte[]) i.next();

                    if (c != null) {
                        PureTLSPackageBridge.addCertificate(context, c);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

                throw new BEEPException(e);
            }
        }
    }

    /**
     * allows an initializer class to set the trusted certificates for
     * the profile.  The initializers are profile classes with a
     * custom {@link init} method that takes the certificates (a
     * {@link List} of byte[], each being the DER format for an X.509
     * certificate) from a given source, such as a file or database
     * and calls this method.
     *
     * @param certs
     */
    void setRootCerts(List certs) throws BEEPException
    {
        if (certs != null) {
            Iterator i = certs.iterator();

            PureTLSPackageBridge.initRootCertificates(context);

            try {
                while (i.hasNext()) {
                    byte[] c = (byte[]) i.next();

                    if (c != null) {
                        PureTLSPackageBridge.addRootCertificate(context, c);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

                throw new BEEPException(e);
            }
        }
    }

    /**
     * allows an initializer class to set the allowed ciphers for the
     * profile.  The initializers are profile classes with a custom
     * {@link init} method that takes the array of ciphers as a
     * <code>short []</code> from a given source, such as a file or
     * database and calls this method.  The numbers in the array for
     * the ciphers are defined in the
     * <a href="http://www.ietf.org/rfc/rfc2246.txt">TLS spec</a> in
     * Appendix A.
     * @param ciphers
     */
    void setCipherSuite(short[] ciphers) throws BEEPException
    {
        try {

            // verify the ciphers
            for (int i = 0; i < ciphers.length; i++) {
                if ((ciphers[i] > MAX_CIPHERS) || (ciphers[i] < 0)) {
                    throw new BEEPException("Invalid cipher at " + i);
                }
            }

            policy.setCipherSuites(ciphers);
        } catch (Exception e) {
            throw new BEEPException(e);
        }
    }

    /**
     * sets whether or not the the peer we're talking to must be authenticated
     * @param needAuth
     */
    void setNeedPeerAuthentication( boolean needAuth ) {

        if (needAuth == false) {
            policy.acceptUnverifiableCertificates(true);
            policy.checkCertificateDates(false);
            policy.requireClientAuth(false);

            needPeerAuth = false;
        } else {
            policy.acceptUnverifiableCertificates(false);
            policy.checkCertificateDates(true);
            policy.requireClientAuth(true);

            needPeerAuth = true;
        }
    }
}
