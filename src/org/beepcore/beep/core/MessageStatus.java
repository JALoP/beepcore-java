
/*
 * MessageStatus.java            $Revision: 1.1 $ $Date: 2001/04/02 08:56:06 $
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
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision, $Date: 2001/04/02 08:56:06 $
 */
public class MessageStatus {

    /** Uninitialized BEEP status. */
    public static final int MESSAGE_STATUS_UNK = 0;

    /** BEEP message status. */
    public static final int MESSAGE_STATUS_NOT_SENT = 1;

    /** BEEP message status. */
    public static final int MESSAGE_STATUS_SENT = 2;

    /** BEEP message status. */
    public static final int MESSAGE_STATUS_RECEIVED_REPLY = 3;

    /** BEEP message status. */
    public static final int MESSAGE_STATUS_RECEIVED_ERROR = 4;

    /** Status of message. */
    private int messageStatus = MESSAGE_STATUS_UNK;
    private Message message;
    private FrameListener frameListener;
    private ReplyListener replyListener;

    MessageStatus(Message message)
    {
        this.messageStatus = MESSAGE_STATUS_UNK;
        this.message = message;
    }

    MessageStatus(Message message, ReplyListener replyListener)
    {
        this.messageStatus = MESSAGE_STATUS_UNK;
        this.message = message;
        this.replyListener = replyListener;
    }

    MessageStatus(Message message, FrameListener frameListener)
    {
        this.messageStatus = MESSAGE_STATUS_UNK;
        this.message = message;
        this.frameListener = frameListener;
    }

    /**
     * Returns the message status.
     *
     */
    public int getMessageStatus()
    {
        return this.messageStatus;
    }

    /**
     * Returns the message status.
     *
     */
    public Message getMessage()
    {
        return this.message;
    }

    /**
     * Method setMessageStatus
     *
     *
     * @param status
     *
     */
    void setMessageStatus(int status)
    {
        this.messageStatus = status;
    }

    /**
     * Method getListener
     *
     *
     */
    ReplyListener getReplyListener()
    {
        return this.replyListener;
    }

    /**
     * Method getFrameListener
     *
     *
     */
    FrameListener getFrameListener()
    {
        return this.frameListener;
    }
}
