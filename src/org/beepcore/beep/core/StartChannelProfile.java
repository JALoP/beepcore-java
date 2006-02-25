/*
 * StartChannelProfile.java  $Revision: 1.3 $ $Date: 2006/02/25 17:48:37 $
 *
 * Copyright (c) 2004 Huston Franklin.  All rights reserved.
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

import org.beepcore.beep.core.serialize.ProfileElement;


/**
 * Class StartChannelProfile
 *
 * @deprecated Use org.beepcore.beep.core.serialize.ProfileElement instead.
 * @version $Revision: 1.3 $, $Date: 2006/02/25 17:48:37 $
 */
public class StartChannelProfile extends ProfileElement {
    /**
     * Constructor StartChannelProfile
     *
     *
     * @param uri
     * @param base64Encoding
     * @param data
     *
     */
    public StartChannelProfile(String uri, boolean base64Encoding,
                               String data)
    {
        super(uri, base64Encoding, data);
    }

    /**
     * Constructor StartChannelProfile
     *
     *
     * @param uri
     *
     */
    public StartChannelProfile(String uri)
    {
        super(uri);
    }
}
