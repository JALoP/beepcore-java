/*
 * ProfileRegistry.java  $Revision: 1.14 $ $Date: 2003/11/07 17:38:11 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2001,2002 Huston Franklin.  All rights reserved.
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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Enumeration;
import java.util.Hashtable;

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
 * @version $Revision: 1.14 $, $Date: 2003/11/07 17:38:11 $
 */
public class ProfileRegistry implements Cloneable {

    // Instance Data
    private Log log = LogFactory.getLog(this.getClass());

    private class InternalProfile {
        StartChannelListener listener;
        SessionTuningProperties tuning;
    }

    private Hashtable profileListeners;
    String localize;

    // Constructors

    /**
     * Constructor ProfileRegistry
     *
     *
     */
    public ProfileRegistry()
    {
        this.localize = Constants.LOCALIZE_DEFAULT;
        this.profileListeners = new Hashtable();
    }

    private ProfileRegistry(String localize, Hashtable profiles)
    {
        this.localize = localize;
        this.profileListeners = profiles;
    }

    public Object clone()
    {
        return new ProfileRegistry(this.localize,
                                   (Hashtable) this.profileListeners.clone());
    }

    /**
     * Returns the currently registered profile URIs.
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
            log.debug("Session does not have any tuning properties");
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
                if (log.isDebugEnabled()) {
                    log.debug("Session does not have tuning property " +
                              SessionTuningProperties.STANDARD_PROPERTIES[i]);
                }
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

    byte[] getGreeting(Session session) {
        return getGreeting(session, null);
    }

    byte[] getGreeting(Session session, String features)
    {
        int bufferSize = "<greeting></greeting>".length();
        int profileCount = 0;

        profileCount = profileListeners.size();

        Enumeration e = profileListeners.keys();

        while (e.hasMoreElements()) {
            bufferSize += ((String) e.nextElement()).length()
                + "<profile>".length();
        }

        bufferSize++;

        StringBuffer sb = new StringBuffer(bufferSize);

        // Create Greeting
        // Wish I could reset these.
        Enumeration f = profileListeners.keys();

        sb.append("<greeting");

        if ((localize != null)
                &&!localize.equals(Constants.LOCALIZE_DEFAULT)) {
            sb.append(" localize='");
            sb.append(localize);
            sb.append('\'');
        }

        if (features != null) {
            sb.append(" features='");
            sb.append(features);
            sb.append('\'');
        }

        sb.append('>');

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
                    sb.append("<profile uri='");
                    sb.append(profileName);
                    sb.append("' />");
                }
            } catch (BEEPException x) {
                x.printStackTrace();

                continue;
            }
        }

        sb.append("</greeting>");

        return StringUtil.stringBufferToAscii(sb);
    }
}
