/*
 * ProfileRegistry.java  $Revision: 1.10 $ $Date: 2001/11/27 17:37:22 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2001 Huston Franklin.  All rights reserved.
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

import org.beepcore.beep.util.Log;
import org.beepcore.beep.util.StringUtil;


/**
 * Maintains a set of associations between URIs and
 * <code>StartChannelListener</code>s. This set is used to generate
 * the <code>greeting</code> and to demux <code>start</code> requests.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.10 $, $Date: 2001/11/27 17:37:22 $
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
    private class InternalProfile {
        StartChannelListener listener;
        SessionTuningProperties tuning;
    }

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

    private ProfileRegistry(String greeting, String localize,
                            String features, Hashtable profiles)
    {
        this.greeting = greeting;
        this.features = features;
        this.localize = localize;
        this.profileListeners = profiles;
    }

    public Object clone()
    {
        return new ProfileRegistry(this.greeting, this.localize,
                                   this.features,
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
    public StartChannelListener
        getStartChannelListener(SessionTuningProperties tuning,
                                String uri)
    {

        InternalProfile profile = (InternalProfile) profileListeners.get(uri);

        if (profile == null) {
            return null;
        }

        // if there are no qualifications, then just return the listener
        if (profile.tuning == null || profile.tuning.isEmpty()) {
            return ((InternalProfile) profileListeners.get(uri)).listener;
        }

        // so the profile requires something, but if the session doesn't
        // have anything, then return null
        if (tuning == null) {
            Log.logEntry(Log.SEV_DEBUG,
                         "Session does not have any tuning properties");
            return null;
        }

        // if the profile requires any of the standard properties, then
        // make sure they are set on the session before returning the listener
        int i = 0;

        for (i = 0; i < SessionTuningProperties.STANDARD_PROPERTIES.length;
                i++)
        {
            if ((profile.tuning.getProperty(SessionTuningProperties.STANDARD_PROPERTIES[i]) != null)
                    && (tuning.getProperty(SessionTuningProperties.STANDARD_PROPERTIES[i])
                        == null))
            {
                Log.logEntry(Log.SEV_DEBUG,
                             "Session does not have tuning property " +
                             SessionTuningProperties.STANDARD_PROPERTIES[i]);
                return null;
            }
        }

        // all the ones the profile requested must be there so we return the
        // listener
        return ((InternalProfile) profileListeners.get(uri)).listener;
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
    public synchronized StartChannelListener
        addStartChannelListener(String profile,
                                StartChannelListener listener,
                                SessionTuningProperties tuning)
    {

        // Replace semantics - change this if we want to prevent clobbering.
        StartChannelListener temp = null;

        if (profileListeners.get(profile) != null) {
            temp = ((InternalProfile) profileListeners.get(profile)).listener;
        }

        InternalProfile tempProfile = new InternalProfile();

        tempProfile.listener = listener;

        tempProfile.tuning = tuning;

        profileListeners.put(profile, tempProfile);

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
    public synchronized StartChannelListener
        removeStartChannelListener(String profile)
    {
        InternalProfile temp =
            (InternalProfile) profileListeners.remove(profile);

        return temp.listener;
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

    byte[] getGreeting(Session session)
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

            // make sure this profile wants to be advertised
            try {
                String profileName = (String) f.nextElement();
                InternalProfile profile =
                    (InternalProfile) profileListeners.get(profileName);
                boolean callAdvertise = false;
                SessionTuningProperties sessionTuning =
                    session.getTuningProperties();

                // check the standard tuning settings first
                for (int i = 0;
                        i < SessionTuningProperties.STANDARD_PROPERTIES.length;
                        i++) {

                    if ((profile.tuning != null) && (sessionTuning != null) &&
                        (profile.tuning.getProperty(SessionTuningProperties.STANDARD_PROPERTIES[i]) != null) &&
                        (sessionTuning.getProperty(SessionTuningProperties.STANDARD_PROPERTIES[i]) != null))
                    {
                        callAdvertise = true;
                    }
                }

                if ((profile.tuning == null) ||
                    (callAdvertise &&
                     profile.listener.advertiseProfile(session)))
                {
                    sb.append(FRAGMENT_PROFILE_PREFIX);
                    sb.append(FRAGMENT_URI_PREFIX);
                    sb.append((String) profileName);
                    sb.append(FRAGMENT_QUOTE_SLASH_ANGLE_SUFFIX);
                }
            } catch (BEEPException x) {
                x.printStackTrace();

                continue;
            }
        }

        sb.append(FRAGMENT_GREETING_SUFFIX);

        return StringUtil.stringBufferToAscii(sb);
    }
}
