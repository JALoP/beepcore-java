/*
 * Channel.java            $Revision: 1.16 $ $Date: 2001/10/31 00:32:37 $
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


import java.io.*;

import java.util.*;

import org.beepcore.beep.util.BufferSegment;
import org.beepcore.beep.util.Log;


/**
 * Channel is a conduit for a certain kind of traffic over a session,
 * determined by the profile of the channel.  Channels are created by Session
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.16 $, $Date: 2001/10/31 00:32:37 $
 *
 */
public class Channel {

    // class variables
    static final int STATE_UNINITIALISED = 1;
    static final int STATE_OK = 2;
    static final int STATE_CLOSING = 3;
    static final int STATE_CLOSED = 4;
    static final int STATE_ERROR = 5;
    private static final String ERR_CHANNEL_MESSAGE_NUMBER_PREFIX =
        "Incorrect message number: was ";
    private static final String ERR_CHANNEL_MIDDLE = "; expecting ";
    private static final String ERR_CHANNEL_SEQUENCE_NUMBER_PREFIX =
        "Incorrect sequence number: was ";
    private static final String ERR_CHANNEL_ERROR_STATE =
        "Channel in currently experiencing technical difficulties.";
    private static final String ERR_CHANNEL_UNINITIALISED_STATE =
        "Channel is uninitialised.";
    private static final String ERR_CHANNEL_UNKNOWN_STATE =
        "Channel is in an unknown state.";
    private static final String ERR_CHANNEL_INCONSISTENT_FRAME_TYPE_PREFIX =
        "Incorrect message type: was ";
    private static final String ERR_REPLY_RECEIVED_FOR_NO_MESSAGE =
        "Reply received for a message never sent.";
    private static final int MAX_PAYLOAD_SIZE = 4096;
    private static final BufferSegment zeroLengthSegment =
        new BufferSegment(new byte[0]);

    /** @todo check this */

    // default values for some variables
    static final int DEFAULT_WINDOW_SIZE = 4096;

    // instance variables

    /** syntax of messages */
    private String profile;

    /** encoding of used by profile */
    private String encoding;

    /** channel number on the session */
    private String number;

    /** Used to pass data sent on the Start Channel request */
    private String startData;

    /** receiver of messages (or partial messages) */
    private MessageListener listener;

    /** number of last message sent */
    private int lastMessageSent;

    /** sequence number for messages sent */
    private long sentSequence;

    /** sequence for messages received */
    private long recvSequence;

    /** messages waiting for replies */
    private List sentMSGQueue;

    /** MSG we've received by awaiting proceesing of a former MSG */
    private LinkedList recvMSGQueue;

    /** messages queued to be sent */
    private LinkedList pendingSendMessages;

    /** size of the frames */
    private int frameSize;

    /** session this channel sends through. */
    private Session session;

    /** message that we are receiving frames */
    private LinkedList recvReplyQueue;

    private int state = STATE_UNINITIALISED;

    private BEEPError errMessage;

    private Frame previousFrame;

    /** size of the peer's receive buffer */
    private int peerWindowSize;

    /** size of the receive buffer */
    private int recvWindowSize;

    /** amount of the buffer in use */
    private int recvWindowUsed;

    private long prevAckno;

    private int prevWindowUsed;

    private int waitTimeForPeer;

    private boolean notifyOnFirstFrame;

    private Object applicationData = null;

    /* this is to support deprecated sendXXX methods */
    private MessageMSG currentMSG;

    // in shutting down the session
    // something for waiting synchronous messages (semaphores or something)

    /**
     * Create a <code>Channel</code> object.
     *
     * @param profile URI string of the profile that this channel will "speak".
     * @param number The channel number.
     * @param listener message listener that will receive callbacks for
     *    messages received on this channel
     * @param session <code>Session</code> over which this channel
     *                sends/receives messages
     *
     * @see org.beepcore.beep.core.Session
     * @see org.beepcore.beep.core.DataListener
     */
    protected Channel(String profile, String number, MessageListener listener,
                      Session session)
    {
        this.profile = profile;
        this.encoding = Constants.ENCODING_DEFAULT;
        this.number = number;
        this.listener = listener;
        this.session = session;
        this.frameSize = MAX_PAYLOAD_SIZE;
        sentSequence = 0;
        recvSequence = 0;
        lastMessageSent = 1;

        pendingSendMessages = new LinkedList();
        sentMSGQueue = Collections.synchronizedList(new LinkedList());
        recvMSGQueue = new LinkedList();
        recvReplyQueue = new LinkedList();
        state = STATE_UNINITIALISED;
        recvWindowUsed = 0;
        recvWindowSize = DEFAULT_WINDOW_SIZE;
        prevAckno = 0;
        prevWindowUsed = 0;
        peerWindowSize = DEFAULT_WINDOW_SIZE;
        waitTimeForPeer = 0;
        this.notifyOnFirstFrame = false;
    }

    /**
     * This is a special constructor for Channel Zero
     *
     * @param Session
     * @param ReplyListener
     *
     */
    Channel(Session session, String number, ReplyListener rl)
    {
        this(null, number, null, session);

        // Add a MSG to the SentMSGQueue to fake channel into accepting the
        // greeting which comes in an unsolicited RPY.
        sentMSGQueue.add(new MessageStatus(this, Message.MESSAGE_TYPE_MSG, 0,
                                           null, rl));

        state = STATE_OK;
    }

    /**
     * Closes the channel.
     *
     * @throws BEEPException
     */
    public void close() throws BEEPException
    {

        // @todo the other BEEP peer may refuse this request
        // should we return a boolean or throw a CloseChannelException?
        session.closeChannel(this, BEEPError.CODE_SUCCESS, null);
    }

    // instance methods

    /**
     * Returns application context data previously set using
     * <code>setAppData()</code>.
     *
     * @see #setAppData
     */
    public Object getAppData()
    {
        return this.applicationData;
    }

    /**
     * Set the application context data member for future retrieval.
     *
     * @see #getAppData
     */
    public void setAppData(Object applicationData)
    {
        this.applicationData = applicationData;
    }

    /**
     * Returns the receive buffer size for this channel.
     */
    public synchronized int getBufferSize()
    {
        return recvWindowSize;
    }

    /**
     * Returns the size of the used portion of the receive buffer for this
     * channel.
     */
    public synchronized int getBufferUsed()
    {
        return recvWindowUsed;
    }

    /**
     * Returns the encoding used on this <code>Channel</code>
     * @todo look at removing this and adding the information to getProfile()
     */
    String getEncoding()
    {
        return encoding;
    }

    void setEncoding(String enc)
    {
        this.encoding = enc;
    }

    /**
     * Return the number of this <code>Channel</code>.
     *
     */
    public int getNumber()
    {
        return Integer.parseInt(number);
    }

    /**
     * Sets the receive buffer size for this channel.  Default size is 4K.
     *
     *
     * @param size
     *
     * @throws BEEPException
     *
     */
    public void setReceiveBufferSize(int size) throws BEEPException
    {
        synchronized (this) {
            if ((state != STATE_OK) && (state != STATE_UNINITIALISED)) {
                throw new BEEPException("Channel in a bad state.");
            }

            // make sure we aren't setting the size less than what is currently
            // in the buffer right now.
            if (size < recvWindowUsed) {
                throw new BEEPException("Size must be less than what is currently in use.");
            }

            // @TODO what if they decide to shrink the buffer?  Is that even
            // allowed?
            // set the new size and copy the buffer
            recvWindowSize = size;

            Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                         "Buffer size for channel " + number + " set to "
                         + recvWindowSize);

            // send a new SEQ message to update the buffer size
            if (session.updateMyReceiveBufferSize(this, prevAckno,
                                                  recvSequence,
                                                  prevWindowUsed,
                                                  recvWindowUsed,
                                                  recvWindowSize)) {
                prevAckno = recvSequence;
                prevWindowUsed = recvWindowUsed;
            }
        }
    }

    /**
     * Determines whether calls to the <code>MessageListener</code> are
     * made upon receiving the first or last <code>Frame</code> of the message.
     *
     * @param n If <code>true</code>, calls to <code>MessageListener</code> are
     * received on the first <code>Frame</code> of a message.  Otherwise, the
     * <code>MessageListener</code> will be called once the message is complete
     * (when the last <code>Frame</code> is received).
     *
     * @see MessageListener
     */
    public void setNotifyMessageListenerOnFirstFrame(boolean n)
    {
        this.notifyOnFirstFrame = n;
    }

    /**
     * Returns whether or not calls to the <code>MessageListener</code> are
     * made upon receiving the first or last <code>Frame</code> of the message.
     *
     * @see #setNotifyMessageListenerOnFirstFrame
     */
    public boolean getNotifyMessageListenerOnFirstFrame()
    {
        return this.notifyOnFirstFrame;
    }

    /**
     * Sets the <code>MessageListener</code> for this channel.
     *
     * @param ml
     * @returns The previous MessageListener or null if none was set.
     */
    public MessageListener setMessageListener(MessageListener ml)
    {
        MessageListener tmp = this.listener;
        this.listener = ml;
        return tmp;
    }

    /**
     * Returns the message listener for this channel.
     */
    public MessageListener getMessageListener()
    {
        return this.listener;
    }

    /**
     * Returns the session for this channel.
     *
     */
    public Session getSession()
    {
        return this.session;
    }

    /**
     * Sends a message of type MSG.
     *
     * @param stream Data to send in the form of <code>DataStream</code>.
     * @param replyListener A "one-shot" listener that will handle replies
     * to this sendMSG listener.
     *
     * @see DataStream
     * @see MessageStatus
     *
     * @return MessageStatus
     *
     * @throws BEEPException if an error is encoutered.
     */
    public MessageStatus sendMSG(OutputDataStream stream,
                                 ReplyListener replyListener)
            throws BEEPException
    {
        MessageStatus status;

        if (state != STATE_OK) {
            switch (state) {
            case STATE_ERROR :
                throw new BEEPException(ERR_CHANNEL_ERROR_STATE);
            case STATE_UNINITIALISED :
                throw new BEEPException(ERR_CHANNEL_UNINITIALISED_STATE);
            default :
                throw new BEEPException(ERR_CHANNEL_UNKNOWN_STATE);
            }
        }

        synchronized (this) {

            // create a new request
            status = new MessageStatus(this, Message.MESSAGE_TYPE_MSG,
                                       lastMessageSent, stream, replyListener);

            // message 0 was the greeting, it was already sent, inc the counter
            ++lastMessageSent;
        }

        // put this in the list of messages waiting
        // may want to put an expiration or something in here so they
        // don't just stay around taking up space.
        // @todo it's a synchronized list, you don't have to sync
        synchronized (sentMSGQueue) {
            sentMSGQueue.add(status);
        }

        // send it on the session
        sendToPeer(status);

        return status;
    }

    /**
     * get the number of this <code>Channel</code> as a <code>String</code>
     *
     */
    String getNumberAsString()
    {
        return number;
    }

    /**
     * returns the state of the <code>Channel</code>
     * The possible states are (all defined as Channel.STATE_*):
     * STATE_UNINITIALISED - after a channel is created
     * STATE_OK - a channel is acknowledged by the other session
     * STATE_CLOSED - the channel has been closed
     * STATE_ERROR - an error occured during creation or transmission
     */
    int getState()
    {
        return state;
    }

    private void receiveFrame(Frame frame) throws BEEPException
    {

        // if this is an incoming message rather than a reply to a
        // previously sent message
        if (frame.getMessageType() == Message.MESSAGE_TYPE_MSG) {
            MessageMSG m = null;

            synchronized (recvMSGQueue) {
                if (recvMSGQueue.size() != 0) {
                    m = (MessageMSG) recvMSGQueue.getLast();

                    if (m.getMsgno() != frame.getMsgno()) {
                        m = null;
                    }
                }

                if (m == null) {
                    m = new MessageMSG(this, frame.getMsgno(),
                                       new InputDataStream(this));

                    recvMSGQueue.addLast(m);
                }
            }

            Iterator i = frame.getPayload();
            while (i.hasNext()) {
                m.getDataStream().add((BufferSegment)i.next());
            }

            if (frame.isLast()) {
                m.getDataStream().setComplete();
            }

            // The MessageListener interface only allows one message
            // up to be processed at a time so if this is not the
            // first message on the queue just return.
            // Question, so how do we EVER catch up?  If something
            // gets stuck here.  I suspect it isn't getting taken off.
            synchronized (recvMSGQueue) {
                if (m != recvMSGQueue.getFirst()) {
                    return;
                }

                if (frame.isLast()) {
                    recvMSGQueue.remove(m);
                }
            }

            // notify message listener if this message has not been
            // notified before and notifyOnFirstFrame is set, the
            // window is full, this is the last frame.
            synchronized (m) {
                if (m.isNotified()
                        || ((this.notifyOnFirstFrame == false)
                            && (recvSequence - prevAckno) !=
                            (recvWindowSize - prevWindowUsed)
                            && (frame.isLast() == false))) {
                    return;
                }

                m.setNotified();
            }

            this.currentMSG = m;
            ((MessageListener) this.listener).receiveMSG(m);

            return;
        }

        Message m = null;

        // This frame must be for a reply (RPY, ERR, ANS, NUL)
        MessageStatus mstatus;

        // Find corresponding MSG for this reply
        synchronized (sentMSGQueue) {
            Message sentMSG;

            if (sentMSGQueue.size() == 0) {

                // @todo shutdown session (we think)
            }

            mstatus = (MessageStatus) sentMSGQueue.get(0);

            if (mstatus.getMsgno() != frame.getMsgno()) {

                // @todo shutdown session (we think)
            }

            // If this is the last frame for the reply (NUL, RPY, or
            // ERR) to this MSG.
            if ((frame.isLast() == true)
                    && (frame.getMessageType() != Message.MESSAGE_TYPE_ANS)) {
                sentMSGQueue.remove(0);
            }
        }

        ReplyListener replyListener = mstatus.getReplyListener();

        // error if they don't have either a frame or reply listener
        if (replyListener == null) {

            // @todo should we check this on sendMSG instead?
        }

        if (frame.getMessageType() == Message.MESSAGE_TYPE_NUL) {
            synchronized (recvReplyQueue) {
                if (recvReplyQueue.size() != 0) {

                    // There are ANS messages on the queue for which we
                    // haven't received the last frame.
                    Log.logEntry(Log.SEV_DEBUG,
                                 "Received NUL before last ANS");
                    session.terminate("Received NUL before last ANS");
                }
            }

            m = new Message(this, frame.getMsgno(), null,
                            Message.MESSAGE_TYPE_NUL);

            mstatus.setMessageStatus(MessageStatus.MESSAGE_STATUS_RECEIVED_REPLY);
            Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                         "Notifying reply listener =>" + replyListener +
                         "for NUL message");

            replyListener.receiveNUL(m);

            return;
        }

        // is this an ANS message?
        if (frame.getMessageType() == Message.MESSAGE_TYPE_ANS) {

            // see if this answer number has already come in
            synchronized (recvReplyQueue) {
                Iterator i = recvReplyQueue.iterator();

                m = null;

                while (i.hasNext()) {
                    Message tmp = (Message) i.next();

                    if (tmp.getAnsno() == frame.getAnsno()) {
                        m = tmp;

                        break;
                    }
                }

                // if no answer was found, then create a new one and
                // add it to the queue
                if (m == null) {
                    m = new Message(this, frame.getMsgno(), frame.getAnsno(),
                                    new InputDataStream(this));

                    if (!frame.isLast()) {
                        recvReplyQueue.add(m);
                    }
                } else if (frame.isLast()) {

                    // remove the found ANS from the recvReplyQueue
                    i.remove();
                }
            }
        } else {    // ERR or RPY
            synchronized (recvReplyQueue) {
                if (recvReplyQueue.size() == 0) {
                    m = new Message(this, frame.getMsgno(),
                                    new InputDataStream(this),
                                    frame.getMessageType());

                    if (frame.isLast() == false) {
                        recvReplyQueue.add(m);
                    }
                } else {

                    // @todo sanity check: make sure this is the
                    // right Message
                    m = (Message) recvReplyQueue.getFirst();

                    if (frame.isLast()) {
                        recvReplyQueue.removeFirst();
                    }
                }

                if (frame.isLast()) {
                    if (frame.getMessageType() == Message.MESSAGE_TYPE_ERR) {
                        mstatus.setMessageStatus(MessageStatus.MESSAGE_STATUS_RECEIVED_ERROR);
                    } else {
                        mstatus.setMessageStatus(MessageStatus.MESSAGE_STATUS_RECEIVED_REPLY);
                    }
                }
            }
        }

        Iterator i = frame.getPayload();
        while (i.hasNext()) {
            m.getDataStream().add((BufferSegment)i.next());
        }

        if (frame.isLast()) {
            m.getDataStream().setComplete();
        }

        // notify message listener if this message has not been
        // notified before and notifyOnFirstFrame is set, the
        // window is full, this is the last frame.
        synchronized (m) {
            if (m.isNotified() || ((this.notifyOnFirstFrame == false)

            //                && recvWindowUsed != recvWindowSize
                                   && (recvSequence - prevAckno)
                                   != (recvWindowSize - prevWindowUsed) &&
                                   (frame.isLast() == false))) {
                Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                             "recvWindowUsed = " + recvWindowUsed
                             + " recvWindowSize = " + recvWindowSize
                             + "\t\r\nNot notifying frame listener.");
                return;
            }

            m.setNotified();
        }

        Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                     "Notifying reply listener.=>" + replyListener);

        if (m.messageType == Message.MESSAGE_TYPE_RPY) {
            replyListener.receiveRPY(m);
        } else if (m.messageType == Message.MESSAGE_TYPE_ERR) {
            replyListener.receiveERR(m);
        } else if (m.messageType == Message.MESSAGE_TYPE_ANS) {
            replyListener.receiveANS(m);
        }
    }

    /**
     * interface between the session.  The session receives a frame and then
     * calls this function.  The function then calls the message listener
     * via some intermediary thread functions.  The message hasn't been
     * completely received.  The data stream contained in the message will
     * block if more is expected.
     * @param frame - the frame received by the session
     */
    void postFrame(Frame frame) throws BEEPException
    {
        Log.logEntry(Log.SEV_DEBUG_VERBOSE, "Channel::postFrame");

        boolean firstFrame = false;
        boolean createAndPostMessage = false;
        Message currentMessage = null;
        int msgno = frame.getMsgno();

        if (state != STATE_OK) {
            throw new BEEPException("State is " + state);
        }

        // Validate Frame
        synchronized (this) {

            // assume the frame has already been parsed by the session and
            // that the flags indicated are part of the frame object

            // is the message number correct?
            if (frame.getMessageType() == Message.MESSAGE_TYPE_MSG) {
                if (previousFrame != null) {
                    if (frame.getMsgno() != previousFrame.getMsgno()) {
                        throw new BEEPException(ERR_CHANNEL_MESSAGE_NUMBER_PREFIX
                                                + frame.getMsgno()
                                                + ERR_CHANNEL_MIDDLE
                                                + previousFrame.getMsgno());
                    }
                } else {
                    synchronized (recvMSGQueue) {
                        ListIterator i =
                            recvMSGQueue.listIterator(recvMSGQueue.size());
                        while (i.hasPrevious()) {
                            if (((Message) i.previous()).getMsgno()
                                == frame.getMsgno())
                            {
                                throw new BEEPException("Received a frame " +
                                                        "with a duplicate " +
                                                        "msgno (" +
                                                        frame.getMsgno() +
                                                        ")");
                            }
                        }
                    }
                }
            } else {
                MessageStatus mstatus;

                synchronized (sentMSGQueue) {
                    if (sentMSGQueue.size() == 0) {
                        throw new BEEPException("Received unsolicited reply");
                    }

                    mstatus = (MessageStatus) sentMSGQueue.get(0);
                }

                if (frame.getMsgno() != mstatus.getMsgno()) {
                    throw new BEEPException(ERR_CHANNEL_MESSAGE_NUMBER_PREFIX
                                            + frame.getMsgno()
                                            + ERR_CHANNEL_MIDDLE
                                            + mstatus.getMsgno());
                }
            }

            // is the sequence number correct?
            if (frame.getSeqno() != recvSequence) {
                throw new BEEPException(ERR_CHANNEL_SEQUENCE_NUMBER_PREFIX
                                             + frame.getSeqno()
                                             + ERR_CHANNEL_MIDDLE
                                             + recvSequence);
            }

            // is the message type the same as the previous frames?
            if ((previousFrame != null)
                    && (previousFrame.getMessageType()
                        != frame.getMessageType())) {
                throw new BEEPException(ERR_CHANNEL_INCONSISTENT_FRAME_TYPE_PREFIX
                                             + frame.getMessageTypeString()
                                             + ERR_CHANNEL_MIDDLE
                                             + previousFrame.getMessageTypeString());
            }
        }

        recvSequence += frame.getSize();

        // subtract this from the amount available in the buffer
        recvWindowUsed += frame.getSize();

        // make sure we didn't overflow the buffer
        if (recvWindowUsed > recvWindowSize) {
            throw new BEEPException("Channel window overflow");
        }

        if (frame.getMessageType() != Message.MESSAGE_TYPE_MSG) {
            MessageStatus mstatus;

            synchronized (sentMSGQueue) {
                if (sentMSGQueue.size() == 0) {
                    throw new BEEPException("Received unsolicited reply");
                }

                mstatus = (MessageStatus) sentMSGQueue.get(0);

                if (mstatus.getMsgno() != frame.getMsgno()) {
                    throw new BEEPException("Received reply out of order");
                }
            }
        }

        try {
            receiveFrame(frame);
        } catch (BEEPException e) {
            // @todo change this to do the right thing
            throw new BEEPException(e.getMessage());
        }

        // is this the last frame in the message?
        if (frame.isLast() == true) {
            Log.logEntry(Log.SEV_DEBUG_VERBOSE, "Got the last frame");
        }

        // save the previous frame to compare message types
        if (frame.isLast()) {
            previousFrame = null;
        } else {
            previousFrame = frame;
        }
    }

    void sendMessage(MessageStatus m) throws BEEPException
    {
        if (state != STATE_OK) {
            switch (state) {
            case STATE_ERROR :
                throw new BEEPException(ERR_CHANNEL_ERROR_STATE);
            case STATE_UNINITIALISED :
                throw new BEEPException(ERR_CHANNEL_UNINITIALISED_STATE);
            default :
                throw new BEEPException(ERR_CHANNEL_UNKNOWN_STATE);
            }
        }

        // send it on the session
        sendToPeer(m);
    }

    void sendToPeer(MessageStatus status) throws BEEPException
    {
        synchronized (pendingSendMessages) {
            pendingSendMessages.add(status);
        }
        sendQueuedMessages();
    }

    private synchronized void sendQueuedMessages() throws BEEPException
    {
        while (true) {
            MessageStatus status;

            synchronized (pendingSendMessages) {
                if (pendingSendMessages.isEmpty()) {
                    return;
                }
                status = (MessageStatus) pendingSendMessages.removeFirst();
            }

            sendFrames(status);

            if (status.getMessageStatus() !=
                MessageStatus.MESSAGE_STATUS_SENT)
            {
                synchronized (pendingSendMessages) {
                    pendingSendMessages.addFirst(status);
                }
                return;
            }
        }
    }

    private void sendFrames(MessageStatus status)
        throws BEEPException
    {
        int sessionBufferSize = session.getMaxFrameSize();
        OutputDataStream ds = status.getMessageData();

        do {
            synchronized (this) {
                Frame frame;
                // create a frame
                frame = new Frame(status.getMessageType(),
                                  status.getChannel(), status.getMsgno(),
                                  false,
                                  sentSequence, 0, status.getAnsno());

                // make sure the other peer can accept something
                if (peerWindowSize == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("caught InterruptedException");
                    }

                    // wait until there is something to send up to
                    // our timeout
                    if (peerWindowSize == 0) {
                        throw new BEEPException("Time expired waiting " +
                                                "for peer.");
                    }
                }

                int maxToSend =
                    Math.min(sessionBufferSize, peerWindowSize);

                int size = 0;
                while (size < maxToSend) {
                    if (ds.availableSegment() == false) {
                        if (size == 0) {
                            if (ds.isComplete() == false) {
                                // More BufferSegments are expected...
                                return;
                            }

                            frame.addPayload(zeroLengthSegment);
                        }

                        // Send what we have
                        break;
                    }

                    BufferSegment b = ds.getNextSegment(maxToSend - size);

                    frame.addPayload(b);

                    size += b.getLength();
                }

                if (ds.isComplete() && ds.availableSegment() == false) {
                    frame.setLast();
                }

                try {
                    session.sendFrame(frame);
                } catch (BEEPException e) {
                    /*
                     * @todo we should do something more than just log
                     * the error (e.g. close the channel or session).
                     */
                    Log.logEntry(Log.SEV_ERROR, e);
                    status.setMessageStatus(MessageStatus.MESSAGE_STATUS_NOT_SENT);

                    throw e;
                }

                // update the sequence and peer window size
                sentSequence += size;
                peerWindowSize -= size;
            }
        } while (ds.availableSegment() == true || ds.isComplete() == false);

        status.setMessageStatus(MessageStatus.MESSAGE_STATUS_SENT);
    }

    /*
    synchronized void sendToPeer(MessageStatus status) throws BEEPException
    {
        Frame frame = null;
        int available = 0;
        OutputDataStream stream;
        byte[] payload;
        int sessionBufferSize;

        // allocate a shared buffer for this message
        // @todo This call should be changed
        sessionBufferSize = session.getMaxFrameSize();
        payload = new byte[sessionBufferSize];

        // get the message data
        stream = status.getMessageData();

        if (stream == null ||
            (stream.availableSegment() == false && stream.isComplete()))
        {
            Log.logEntry(Log.SEV_DEBUG, "Sending NUL or size 0 frame");
            frame = new Frame(status.getMessageType(),
                              status.getChannel(), status.getMsgno(), true,
                              sentSequence, 0, status.getAnsno());
            try {
                session.sendFrame(frame);
            } catch (BEEPException e) {
                Log.logEntry(Log.SEV_ERROR, e);
                status.setMessageStatus(MessageStatus.MESSAGE_STATUS_NOT_SENT);

                throw e;
            }

            // set the status to sent and return it
            status.setMessageStatus(MessageStatus.MESSAGE_STATUS_SENT);

            return;
        }

        // while we still have data to read
        available = stream.availableHeadersAndData();

        synchronized (this) {
            msgsPending++;
        }

        // for this message, is there anything waiting?
        boolean done = false;
        while (!done) {
            try {
                synchronized (this) {

                    // make sure the other peer can accept something
                    if (peerWindowSize == 0) {
                        wait(waitTimeForPeer);

                        // wait until there is something to send up to
                        // our timeout
                        if (peerWindowSize == 0) {
                            throw new BEEPException("Time expired waiting " +
                                                    "for peer.");
                        }
                    }

                    // how much should we send?
                    int maxCanRead = 0;

                    // we should send the least of these:
                    // 1) the size of the send buffer in the session
                    // (transport specific)
                    maxCanRead = sessionBufferSize;

                    // 2) the amount our peer can accept
                    if (maxCanRead > peerWindowSize) {
                        maxCanRead = peerWindowSize;
                    }

                    int amountToSend = stream.readHeadersAndData(payload, 0,
                                                                 maxCanRead);
                    if ((stream.available() == 0) && stream.isComplete()) {
                        done = true;
                    }
                    else if (amountToSend == -1) {
                        done = true;
                        // send an empty payload
                        payload = new byte[0];
                        amountToSend = 0;
                    }

                    // create a frame
                    frame =
                        new Frame(status.getMessageType(),
                                  status.getChannel(), status.getMsgno(),
                                  done ? true : false,
                                  sentSequence, status.getAnsno(),
                                  new BufferSegment(payload, 0,
                                                    amountToSend));

                    // update the sequence and peer window size
                    sentSequence += amountToSend;    // update the sequence
                    peerWindowSize -= amountToSend;
                }

                // send it
                if (done) {
                    Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                                 "Channel.sendToPeer sending last frame on channel "
                                 + number);
                } else {
                    Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                                 "Channel.sendToPeer sending a frame on channel "
                                 + number);
                }
            } catch (Exception e) {
                Log.logEntry(Log.SEV_ERROR, e);
                status.setMessageStatus(MessageStatus.MESSAGE_STATUS_NOT_SENT);

                throw new BEEPException(e.getMessage());
            }

            try {
                session.sendFrame(frame);
            } catch (BEEPException e) {
                Log.logEntry(Log.SEV_ERROR, e);
                status.setMessageStatus(MessageStatus.MESSAGE_STATUS_NOT_SENT);

                synchronized (this) {
                    msgsPending--;

                    if (msgsPending == 0) {
                        notify();
                    }
                }

                return;
            }
        }

        // set the status to sent and return it
        status.setMessageStatus(MessageStatus.MESSAGE_STATUS_SENT);

        synchronized (this) {
            msgsPending--;

            if (msgsPending == 0) {
                notify();
            }
        }
    }
*/

    // used by session to control the frame size of the channel
    void setFrameSize(int frameSize)
    {
        this.frameSize = frameSize;
    }

    /**
     * Method setState
     *
     *
     * @param newState
     *
     * @throws BEEPException
     *
     */
    synchronized void setState(int newState)
    {
        Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                     "CH" + number + " state=" + newState);

        this.state = newState;

        /**
         * @todo state transition rules and error checking
         */
        if (false) {
            session.terminate("Bad state transition in channel");
        }
    }

    void setProfile(String profile)
    {
        this.profile = profile;
    }

    /**
     * Returns the profile for this channel.
     */
    public String getProfile()
    {
        return this.profile;
    }

    BEEPError getErrorMessage()
    {
        return this.errMessage;
    }

    void setErrorMessage(BEEPError message)
    {
        this.errMessage = message;
    }

    synchronized void updatePeerReceiveBufferSize(long lastSeq, int size)
    {
        int previousPeerWindowSize = peerWindowSize;

        Log.logEntry(Log.SEV_DEBUG,
                     "Channel.updatePeerReceiveBufferSize: size = " + size
                     + " lastSeq = " + lastSeq + " sentSequence = "
                     + sentSequence + " peerWindowSize = " + peerWindowSize);

        peerWindowSize = size - (int) (sentSequence - lastSeq);

        Log.logEntry(Log.SEV_DEBUG,
                     "Channel.updatePeerReceiveBufferSize: New window size = "
                     + peerWindowSize);

        if ((previousPeerWindowSize == 0) && (peerWindowSize > 0)) {
            notify();    // unblock if we're waiting to send
        }
    }

    synchronized void freeReceiveBufferBytes(int size)
    {
        try {
            Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                         "Freed up " + size + " bytes on channel " + number);

            recvWindowUsed -= size;

            Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                         "recvWindowUsed = " + recvWindowUsed);

            if (session.updateMyReceiveBufferSize(this, prevAckno,
                                                  recvSequence,
                                                  prevWindowUsed,
                                                  recvWindowUsed,
                                                  recvWindowSize)) {
                prevAckno = recvSequence;
                prevWindowUsed = recvWindowUsed;
            }
        } catch (BEEPException e) {

            // do nothing
            Log.logEntry(Log.SEV_ALERT, e);
        }
    }

    /**
     * Method getAvailableWindow
     *
     *
     * @return int the amount of free buffer space
     * available.
     *
     * This is called from Session to provide a # used
     * to screen frame sizes against and enforce the
     * protocol.
     *
     */
    synchronized int getAvailableWindow()
    {
        return (recvWindowSize - recvWindowUsed);
    }

    /**
     * Used to set data that can be piggybacked on
     * a profile reply to a start channel request
     * (or any other scenario we choose)
     *
     * called by Channel Zero
     */
    void setStartData(String data)
    {
        startData = data;
    }

    /**
     * Used to get data that can be piggybacked on
     * a profile reply to a start channel request
     * (or any other scenario we choose)
     *
     * Could be called by users, profile implementors etc.
     * to fetch data off a profile response.
     *
     * @return String the attached data, if any
     */
    public String getStartData()
    {
        return startData;
    }
}
