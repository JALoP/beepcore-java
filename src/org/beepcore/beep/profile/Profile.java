
/*
 * Profile.java            $Revision: 1.1 $ $Date: 2001/04/02 08:45:28 $
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
package org.beepcore.beep.profile;


import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.StartChannelListener;
import org.beepcore.beep.core.Channel;


/**
 * All profile implementations that register with this
 * beep library must implement these methods.
 *
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.1 $, $Date: 2001/04/02 08:45:28 $
 */
public interface Profile {

    /**
     * Registers <code>StartChannelListener</code>s for the profiles
     * supported by the class.
     */
//    public void registerStartChannelListeners(ProfileRegistry registry);

    /**
     * Returns the <code>StartChannelListener</code> to use when add
     * this profile to the <code>ProfileRegistry</code>.
     *
     */
    public StartChannelListener getStartChannelListener();

    /**
     * Called to initalize the <code>Profile</code>. This must be
     * called before any of the methods in the
     * <code>StartChannelListener</code> that is returned from
     * <code>getStartChannelListener</code>.
     *
     *
     * @param config
     *
     * @throws BEEPException
     *
     */
    public void init(ProfileConfiguration config) throws BEEPException;

    /**
     * Returns the <code>ProfileConfiguration</code> passed to the
     * <code>Profile</code> on the <code>init</code> call.
     *
     *
     */
    public ProfileConfiguration getConfiguration();

    /**
     * Returns the URI for this <code>Profile</code>. The URI typically
     * references the DTD which describes the message exchanges for the
     * BEEP Profile.
     */
    public String getURI();
}
