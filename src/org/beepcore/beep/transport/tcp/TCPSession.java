/*
 * TCPSession.java  $Revision: 1.36 $ $Date: 2003/11/18 14:03:10 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2001-2003 Huston Franklin.  All rights reserved.
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
package org.beepcore.beep.transport.tcp;


import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.Channel;
import org.beepcore.beep.core.Frame;
import org.beepcore.beep.core.ProfileRegistry;
import org.beepcore.beep.core.Session;
import org.beepcore.beep.core.SessionImpl;
import org.beepcore.beep.core.SessionCredential;
import org.beepcore.beep.core.SessionTuningProperties;
import org.beepcore.beep.util.BufferSegment;
import org.beepcore.beep.util.HeaderParser;
import org.beepcore.beep.util.StringUtil;


/**
 * Provides the TCP transport mapping for BEEP according to RFC 3081.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.36 $, $Date: 2003/11/18 14:03:10 $
 */
public class TCPSession extends SessionImpl {

    // Constants
    private static final char[] MESSAGE_TYPE_SEQ = new char[] {'S', 'E', 'Q'};
    private static final int MAX_RECEIVE_BUFFER_SIZE = 64 * 1024;
    private static final String TCP_MAPPING = "TCP Mapping";
    private static final String CRLF = "\r\n";
    private static final int MIN_SEQ_HEADER_SIZE = (3        // msg type
                                                    + 1      // space
                                                    + 1      // channel number
                                                    + 1      // space
                                                    + 1      // acknum
                                                    + 1      // space
                                                    + 1      // window
                                                    + CRLF.length());

    private static final int CHANNEL_START_ODD = 1;
    private static final int CHANNEL_START_EVEN = 2;


    // Instance Data
    private Log log = LogFactory.getLog(this.getClass());

    private byte headerBuffer[] = new byte[Frame.MAX_HEADER_SIZE];
    private byte[] outputBuf = new byte[0];
    private Object writerLock;
    private Socket socket;
    private boolean running;
    private static int THREAD_COUNT = 0;
    private static final String THREAD_NAME = "TCPSession Thread #";
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
     * @throws BEEPException
     */
    private TCPSession(Socket sock, ProfileRegistry registry, int firstChannel,
                       SessionCredential localCred, SessionCredential peerCred,
                       SessionTuningProperties tuning, String servername)
            throws BEEPException
    {
	super(registry, firstChannel, localCred, peerCred, tuning, servername);

        socket = sock;
        writerLock = new Object();

        if ((peerCred != null) || (localCred != null) || (tuning != null)) {
            tuningInit();
        } else {
            init();
        }

        try {
            socket.setReceiveBufferSize(MAX_RECEIVE_BUFFER_SIZE);
        } catch (Exception x) {
            log.debug("Socket doesn't support setting receive buffer size");
        }
    }

    /**
     * Creates a TCPSession for a Socket that was created by
     * initiating a connection.
     *
     *
     * @param sock
     * @param registry
     * @param servername
     *
     * @throws BEEPException
     *
     */
    public static TCPSession createInitiator(Socket sock,
					     ProfileRegistry registry,
					     String servername)
            throws BEEPException
    {
        return new TCPSession(sock, (ProfileRegistry) registry.clone(),
			      CHANNEL_START_ODD, null, null, null, servername);
    }
    /**
     * Creates a TCPSession for a Socket that was created by
     * initiating a connection.
     *
     *
     * @param sock
     * @param registry
     *
     * @throws BEEPException
     *
     */
    public static TCPSession createInitiator(Socket sock,
                                             ProfileRegistry registry)
	throws BEEPException
    {
        return createInitiator(sock, registry, null);
    }

    /**
     * Creates a TCPSession for a Socket that was created by
     * listening and accepting a connection.
     *
     *
     * @param sock
     * @param registry
     *
     * @throws BEEPException
     *
     */
    public static TCPSession createListener(Socket sock,
                                            ProfileRegistry registry)
            throws BEEPException
    {
        return new TCPSession(sock, (ProfileRegistry) registry.clone(),
                              CHANNEL_START_EVEN, null, null, null, null);
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
            running = false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString() + " (" +
            socket.getLocalAddress() + ":" + socket.getLocalPort() + "-" +
            socket.getInetAddress() + ":" + socket.getPort() + ")";
    }

    // Implementation of method declared in Session
    protected void disableIO()
    {
        running = false;
    }

    // Implementation of method declared in Session
    protected void enableIO()
    {
        running = false;
        thread = null;

        if (thread == null) {
            String threadName;

            synchronized (THREAD_NAME) {
                threadName = new String(THREAD_NAME + THREAD_COUNT++);
            }

            thread = new Thread(threadName)
                {
                    public void run() {
                        processNextFrame();
                    }
                };

            thread.setDaemon(true);
            thread.start();
        }
    }

    // Implementation of method declared in Session
    protected int getMaxFrameSize()
    {
        /**
         * @todo - test this and find an optimal frame size, key it up
         * to approximate ethernet packet size, less header, trailer
         */
        return 1400;
    }

    /**
     * Generates a header, then writes the header, payload, and
     * trailer to the wire.
     *
     * @param f the Frame to send.
     * @return boolean true of the frame was sent, false otherwise.
     *
     * @throws BEEPException
     *
     * @todo make this one write operation later.
     */
    protected void sendFrame(Frame f) throws BEEPException
    {
        try {

            OutputStream os = socket.getOutputStream();

            synchronized (writerLock) {
                /* Inspite of the extra data copy if is faster to have
                 * a single call to write() (at least with the JVMs we
                 * have tested with).
                 */
                BufferSegment[] bs = f.getBytes();

                int n = 0;
                for (int i=0; i<bs.length; ++i) {
                    n += bs[i].getLength();
                }

                if (n > outputBuf.length) {
                    outputBuf = new byte[n];
                }

                int off = 0;

                for (int i=0; i<bs.length; ++i) {
                    System.arraycopy(bs[i].getData(), bs[i].getOffset(),
                                     outputBuf, off, bs[i].getLength());

                    off += bs[i].getLength();
                }

                os.write(outputBuf, 0, n);
                os.flush();

                if (log.isTraceEnabled()) {
                    log.trace("Wrote the following\n" +
                              new String(outputBuf, 0, n));
                }
            }
        } catch (IOException e) {
            throw new BEEPException(e);
        } catch (Exception e) {
            throw new BEEPException(e);
        }
    }

    // Implementation of method declared in Session
    protected Session reset(SessionCredential localCred,
                            SessionCredential peerCred,
                            SessionTuningProperties tuning,
                            ProfileRegistry reg, Object argument)
            throws BEEPException
    {
        if (log.isTraceEnabled()) {
            log.trace("Reset as "
                      + (isInitiator() ? "INITIATOR" : "LISTENER"));
        }

        Socket s = null;

        try {
            s = (Socket) argument;
        } catch (ClassCastException x) {
            s = socket;
        }

        if (reg == null) {
            reg = this.getProfileRegistry();
        }

        Session newSession = new TCPSession(s, reg,
                                            (isInitiator() ? CHANNEL_START_ODD:
                                             CHANNEL_START_EVEN),
                                            localCred, peerCred, tuning, null);

        this.fireSessionReset(newSession);

        return newSession;
    }

    /**
     * Update the channel window size with the remote peer by sending
     * SEQ frames as per RFC 3081.
     *
     *
     * @return true if the Receive Buffer Size was updated
     *
     * @throws BEEPException if a specified buffer size is larger
     *    than what's available on the Socket.
     *
     */
    protected boolean updateMyReceiveBufferSize(Channel channel,
                                                long currentSeq,
                                                int currentAvail)
            throws BEEPException
    {
        StringBuffer sb = new StringBuffer(Frame.MAX_HEADER_SIZE);

        sb.append(MESSAGE_TYPE_SEQ);
        sb.append(' ');
        sb.append(this.getChannelNumberAsString(channel));
        sb.append(' ');
        sb.append(Long.toString(currentSeq));
        sb.append(' ');
        sb.append(Integer.toString(currentAvail));
        sb.append(CRLF);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Wrote: " + sb.toString());
            }

            OutputStream os = socket.getOutputStream();

            synchronized (writerLock) {
                os.write(StringUtil.stringBufferToAscii(sb));
                os.flush();
            }
        } catch (IOException x) {
            throw new BEEPException("Unable to send SEQ", x);
        }

        return true;
    }

    private void processNextFrame()
    {
        running = true;
        
        if (socket == null) {
            running = false;
            return;
        }

        try {
            InputStream is = socket.getInputStream();

            while (running) {
                if (log.isTraceEnabled()) {
                    log.trace("Processing next frame");
                }

                int amountRead;

                try {
                    do {
                        amountRead =
                            is.read(headerBuffer, 0, MIN_SEQ_HEADER_SIZE);

                        if (amountRead == -1) {
                            throw new SessionAbortedException();
                        }
                    } while (amountRead == 0);

                } catch (java.net.SocketException e) {
                    if (running) {
                        throw e;
                    }

                    // socket closed intentionally (session closing)
                    // so just return
                    return;
                }

                if (headerBuffer[0] == (byte) MESSAGE_TYPE_SEQ[0]) {
                    processSEQFrame(headerBuffer, amountRead, is);
                    continue;
                } else {
                    if (processCoreFrame(headerBuffer, amountRead, is) == false)
                    {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);

            socket = null;

            terminate(e.getMessage());
        } catch (SessionAbortedException e) {
            terminate("Session aborted by remote peer.");
        } catch (Throwable e) {
            log.error(e);
            terminate(e.getMessage());
        }

        if (log.isDebugEnabled()) {
            log.debug("Session listener thread exiting.  State = "
                      + TCPSession.this.getState());
        }
    }

    private boolean processCoreFrame(byte[] headerBuffer, int amountRead,
                                     InputStream is)
        throws SessionAbortedException, BEEPException, IOException
    {
        int headerLength = 0;
        int amountToRead = Frame.MIN_FRAME_SIZE - amountRead;

    headerFound:
        while (true) {
            int tokenCount = 6;

            try {
                int n = is.read(headerBuffer, amountRead, amountToRead);

                if (n == -1) {
                    throw new SessionAbortedException();
                }

                if (n == 0) {
                    continue;
                }

                amountRead += n;

            } catch (java.net.SocketException e) {
                if (running) {
                    throw e;
                }

                // socket closed intentionally (session closing)
                // so just return
                return false;
            }

            while (headerLength < amountRead) {
                if (headerBuffer[headerLength] == '\n') {
                    if (headerLength == 0 ||
                        headerBuffer[headerLength - 1] != '\r')
                    {
                        throw new BEEPException("Malformed BEEP header");
                    }

                    ++headerLength;
                    break headerFound;
                }

                if (headerBuffer[headerLength] == ' ') {
                    if (tokenCount > 1) { // This is for ANS frames
                        --tokenCount;
                    }
                }

                ++headerLength;
            }

            if (headerLength > Frame.MAX_HEADER_SIZE) {
                throw new BEEPException("Malformed BEEP header, no CRLF");
            }

            /* 2 = 1 for the min token size and 1 is for the separator ' '
             * or "\r\n"
             */
            amountToRead = (tokenCount * 2) + Frame.TRAILER.length();
        }

        if (log.isTraceEnabled()) {
            log.trace(new String(headerBuffer, 0, headerLength));
        }

        Frame f = super.createFrame(headerBuffer,
                                    headerLength - CRLF.length());
        byte[] payload = new byte[f.getSize()];

        int count = amountRead - headerLength;
        if (count > payload.length) {
            System.arraycopy(headerBuffer, headerLength, payload, 0,
                             payload.length);
            count -= payload.length;
            
            for (int i = 0; i < Frame.TRAILER.length(); ++i) {
                int b;
                if (count > 0) {
                    b = headerBuffer[headerLength + payload.length + i];
                    --count;
                } else {
                    b = is.read();

                    if (b == -1) {
                        throw new SessionAbortedException();
                    }
                }

                if (((byte) b) != ((byte) Frame.TRAILER.charAt(i))) {
                    throw new BEEPException("Malformed BEEP frame, "
                                            + "invalid trailer");
                }
            }
        } else {
            System.arraycopy(headerBuffer, headerLength, payload, 0, count);

            while (count < payload.length) {
                int n = is.read(payload, count, payload.length - count);
                if (n == -1) {
                    throw new SessionAbortedException();
                }
                count += n;
            }

            if (log.isTraceEnabled()) {
                log.trace(new String(payload));
            }

            for (int i = 0; i < Frame.TRAILER.length(); ++i) {
                int b = is.read();

                if (b == -1) {
                    throw new SessionAbortedException();
                }

                if (((byte) b) != ((byte) Frame.TRAILER.charAt(i))) {
                    throw new BEEPException("Malformed BEEP frame, "
                                            + "invalid trailer");
                }
            }
        }

        f.addPayload(new BufferSegment(payload));

        return super.postFrame(f);
    }

    private void processSEQFrame(byte[] headerBuffer, int amountRead,
                                 InputStream is)
        throws BEEPException, IOException, SessionAbortedException
    {
        int headerLength = 0;
        int tokenCount = 4;

    headerFound:
        while (true) {

            while (headerLength < amountRead) {
                if (headerBuffer[headerLength] == '\n') {
                    if (headerLength == 0 ||
                        headerBuffer[headerLength - 1] != '\r')
                    {
                        throw new BEEPException("Malformed BEEP header");
                    }

                    ++headerLength;
                    break headerFound;
                }

                if (headerBuffer[headerLength] == ' ') {
                    if (tokenCount > 1) {
                        --tokenCount;
                    }
                }

                ++headerLength;
            }

            if (headerLength > Frame.MAX_HEADER_SIZE) {
                throw new BEEPException("Malformed BEEP header, no CRLF");
            }

            int amountToRead = headerBuffer[headerLength - 1] == '\r' ? 1 :
                tokenCount * 2;
            try {
                /* 2 = 1 for the min token size and 1 is for the separator ' '
                 * or "\r\n"
                 */
                int n = is.read(headerBuffer, amountRead, amountToRead);

                if (n == -1) {
                    throw new SessionAbortedException();
                }

                if (n == 0) {
                    continue;
                }

                amountRead += n;

            } catch (java.net.SocketException e) {
                if (running) {
                    throw e;
                }

                // socket closed intentionally (session closing)
                // so just return
                return;
            }
        }

        // Process the header
        HeaderParser header = new HeaderParser(headerBuffer,
                                               headerLength - CRLF.length());
        
        char[] type = header.parseType();
        if (java.util.Arrays.equals(type, MESSAGE_TYPE_SEQ) == false) {
            throw new BEEPException("Malformed BEEP header");
        }

        int channelNum = header.parseInt();
        long ackNum = header.parseUnsignedInt();
        int window = header.parseInt();

        if (header.hasMoreTokens()) {
            throw new BEEPException("Malformed BEEP Header");
        }

        // update the channel with the new receive window size
        this.updatePeerReceiveBufferSize(channelNum, ackNum, window);
    }
    
    private static class SessionAbortedException extends Exception {
    }
}
