/*
 * SessionImpl.java  $Revision: 1.14 $ $Date: 2003/11/19 17:30:06 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2001-2003 Huston Franklin.  All rights reserved.
 * Copyright (c) 2002 Kevin Kress.  All rights reserved.
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


import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

import sun.misc.BASE64Decoder;

import org.beepcore.beep.core.event.ChannelEvent;
import org.beepcore.beep.core.event.ChannelListener;
import org.beepcore.beep.core.event.SessionEvent;
import org.beepcore.beep.core.event.SessionResetEvent;
import org.beepcore.beep.core.event.SessionListener;
import org.beepcore.beep.util.StringUtil;


/**
 * This class encapsulates the notion of a BEEP Session (a relationship
 * between BEEP peers).
 * <p>
 * The implementor should sub-class <code>Session</code>'s abstract methods
 * for a given transport.
 * It's principal function is to sit on whatever network or referential
 * 'connection' exists between the BEEP peers, read and write BEEP frames and
 * deliver them to or receive them from the associated <code>Channel</code>.
 *
 * @todo Improvments could include sharing buffers from DataStream to here,
 * minimizing stream writes/reads, and pooling I/O threads.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.14 $, $Date: 2003/11/19 17:30:06 $
 *
 * @see Channel
 */
public abstract class SessionImpl implements Session {

    // Constants
    private static final SessionOperations[] ops =
    {new INITIALIZED_SessionOperations(),
     new GREETING_SENT_SessionOperations(),
     new ACTIVE_SessionOperations(),
     new TUNING_PENDING_SessionOperations(),
     new TUNING_SessionOperations(),
     new CLOSE_PENDING_SessionOperations(),
     new CLOSING_SessionOperations(),
     new CLOSED_SessionOperations(),
     new ABORTED_SessionOperations()};

    private static final int DEFAULT_CHANNELS_SIZE = 4;
    private static final int DEFAULT_PROPERTIES_SIZE = 4;
    private static final int DEFAULT_POLL_INTERVAL = 500;

    /** @todo check this */
    static final int MAX_PCDATA_SIZE = 4096;
    private static final int MAX_START_CHANNEL_WAIT = 60000;
    private static final int MAX_START_CHANNEL_INTERVAL = 100;

    private static final String CHANNEL_ZERO = "0";

    private static final String ERR_MALFORMED_XML_MSG = "Malformed XML";
    private static final String ERR_UNKNOWN_OPERATION_ELEMENT_MSG =
        "Unknown operation element";

    private static final byte[] OK_ELEMENT =
        StringUtil.stringToAscii("<ok />");

    // Instance Data
    private Log log = LogFactory.getLog(this.getClass());

    private int state;
    private long nextChannelNumber = 0;
    private ChannelImpl zero;
    private Hashtable channels = null;
    private Hashtable properties = null;
    private List sessionListenerList =
        Collections.synchronizedList(new LinkedList());
    private SessionListener[] sessionListeners = new SessionListener[0];
    private List channelListenerList =
        Collections.synchronizedList(new LinkedList());
    private ChannelListener[] channelListeners = new ChannelListener[0];
    private ProfileRegistry profileRegistry = null;
    private SessionCredential localCredential, peerCredential;
    private SessionTuningProperties tuningProperties = null;
    private Collection peerSupportedProfiles = null;
    private boolean overflow;
    private boolean allowChannelWindowUpdates;
    private DocumentBuilder builder;    // generic XML parser
    private String serverName;
    private boolean sentServerName = false;

    /**
     * Default Session Constructor.  A relationship between peers - a session -
     * consists of a set of profiles they share in common, and an ordinality
     * (to prevent new channel collision) so that the initiator starts odd
     * channels and the listener starts channels with even numbers.
     * @param registry The Profile Registry summarizing the profiles this
     *                 Session will support
     * @param firstChannel used internally in the API, an indication of the
     *        ordinality of the channels this peer can start, odd, or even.
     * @param localCred
     * @param peerCred
     * @param tuning
     * @param serverName
     */
    protected SessionImpl(ProfileRegistry registry, int firstChannel,
                      SessionCredential localCred, SessionCredential peerCred,
                      SessionTuningProperties tuning, String serverName)
        throws BEEPException
    {
        state = SESSION_STATE_INITIALIZED;
        allowChannelWindowUpdates = true;
        localCredential = localCred;
        peerCredential = peerCred;
        nextChannelNumber = firstChannel;
        overflow = false;
        profileRegistry = registry;
        channels = new Hashtable(DEFAULT_CHANNELS_SIZE);
        properties = new Hashtable(DEFAULT_PROPERTIES_SIZE);
        tuningProperties = tuning;
        this.serverName = serverName;

        try {
            builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new BEEPException("Invalid parser configuration");
        }
    }

    /**
     * Initializes the <code>Session</code>.  Initializes Channel Zero and its
     * listener. Sends a greeting and waits for corresponding greeting.
     *
     * @throws BEEPException
     */
    protected void init() throws BEEPException
    {
        this.peerSupportedProfiles = null;

        GreetingListener greetingListener = new GreetingListener();

        zero = ChannelImpl.createChannelZero(this, greetingListener,
                                             new ChannelImpl.MessageListenerAdapter(new ChannelZeroListener()));

        channels.put(CHANNEL_ZERO, zero);

        // send greeting
        sendGreeting();
        changeState(Session.SESSION_STATE_GREETING_SENT);

        // start our listening thread we can now receive a greeting
        this.enableIO();

        // blocks until greeting is received or MAX_GREETING_WAIT is reached
        int waitCount = 0;

        while ((state < SESSION_STATE_ACTIVE)
                && (waitCount < MAX_START_CHANNEL_WAIT)) {
            try {
                synchronized (greetingListener) {

                    //zero.wait(MAX_START_CHANNEL_INTERVAL);
                    greetingListener.wait(MAX_START_CHANNEL_INTERVAL);

                    waitCount += MAX_START_CHANNEL_INTERVAL;
                }
            } catch (InterruptedException e) {
                waitCount += MAX_START_CHANNEL_INTERVAL;
            }
        }

        // check the channel state and return the appropriate exception
        if (state != SESSION_STATE_ACTIVE) {
            throw new BEEPException("Greeting exchange failed");
        }
    }

    /**
     * A reentrant version of init() that doesn't block the
     * first I/O thread waiting on a greeting when it should die
     * and go away.
     *
     * @throws BEEPException
     */
    protected void tuningInit() throws BEEPException
    {
        log.debug("Session.tuningInit");

        this.peerSupportedProfiles = null;

        GreetingListener greetingListener = new GreetingListener();

        zero = ChannelImpl.createChannelZero(this, greetingListener,
                                             new ChannelImpl.MessageListenerAdapter(new ChannelZeroListener()));
        channels.put(CHANNEL_ZERO, zero);

        // send greeting
        sendGreeting();
        changeState(Session.SESSION_STATE_GREETING_SENT);

        // start our listening thread we can now receive a greeting
        this.enableIO();
    }

    /**
     * adds the listener from the list of listeners to be notified
     * of future events.
     *
     * @see #removeChannelListener
     */
    public void addChannelListener(ChannelListener l)
    {
        channelListenerList.add(l);
        channelListeners =
            (ChannelListener[]) channelListenerList.toArray(channelListeners);
    }

    /**
     * adds the listener from the list of listeners to be notified
     * of future events.
     *
     * @see #removeSessionListener
     */
    public void addSessionListener(SessionListener l)
    {
        sessionListenerList.add(l);
        sessionListeners =
            (SessionListener[]) sessionListenerList.toArray(sessionListeners);
    }

    /**
     * Closes the <code>Session</code> gracefully. The profiles for
     * the open channels on the session may veto the close request.
     *
     * @throws BEEPException
     */
    public void close() throws BEEPException
    {
        if (log.isDebugEnabled()) {
            log.debug("Closing Session with " + channels.size() + " channels");
        }

        changeState(SESSION_STATE_CLOSE_PENDING);

        Iterator i = channels.values().iterator();

        while (i.hasNext()) {
            ChannelImpl ch = (ChannelImpl) i.next();

            // if this channel is not zero, call the channel's scl
            if (ch.getNumber() == 0) {
                continue;
            }

            StartChannelListener scl =
                profileRegistry.getStartChannelListener(this.tuningProperties,
                                                        ch.getProfile());

            if (scl == null) {
                continue;
            }

            // check locally first to see if it is ok to close the channel
            try {
                scl.closeChannel(ch);
            } catch (CloseChannelException cce) {
                changeState(SESSION_STATE_ACTIVE);
                // @todo rollback notification
                throw new BEEPException("Close Session rejected by local "
                                        + "channel " + ch.getProfile());
            }
        }

        changeState(SESSION_STATE_CLOSING);

        try {
            // check with the peer to see if it is ok to close the channel
            zero.close();
        } catch (BEEPError e) {
            changeState(SESSION_STATE_ACTIVE);
            throw e;
        } catch (BEEPException e) {
            terminate(e.getMessage());
            log.error("Error sending close", e);
            throw e;
        }

        this.disableIO();
        // @todo close the socket

        channels.clear();
        zero = null;

        this.changeState(SESSION_STATE_CLOSED);
        fireSessionTerminated();
    }

    /**
     * Get the local <code>SessionCredential</code> for this session.
     *
     *
     * @return May return <code>null</code> if this session has not
     *         been authenticated
     */
    public SessionCredential getLocalCredential()
    {
        return localCredential;
    }

    /**
     * Get our peer's <code>SessionCredential</code> for this session.
     *
     *
     * @return May return <code>null</code> if this session has not
     *         been authenticated
     */
    public SessionCredential getPeerCredential()
    {
        return peerCredential;
    }

    /**
     * Returns the profiles sent by the remote peer in the greeting.
     */
    public Collection getPeerSupportedProfiles()
    {
        return this.peerSupportedProfiles;
    }

    /**
     * Returns the <code>ProfileRegistry</code> for <code>Session</code>.
     *
     * @return A <code>ProfileRegistry</code>.
     * @see ProfileRegistry
     */
    public ProfileRegistry getProfileRegistry()
    {
        return profileRegistry;
    }

    /**
     * Returns the state of <code>Session</code>.
     *
     * @return Session state (see the Constants in this class).
     */
    public int getState()
    {
        return this.state;
    }

    /**
     * Indicates whehter or not this session is in the initiator role.
     */
    public boolean isInitiator()
    {
        return ((nextChannelNumber % 2) == 1);
    }

    /**
     * Removes the listener from the list of listeners to be notified
     * of future events. Note that the listener will be notified of
     * events which have already happened and are in the process of
     * being dispatched.
     *
     * @see #addChannelListener
     */
    public void removeChannelListener(ChannelListener l)
    {
        channelListenerList.remove(l);
        channelListeners =
            (ChannelListener[]) channelListenerList.toArray(channelListeners);
    }

    /**
     * Removes the listener from the list of listeners to be notified
     * of future events. Note that the listener will be notified of
     * events which have already happened and are in the process of
     * being dispatched.
     *
     * @see #addSessionListener
     */
    public void removeSessionListener(SessionListener l)
    {
        sessionListenerList.remove(l);
        sessionListeners =
            (SessionListener[]) sessionListenerList.toArray(sessionListeners);
    }

    public Channel startChannel(String profile)
            throws BEEPException, BEEPError
    {
        return startChannel(profile, (RequestHandler)null);
    }

    public Channel startChannel(String profile, MessageListener listener)
            throws BEEPException, BEEPError
    {
        StartChannelProfile p = new StartChannelProfile(profile);
        LinkedList l = new LinkedList();

        l.add(p);

        return startChannelRequest(l, listener, false);
    }

    public Channel startChannel(String profile, RequestHandler handler)
            throws BEEPException, BEEPError
    {
        StartChannelProfile p = new StartChannelProfile(profile);
        LinkedList l = new LinkedList();

        l.add(p);

        return startChannelRequest(l, handler, false);
    }

    public Channel startChannel(String profile, boolean base64Encoding,
                                String data)
            throws BEEPException, BEEPError
    {
        StartChannelProfile p = new StartChannelProfile(profile,
                                                        base64Encoding, data);
        return startChannel(p, null);
    }
    
    public Channel startChannel(String profile, boolean base64Encoding,
                                String data, MessageListener listener)
            throws BEEPException, BEEPError
    {
        StartChannelProfile p = new StartChannelProfile(profile,
                                                        base64Encoding, data);
        LinkedList l = new LinkedList();

        l.add(p);

        return startChannelRequest(l, listener, false);
    }
    
    public Channel startChannel(StartChannelProfile profile, RequestHandler handler)
            throws BEEPException, BEEPError
    {
        LinkedList l = new LinkedList();

        l.add(profile);

        return startChannelRequest(l, handler, false);
    }

    public Channel startChannel(Collection profiles, MessageListener listener)
        throws BEEPException, BEEPError
    {
        return startChannelRequest(profiles, listener, false);
    }

    Channel startChannelRequest(Collection profiles, MessageListener listener,
                                boolean tuning)
        throws BEEPException, BEEPError
    {
        return startChannelRequest(profiles,
                                   listener == null ? null : new ChannelImpl.MessageListenerAdapter(listener),
                                   tuning);
    }

    public Channel startChannel(Collection profiles, RequestHandler handler)
        throws BEEPException, BEEPError
    {
        return startChannelRequest(profiles, handler, false);
    }

    Channel startChannelRequest(Collection profiles, RequestHandler handler,
                                boolean tuning)
            throws BEEPException, BEEPError
    {

        String channelNumber = getNextFreeChannelNumber();

        // create the message in a buffer and send it
        StringBuffer startBuffer = new StringBuffer();

        startBuffer.append("<start number='");
        startBuffer.append(channelNumber);
        if (serverName != null && !sentServerName) {
            startBuffer.append("' serverName='");
            startBuffer.append(serverName);
        }
        startBuffer.append("'>");

        Iterator i = profiles.iterator();

        while (i.hasNext()) {
            StartChannelProfile p = (StartChannelProfile) i.next();

            // @todo maybe we should check these against peerSupportedProfiles
            startBuffer.append("<profile uri='");
            startBuffer.append(p.uri);
            startBuffer.append("' ");

            if (p.data == null) {
                startBuffer.append(" />");
            } else {
                if (p.base64Encoding) {
                    startBuffer.append("encoding='base64' ");
                }

                startBuffer.append("><![CDATA[");
                startBuffer.append(p.data);
                startBuffer.append("]]></profile>");
            }
        }

        startBuffer.append("</start>");

        // @todo handle the data element
        // Create a channel
        ChannelImpl ch = new ChannelImpl(null, channelNumber, handler, false,
                                         this);

        // Make a message
        OutputDataStream ds =
            new ByteOutputDataStream(MimeHeaders.BEEP_XML_CONTENT_TYPE,
                                     StringUtil.stringBufferToAscii(startBuffer));

        if (tuning) {
            this.changeState(SESSION_STATE_TUNING_PENDING);
            this.changeState(SESSION_STATE_TUNING);
            this.zero.setState(ChannelImpl.STATE_TUNING);
        }

        // Tell Channel Zero to start us up
        StartReplyListener reply = new StartReplyListener(ch);
        synchronized (reply) {
            this.zero.sendMSG(ds, reply);
            try {
                reply.wait();
            } catch (InterruptedException e) {
                log.error("Interrupted waiting for reply", e);
                throw new BEEPException("Interrupted waiting for reply");
            }
        }

        // check the channel state and return the appropriate exception
        if (reply.isError()) {
            reply.getError().fillInStackTrace();
            throw reply.getError();
        }

        if (ch.getState() != ChannelImpl.STATE_ACTIVE) {
            throw new BEEPException("Error channel state (" +
                                    ch.getState() + ")");
        }

        if (tuning) {
            ch.setState(ChannelImpl.STATE_TUNING);
        }

        if (serverName != null) {
            sentServerName = true;
        }
    
        fireChannelStarted(ch);
        return ch;
    }

    /**
     * This method is used to terminate the session when there is an
     * non-recoverable error in the BEEP protocol (framing error, etc.).
     *
     *
     * @param reason
     *
     */
    public void terminate(String reason)
    {
        log.error(reason);

        try {
            this.changeState(SESSION_STATE_ABORTED);
        } catch (BEEPException e) {

            // Ignore this since we are terminating anyway.
        }

        this.disableIO();
        channels.clear();

        zero = null;

        fireSessionTerminated();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString() + " (" + (isInitiator() ? "I " : "L ") +
            getStateString() + ")";
    }

    synchronized void changeState(int newState) throws BEEPException {
        try {
            ops[state].changeState(this, newState);
        } catch (BEEPException e) {
            e.printStackTrace();
            throw e;
        }

        if (log.isDebugEnabled()) {
            log.debug("State changed to " + newState);
        }
    }

    /**
     * This method is intended for use by tranport specific Sessions to create
     * a new <code>Frame</code> object representing a BEEP MSG, RPY, ERR,
     * or NUL frames.
     *
     * @return a <code>Frame</code> for the specified values
     *
     * @throws BEEPException
     */
    protected Frame createFrame(byte[] header, int headerLength)
            throws BEEPException
    {
        Frame f = Frame.parseHeader(this, header, headerLength);

        // The window size and frame size have nothing in common.
        if (f.getSize() > ((ChannelImpl)f.getChannel()).getAvailableWindow()) {
            throw new BEEPException("Payload size is greater than channel "
                                    + "window size");
        }

        return f;
    }

    /**
     * Method disableIO
     *
     *
     */
    protected abstract void disableIO();

    /**
     * Method enableIO
     *
     *
     */
    protected abstract void enableIO();

    /**
     * Returns the channel's available window size.
     */
    protected int getChannelAvailableWindow(int channel) throws BEEPException
    {
        ChannelImpl ch = (ChannelImpl) channels.get(Integer.toString(channel));

        if (ch == null) {
            throw new BEEPException("Session call on nonexistent channel.");
        }

        return ch.getAvailableWindow();
    }

    /**
     * Get the channel number as a String
     *
     *
     * @param channel
     *
     */
    protected String getChannelNumberAsString(Channel channel)
    {
        return ((ChannelImpl)channel).getNumberAsString();
    }

    /**
     * Returns the maximum frame size that a channel should send for
     * this session.
     *
     *
     * @throws BEEPException
     *
     */
    protected abstract int getMaxFrameSize() throws BEEPException;

    /**
     * Method postFrame
     *
     * @param f
     *
     * @throws BEEPException
     *
     */
    protected boolean postFrame(Frame f) throws BEEPException {
        try {
            return ops[state].postFrame(this, f);
        } catch (BEEPException e) {
            this.terminate(e.getMessage());

            return false;
        } catch (Throwable e) {
            log.error("Error posting frame", e);
            this.terminate("Uncaught exception, terminating session");

            return false;
        }
    }

    /**
     * This method is used by a tuning profile to reset the session after the
     * tuning is complete.
     *
     *
     * @return A new <code>Session</code> with the tuning complete.
     *
     */
    protected abstract Session reset(SessionCredential localCred,
                                     SessionCredential peerCred,
                                     SessionTuningProperties tuning,
                                     ProfileRegistry registry, Object argument)
        throws BEEPException;

    /**
     * Implement this method to send frames and on the sub-classed transport.
     *
     *
     * @param f BEEP frame to send.
     *
     *
     * @throws BEEPException
     */
    protected abstract void sendFrame(Frame f) throws BEEPException;

    /**
     * Method setLocalCredential
     *
     *
     * @param cred
     *
     */
    protected void setLocalCredential(SessionCredential cred)
    {
        localCredential = cred;
    }

    /**
     * Method setPeerCredential
     *
     *
     * @param cred
     *
     */
    protected void setPeerCredential(SessionCredential cred)
    {
        peerCredential = cred;
    }

    /**
     * sets the tuning properties for this session
     * @param tuning
     * @see SessionTuningProperties
     */
    protected void setTuningProperties(SessionTuningProperties tuning)
    {
        tuningProperties = tuning;
    }

    public SessionTuningProperties getTuningProperties()
    {
        return tuningProperties;
    }

    public String getServerName()
    {
        return serverName;
    }
    
    /**
     * This method is designed to allow for flow control across the multiplexed
     * connection we have. <p> The idea is to throttle data being sent over
     * this session to be manageable per Channel, so that a given Channel
     * doesn't take up all the bandwidth. <p>
     * This method restricts the bufferSize, per the beep spec, to be at most
     * two-thirds of the socket's receiveBufferSize.  If a size is requested
     * beyond that, an exception is thrown.
     *
     *
     * @param channel
     * @param currentSeq
     * @param currentAvail
     *
     * @return true if the Receive Buffer Size was updated
     *
     * @exception throws BEEPException if a specified buffer size is larger
     *    than what's available on the Socket.
     *
     * @throws BEEPException
     */
    protected abstract boolean updateMyReceiveBufferSize(Channel channel,
                                                         long currentSeq,
                                                         int currentAvail)
        throws BEEPException;

    // @todo update the java-doc to correctly identify the params

    /**
     * Method updatePeerReceiveBufferSize
     *
     *
     *
     * @param channelNum
     * @param lastSeq
     * @param size
     *
     *
     * @throws BEEPException
     */
    protected void updatePeerReceiveBufferSize(int channelNum, long lastSeq,
                                               int size)
            throws BEEPException
    {
        ChannelImpl channel = getValidChannel(channelNum);

        channel.updatePeerReceiveBufferSize(lastSeq, size);
    }

    /**
     * The Initiator Oriented close channel call...but this one is not an
     * external call, it's invoked from Channel.close();
     *
     * @param channel
     * @param code
     * @param xmlLang
     *
     * @throws BEEPException
     */
    void closeChannel(ChannelImpl channel, int code, String xmlLang)
            throws BEEPException
    {

        // Construct Message
        StringBuffer closeBuffer = new StringBuffer();

        closeBuffer.append("<close number='");
        closeBuffer.append(channel.getNumberAsString());
        closeBuffer.append("' code='");
        closeBuffer.append(code);

        if (xmlLang != null) {
            closeBuffer.append("' xml:lang='");
            closeBuffer.append(xmlLang);
        }

        closeBuffer.append("' />");

        // Lock necessary because we have to know the msgNo
        // before we send the message, in order to be able
        // to associate the reply with this start request
        CloseReplyListener reply = new CloseReplyListener(channel);
        synchronized (reply) {
            OutputDataStream ds =
                new ByteOutputDataStream(MimeHeaders.BEEP_XML_CONTENT_TYPE,
                                         StringUtil.stringBufferToAscii(closeBuffer));

            this.zero.sendMSG(ds,
                              reply);
            try {
                reply.wait();
            } catch (InterruptedException e) {
                log.error("Error waiting for reply", e);
                throw new BEEPException("Interrupted waiting for reply");
            }
        }

        // check the channel state and return the appropriate exception
        if (reply.isError()) {
            reply.getError().fillInStackTrace();
            throw reply.getError();
        }

        if (channel.getState() != ChannelImpl.STATE_CLOSED) {
            throw new BEEPException("Error channel state (" +
                                    channel.getState() + ")");
        }

        fireChannelClosed(channel);
    }

    ChannelImpl getValidChannel(int number) throws BEEPException
    {
        ChannelImpl ch = (ChannelImpl) channels.get(Integer.toString(number));

        if (ch == null) {
            throw new BEEPException("Session call on nonexistent channel.");
        }

        return ch;
    }

    void sendProfile(String uri, String datum, ChannelImpl ch)
            throws BEEPException
    {

        // Send the profile
        StringBuffer sb = new StringBuffer();

        sb.append("<profile uri='");
        sb.append(uri);

        if (datum != null) {
            sb.append("'><![CDATA[");
            sb.append(datum);
            sb.append("]]></profile>");
        } else {
            sb.append("' />");
        }

        OutputDataStream ds =
            new ByteOutputDataStream(MimeHeaders.BEEP_XML_CONTENT_TYPE,
                                     StringUtil.stringBufferToAscii(sb));

        // Store the Channel
        channels.put(ch.getNumberAsString(), ch);
        ((MessageMSG)zero.getAppData()).sendRPY(ds);
    }

    private void fireChannelClosed(Channel c)
    {
        ChannelListener[] l = this.channelListeners;
        if (l.length == 0)
            return;

        ChannelEvent e = new ChannelEvent(c);
        for (int i=0; i<l.length; ++i) {
            l[i].channelClosed(e);
        }
    }

    void fireChannelStarted(Channel c)
    {
        ChannelListener[] l = this.channelListeners;
        if (l.length == 0)
            return;

        ChannelEvent e = new ChannelEvent(c);
        for (int i=0; i<l.length; ++i) {
            l[i].channelStarted(e);
        }
    }

    private void fireGreetingReceived()
    {
        SessionListener[] l = this.sessionListeners;
        if (l.length == 0)
            return;

        SessionEvent e = new SessionEvent(this);
        for (int i=0; i<l.length; ++i) {
            l[i].greetingReceived(e);
        }
    }

    private void fireSessionClosed()
    {
        SessionListener[] l = this.sessionListeners;
        if (l.length == 0)
            return;

        SessionEvent e = new SessionEvent(this);
        for (int i=0; i<l.length; ++i) {
            l[i].sessionClosed(e);
        }
    }

    private void fireSessionTerminated()
    {
        SessionListener[] l = this.sessionListeners;
        if (l.length == 0)
            return;

        SessionEvent e = new SessionEvent(this);
        for (int i=0; i<l.length; ++i) {
            l[i].sessionClosed(e);
        }
    }

    protected void fireSessionReset(Session newSession)
    {
        SessionListener[] l = this.sessionListeners;
        if (l.length == 0)
            return;

        SessionResetEvent e = new SessionResetEvent(this, newSession);
        for (int i=0; i<l.length; ++i) {
            l[i].sessionReset(e);
        }
    }

    /**
     * This method is called when Channel Zero receives - from our
     * session peer - a request to close a channel.
     *
     * @param channelNumber
     * @param code
     * @param xmlLang
     * @param data
     *
     * @throws BEEPException
     */
    private void receiveCloseChannel(String channelNumber, String code,
                                     String xmlLang, String data)
        throws BEEPError
    {

        // @todo fix close channel
        if (channelNumber.equals(CHANNEL_ZERO)) {
            receiveCloseChannelZero();

            return;
        }

        enableIO();
                
        ChannelImpl channel = (ChannelImpl) channels.get(channelNumber);

        if (channel == null) {
            throw new BEEPError(BEEPError.CODE_PARAMETER_INVALID,
                                "Close requested for nonexistent channel");
        }

        try {
            StartChannelListener scl =
                profileRegistry.getStartChannelListener(this.tuningProperties,
                                                        channel.getProfile());

            scl.closeChannel(channel);
            channel.setState(ChannelImpl.STATE_CLOSING);
        } catch (BEEPError x) {
            channel.setState(ChannelImpl.STATE_CLOSING);

            throw x;
        }

        // Send an ok
        OutputDataStream sds =
            new ByteOutputDataStream(MimeHeaders.BEEP_XML_CONTENT_TYPE,
                                     OK_ELEMENT);

        try {
            ((MessageMSG)zero.getAppData()).sendRPY(sds);
        } catch (BEEPException x) {
            terminate("Error sending RPY for <close>");

            return;
        }

        // We're past the CCL approval
        channel.setState(ChannelImpl.STATE_CLOSED);
        channels.remove(channel.getNumberAsString());
        fireChannelClosed(channel);
    }

    private void receiveCloseChannelZero() throws BEEPError
    {

        // closing the session
        // @todo fireEvent(SESSION_STATE_CLOSING);

        if (log.isDebugEnabled()) {
            log.debug("Closing Session with " + channels.size() + " channels");
        }

        try {
            changeState(SESSION_STATE_CLOSE_PENDING);
            changeState(SESSION_STATE_CLOSING);
        } catch (BEEPException x) {
            terminate("Error changing Session state to closing.");
            return;
        }

        Iterator i = channels.values().iterator();

        while (i.hasNext()) {
            ChannelImpl ch = (ChannelImpl) i.next();

            // if this channel is not zero, call the channel's scl
            if (ch.getNumber() == 0) {
                continue;
            }

            StartChannelListener scl =
                profileRegistry.getStartChannelListener(this.tuningProperties,
                                                        ch.getProfile());

            // check locally first to see if it is ok to close the channel
            try {
                scl.closeChannel(ch);
                i.remove();
            } catch (CloseChannelException e) {
                try {
                    changeState(SESSION_STATE_ACTIVE);

                    enableIO();
                    throw e;
                } catch (BEEPException x) {
                    terminate("Error changing Session state from closing " +
                              "to active");

                    return;
                }
            }
            fireChannelClosed(ch);
        }

        OutputDataStream sds =
            new ByteOutputDataStream(MimeHeaders.BEEP_XML_CONTENT_TYPE,
                                     OK_ELEMENT);

        try {
            ((MessageMSG)zero.getAppData()).sendRPY(sds);
        } catch (BEEPException x) {
            terminate("Error sending RPY for <close> for channel 0");

            return;
        }

        try {
            this.changeState(SESSION_STATE_CLOSED);
        } catch (BEEPException e) {
            log.error("Error changing state", e);
        }

        fireSessionClosed();
    }

    /**
     * Returns the next free channel number as a string.
     * @return Channel number.
     */
    private synchronized String getNextFreeChannelNumber()
    {
        long i;

        synchronized (this) {

            // next channel needs to be incremented by two since the peer
            // uses every other channel (see beep spec)
            i = nextChannelNumber;
            nextChannelNumber += 2;
        }

        String nextChannel = Long.toString(i);

        if (overflow) {

            // Equally insane collision check after the rollover
            if (channels.get(nextChannel) != null) {
                return getNextFreeChannelNumber();
            }
        }

        // Insane bounds check that will probably never happen
        if (nextChannelNumber > Frame.MAX_CHANNEL_NUMBER) {
            nextChannelNumber = nextChannelNumber % Frame.MAX_CHANNEL_NUMBER;
            overflow = true;
        }

        // Warning: nextChannelNumber is a long to detect overflow
        return nextChannel;
    }
    
    private String getStateString() {
        switch (state) {
            case SESSION_STATE_INITIALIZED:
                return "initialized";
            case SESSION_STATE_GREETING_SENT:
                return "greeting sent";
            case SESSION_STATE_ACTIVE:
                return "active";
            case SESSION_STATE_TUNING_PENDING:
                return "tuning pending";
            case SESSION_STATE_TUNING:
                return "tuning";
            case SESSION_STATE_CLOSE_PENDING:
                return "close pending";
            case SESSION_STATE_CLOSING:
                return "closing";
            case SESSION_STATE_CLOSED:
                return "closed";
            case SESSION_STATE_ABORTED:
                return "aborted";
            default:
                return "unknown";
        }
    }

    /**
     *  Listener oriented Start Channel call, a call here means that
     *  we've received a start channel request over the wire.
     */
    private void processStartChannel(String channelNumber,
                                     Collection profiles)
            throws BEEPError
    {
        StartChannelListener scl;
        ChannelImpl ch = null;
        Iterator i = profiles.iterator();

        while (i.hasNext()) {
            StartChannelProfile p = (StartChannelProfile) i.next();

            scl =
                profileRegistry.getStartChannelListener(this.tuningProperties,
                                                        p.uri);

            if (scl == null) {
                continue;
            }

            ch = new ChannelImpl(p.uri, channelNumber, this);

            try {
                String encoding = p.base64Encoding ? "base64" : "none";

                scl.startChannel(ch, encoding, p.data);
            } catch (TuningResetException e) {
                log.debug("Leaving profile response to Tuning Profile CCL");

                fireChannelStarted(ch);

                return;
            } catch (StartChannelException e) {
                this.enableIO();

                try {
                    ((MessageMSG)zero.getAppData()).sendERR(e);
                } catch (BEEPException x) {
                    terminate("Error sending ERR response to start channel");
                }

                return;
            }

            if (p.data != null && ch.getStartData() == null) {
                byte[] data;
                if (p.base64Encoding) {
                    try {
                        data = new BASE64Decoder().decodeBuffer(p.data);
                    } catch (IOException e) {
                        ch.abort();
                        this.enableIO();
                        throw new BEEPError(BEEPError.CODE_REQUESTED_ACTION_ABORTED,
                                            "Error parsing piggybacked data.");
                    }
                } else {
                    try {
                        data = p.data.getBytes("UTF-8"); 
                    } catch (UnsupportedEncodingException e) {
                        terminate("UTF-8 not supported");
                        return;
                    }
                }
                
                PiggybackedMSG msg = new PiggybackedMSG(ch, data,
                                                        p.base64Encoding);

                ch.setState(ChannelImpl.STATE_STARTING);

                try {
                    ch.addPiggybackedMSG(msg);
                } catch (BEEPException e) {
                    terminate("Error sending profile. " + e.getMessage());

                    return;
                }
            } else {
                try {
                    sendProfile(p.uri, ch.getStartData(), ch);
                    ch.setState(ChannelImpl.STATE_ACTIVE);
                } catch (BEEPException e) {
                    terminate("Error sending profile. " + e.getMessage());

                    return;
                }

                fireChannelStarted(ch);

                if (p.data == null && ch.getState() != ChannelImpl.STATE_TUNING) {
                    this.enableIO();
                }
            }

            return;
        }

        this.enableIO();

        try {
            ((MessageMSG)zero.getAppData()).sendERR(BEEPError.CODE_REQUESTED_ACTION_NOT_TAKEN2, "all requested profiles are unsupported");
        } catch (Exception x) {
            terminate("Error sending error. " + x.getMessage());
        }
    }

    private Element processMessage(Message message) throws BEEPException
    {

        // check the message content type
        if (!message.getDataStream().getInputStream().getContentType().equals(MimeHeaders.BEEP_XML_CONTENT_TYPE)) {
            throw new BEEPException("Invalid content type for this message");
        }

        // parse the stream
        Document doc;

        try {
            doc = builder.parse(message.getDataStream().getInputStream());
        } catch (SAXException se) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        } catch (IOException ioe) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        }

        if (doc == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        }

        Element topElement = doc.getDocumentElement();

        if (topElement == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        }

        return topElement;
    }

    private void sendGreeting() throws BEEPException
    {
        log.debug("sendGreeting");

        // get the greeting from the session
        byte[] greeting = SessionImpl.this.getProfileRegistry().getGreeting(this);
        ByteOutputDataStream f =
            new ByteOutputDataStream(MimeHeaders.BEEP_XML_CONTENT_TYPE,
                                     greeting);

        MessageMSG m = new MessageMSGImpl(this.zero, 0, null);

        // send the greeting
        m.sendRPY(f);
    }

    private class ChannelZeroListener implements MessageListener {

        public void receiveMSG(Message message)
            throws BEEPError, AbortChannelException
        {
            Element topElement;

            try {
                topElement = processMessage(message);
            } catch (BEEPException e) {
                throw new BEEPError(BEEPError.CODE_GENERAL_SYNTAX_ERROR,
                                    ERR_MALFORMED_XML_MSG);
            }

            String elementName = topElement.getTagName();

            if (elementName == null) {
                throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                    ERR_MALFORMED_XML_MSG);
            }

            // is this MSG a <start>
            if (elementName.equals("start")) {
                log.debug("Received a start channel request");

                String channelNumber = topElement.getAttribute("number");

                if (channelNumber == null) {
                    throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                        "Malformed <start>: no channel number");
                }

                // this attribute is implied
                String serverName = topElement.getAttribute("serverName");
                NodeList profiles =
                    topElement.getElementsByTagName("profile");

                if (profiles == null) {
                    throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                        "Malformed <start>: no profiles");
                }

                LinkedList profileList = new LinkedList();

                for (int i = 0; i < profiles.getLength(); i++) {
                    Element profile = (Element) profiles.item(i);
                    String uri = profile.getAttribute("uri");

                    if (uri == null) {
                        throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                            "no profiles in start");
                    }

                    String encoding = profile.getAttribute("encoding");
                    boolean b64;

                    if ((encoding == null) || encoding.equals("")) {
                        b64 = false;
                    } else if (encoding.equalsIgnoreCase("base64")) {
                        b64 = true;
                    } else if (encoding.equalsIgnoreCase("none")) {
                        b64 = false;
                    } else {
                        throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                            "unkown encoding in start");
                    }

                    String data = null;
                    Node dataNode = profile.getFirstChild();

                    if (dataNode != null) {
                        data = dataNode.getNodeValue();

                        if (data.length() > MAX_PCDATA_SIZE) {
                            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                                "Element's PCDATA exceeds " +
                                                "the maximum size");
                        }
                    }

                    profileList.add(new StartChannelProfile(uri, b64, data));
                }

                SessionImpl.this.zero.setAppData(message);
                SessionImpl.this.processStartChannel(channelNumber, profileList);
            }

            // is this MSG a <close>
            else if (elementName.equals("close")) {
                log.debug("Received a channel close request");
                
                try {
                    String channelNumber = topElement.getAttribute("number");

                    if (channelNumber == null) {
                        throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                            "Malformed <close>: no channel number");
                    }

                    String code = topElement.getAttribute("code");

                    if (code == null) {
                        throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                            "Malformed <close>: no code attribute");
                    }

                    // this attribute is implied
                    String xmlLang = topElement.getAttribute("xml:lang");
                    String data = null;
                    Node dataNode = topElement.getFirstChild();

                    if (dataNode != null) {
                        data = dataNode.getNodeValue();

                        if (data.length() > MAX_PCDATA_SIZE) {
                            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                                "Element's PCDATA exceeds " +
                                                "the maximum size");
                        }
                    }
                    SessionImpl.this.zero.setAppData(message);
                    SessionImpl.this.receiveCloseChannel(channelNumber, code,
                                                         xmlLang, data);
                } catch (BEEPError e) {
                    enableIO();
                    throw e;
                }

            } else {
                throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                    ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
            }
        }
    }

    private class GreetingListener implements ReplyListener {

        public void receiveRPY(Message message)
        {
            try {
                Element topElement = processMessage(message);

                // is this RPY a <greeting>
                String elementName = topElement.getTagName();

                if (elementName == null) {
                    throw new BEEPException(ERR_MALFORMED_XML_MSG);
                } else if (!elementName.equals("greeting")) {
                    throw new BEEPException(ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
                }

                log.debug("Received a greeting");

                // this attribute is implied
                String features = topElement.getAttribute("features");

                // This attribute has a default value
                String localize = topElement.getAttribute("localize");

                if (localize == null) {
                    localize = Constants.LOCALIZE_DEFAULT;
                }

                // Read the profiles - note, the greeting is valid
                // with 0 profiles
                NodeList profiles =
                    topElement.getElementsByTagName("profile");

                if (profiles.getLength() > 0) {
                    LinkedList profileList = new LinkedList();

                    for (int i = 0; i < profiles.getLength(); i++) {
                        Element profile = (Element) profiles.item(i);
                        String uri = profile.getAttribute("uri");

                        if (uri == null) {
                            throw new BEEPException("Malformed profile");
                        }

                        String encoding = profile.getAttribute("encoding");

                        // encoding is not allowed in greetings
                        if (encoding != null) {

                            // @todo check this
                            // terminate("Invalid attribute 'encoding' in greeting.");
                            // return;
                        }

                        profileList.add(i, uri);
                    }

                    SessionImpl.this.peerSupportedProfiles =
                        Collections.unmodifiableCollection(profileList);
                }

                changeState(Session.SESSION_STATE_ACTIVE);

                synchronized (this) {
                    this.notifyAll();
                }
            } catch (BEEPException e) {
                terminate("Problem with RPY: " + e.getMessage());
            }
        }

        public void receiveERR(Message message)
        {
            terminate("Received an unexpected ERR");
        }

        public void receiveANS(Message message)
        {
            terminate("Received an unexpected ANS");
        }

        public void receiveNUL(Message message)
        {
            terminate("Received an unexpected NUL");
        }
    }

    private class StartReplyListener implements ReplyListener {

        ChannelImpl channel;
        BEEPError error;

        StartReplyListener(ChannelImpl channel)
        {
            this.channel = channel;
            this.error = null;
        }

        boolean isError() {
            return this.error != null;
        }

        BEEPError getError() {
            return this.error;
        }

        public void receiveRPY(Message message)
        {
            try {
                Element topElement = processMessage(message);

                // is this RPY a <greeting>
                String elementName = topElement.getTagName();

                if (elementName == null) {
                    throw new BEEPException(ERR_MALFORMED_XML_MSG);

                    // is this RPY a <profile>
                } else if (elementName.equals("profile")) {
                    try {
                        String uri = topElement.getAttribute("uri");

                        if (uri == null) {
                            throw new BEEPException("Malformed profile");
                        }

                        String encoding =
                            topElement.getAttribute("encoding");

                        if (encoding == null) {
                            encoding = Constants.ENCODING_NONE;
                        }

                        // see if there is data and then turn it into a message
                        Node dataNode = topElement.getFirstChild();
                        String data = null;

                        if (dataNode != null) {
                            data = dataNode.getNodeValue();

                            if (data.length() > MAX_PCDATA_SIZE) {
                                throw new BEEPException("Element's PCDATA " +
                                                        "exceeds the " +
                                                        "maximum size");
                            }
                        }

                        channel.setEncoding(encoding);
                        channel.setProfile(uri);
                        channel.setStartData(data);

                        // set the state
                        channel.setState(ChannelImpl.STATE_ACTIVE);
                        channels.put(channel.getNumberAsString(), channel);

                        /**
                         * @todo something with data
                         */

                        // release the block waiting for the channel
                        // to start or close
                        synchronized (this) {
                            this.notify();
                        }
                    } catch (Exception x) {
                        throw new BEEPException(x);
                    }
                } else {
                    throw new BEEPException(ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
                }
            } catch (BEEPException e) {
                terminate("Problem with RPY: " + e.getMessage());
            }
        }

        public void receiveERR(Message message)
        {
            BEEPError err;

            try {
                err = BEEPError.convertMessageERRToException(message);
            } catch (BEEPException e) {
                terminate(e.getMessage());

                return;
            }

            log.error("Received an error in response to a start. code="
                      + err.getCode() + " diagnostic=" + err.getDiagnostic());

            this.error = err;

            channel.setState(ChannelImpl.STATE_CLOSED);
            channels.remove(channel.getNumberAsString());

            // release the block waiting for the channel to start or close
            synchronized (this) {
                this.notify();
            }
        }

        public void receiveANS(Message message)
        {
            terminate("Received an unexpected ANS");
        }

        public void receiveNUL(Message message)
        {
            terminate("Received an unexpected NUL");
        }
    }

    private class CloseReplyListener implements ReplyListener {

        ChannelImpl channel;
        BEEPError error;

        CloseReplyListener(ChannelImpl channel)
        {
            this.channel = channel;
            this.error = null;
        }

        boolean isError() {
            return this.error != null;
        }

        BEEPError getError() {
            return this.error;
        }

        public void receiveRPY(Message message)
        {
            try {
                Element topElement = processMessage(message);
                String elementName = topElement.getTagName();

                if (elementName == null) {
                    throw new BEEPException(ERR_MALFORMED_XML_MSG);

                    // is this RPY an <ok> (the positive response to a
                    // channel close)
                } else if (elementName.equals("ok")) {
                    log.debug("Received an OK for channel close");

                    // @todo we should fire an event instead.
                    // set the state
                    channel.setState(ChannelImpl.STATE_CLOSING);
                    channels.remove(channel.getNumberAsString());
                    channel.setState(ChannelImpl.STATE_CLOSED);

                    // release the block waiting for the channel to
                    // start or close
                    synchronized (this) {
                        this.notify();
                    }
                } else {
                    throw new BEEPException(ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
                }
            } catch (BEEPException e) {
                terminate("Problem with RPY: " + e.getMessage());
            }
        }

        public void receiveERR(Message message)
        {
            BEEPError err;

            try {
                err = BEEPError.convertMessageERRToException(message);
            } catch (BEEPException e) {
                terminate(e.getMessage());

                return;
            }

            log.debug("Received an error in response to a close. code="
                      + err.getCode() + " diagnostic=" + err.getDiagnostic());

            this.error = err;

            // set the state
            channel.setState(ChannelImpl.STATE_ACTIVE);
            channels.remove(channel.getNumberAsString());

            // release the block waiting for the channel to start or close
            synchronized (this) {
                this.notify();
            }
        }

        public void receiveANS(Message message)
        {
            terminate("Received an unexpected ANS");
        }

        public void receiveNUL(Message message)
        {
            terminate("Received an unexpected NUL");
        }
    }

    interface SessionOperations {
        void changeState(SessionImpl s, int newState) throws BEEPException;
        boolean postFrame(SessionImpl s, Frame f) throws BEEPException;
    }

    static class INITIALIZED_SessionOperations implements SessionOperations {
        public void changeState(SessionImpl s, int newState) throws BEEPException {
            if (!((newState == SESSION_STATE_GREETING_SENT) ||
                  (newState == SESSION_STATE_ABORTED)))
            {
                throw new BEEPException("Illegal session state transition");
            }

            s.state = newState;
        }

        public boolean postFrame(SessionImpl s, Frame f) throws BEEPException {
            // If we're in a PRE-GREETING state
            // only handle one frame at a time...
            // to avoid processing post-greeting
            // frames before the greeting has been
            // fully handled.
            synchronized (s) {
                return ((ChannelImpl)f.getChannel()).postFrame(f);
            }
        }

        private Log log = LogFactory.getLog(this.getClass());
    }

    static class GREETING_SENT_SessionOperations implements SessionOperations {
        public void changeState(SessionImpl s, int newState) throws BEEPException {
            if (!((newState == SESSION_STATE_ACTIVE) ||
                  (newState == SESSION_STATE_ABORTED)))
            {
                throw new BEEPException("Illegal session state transition");
            }

            s.state = newState;
        }

        public boolean postFrame(SessionImpl s, Frame f) throws BEEPException {
            // If we're in a PRE-GREETING state
            // only handle one frame at a time...
            // to avoid processing post-greeting
            // frames before the greeting has been
            // fully handled.
            synchronized (s) {
                return ((ChannelImpl)f.getChannel()).postFrame(f);
            }
        }

        private Log log = LogFactory.getLog(this.getClass());
    }

    static class ACTIVE_SessionOperations implements SessionOperations {
        public void changeState(SessionImpl s, int newState) throws BEEPException {
            if (!((newState == SESSION_STATE_TUNING_PENDING) ||
                  (newState == SESSION_STATE_CLOSE_PENDING) ||
                  (newState == SESSION_STATE_ABORTED)))
            {
                throw new BEEPException("Illegal session state transition");
            }

            s.state = newState;
        }

        public boolean postFrame(SessionImpl s, Frame f) throws BEEPException {
            return ((ChannelImpl)f.getChannel()).postFrame(f);
        }

        private Log log = LogFactory.getLog(this.getClass());
    }

    static class TUNING_PENDING_SessionOperations
        implements SessionOperations
    {
        public void changeState(SessionImpl s, int newState) throws BEEPException {
            if (!((newState == SESSION_STATE_ACTIVE) ||
                  (newState == SESSION_STATE_TUNING) ||
                  (newState == SESSION_STATE_ABORTED)))
            {
                throw new BEEPException("Illegal session state transition");
            }

            s.state = newState;
        }

        public boolean postFrame(SessionImpl s, Frame f) throws BEEPException {
            return ((ChannelImpl)f.getChannel()).postFrame(f);
        }

        private Log log = LogFactory.getLog(this.getClass());
    }

    static class TUNING_SessionOperations implements SessionOperations {
        public void changeState(SessionImpl s, int newState) throws BEEPException {
            if (!((newState == SESSION_STATE_CLOSED) ||
                  (newState == SESSION_STATE_ABORTED)))
            {
                throw new BEEPException("Illegal session state transition");
            }

            s.state = newState;
        }

        public boolean postFrame(SessionImpl s, Frame f) throws BEEPException {
            return ((ChannelImpl)f.getChannel()).postFrame(f);
        }

        private Log log = LogFactory.getLog(this.getClass());
    }

    static class CLOSE_PENDING_SessionOperations implements SessionOperations {
        public void changeState(SessionImpl s, int newState) throws BEEPException {
            if (!((newState == SESSION_STATE_ACTIVE) ||
                  (newState == SESSION_STATE_CLOSING) ||
                  (newState == SESSION_STATE_ABORTED)))
            {
                throw new BEEPException("Illegal session state transition");
            }

            s.state = newState;
        }

        public boolean postFrame(SessionImpl s, Frame f) throws BEEPException {
            // If we're in an error state
            log.debug("Dropping a frame because the Session state is " +
                      "no longer active.");
            return false;
        }

        private Log log = LogFactory.getLog(this.getClass());
    }

    static class CLOSING_SessionOperations implements SessionOperations {
        public void changeState(SessionImpl s, int newState) throws BEEPException {
            if (!((newState == SESSION_STATE_CLOSED) ||
                  (newState == SESSION_STATE_ABORTED)))
            {
                throw new BEEPException("Illegal session state transition");
            }

            s.state = newState;
        }

        public boolean postFrame(SessionImpl s, Frame f) throws BEEPException {
            return ((ChannelImpl)f.getChannel()).postFrame(f);
        }

        private Log log = LogFactory.getLog(this.getClass());
    }

    static class CLOSED_SessionOperations implements SessionOperations {
        public void changeState(SessionImpl s, int newState) throws BEEPException {
            if (newState == Session.SESSION_STATE_ABORTED) {
                log.info("Error aborting, session already in a closed state.");
            } else if (newState == Session.SESSION_STATE_CLOSE_PENDING) {
                log.info("Error changing state to close pending, session already in a closed state.");
            } else {
                throw new BEEPException("Illegal session state transition (" +
                                        newState + ")");
            }
        }

        public boolean postFrame(SessionImpl s, Frame f) throws BEEPException {
            // If we're in an error state
            log.debug("Dropping a frame because the Session state is " +
                      "no longer active.");
            return false;
        }

        private Log log = LogFactory.getLog(this.getClass());
    }

    static class ABORTED_SessionOperations implements SessionOperations {
        public void changeState(SessionImpl s, int newState) throws BEEPException {
            throw new BEEPException("Illegal session state transition");
        }

        public boolean postFrame(SessionImpl s, Frame f) throws BEEPException {
            // If we're in an error state
            log.debug("Dropping a frame because the Session state is " +
                      "no longer active.");
            return false;
        }

        private Log log = LogFactory.getLog(this.getClass());
    }
}
