
/*
 * BEEPException.java            $Revision: 1.1 $ $Date: 2001/04/02 08:56:06 $
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
 * A subclass of java.lang.Exception that we provide so users of the
 * library can differentiate between BEEP exceptions, and JVM exceptions.
 *
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision, $Date: 2001/04/02 08:56:06 $
 */
public class BEEPException extends Exception {

    /**
     * Constructor BEEPException
     *
     *
     * @param s
     *
     */
    public BEEPException(String s)
    {
        super(s);
    }
}
