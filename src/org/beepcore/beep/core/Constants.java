
/*
 * Constants.java            $Revision: 1.2 $ $Date: 2001/05/25 15:27:10 $
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


/**
 * A placeholder interface for all our constants, both for ease
 * of use, for potential management later, and to minimize string creation.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/05/25 15:27:10 $
 */
interface Constants {

    public static final String CHANNEL_ZERO = "0";
    public static final String ENCODING_NONE = "none";
    public static final String ENCODING_BASE64 = "base64";
    public static final String ENCODING_DEFAULT = ENCODING_NONE;
    public static final String LOCALIZE_DEFAULT = "i-default";
    public static final String LOCALIZE_US = "en-us";
    public static final String FRAGMENT_ENCODING_PREFIX = "encoding='";
    public static final String FRAGMENT_QUOTE_SUFFIX = "' ";
    public static final String FRAGMENT_CODE_PREFIX = "code='";
    public static final String FRAGMENT_XML_LANG_PREFIX = "xml:lang='";
    public static final String FRAGMENT_GREETING_PREFIX = "<greeting";
    public static final String FRAGMENT_LOCALIZE_PREFIX = "localize='";
    public static final String FRAGMENT_FEATURES_PREFIX = "features='";
    public static final String FRAGMENT_GREETING_SUFFIX = "</greeting>";
    public static final String FRAGMENT_PROFILE_PREFIX = "<profile ";
    public static final String FRAGMENT_PROFILE_SUFFIX = "</profile>";
    public static final String FRAGMENT_URI_PREFIX = "uri='";
    public static final String FRAGMENT_ANGLE_SUFFIX = ">";
    public static final String FRAGMENT_SLASH_ANGLE_SUFFIX = " />";
    public static final String FRAGMENT_QUOTE_SLASH_ANGLE_SUFFIX = "' />";
    public static final String FRAGMENT_QUOTE_ANGLE_SUFFIX = "'>";
    public static final int FRAGMENT_GREETING_LENGTH =
        FRAGMENT_GREETING_PREFIX.length()
        + FRAGMENT_QUOTE_ANGLE_SUFFIX.length()
        + FRAGMENT_GREETING_SUFFIX.length();
    public static final int FRAGMENT_PROFILE_LENGTH =
        FRAGMENT_PROFILE_PREFIX.length()
        + FRAGMENT_QUOTE_ANGLE_SUFFIX.length();
}
