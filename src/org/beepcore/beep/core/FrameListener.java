
/*
 * FrameListener.java            $Revision: 1.1 $ $Date: 2001/04/02 08:56:06 $
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
 * Provided to allow for the registration of a <code>FrameListener</code>
 * on a given <code>Channel</code>.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision, $Date: 2001/04/02 08:56:06 $
 */
public interface FrameListener extends DataListener {

    /**
     * Called every time the underlying BEEP framework receives a frame on
     * the channel for which this listener is registered.
     *
     *
     * @param f <code>Frame</code>
     *
     * @throws BEEPException
     * @see <code>org.beepcore.beep.core.Frame</code>
     *
     */
    public void receiveFrame(Frame f) throws BEEPException;

    // @todo remove BEEPException from message signature.
}
