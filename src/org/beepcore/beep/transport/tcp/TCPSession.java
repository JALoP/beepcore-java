
/*
 * TCPSession.java            $Revision: 1.1 $ $Date: 2001/04/02 08:45:53 $
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
package org.beepcore.beep.transport.tcp;


import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.Socket;
import java.net.SocketException;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.Channel;
import org.beepcore.beep.core.Frame;
import org.beepcore.beep.core.Message;
import org.beepcore.beep.core.ProfileRegistry;
import org.beepcore.beep.core.Session;
import org.beepcore.beep.core.SessionCredential;
import org.beepcore.beep.util.Log;


/**
 * This class encapsulates the notion of a BEEP Session ( a relationship
 * between BEEP peers) that exists over a TCP (socket-based) connection.
 * Notice that I've set up properties for both the TCPSession class (statics)
 * and the instance itself.  This may change, but for now it provides us
 * a general management facility with various degrees of granularity.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision, $Date: 2001/04/02 08:45:53 $
 */
public class TCPSession extends Session {

    // Constants
    private static final String ERR_SEND_FRAME_FAILED =
        "Unable to send a frame";
    private static final String ERR_TCP_BUFFER_TOO_LARGE = "";
    private static final String SEQ_PREFIX = "SEQ ";
    protected static final char NEWLINE_CHAR = '\n';
    private static final int DEFAULT_PROPERTIES_SIZE = 4;
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 4 * 1024;
    private static final int MAX_RECEIVE_BUFFER_SIZE = 64 * 1024;
    private static final int MIN_RECEIVE_BUFFER_SIZE = 4 * 1024;
    private static final int SEQ_LENGTH = SEQ_PREFIX.length();
    private static final String TCP_MAPPING = "TCP Mapping";
    private static final String CRLF = "\r\n";

    // Instance Data
    // @todo had these per stack, but have
    // since changed my tune, since we'll be thread/session
    // for probably a while yet...this'll help on performance.
    // reusing fixed size buffers.
    protected byte headerBuffer[] = new byte[Frame.MAX_HEADER_SIZE];
    protected byte trailerBuffer[] = new byte[Frame.TRAILER.length()];
    private Object writerLock;
    protected Socket socket;
    private boolean running;
    protected static int THREAD_COUNT = 0;
    protected static final String THREAD_NAME = "Session Thread #";
    private Thread thread;

    /**
     * @param sock the Socket for this TCPConnection
     *
     * @registry the ProfileRegistry (set of profiles) to be used on
     * this Session.
     *
     * @firstChannel the integer indicating the
     * ordinality for this Peer, which determines whether Channels
     * started by this peer have odd or even numbers.
     *
     * @param registry
     * @param localCred
     * @param peerCred
     *
     * The hack-ish part of this is the credential parameter.  Here's
     * the deal.  (1) TCPSessionCreator has public methods that don't
     * expose the credential (2) Therefore if it's called by the User,
     * it can only have a null credential.  (3) If it's called via a
     * Tuning Profile reset though, it can have a real value.  (4)
     * When that's the case, we call a 'different' init method that
     * doesn't block the thread of the former (soon to die) session.
     *
     * @param firstChannel
     *
     * @throws BEEPException */
    TCPSession(Socket sock, ProfileRegistry registry, int firstChannel, 
               SessionCredential localCred, SessionCredential peerCred)
            throws BEEPException
    {
        super(registry, firstChannel, localCred, peerCred);

        socket = sock;
        writerLock = new Object();

        if (peerCred != null || localCred != null) {
            tuningInit();
        } else {
            init();
        }

        try {
            socket.setReceiveBufferSize(MAX_RECEIVE_BUFFER_SIZE);
        } catch (Exception x) {
            throw new BEEPException("Error allocating TCP Buffers");
        }
    }

    // Overrides method in Session
    public synchronized void close() throws BEEPException
    {
        super.close();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
            socket = null;
        }
    }

    public Socket getSocket()
    {
        return this.socket;
    }

    // Overrides method in Session
    public void terminate(String reason)
    {
        super.terminate(reason);
        if (socket != null) {
            try {
            socket.close();
            } catch (IOException e) {
            }
            socket = null;
        }
    }

    protected void disableIO()
    {
        running = false;
    }

    protected void enableIO()
    {
        running = false;
        thread = null;

        if (thread == null) {
            String threadName;

            synchronized (THREAD_NAME) {
                threadName = new String(THREAD_NAME + THREAD_COUNT++);
            }

            thread = new Thread(new SessionThread(), threadName);

            thread.setDaemon(true);
            thread.start();
        }
    }

    protected int getMaxFrameSize()
    {
        /**
         * @todo - test this and find an optimal frame size, key it up
         * to approximate ethernet packet size, less header, trailer
         */

        return 1400;
    }

    /**
     * Socket-level call to send a frame along a socket.  Generates
     * a header, then writes the header, payload, and trailer to
     * the wire.
     *
     * @param f the Frame to send.
     * @returns boolean true of the frame was sent, false otherwise.
     *
     * @throws BEEPException
     *
     * @todo make this one write operation later.
     */
    protected void sendFrame(Frame f) throws BEEPException
    {
        try {

            // @todo consider the costs of colliding these into one
            // buffer and one write..test to see if it's faster
            // this way or faster copying.
            OutputStream os = socket.getOutputStream();
            byte[] header = null;

            synchronized (writerLock) {

                // Create header
                header = f.buildHeader();

                os.write(header);

                // Write message payload
                Frame.BufferSegment p = f.getPayload();

                os.write(p.getBytes(), p.getOffset(), p.getLength());

                // Write trailer
                os.write(Frame.TRAILER.getBytes());
                os.flush();
                Log.logEntry(Log.SEV_DEBUG_VERBOSE, TCP_MAPPING,
                             "Wrote the following\n" + new String(header)
                             + new String(p.getBytes(), p.getOffset(), p.getLength())
                             + Frame.TRAILER);
            }
        } catch (IOException e) {
            throw new BEEPException(e.toString());
        } catch (Exception e) {
            throw new BEEPException(e.toString());
        }
    }

    protected Session reset(SessionCredential localCred,
                            SessionCredential peerCred, 
                            ProfileRegistry reg,
                            Object argument)
            throws BEEPException
    {
        Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                     "Reset as "
                     + (isInitiator() ? "INITIATOR" : "LISTENER"));

        Socket s = null;

        try {
            s = (Socket) argument;
        } catch (ClassCastException x) {
            s = socket;
        }

        if (reg == null) {
            reg = this.getProfileRegistry();
        }

        if (isInitiator()) {
            return TCPSessionCreator.initiate(s, reg, 
                                              localCred, peerCred);
        } else {
            return TCPSessionCreator.listen(s, reg, 
                                            localCred, peerCred);
        }
    }

    /**
     * This method is designed to allow for flow control across the multiplexed
     * connection we have.  The only subclass at present - TCPSession -
     * implements
     * a real version of it.  The idea is to throttle data being sent over this
     * session to be manageable per Channel, so that a given Channel doesn't
     * take
     * up all the bandwidth.  The Java implementation is constrained a bit by a
     * dependency on the Socket's get/setReceiveBufferSize calls.  Because of
     * how we've designed the library, it's impossible for us to actually have
     * a socket during the greeting period, therefore, we use a default minimal
     * buffer size of 4k bytes in the initial SEQ frame.  Once the constructor
     * for TCPSession has completed (the greeting has been sent) and we have
     * a real socket, then we attempt to set the buffer size based on the
     * definitions in <code>Constants</code>, and allow things to go.
     *
     * This method restricts the bufferSize, per the beep spec, to be at most
     * two-thirds of the socket's receiveBufferSize.  If a size is requested
     * beyond that, an exception is thrown.
     *
     *
     * @param channel
     * @param previouslySeq
     * @param currentSeq
     * @param previouslyUsed
     * @param currentlyUsed
     * @param bufferSize
     *
     * @return true if the Receive Buffer Size was updated
     *
     * @throws BEEPException if a specified buffer size is larger
     *    than what's available on the Socket.
     *
     */
    protected boolean updateMyReceiveBufferSize(Channel channel,
                                                long previouslySeq,
                                                long currentSeq,
                                                int previouslyUsed,
                                                int currentlyUsed,
                                                int bufferSize)
            throws BEEPException
    {
      // @todo update the java-doc to correctly identify the params
        if (currentSeq > 0) {    // don't send it the first time
            if (((currentSeq - previouslySeq) < (bufferSize / 2))
                    || (currentlyUsed > (bufferSize / 2))) {
                return false;
            }
        }

        StringBuffer sb = new StringBuffer(Frame.MAX_HEADER_SIZE);

        sb.append(SEQ_PREFIX);
        sb.append(this.getChannelNumberAsString(channel));
        sb.append(' ');
        sb.append(Long.toString(currentSeq));
        sb.append(' ');
        sb.append(Integer.toString(bufferSize - currentlyUsed));
        sb.append(CRLF);

        try {
            Log.logEntry(Log.SEV_DEBUG_VERBOSE, TCP_MAPPING,
                         "Wrote the following\n" + sb.toString());

            OutputStream os = socket.getOutputStream();

            os.write(sb.toString().getBytes());
            os.flush();
        } catch (IOException x) {
            throw new BEEPException("Unable to send SEQ" + x.getMessage());
        }

        return true;
    }

    // Lame hack for J++

    /**
     * Method modState
     *
     *
     * @param i
     *
     * @return true if the state change was successful
     *
     */
    private boolean modState(int i) throws BEEPException
    {
        return super.changeState(i);
    }

    /**
     * This specialization is designed to process Frames unique
     * to the TCP mapping (e.g. SEQ frames).  If the Frame is
     * not an SEQ frame, it calls the regular TCPSession
     * processNextFrame( int ) with an integer that simply
     * indicates how far it's read ahead into the stream.
     * @exception Throws an exception if it encounters a poorly
     * formed header or an IOException in the underlying socket
     * stream.
     *
     * @throws BEEPException
     */
    private void processNextFrame() throws BEEPException, IOException
    {
        int msgType = Message.MESSAGE_TYPE_UNK;
        int ackNum = 0, msgNum = 0, pos = 0;
        char isLast = 0;
        boolean last = false;
        int channelNum = -1;
        InputStream is = null;
        String temp = null;

        Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING, "Processing next frame");

        for (int c = 0; c < Frame.MAX_HEADER_SIZE; c++) {
            headerBuffer[c] = 0;
        }

        int i = -1;

        headerBuffer[SEQ_LENGTH] = 0;
        is = socket.getInputStream();

        for (i = 0; i < SEQ_LENGTH; i++) {
            headerBuffer[i] = (byte) is.read();

            if (headerBuffer[i] != (byte) SEQ_PREFIX.charAt(i)) {
                break;
            }
        }

        if (i != SEQ_LENGTH) {
            processNextFrame(++i);

            return;
        }

        Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING, "Processing SEQ frame");

        // Otherwise it's an SEQ frame
        // Read the header
        pos = SEQ_LENGTH;

        while (((pos == 0) || (headerBuffer[pos - 1] != NEWLINE_CHAR))
               && (pos < Frame.MAX_HEADER_SIZE)) {
            byte b = (byte) is.read();

            if (b != -1) {
                headerBuffer[pos++] = b;
            } else {
                throw new BEEPException("Malformed BEEP header");
            }
        }

        headerBuffer[pos] = 0;

        Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                     new String(headerBuffer, 0, pos));

        // Process the header
        StringTokenizer st = new StringTokenizer(new String(headerBuffer,
                                                            0, pos));

        pos = st.countTokens();

        if (!((pos == 4) || (pos == 5) || (pos == 6))) {

            // This should just shut the session down.
            Log.logEntry(Log.SEV_ERROR, TCP_MAPPING,
                         "Malformed BEEP header");

            throw new BEEPException("Malformed BEEP header");
        }

        // Skip the SEQ
        st.nextToken();

        // Read the Channel Number
        channelNum = Integer.parseInt(st.nextToken());

        // Read the Ack Number
        ackNum = Integer.parseInt(st.nextToken());

        // Read the Window Number
        pos = Integer.parseInt(st.nextToken());

        // update the channel with the new receive window size
        this.updatePeerReceiveBufferSize(channelNum, ackNum, pos);

        // We need to recurse from method end to prevent
        // SEQs from being treated as legit frames
        // in Session.run()...yes, we exit above if it's
        // a normal message with super.process.. and return
        if (getState() == SESSION_STATE_ACTIVE) {
            processNextFrame();
        }
    }

    /**
     * The arg 'pos' indicates how far another method may have read
     * into headerBuffer @todo this will hae to be a byte[] or
     * something if we go beyond the thread per session model.
     * @param pos the position in the header where we should start reading.
     *
     * @throws BEEPException
     */
    private void processNextFrame(int pos) throws BEEPException, IOException
    {

        // @todo
        // When or if we ever change from thread per session, then bring this
        // back byte trailerBuffer[] = new byte[ TRAILER.length() ];
        // byte headerBuffer[] = new byte[ TRAILER.length() ];
        // If I could declare byte[] on the stack, I would...oh well,
        // 'C' beckons ;)
        int msgType = Message.MESSAGE_TYPE_UNK;
        int msgNum = 0, seqNum = 0, count = 0;
        int ansNum = 0, size = 0, limit = 0;
        char isLast = 0;
        boolean last = false;

        //        Channel     channel = null;
        int channelNum = -1;
        InputStream is = null;
        String temp = null;

        Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                     "Processing normal BEEP frame");

        is = socket.getInputStream();

        // Grok the header
        // Brutal, but I need to think about how to read the header
        // nicely...
        // @todo read ahead in larger chunks, 16-32 bytes or so, and then
        // remark the stream back a few bytes when you overrun, so as to be
        // efficient.
        while (((pos == 0) || (headerBuffer[pos - 1] != NEWLINE_CHAR))
               && (pos < Frame.MAX_HEADER_SIZE)) {
            byte b = (byte) is.read();

            if (b != -1) {
                headerBuffer[pos++] = b;
            } else {
                throw new BEEPException("Malfored BEEP Header");
            }
        }

        // Process the header
        headerBuffer[pos] = 0;
        limit = pos;

        StringTokenizer st = new StringTokenizer(new String(headerBuffer,
                                                            0, pos));

        count = st.countTokens();

        if (!((count == 6) || (count == 7) || (count == 8))) {
            Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                         "Illegal header tokens=" + count + "\n"
                         + new String(headerBuffer));

            throw new BEEPException("Malfored BEEP Header");
        }

        // Get Message Type
        // Kick out if we've maxed, or if the type gets set.
        temp = st.nextToken();
        msgType = Message.getMessageType(temp);

        // Read the Channel Number
        channelNum = Integer.parseInt(st.nextToken());

        // Read the Message Number
        msgNum = Integer.parseInt(st.nextToken());

        // Read the more flag
        isLast = st.nextToken().charAt(0);

        if (isLast == '*') {
            last = false;
        } else if (isLast == '.') {
            last = true;
        } else {

            // This should just shut the session down.
            Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING, "lastFrame=" + last);

            throw new BEEPException("Malfored BEEP Header");
        }

        // Sequence Number
        seqNum = Integer.parseInt(st.nextToken());

        // Size
        pos = Integer.parseInt(st.nextToken());

        if (pos < 0) {
            throw new BEEPException("Malfored BEEP Header");
        }

        // The window size and frame size have nothing in common.
        if (pos > getChannelAvailableWindow(channelNum)) {
            throw new BEEPException("Payload size is greater than channel "
                                    + "window size");
        }

        if (count == 8) {

            // AnsNo
            ansNum = Integer.parseInt(st.nextToken());
        }

        // Read the other MIME headers...er...
        // TBD
        // Read the payload
        byte payload[] = new byte[pos];

        // Might come in stages so track it until we get
        // the full blast length...or some other contraints
        // kick in.
        count = 0;

        while (count < pos) {
            count += is.read(payload, count, pos - count);
        }

        Log.logEntry(Log.SEV_DEBUG_VERBOSE, TCP_MAPPING,
                     "Read the following\n"
                     + new String(headerBuffer, 0, limit)
                         + new String(payload, 0, count));

        count = 0;
        limit = Frame.TRAILER.length();

        while (count < limit) {
            count += is.read(trailerBuffer, count, limit - count);
        }

        if (!Frame.TRAILER.equals(new String(trailerBuffer))) {
            throw new BEEPException("Malfored BEEP frame. No trailer");
        }

        String p = new String(payload);
        Frame f = createFrame(msgType, channelNum, msgNum, seqNum,
                              ansNum, payload, last);

        super.postFrame(f);
    }

    private class SessionThread implements Runnable {

        /**
         * Right now our threading scheme consists of one thread per session,
         * to manage the I/O, and an additional shared pool of threads we use
         * to call up into User space
         *
         * This method is only public because we inherit it from Runnable and
         * cannot change the protection.  Please don't invoke it.  You will
         * create race conditions and experience weirdness.
         *
         * @overrides Runnable.run
         */
        public void run()
        {
            try {
                running = true;

                // Listen for and post the Greeting Frame
                while ((getState() != SESSION_STATE_ACTIVE) && running) {
                    processNextFrame();
                }

                // Keep processing frames as long as we're active
                while ((getState() == SESSION_STATE_ACTIVE) && running) {
                    processNextFrame();
                }
            } catch (IOException e) {
                Log.logEntry(Log.SEV_ERROR, TCP_MAPPING, e);
                socket = null;
                TCPSession.super.terminate(e.getMessage());
            } catch (Throwable e) {
                Log.logEntry(Log.SEV_ERROR, TCP_MAPPING, e);
                terminate(e.getMessage());
            }

            Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                         "Session listener thread exiting.  State = "
                         + TCPSession.this.getState());
        }
    }
}
