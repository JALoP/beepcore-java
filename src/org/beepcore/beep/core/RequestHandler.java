/*
 * RequestHandler.java  $Revision: 1.1 $ $Date: 2003/06/10 18:59:19 $
 *
 * Copyright (c) 2003 Huston Franklin.  All rights reserved.
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
 * This interface is used by profiles to receive MSG messages. This handler is
 * registered with a channel using the <code>setRequestHandler()</code> method.
 *
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2003/06/10 18:59:19 $
 *
 */
public interface RequestHandler {
    /**
     * Called to process the request in received MSG message.
     *
     * @param message MSG Message received.
     *
     * @see MessageMSG
     */
    public void receiveMSG(MessageMSG message);
}
