/*
 * SessionCredential.java  $Revision: 1.4 $ $Date: 2001/11/08 05:51:34 $
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


import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Class SessionCredential
 *
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.4 $, $Date: 2001/11/08 05:51:34 $
 */
public class SessionCredential {

    // Constants
    public final static String ALGORITHM = "ALGORITHM";
    public final static String AUTHENTICATOR = "AUTHENTICATOR";
    public final static String AUTHORIZED = "AUTHORIZED";
    public final static String AUTHENTICATOR_TYPE = "AUTHENTICATOR_TYPE";
    public final static String LOCAL_CERTIFICATE = "LOCAL_CERTIFICATE";
    public final static String NO_CREDENTIALS = "Peer has no credentials.";
    public final static String REMOTE_ADDRESS = "REMOTE_ADDRESS";
    public final static String REMOTE_CERTIFICATE = "REMOTE_CERTIFICATE";

    // Pretty Print Crap
    public static final String LEFT = "[";
    public static final String RIGHT = "]";
    public static final String MIDDLE = "=>";
    public static final String NEWLINE = "\n";

    // Data
    private Hashtable properties;

    /**
     * Constructor SessionCredential
     *
     *
     */
    public SessionCredential()
    {
        properties = new Hashtable();
    }

    /**
     * Constructor SessionCredential
     *
     *
     * @param props
     *
     */
    public SessionCredential(Hashtable props)
    {
        properties = props;
    }

    /**
     * Method getAlgorithm
     *
     *
     */
    public String getAlgorithm()
    {
        return (String) properties.get(ALGORITHM);
    }

    /**
     * Method getAuthenticator
     *
     *
     */
    public String getAuthenticator()
    {
        return (String) properties.get(AUTHENTICATOR);
    }

    /**
     * Method getAuthorized
     *
     *
     */
    public String getAuthorized()
    {
        return (String) properties.get(AUTHORIZED);
    }

    /**
     * Method getAuthenticatorType
     *
     *
     */
    public String getAuthenticatorType()
    {
        return (String) properties.get(AUTHENTICATOR_TYPE);
    }

    /**
     * Method getLocalCertificate
     *
     *
     */
    public Object getLocalCertificate()
    {
        return properties.get(LOCAL_CERTIFICATE);
    }

    /**
     * Method getRemoteAddress
     *
     *
     */
    public String getRemoteAddress()
    {
        return (String) properties.get(REMOTE_ADDRESS);
    }

    /**
     * Method getRemoteCertificate
     *
     *
     */
    public Object getRemoteCertificate()
    {
        return properties.get(REMOTE_CERTIFICATE);
    }

    /**
     * Method toString
     *
     *
     */
    public String toString()
    {
        StringBuffer b = new StringBuffer(1024);
        String key, value;
        int i = 0;

        if (properties.size() == 0) {
            return NO_CREDENTIALS;
        }

        Enumeration e = properties.keys();

        while (e.hasMoreElements()) {
            key = (String) e.nextElement();
            value = (String) properties.get(key);

            b.append(LEFT);
            b.append(i);
            b.append(RIGHT);
            b.append(key);
            b.append(MIDDLE);
            b.append(value);
            b.append(NEWLINE);
        }

        return b.toString();
    }
}
