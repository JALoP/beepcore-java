/*
 * Profile.java  $Revision: 1.5 $ $Date: 2003/04/23 15:23:05 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2002 Huston Franklin.  All rights reserved.
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
package org.beepcore.beep.profile;


import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.StartChannelListener;


/**
 * All profile implementations that register with this
 * beep library must implement these methods.
 *
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.5 $, $Date: 2003/04/23 15:23:05 $
 */
public interface Profile {

    /**
     * Initializes the profile and returns the
     * <code>StartChannelListener</code> for <code>uri</code>.
     *
     * @param uri
     * @param config
     *
     * @return The corresponding <code>StartChannelListener</code> for
     *         the specified uri.
     * @throws BEEPException
     */
    public StartChannelListener init(String uri, ProfileConfiguration config)
        throws BEEPException;

    /**
     * Called when the <code>StartChannelListener</code> is removed from
     * the <code>ProfileRegistry</code>.
     */
//    public void shutdown();
}
