/*
 * Constants.java  $Revision: 1.4 $ $Date: 2001/11/08 05:51:34 $
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
 * A placeholder interface for all our constants, both for ease
 * of use, for potential management later, and to minimize string creation.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.4 $, $Date: 2001/11/08 05:51:34 $
 */
interface Constants {

    public static final String ENCODING_NONE = "none";
    public static final String ENCODING_BASE64 = "base64";
    public static final String ENCODING_DEFAULT = ENCODING_NONE;
    public static final String LOCALIZE_DEFAULT = "i-default";
    public static final String LOCALIZE_US = "en-us";
}
