/*
 * TLSProfilePureTLSPemInit.java  $Revision: 1.3 $ $Date: 2001/11/08 05:51:35 $
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
import org.beepcore.beep.profile.*;
import org.beepcore.beep.profile.tls.TLSProfile;

import java.security.PrivateKey;

import java.util.*;

import java.io.*;

import COM.claymoresystems.ptls.*;
import COM.claymoresystems.sslg.*;
import COM.claymoresystems.cert.*;
import COM.claymoresystems.crypto.*;


/**
 * An initialiser class that takes a specific initialisation sequence and
 * creates a TLSProfilePureTLS and returns it.  This one takes as parameters file
 * names for PEM files (base64 encoded files with BEGIN/END delimiters).
 *
 * An initialiser class for TLS is one that takes the necessary data,
 * a private key, certificate chain, and trusted certificates, in a
 * certain format and translates them to their raw formats and
 * initialises a TLSProfilePureTLS instance with them. {@link
 * TLSProfilePureTLS} is designed to be flexible and not require any
 * specific configuration to encrypt a session with TLS.
 */
public class TLSProfilePureTLSPemInit implements Profile {

    // property names
    //      public static final String PROPERTY_PEER_AUTHENTICATION_REQUIRED = "Peer Authentication Required";

    /**
     * @see #init
     */
    public static final String PROPERTY_CLIENT_AUTH_REQUIRED = 
        "Client Authenticaton Required";
    public static final String PROPERTY_CIPHER_SUITE = "Cipher Suite";
    public static final String PROPERTY_CERTIFICATES = "Certificates";
    public static final String PROPERTY_PRIVATE_KEY = "Private Key";
    public static final String PROPERTY_PRIVATE_KEY_PASSPHRASE =
        "Private Key Passphrase";
    public static final String PROPERTY_PRIVATE_KEY_TYPE = "Private Key Type";
    public static final String PROPERTY_TRUSTED_CERTS =
        "Trusted Certificates";

    /**
     * init sets the criteria for which an TLS connection is made when
     * a TLS channel is started for a profile.  It should only be
     * called once.  For the properties, the initiator is defined as
     * the peer who starts the channel for the TLS profile, the
     * listener is the peer that receives the the channel start
     * request, irregardless of which actually started the session.<p>
     *
     * @param uri used to start a channel with TLS protection
     * @param config used to specify the parameters for sessions protected
     * by this profile's version of TLS.  In other words, if you want another
     * set of paramters, you must either recall this method or create another
     * <code>TLSProfilePureTLSPemInit</code> and call this method with a new
     * configuration.  Note: All different parameters may be in the same PEM file.
     * The meaningful properties that can be set are:
     * <table>
     * <tr>
     * <td><i>Cipher Suite</i></td><td>List of cipher names (comma separated)
     * to accept.  Cipher names are formatted as per Appendix A in the TLS spec.
     * By default all the ciphers (except anonymous for now) are available.  Use this
     * to restrict to a certain strength of cipher if you desire to do so.</td>
     * </tr>
     * <tr>
     * <td><i>Certificates</i></td><td>Name of the PEM file that contains the
     * certificates to present.  These are in order from the user's certificate
     * to the root certificate.</td>
     * </tr>
     * <tr>
     * <td><i>Private Key</i></td><td>Name of the PEM file that contains the
     * encrypted private key to use.</td>
     * </tr>
     * <tr>
     * <td><i>Private Key Passphrase</i></td><td>{@link String} passphrase used to
     * encrypt the private key in its file.</td>
     * </tr>
     * <tr>
     * <td><i>Private Key Type</i></td><td>"RSA" or "DSA" are the two accepted private key formats.</td>
     * </tr>
     * <tr>
     * <td><i>Trusted Certificates</i></td><td>Name of the PEM file that contains
     * the root certificates used to verify a peer's identity.</td>
     * </tr>
     * </table>
     */
    public StartChannelListener init(String uri, ProfileConfiguration config)
            throws BEEPException
    {
        TLSProfilePureTLS tlsp = new TLSProfilePureTLS();

        // set whether or not peer must send a certificate
        if (config.get(PROPERTY_CLIENT_AUTH_REQUIRED) != null) {
            if (new Boolean((String) config.get(PROPERTY_CLIENT_AUTH_REQUIRED)).booleanValue() == true) {
                tlsp.setNeedPeerAuthentication(true);
            } else {
                tlsp.setNeedPeerAuthentication(false);
            }
        }
        // set the cipher suites
        if (config.get(PROPERTY_CIPHER_SUITE) != null) {

            // parse the cipher names
            int fromIndex = 0;
            String cipherNames = (String) config.get(PROPERTY_CIPHER_SUITE);
            short cipherTemp[] = new short[TLSProfilePureTLS.MAX_CIPHERS];
            int cipherCount = 0;
            int toIndex = cipherNames.indexOf(',', fromIndex);
            String cipherName;

            while (toIndex != -1) {
                cipherName = cipherNames.substring(fromIndex, toIndex);
                cipherTemp[cipherCount] =
                    (short) SSLPolicyInt.getCipherSuiteNumber(cipherName);

                if (cipherTemp[cipherCount] == -1) {
                    throw new BEEPException("Unknown cipher suite "
                                            + cipherName);
                }

                cipherCount++;

                fromIndex = toIndex + 1;
                toIndex = cipherNames.indexOf(',', fromIndex);
            }

            // catch the last one
            cipherName = cipherNames.substring(fromIndex);
            cipherTemp[cipherCount] =
                (short) SSLPolicyInt.getCipherSuiteNumber(cipherName);

            if (cipherTemp[cipherCount] == -1) {
                throw new BEEPException("Unknown cipher suite " + cipherName);
            }

            short[] ciphers = new short[cipherCount];

            System.arraycopy(cipherTemp, 0, ciphers, 0, cipherCount);

            try {
                tlsp.setCipherSuite(ciphers);
            } catch (Exception e) {
                throw new BEEPException(e.getMessage());
            }
        }

        // for now, we don't support the anonymous cipher suites, meaning
        // the user must supply a private key, certificate(s), and trusted
        // certificate.
        if ((config.get(PROPERTY_PRIVATE_KEY) == null)
                || (config.get(PROPERTY_PRIVATE_KEY_TYPE) == null)
                || (config.get(PROPERTY_CERTIFICATES) == null)
                || (config.get(PROPERTY_TRUSTED_CERTS) == null)) {
            throw new BEEPException("Must have a private key and certificates with root certificates that match the key's algorithm");
        }

        // set the private key to use in encrypting/decrypting either the
        // pre_master_secret (server) or encrypting the Certificate Verify 
        // (client)
        try {
            // set the certificate(s) by which we are known.  We can
            // actually verify clients with several root certificates,
            // but this is the certificates that we present according
            // to the negotiated cipher suite.  We assume that the
            // peer has a root that is in common with us.
            String certFile = (String) config.get(PROPERTY_CERTIFICATES);
            BufferedReader certbr =
                new BufferedReader(new FileReader(certFile));
            StringBuffer certType = new StringBuffer();
            Vector certs = new Vector();

            while (true) {
                byte[] cert = WrappedObject.loadObject(certbr, "CERTIFICATE",
                                                       certType);

                if (cert == null) {
                    break;
                }

                certs.add(cert);
            }

            tlsp.setCertChain(certs);

            String keyFile = (String) config.get(PROPERTY_PRIVATE_KEY);
            BufferedReader keybr =
                new BufferedReader(new FileReader(keyFile));
            String keyType = (String) config.get(PROPERTY_PRIVATE_KEY_TYPE);
            String passphrase =
                (String) config.get(PROPERTY_PRIVATE_KEY_PASSPHRASE);
            StringBuffer actualKeyType = new StringBuffer();

            if (!WrappedObject.findObject(keybr, "PRIVATE KEY",
                                          actualKeyType)) {
                throw new BEEPException("Private Key not found in "
                                        + PROPERTY_PRIVATE_KEY);
            }

            if (!actualKeyType.toString().equals(keyType)) {
                throw new BEEPException("Private key types differ.  Looking for "
                                        + keyType + " and found "
                                        + actualKeyType.toString());
            }

            PrivateKey key =
                EAYEncryptedPrivateKey.createPrivateKey(keybr, keyType,
                                                        passphrase.getBytes());

            tlsp.setPrivateKey(key);

            // verify that the object passed in is either a list or a String
            certFile = (String) config.get(PROPERTY_TRUSTED_CERTS);
            certbr = new BufferedReader(new FileReader(certFile));
            certType = new StringBuffer();

            Vector rootCerts = new Vector();

            while (true) {
                byte[] cert = WrappedObject.loadObject(certbr, "CERTIFICATE",
                                                       certType);

                if (cert == null) {
                    break;
                }

                rootCerts.add(cert);
            }

            tlsp.setRootCerts(rootCerts);
        } catch (Exception e) {
            throw new BEEPException(e.getMessage());
        }

        // return the TLSProfilePureTLS as the start channel listener
        return tlsp;
    }
}
