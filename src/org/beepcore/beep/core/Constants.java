
/*
 * Constants.java            $Revision: 1.1 $ $Date: 2001/04/02 08:56:06 $
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
 * @version $Revision: 1.1 $, $Date: 2001/04/02 08:56:06 $
 */
interface Constants {

    public static final String CHANNEL_ZERO = "0";
    public static final int SETTING_PENDING_CHANNEL_TABLE_SIZE = 16;
    public static final String NEWLINE = "\n";
    public static final String EMPTY_STRING = "";
    public static final String ENCODING_NONE = "none";
    public static final String ENCODING_BASE64 = "base64";
    public static final String ENCODING_DEFAULT = ENCODING_NONE;
    public static final String LOCALIZE_DEFAULT = "i-default";
    public static final String LOCALIZE_US = "en-us";
    public static final String TAG_ERROR = "error";
    public static final String TAG_GREETING = "greeting";
    public static final String TAG_OK = "ok";
    public static final String TAG_START = "start";
    public static final String TAG_SERVER_NAME = "serverName";
    public static final String TAG_NUMBER = "number";
    public static final String TAG_PROFILE = "profile";
    public static final String TAG_URI = "uri";
    public static final String TAG_ENCODING = "encoding";
    public static final String TAG_CLOSE = "close";
    public static final String TAG_CODE = "code";
    public static final String TAG_XML_LANG = "xml:lang";
    public static final String TAG_FEATURES = "features";
    public static final String TAG_LOCALIZE = "localize";
    public final static String FRAGMENT_CDATA_PREFIX = "<![CDATA[";
    public final static String FRAGMENT_CDATA_SUFFIX = "]]>";
    public static final String FRAGMENT_START_PREFIX = "<start ";
    public static final String FRAGMENT_NUMBER_PREFIX = "number='";
    public static final String FRAGMENT_ENCODING_PREFIX = "encoding='";
    public static final String FRAGMENT_QUOTE_SUFFIX = "' ";
    public static final String FRAGMENT_SERVERNAME_PREFIX = "serverName='";
    public static final String FRAGMENT_START_SUFFIX = "</start>";
    public static final String FRAGMENT_ERROR_SUFFIX = "</error>";
    public static final String FRAGMENT_CLOSE_PREFIX = "<close ";
    public static final String FRAGMENT_ERROR_PREFIX = "<error ";
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
    public static final String FRAGMENT_OK = "<ok />";
    public static final int FRAGMENT_GREETING_LENGTH =
        FRAGMENT_GREETING_PREFIX.length()
        + FRAGMENT_QUOTE_ANGLE_SUFFIX.length()
        + FRAGMENT_GREETING_SUFFIX.length();
    public static final int FRAGMENT_PROFILE_LENGTH =
        FRAGMENT_PROFILE_PREFIX.length()
        + FRAGMENT_QUOTE_ANGLE_SUFFIX.length();
    public static final int MAX_ACCEPTABLE_PAYLOAD_SIZE = 1024 * 16;
    public static final int MAX_PCDATA_SIZE = 4096;
    public static final int MAX_ANS_NUMBER = 2147483647;
    public static final long MAX_CHANNEL_NUMBER = 2147483647;
    public static final long MAX_SEQUENCE_NUMBER =
        Long.MAX_VALUE;                                  //4294967295;
    public static final int MAX_MESSAGE_SIZE = 2147483647;
    public static final int MAX_MESSAGE_NUMBER = 2147483647;
    public static final int MAX_GREETING_WAIT = 2000;    //15000;
    public static final int MAX_START_CHANNEL_WAIT = 60000;
    public static final int MAX_START_CHANNEL_INTERVAL = 100;
}
