/*
 * StartChannelProfile.java  $Revision: 1.2 $ $Date: 2001/11/08 05:51:34 $
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


/**
 * Class StartChannelProfile
 *
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/11/08 05:51:34 $
 */
public class StartChannelProfile {

    String uri;
    boolean base64Encoding;
    String data;

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
        this.uri = uri;
        this.base64Encoding = base64Encoding;
        this.data = data;
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
        this.uri = uri;
        this.base64Encoding = false;
        this.data = null;
    }
}
