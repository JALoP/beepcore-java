/*
 * ProfileRegistry.java  $Revision: 1.2 $ $Date: 2001/05/27 23:49:09 $
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
package org.beepcore.beep.core;


import java.util.Enumeration;
import java.util.Hashtable;


/**
 * This class is used to save pointers in Sessions - if the peer is using
 * the same set of profile and associated StartChannelListener for all
 * sessions then they should create one of these helpers and use them in
 * the corresponding factory methods.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision, $Date: 2001/05/27 23:49:09 $
 */
public class ProfileRegistry implements Cloneable {

    private static final String SPACE = " ";

    private static final String FRAGMENT_ANGLE_SUFFIX = ">";
    private static final String FRAGMENT_FEATURES_PREFIX = "features='";
    private static final String FRAGMENT_GREETING_PREFIX = "<greeting";
    private static final String FRAGMENT_GREETING_SUFFIX = "</greeting>";
    private static final String FRAGMENT_LOCALIZE_PREFIX = "localize='";
    private static final String FRAGMENT_PROFILE_PREFIX = "<profile ";
    private static final String FRAGMENT_QUOTE_ANGLE_SUFFIX = "'>";
    private static final String FRAGMENT_QUOTE_SLASH_ANGLE_SUFFIX = "' />";
    private static final String FRAGMENT_QUOTE_SUFFIX = "' ";
    private static final String FRAGMENT_URI_PREFIX = "uri='";
    private static final int FRAGMENT_GREETING_LENGTH =
        FRAGMENT_GREETING_PREFIX.length()
        + FRAGMENT_QUOTE_ANGLE_SUFFIX.length()
        + FRAGMENT_GREETING_SUFFIX.length();
    private static final int FRAGMENT_PROFILE_LENGTH =
        FRAGMENT_PROFILE_PREFIX.length()
        + FRAGMENT_QUOTE_ANGLE_SUFFIX.length();

    // Instance Data
    private Hashtable profileListeners;
    String greeting;
    String localize;
    String features;

    // Constructors

    /**
     * Constructor ProfileRegistry
     *
     *
     */
    public ProfileRegistry()
    {
        this.greeting = null;
        this.features = null;
        this.localize = Constants.LOCALIZE_DEFAULT;
        this.profileListeners = new Hashtable();
    }

    private ProfileRegistry(String greeting, String localize, String features,
                            Hashtable profiles)
    {
        this.greeting = greeting;
        this.features = features;
        this.localize = localize;
        this.profileListeners = profiles;
    }

    public Object clone()
    {
        return new ProfileRegistry(this.greeting, this.localize, this.features,
                                   (Hashtable) this.profileListeners.clone());
    }

    /**
     * Method getProfiles
     *
     *
     * @return
     *
     */
    public Enumeration getProfiles()
    {
        return profileListeners.keys();
    }

    /**
     * Returns the <code>StartChannelListener</code> for the specified URI.
     *
     *
     * @param uri
     *
     */
    public StartChannelListener getStartChannelListener(String uri)
    {
        return (StartChannelListener) profileListeners.get(uri);
    }

    /**
     * Add the <code>StartChannelListener</code> for the specified URI.
     *
     *
     * @param profile
     * @param listener
     *
     * @return the previously registered <code>StartChannelListener</code>
     *
     */
    public synchronized StartChannelListener addStartChannelListener(String profile,
            StartChannelListener listener)
    {

        // Replace semantics - change this if we want to prevent clobbering.
        StartChannelListener temp =
            (StartChannelListener) profileListeners.get(profile);

        profileListeners.put(profile, listener);
        changeGreeting();

        return temp;
    }

    /**
     * Remove the <code>StartChannelListener</code> for the specified uri.
     *
     *
     * @param profile
     *
     * @return <code>StartChannelListener</code> registered for the specified
     *         uri.
     *
     */
    public synchronized StartChannelListener removeStartChannelListener(String profile)
    {
        StartChannelListener temp =
            (StartChannelListener) profileListeners.remove(profile);

        changeGreeting();

        return temp;
    }

    byte[] getGreeting()
    {
        if (greeting == null) {
            changeGreeting();
        }

        return greeting.getBytes();
    }

    /**
     * Set values for the BEEP greeting localize attribute.
     *
     *
     * @param localize
     *
     */
    public void setLocalization(String localize)
    {
        this.localize = localize;
    }

    /**
     * Returns the value for the BEEP greeting localize attribute.
     *
     *
     */
    public String getLocalization()
    {
        return this.localize;
    }

    private void changeGreeting()
    {
        int bufferSize = FRAGMENT_GREETING_LENGTH;
        int profileCount = 0;

        profileCount = profileListeners.size();

        Enumeration e = profileListeners.keys();

        while (e.hasMoreElements()) {
            bufferSize += ((String) e.nextElement()).length()
                          + FRAGMENT_PROFILE_LENGTH;
        }

        bufferSize++;

        StringBuffer sb = new StringBuffer(bufferSize);

        // Create Greeting
        // Wish I could reset these.
        Enumeration f = profileListeners.keys();

        sb.append(FRAGMENT_GREETING_PREFIX);

        if ((localize != null)
                &&!localize.equals(Constants.LOCALIZE_DEFAULT)) {
            sb.append(this.SPACE);
            sb.append(FRAGMENT_LOCALIZE_PREFIX);
            sb.append(localize);
            sb.append(FRAGMENT_QUOTE_SUFFIX);
        }

        if (features != null) {
            sb.append(FRAGMENT_FEATURES_PREFIX);
            sb.append(features);
            sb.append(FRAGMENT_QUOTE_SUFFIX);
        }

        sb.append(FRAGMENT_ANGLE_SUFFIX);

        while (f.hasMoreElements()) {
            sb.append(FRAGMENT_PROFILE_PREFIX);
            sb.append(FRAGMENT_URI_PREFIX);
            sb.append((String) f.nextElement());
            sb.append(FRAGMENT_QUOTE_SLASH_ANGLE_SUFFIX);
        }

        sb.append(FRAGMENT_GREETING_SUFFIX);

        greeting = sb.toString();
    }
}
