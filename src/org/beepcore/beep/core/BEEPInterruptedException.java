
/*
 * BEEPInterruptedException.java            $Revision: 1.3 $ $Date: 2001/11/08 05:51:34 $
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


///*
// * A subclass of java.lang.Exception that we provide so users of the
// * library can differentiate between our exceptions, and JVM exceptions.
// */

/**
 * Signals that an I/O operation has been interrupted. An
 * <code>BEEPInterruptedException</code> is thrown to indicate that an
 * input or output transfer has been terminated because the thread
 * performing it was terminated. The field {@link #bytesTransferred}
 * indicates how many bytes were successfully transferred before
 * the interruption occurred.
 *
 */
public class BEEPInterruptedException extends BEEPException {

    /**
     * Reports how many bytes had been transferred as part of the I/O
     * operation before it was interrupted.
     *
     * @serial
     */
    public int bytesTransferred = 0;

    /**
     * Constructor BEEPException
     *
     *
     * @param s
     *
     */
    public BEEPInterruptedException(String s)
    {
        super(s);
    }
}
