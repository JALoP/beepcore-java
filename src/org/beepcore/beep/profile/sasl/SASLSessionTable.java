/*
 * SASLSessionTable.java  $Revision: 1.7 $ $Date: 2003/05/20 22:22:02 $
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
package org.beepcore.beep.profile.sasl;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.beepcore.beep.core.Session;
import org.beepcore.beep.core.SessionCredential;
import org.beepcore.beep.core.event.SessionEvent;
import org.beepcore.beep.core.event.SessionResetEvent;
import org.beepcore.beep.core.event.SessionListener;
import org.beepcore.beep.profile.sasl.anonymous.SASLAnonymousProfile;

/**
 * This class is provided to give the SASL profiles a way
 * to record what other peers have authenticated to the peer
 * they're serving.  
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.7 $, $Date: 2003/05/20 22:22:02 $
 *
 */
public class SASLSessionTable implements SessionListener
{
    private final static int DEFAULT_SIZE = 4;
    
    private static final String ERR_INVALID_PARAMETERS = "Invalid parameters to Session Table call";
    private static final String MSG_SESSIONS_TABLE_HEADER="===] BEEP Peer Session Table";
    private static final String MSG_EMPTY="===]  NONE";
    private static final String MSG_MECHANISM_PREFIX = " Mechanism=>";
    private static final String MSG_USER_PREFIX = "===]  User=>";
    private static final String MSG_SESSIONS_TABLE_TRAILER="===] End of Table";
                     
    private Log log = LogFactory.getLog(this.getClass());
    private Hashtable nameToSession, sessionToName;

    SASLSessionTable()
    {
        sessionToName = new Hashtable(DEFAULT_SIZE);
        nameToSession = new Hashtable(DEFAULT_SIZE);
    }
    
    /**
     * Method getSession locates a session based on the 
     * authenticator
     *  
     *     
     * */
    Session getSession(String authenticator)
        throws SASLException
    {
        if(authenticator == null)
            throw new SASLException(ERR_INVALID_PARAMETERS);
        return (Session)nameToSession.get(authenticator);
    }
    
    /**
     * Method isAuthenticated is used internally to find out
     * whether or not a user has authenticated already with
     * a given identity.
     */
    boolean isAuthenticated(String authenticator)
        throws SASLException
    {
        if(authenticator == null)
            throw new SASLException(ERR_INVALID_PARAMETERS);
        return (nameToSession.get(authenticator) != null);
    }

    /**
     * Method addEntry, adds information to the SASLSession
     * table to track what sessions have been authenicated
     * with what critera.
     */
    protected synchronized void addEntry(Session session)
        throws SASLException
    {
        boolean anonymous = false;
        
        if(session == null)
            throw new SASLException(ERR_INVALID_PARAMETERS);

        SessionCredential cred = session.getPeerCredential();
        if(cred == null)
            throw new SASLException(ERR_INVALID_PARAMETERS);        

        anonymous = cred.getAuthenticatorType().equals(SASLAnonymousProfile.MECHANISM);
        
        // If this session is mapped to another user, get rid of it.
        String authenticator = (String)sessionToName.get(session);

        // If it's OTP, store it both ways.
        if(anonymous)
        {
            // This will be in sessionToName by session
            sessionToName.remove(session);
        }
        else if(authenticator != null)
        {
            sessionToName.remove(session);
            nameToSession.remove(authenticator);
        }
        
        // Get the new credential
        authenticator = session.getPeerCredential().getAuthenticator();
        if(authenticator == null)
            throw new SASLException(ERR_INVALID_PARAMETERS);
        session.addSessionListener(this);
        if(sessionToName.contains(session))
            nameToSession.remove(authenticator);
        sessionToName.put(session, authenticator);
        if(!anonymous)
            nameToSession.put(authenticator, session);
        printContents();
    }
    
    /**
     * Method removeEntry removes SASL/Authenticator data from
     * the SASLSession table.  Called when sessions terminate,
     * or when credentials are 'cleared'.  
     */
    protected synchronized void removeEntry(Session session)
        throws SASLException
    {
        if(session == null)
            throw new SASLException(ERR_INVALID_PARAMETERS);

        if(session.getPeerCredential() != null)
        {
            String authenticator = session.getPeerCredential().getAuthenticator();
            if(authenticator == null)
                throw new SASLException(ERR_INVALID_PARAMETERS);
            nameToSession.remove(authenticator);
        }
        sessionToName.remove(session);
        session.removeSessionListener(this);
        printContents();
    }

    public void greetingReceived(SessionEvent e) {}

    /**
     * Method receiveEvent is implemented here so the SASLSessionTable
     * can receive events when a session is terminated (so that it
     * can update its information about what sessions are actively
     * authenticated etc.
     * 
     * @param event event the SessionEvent used.
     */
    public void sessionClosed(SessionEvent event)
    {
        try
        {
            Session s = (Session)event.getSource();
            removeEntry(s);
        }
        catch(ClassCastException x)
        {}        
        catch(SASLException x)
        {
            log.error("Error removing entry", x);
        }
    }
    
    /**
     * Method receiveEvent is implemented here so the SASLSessionTable
     * can receive events when a session is reset (so that it
     * can update its information about what sessions are actively
     * authenticated etc.
     * 
     * @param event event the SessionResetEvent used.
     */
    public void sessionReset(SessionResetEvent event)
    {
        try {
            removeEntry((Session)event.getSource());
            addEntry(event.getNewSession());
        }
        catch(SASLException e)
        {
            log.error("Error replacing entry", e);
        }
    }

    /**
     * Method printContents does a simple dump of the SASLSessionTable
     * for the purposes of monitoring or debugging.
     */
    void printContents()
    {
        log.debug(MSG_SESSIONS_TABLE_HEADER);
        if(sessionToName.size()==0)
        {
            log.debug(MSG_EMPTY);
        }
        else
        {
            Enumeration e = sessionToName.keys();
            while(e.hasMoreElements())
            {
                Session s = (Session)e.nextElement();
                String user = (String)sessionToName.get(s);
                String mech;
                if(s.getPeerCredential() != null)
                    mech = s.getPeerCredential().getAuthenticatorType();
                else 
                    mech = "UNKNOWN";
                    
                if (log.isDebugEnabled()) {
                    log.debug(MSG_USER_PREFIX + user +
                              MSG_MECHANISM_PREFIX + mech );
                }
            }        
        }
        log.debug(MSG_SESSIONS_TABLE_TRAILER);
    }
}
