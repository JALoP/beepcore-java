/*
 * ChannelListener.java  $Revision: 1.1 $ $Date: 2001/11/22 15:25:29 $
 *
 * Copyright (c) 2001 Huston Franklin.  All rights reserved.
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
package org.beepcore.beep.core.event;


import java.util.EventListener;

/**
 * This is an interface defining the methods that must be implemented
 * to listen for Channel Events.
 *
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2001/11/22 15:25:29 $
 */
public interface ChannelListener extends EventListener {

    /**
     * Invoked when the greeting has been received for a session.
     */
    public void channelStarted(ChannelEvent e);

    /**
     * Invoked when the greeting has been received for a session.
     */
    public void channelClosed(ChannelEvent e);
}
