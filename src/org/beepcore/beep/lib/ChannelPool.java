/*
 * ChannelPool.java  $Revision: 1.10 $ $Date: 2003/11/18 14:03:08 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2002,2003 Huston Franklin.  All rights reserved.
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
package org.beepcore.beep.lib;


import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.MessageListener;
import org.beepcore.beep.core.RequestHandler;
import org.beepcore.beep.core.Session;

import java.util.LinkedList;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <code>ChannelPool</code> holds a collection of available
 * <code>SharedChannel</code>(s) and provides access to them.  Availabe
 * <code>SharedChannel</code>(s) are retrieved through the
 * <code>getSharedChannel</code>.  Each <code>SharedChannel</code> has a time
 * to live, after which, it is removed from the pool.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.10 $, $Date: 2003/11/18 14:03:08 $
 */
public class ChannelPool {

    /**
     * @todo add tuning mechanisms, try to figure out what the average
     * channel reuse is and adjust ttl accordingly
     */
    private static final long DEFAULT_TIME_TO_LIVE = 120000;    // two minutes

    private Log log = LogFactory.getLog(this.getClass());

    private long timeToLive = DEFAULT_TIME_TO_LIVE;
    Session session;
    LinkedList availableChannels;

    /**
     * Creates a <code>ChannelPool</code> with the given session.
     *
     * @param session The <code>session</code> on which all
     * <code>SharedChannel</code>(s) returned from <code>getSharedChannel</code>
     * are started.
     * @see #getSharedChannel
     */
    public ChannelPool(Session session)
    {
        this.session = session;
        this.availableChannels = new LinkedList();
    }

    /**
     * Creates a <code>ChannelPool</code> with the given session and time to
     * live.
     *
     * @param session The <code>session</code> on which all
     * <code>SharedChannel</code>(s) returned from <code>getSharedChannel</code>
     * are started.
     * @param ttl The time in milleseconds an available <code>SharedChannel</code>
     * will live in <code>ChannelPool</code>
     * @see #getSharedChannel
     */
    public ChannelPool(Session session, long ttl)
    {
        this.session = session;
        this.timeToLive = ttl;
        this.availableChannels = new LinkedList();
    }

    /**
     * Returns a <code>SharedChannel</code> which supports the specified
     * <code>profile</code>.
     *
     * @param profile Name of a profile for the requested
     * <code>SharedChannel</code>.
     *
     * @return A <code>SharedChannel</code> for the requested profile.
     *
     * @throws BEEPException
     */
    public SharedChannel getSharedChannel(String profile) throws BEEPException
    {
        SharedChannel sharedCh = null;
        boolean found = false;

        synchronized (availableChannels) {
            Iterator i = availableChannels.iterator();

            while (i.hasNext()) {
                sharedCh = (SharedChannel) i.next();

                if (sharedCh.getProfile().equals(profile)) {
                    log.trace("Found an available channel for sharing");
                    i.remove();

                    found = true;

                    break;
                }
            }
        }

        // nothing found, so create one and return it
        if (!found) {
            sharedCh = new SharedChannel(this.session.startChannel(profile),
                                         this);
        }

        // clean up channels that have expired
        garbageCollect();
        if (log.isTraceEnabled()) {
            log.trace("Sharing channel number:" + sharedCh.getNumber());
        }

        return sharedCh;
    }

    /**
     * Returns a <code>SharedChannel</code> which supports the specified
     * <code>profile</code> and calls back on the specified
     * <code>DataListener</code>.  Once it is no longer needed, call
     * <code>release</code> on the <code>SharedChannel</code>
     * to return it to the pool of available channels.
     *
     * @param profile Name of profile for the requested
     * <code>SharedChannel</code>.
     * @param listener <code>DataListener</code> for the requested
     * <code>SharedChannel</code>.
     *
     * @return A <code>SharedChannel</code>.
     *
     * @see MessageListener
     * @see SharedChannel
     *
     * @throws BEEPException
     * @deprecated
     */
    synchronized public SharedChannel
        getSharedChannel(String profile, MessageListener listener)
            throws BEEPException
    {
        SharedChannel sharedCh = null;
        boolean found = false;

        synchronized (availableChannels) {
            Iterator i = availableChannels.iterator();

            while (i.hasNext()) {
                sharedCh = (SharedChannel) i.next();

                if (sharedCh.getProfile().equals(profile)) {
                    log.trace("Found an available channel for sharing");
                    i.remove();

                    found = true;

                    break;
                }
            }
        }

        // nothing found, so create one and return it
        if (!found) {
            sharedCh =
                new SharedChannel(this.session.startChannel(profile, listener),
                                  this);
        }

        // clean up channels that have expired
        garbageCollect();
        if (log.isTraceEnabled()) {
            log.trace("Sharing channel number:" + sharedCh.getNumber());
        }

        return sharedCh;
    }

    /**
     * Returns a <code>SharedChannel</code> which supports the specified
     * <code>profile</code> and calls back on the specified
     * <code>DataListener</code>.  Once it is no longer needed, call
     * <code>release</code> on the <code>SharedChannel</code>
     * to return it to the pool of available channels.
     *
     * @param profile Name of profile for the requested
     * <code>SharedChannel</code>.
     * @param handler <code>RequestHandler</code> for the requested
     * <code>SharedChannel</code>.
     *
     * @return A <code>SharedChannel</code>.
     *
     * @see MessageListener
     * @see SharedChannel
     *
     * @throws BEEPException
     * @deprecated
     */
    synchronized
    public SharedChannel getSharedChannel(String profile, RequestHandler handler)
            throws BEEPException
    {
        SharedChannel sharedCh = null;
        boolean found = false;

        synchronized (availableChannels) {
            Iterator i = availableChannels.iterator();

            while (i.hasNext()) {
                sharedCh = (SharedChannel) i.next();

                if (sharedCh.getProfile().equals(profile)) {
                    log.trace("Found an available channel for sharing");
                    i.remove();

                    found = true;

                    break;
                }
            }
        }

        // nothing found, so create one and return it
        if (!found) {
            sharedCh =
                new SharedChannel(this.session.startChannel(profile, handler),
                                  this);
        }

        // clean up channels that have expired
        garbageCollect();
        if (log.isTraceEnabled()) {
            log.trace("Sharing channel number:" + sharedCh.getNumber());
        }

        return sharedCh;
    }
    /**
     * Called from <code>SharedChannel</code>.  Releases the sharedCh and adds
     * it to the list of available SharedChannels.
     */
    void releaseSharedChannel(SharedChannel sharedCh)
    {

        // this channel is available to share
        synchronized (availableChannels) {
            availableChannels.add(sharedCh);
        }

        garbageCollect();
    }

    /**
     * Sets the time to live or the number of milleseconds an unused channel will
     * remain in the pool before it is removed from the pool.
     * The default time to live is two minutes.
     *
     * @param ttl The time this channel has to live in the pool while in an
     * available state.
     */
    public void setSharedChannelTTL(long ttl)
    {
        this.timeToLive = ttl;
    }

    /**
     * Closes down the channel pool, its session and all associated channels.
     */
    public void close()
    {
      // close all available channels and the session
      try
      {
        this.session.close();
      }
      catch( BEEPException e )
      {
        e.printStackTrace();
      }
    }

    /**
     * Identifies SharedChannels that have exceeded their ttl, removes the from
     * the list of availableChannels, and closes them.
     */
    private void garbageCollect()
    {
        log.trace("garbage collecting");

        if (availableChannels.size() != 0) {
            Date now = new Date();
            long ttl = now.getTime() - timeToLive;

            synchronized (availableChannels) {
                while (availableChannels.size() > 0){
                    if (ttl > ((SharedChannel) availableChannels.get(0)).getTTL()) {

                        // channel has out lived its time to live
                        // die, die, die
                        SharedChannel shCh =
                            (SharedChannel) availableChannels.remove(0);

                        try {
                            if (log.isTraceEnabled()) {
                                log.trace("garbage collected channel number:"
                                          + shCh.getNumber());
                            }
                            shCh.close();    // last gasp
                        } catch (BEEPException e) {

                            // ignore for now, we'll try again later
                            log.error("unable to close channel number:"
                                      + shCh.getNumber());
                        }

                        shCh = null;    // death by gib @todo does this really set the object to null?
                    } else {

                        // since youngest channels are added to the end of the list,
                        // once we hit the above condition all channels below this one are
                        break;
                    }
                }
            }
        }
    }
}
