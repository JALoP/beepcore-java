/*
 * SASLProfile.java  $Revision: 1.5 $ $Date: 2001/07/30 13:07:11 $
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
package org.beepcore.beep.profile.sasl;


import java.io.*;

import org.beepcore.beep.core.*;
import org.beepcore.beep.profile.*;
import org.beepcore.beep.transport.tcp.TCPSession;
import org.beepcore.beep.util.*;


/**
 * This class is the base SASL Profile implementation.  It's
 * extended by ANONYMOUS and OTP and EXTERNAL
 *
 * It provides a place for shared data and shared functionality
 * for sending messages or replies, encoding data, etc.  Some of
 * these support routine are provided merely because SASLProfile
 * extends TuningProfile
 *
 * It is anticipated that MECHANISM-specific state associated
 * with an ongoing SASL connection will be provided by extensions
 * as we don't want to mandate data structures, storage etc.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.5 $, $Date: 2001/07/30 13:07:11 $
 *
 */
public abstract class SASLProfile extends TuningProfile {

    // Constants
    public static final String COMPLETE = "<blob status='complete'>";
    public final static String ERR_INVALID_SASL_STATUS =
        "Invalid SASL Status attribute value employed";
    public static final String ENCODING_NONE = "none";
    public static final String ENCODING_BASE64 = "base64";
    public static final String ENCODING_DEFAULT = ENCODING_NONE;
    public static final String FRAGMENT_ERROR_PREFIX = "<error ";
    public static final String LOCALIZE_DEFAULT = "i-default";
    public final static String SASL = "sasl";
    public final static String SASL_STATUS_ABORT = "abort";
    public final static String SASL_STATUS_COMPLETE = "complete";
    public final static String SASL_STATUS_CONTINUE = "continue";
    public final static String SASL_STATUS_NONE = "none";

    // Data
    protected SASLSessionTable sessionTable;
    
    public SASLProfile() 
    {
        sessionTable = new SASLSessionTable();
    }

    /**
     * Method clearCredential simply clears the credentials associated
     * with a given Session - this is typically done before a new
     * authentication is attempted.  
     * @todo make sure the spec allows for multiple authentications..
     * I seem to remember it saying 'only once' or something like that.
     * It certainly seems simpler, but if the lib can hack it, then
     * maybe we should allow it?  Or maybe it's just a good security
     * constraint.
     */
    protected static void clearCredential(Session s, SASLProfile profile)
    {

        // @todo bad toad - clean this up with tuning move
        try {
            setLocalCredential(s, null);
            setPeerCredential(s, null);
            if(profile != null)
                profile.sessionTable.removeEntry(s);
        } catch (Exception x) 
        {}
    }
        
    /**
     * Method finishInitiatorAuthentication basically says 'we've
     * authenticated successfully' and calls the tuningprofile
     * method (exposed by SASLProfile's extension of the core
     * class TuningProfile) which sets the local credential.  The
     * session has two credentials, one in each direction, so it's
     * necessary to differentiate between local credentials and 
     * credentials associated with the peer in a given session.
     */
    protected void finishInitiatorAuthentication(SessionCredential cred,
                                                 Session s)
    {

        // @todo incredibly lame -you'retired, go to bed
        setLocalCredential(s, cred);
    }

    /**
     * Method finishListenerAuthentication basically says 'some peer has
     * authenticated successfully' and calls the tuningprofile
     * method (exposed by SASLProfile's extension of the core
     * class TuningProfile) which sets the peer credential.  The
     * session has two credentials, one in each direction, so it's
     * necessary to differentiate between local credentials and 
     * credentials associated with the peer in a given session.
     */
    protected void finishListenerAuthentication(SessionCredential cred,
                                                Session s)
        throws SASLException
    {
        if(cred != null)
        {
            setPeerCredential(s, cred);
            sessionTable.addEntry(s);
        }
    }
    
    protected void failListenerAuthentication(Session session)
    {
        try
        {
            sessionTable.removeEntry(session);
            clearCredential(session, null);
        }
        catch(SASLException x)
        {}
    }
}
