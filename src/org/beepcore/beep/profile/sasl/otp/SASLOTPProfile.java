/*
 * SASLOTPProfile.java            $Revision: 1.2 $ $Date: 2001/04/09 13:26:22 $
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
package org.beepcore.beep.profile.sasl.otp;

import java.security.MessageDigest;

import java.util.Hashtable;
import java.util.StringTokenizer;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.beepcore.beep.core.*;
import org.beepcore.beep.profile.*;
import org.beepcore.beep.profile.sasl.*;
import org.beepcore.beep.profile.sasl.otp.algorithm.*;
import org.beepcore.beep.profile.sasl.otp.algorithm.md5.MD5;
import org.beepcore.beep.profile.sasl.otp.algorithm.sha1.SHA1;
import org.beepcore.beep.profile.sasl.otp.database.*;
import org.beepcore.beep.transport.tcp.*;
import org.beepcore.beep.util.*;


/**
 * This class implements the OTP (One-Time-Password) SASL mechanism
 * as an extension of the base SASL profile.
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/04/09 13:26:22 $
 *
 */
public class SASLOTPProfile extends SASLProfile {

    // Constants
    public static final String uri = "http://iana.org/beep/SASL/OTP";

    // Message Constants
    public static final String EXT = "ext";
    public static final String HEX = "hex:";
    public static final String SPACE = " ";
    public static final String WORD = "word:";
    public static final String HEX_INIT = "hex-init:";
    public static final String OTP_DB_FILENAME = "OTP_DB";
    public static final String SASL_OTP = "SASLOTPProfile";
    public static final String WORD_INIT = "word-init:";
    public static final String MECHANISM = "SASL/OTP";

    // Error Constants
    public static final String ERR_PARSING_DB = "Error parsing OTP DB";
    public static final String ERR_REJECTED =
        "Peer rejected SASL-OTP Start Channel Request";
    public static final String ERR_INVALID_ID =
        "Invalid or improperly formatted Identity information";

    // Instance Data
    private Hashtable authenticators;
    private MD5 md5;
    private SHA1 sha1;

    // Class Data
    private static Hashtable algorithms;
    private static SASLOTPProfile instance;
    private static UserDatabasePool userDatabase;
    
    public SASLOTPProfile() {}

    /**
     * Method init is used to construct various static data
     * used in the SASL OTP profile.
     */
    public void init(ProfileConfiguration config) 
        throws BEEPException
    {
        super.init(config);

        md5 = new MD5();
        sha1 = new SHA1();
        authenticators = new Hashtable();

        if (instance == null) {
            instance = this;
            algorithms = new Hashtable();

            algorithms.put(md5.getAlgorithmName(), md5);
            algorithms.put(sha1.getAlgorithmName(), sha1);
            userDatabase = new UserDatabasePool();
        }
    }

    static SASLOTPProfile instance() throws SASLException
    {
        if (instance == null) {
            throw new SASLException("SASLOTPProfile uninitialized");
        }

        return instance;
    }

    static UserDatabasePool getUserDatabase()
    {
        return userDatabase;
    }
    
    static Algorithm getAlgorithm(String name)
    {
        Object algo = algorithms.get(name);

        if (algo != null) {
            return (Algorithm) algo;
        }

        return null;
    }

    public String getURI()
    {
        return SASLOTPProfile.uri;
    }

    public void startChannel(Channel channel, String encoding, String data)
            throws StartChannelException
    {
        Blob blob = null;
        clearCredential(channel.getSession(), this);

        String authorize, authenticate, challenge = null;

        Log.logEntry(Log.SEV_DEBUG, SASL_OTP, "SASL-OTP Start Channel CCL");

        OTPAuthenticator temp = new OTPAuthenticator(this);

        try {

            // Digest the data
            // and generate a response (challenge) if possible
            // to embed in the profile back.
            if (data != null) {
                temp.started(channel);
                blob = new Blob(data);
                data = blob.getData();

                try {
                    blob = temp.receiveIDs(data);
                    Log.logEntry(Log.SEV_DEBUG, SASL_OTP,
                                 "Challenge is=>" + challenge);
                } catch (SASLException szf) {
                    temp.abortNoThrow(szf.getMessage());
                    // Write an error out in the profile
                    blob = new Blob(Blob.STATUS_ABORT,
                                    szf.getMessage());
                    return;
                }
                Log.logEntry(Log.SEV_DEBUG, SASL_OTP,
                             "Blobbed64 Challenge is=>" + data);
            }
            channel.setDataListener(temp);
            if(blob != null)
                sendProfile(channel.getSession(), uri, blob.toString(), channel);
            else
                sendProfile(channel.getSession(), uri, null, channel);

            // If we processed piggybacked data then blob will be non-null
            // otherwise, we need to 'start' the OTPAuthenticator so that
            // the state transitions remain valid
            if (blob == null) {
                temp.started(channel);
            }

            Log.logEntry(Log.SEV_DEBUG, "Started an SASL-OTP Channel");
        } catch (Exception x) {
            channel.getSession().terminate(x.getMessage());
            return;
        }
        throw new TuningResetException("SASL ANON RESET");
    }

    protected synchronized boolean validateIdentity(String authenticateId, 
                                                    OTPAuthenticator a)
        throws SASLException
    {
        // Do a kind of mutually exclusive bit
        if(authenticators.get(authenticateId) != null)
        {
            return false;
        }
        authenticators.put(authenticateId, a);
        return true;
    }
    
    public void closeChannel(Channel channel) throws CloseChannelException {}

    public StartChannelListener getStartChannelListener()
    {
        return this;
    }

    OTPAuthenticator startAuthentication(String authorizeId, 
                                                String authenticateId, 
                                                String password)
            throws SASLException
    {
        UserDatabase db = userDatabase.getUser(authenticateId);

        if (db == null) {
            throw new SASLException("User OTP data not found");
        }

        OTPAuthenticator a = new OTPAuthenticator(this, db,
                                                  password, authorizeId,
                                                  authenticateId);
        return a;
    }

    // Used only for init calls
    OTPAuthenticator startAuthentication(String authorizeId, 
                                                String authenticateId, 
                                                String password,
                                                String newAlgorithm,
                                                String newHash,
                                                String newSeed,
                                                String newSequence)
            throws SASLException
    {
        UserDatabase db = userDatabase.getUser(authenticateId);

        if (db == null) {
            throw new SASLException("User OTP data not found");
        }

        newSeed = newSeed.toLowerCase();
        OTPAuthenticator a = new OTPAuthenticator(this, db,
                                                  password, authorizeId,
                                                  authenticateId, 
                                                  newAlgorithm,
                                                  newHash,
                                                  newSeed,
                                                  newSequence);
        return a;
    }

    protected void finishInitiatorAuthentication(SessionCredential cred,
                                                 Session s)
    {
        super.finishInitiatorAuthentication(cred,s);
    }

    protected void finishListenerAuthentication(SessionCredential cred,
                                                Session s)
        throws SASLException
    {
        super.finishListenerAuthentication(cred, s);
        authenticators.remove(cred.getAuthenticator());
    }

    protected void failListenerAuthentication(Session session, 
                                              String authenticator)
    {
        authenticators.remove(authenticator);
        super.failListenerAuthentication(session);
    }

    /**
     * Method AuthenticateSASLOTP starts SASL OTP Authentication
     * between two peers. This is the NON-Piggybacking version 
     * (it doesn't send the initial identity information on the 
     * startChannelRequest).
     * 
     * If you want to do that (I recommend it, then use the NEXT one).
     * 
     * @param Session session is the session the user is authenticating on,
     * in other words, represents the peer we want to authenticate to.
     * @param String authorizeId is the identity this peer wants to be authorized 
     * to act as.
     * @param String authenticateId is the identity this peer will authenticate as
     * @param String pwd is the passphrase to authenticate with (it isn't
     * stored or kept around very long at all, it's only used in computation).
     * @throws SASLException if any issue is encountered (usually
     * rejection by the other peer).
     */
    public static Session AuthenticateSASLOTP(Session session, 
                                              String authorizeId, 
                                              String authenticateId, 
                                              String pwd)
            throws SASLException
    {
        boolean success = false;
        
        if (authenticateId == null || 
            session == null ||
            pwd == null ) {
            Log.logEntry(Log.SEV_ERROR, SASL_OTP,
                         ERR_INVALID_ID + authenticateId);

            throw new InvalidParameterException(ERR_INVALID_ID
                                                + authenticateId);
        }

        // @todo bad toad - clean this up with tuning move
        clearCredential(session, null);

        OTPAuthenticator auth =
            SASLOTPProfile.instance().startAuthentication(authorizeId,
                                                          authenticateId,
                                                          pwd);
        Channel ch = null;
        String startData = null;
        try
        {
            ch = session.startChannel(SASLOTPProfile.uri, false, null,
                                              auth);
            startData = ch.getStartData();
        }
        catch(BEEPException x)
        {
            auth.abort(x.getMessage());
        }

        // @todo EITHER use the Session Event Mechanism once it's detached from
        // the session I/O thread via the Channel Message (EVENT) Queues...
        // OR
        // Embed some tuning profile logic in ChannelZero (hacky) to address stuff.
        // For now, this is ok, the only thread blocked is the users, and it waits
        // until the Authentication succeeds or fails.
        Blob blob = null;
        
        if (startData != null)
        {
            blob = new Blob(startData);
            if( blob.getStatus().equals(SASL_STATUS_ABORT))
                throw new SASLException(ERR_REJECTED);
        }

        auth.started(ch);
        auth.sendIdentity(authorizeId, authenticateId);

        // We'll get notified when either
        // (a) An Error Occurs
        // (b) The authentication succeeds
        // Everything else from here on out is basically on autopilot
        try {
            synchronized (auth) {
                auth.wait();

                SessionCredential cred = session.getLocalCredential();

                if (cred == null) {
                    auth.abort("Authentication Failed");
                } else {
                    success = true;
                    Log.logEntry(Log.SEV_DEBUG, SASL_OTP,
                                 "Wow, cool!!! " + session
                                 + " is valid for\n" + cred.toString());
                }
            }
        } catch (Exception x) {
            auth.abort(x.getMessage());
        }
        return session;
    }

    /*
     * Method AuthenticateSASLOTP starts SASL OTP Authentication
     * between two peers.  This is the Piggybacking version (it does send the 
     * initial identity information on the startChannelRequest).
     * 
     * If you want to do that (I recommend it, then use the NEXT one).
     * 
     * @param Session session is the session the user is authenticating on,
     * in other words, represents the peer we want to authenticate to.
     * @param String authorizeId is the identity this peer wants to be authorized 
     * to act as.
     * @param String authenticateId is the identity this peer will authenticate as
     * @param String pwd is the passphrase to authenticate with (it isn't
     * stored or kept around very long at all, it's only used in computation).
     * @throws SASLException if any issue is encountered (usually
     * rejection by the other peer).
     */
    public static Session AuthenticateSASLOTPPiggybacked(Session session, 
                                                         String authorizeId, 
                                                         String authenticateId, 
                                                         String pwd)
            throws SASLException
    {
        boolean success = false;
        
        if (authenticateId == null) {
            Log.logEntry(Log.SEV_ERROR, SASL_OTP,
                         ERR_INVALID_ID + authenticateId);

            throw new InvalidParameterException(ERR_INVALID_ID
                                                + authenticateId);
        }

        // @todo bad toad - clean this up with tuning move
        clearCredential(session, null);

        OTPAuthenticator auth =
            SASLOTPProfile.instance().startAuthentication(authorizeId,
                                                          authenticateId,
                                                          pwd);
        Channel ch = null;
        String startData = null;
        try
        {
            ch = session.startChannel(SASLOTPProfile.uri, false, null,
                                              auth);
            startData = ch.getStartData();
        }
        catch(BEEPException x)
        {
            auth.abort(x.getMessage());
        }

        // @todo EITHER use the Session Event Mechanism once it's detached from
        // the session I/O thread via the Channel Message (EVENT) Queues...
        // OR
        // Embed some tuning profile logic in ChannelZero (hacky) to address stuff.
        // For now, this is ok, the only thread blocked is the users, and it waits
        // until the Authentication succeeds or fails.
        Blob blob = null;
        
        if (startData != null)
        {
            blob = new Blob(startData);
            if( blob.getStatus().equals(SASL_STATUS_ABORT))
                throw new SASLException(ERR_REJECTED);
        }

        auth.started(ch);
        auth.sendIdentity(authorizeId, authenticateId);

        // We'll get notified when either
        // (a) An Error Occurs
        // (b) The authentication succeeds
        // Everything else from here on out is basically on autopilot
        try {
            synchronized (auth) {
                auth.wait();

                SessionCredential cred = session.getLocalCredential();

                if (cred == null) {
                    auth.abort("Authentication Failed");
                } else {
                    success = true;
                    Log.logEntry(Log.SEV_DEBUG, SASL_OTP,
                                 "Wow, cool!!! " + session
                                 + " is valid for\n" + cred.toString());
                }
            }
        } catch (Exception x) {
            auth.abort(x.getMessage());
        }

        return session;
    }

    /*
     * Method AuthenticateSASLOTPWithInit is like the others, except
     * that is actually forces the server (if authentication succeeds)
     * to update the OTP db for this user.
     * 
     * If you want to do that (I recommend it, then use the NEXT one).
     * 
     * @param Session session is the session the user is authenticating on,
     * in other words, represents the peer we want to authenticate to.
     * @param String authorizeId is the identity this peer wants to be authorized 
     * to act as.
     * @param String authenticateId is the identity this peer will authenticate as
     * @param String pwd is the passphrase to authenticate with (it isn't
     * stored or kept around very long at all, it's only used in computation).
     *
     * @param String newSequence String representation of the new Sequence integer
     * @param String newAlgorithm name of the algorithm in the new OTP DB
     * @param String newSeed value of the seed in the new OTP DB
     * @param String newHas value of the lastHash in the new OTP DB
     *
     *
     * @throws SASLException if any issue is encountered (usually
     * rejection by the other peer).
     */
    public static Session AuthenticateSASLOTPWithInit(Session session, 
                                                      String authorizeId, 
                                                      String authenticateId, 
                                                      String pwd,
                                                      String newAlgorithm,
                                                      String newHash,
                                                      String newSeed,
                                                      String newSequence)
            throws SASLException
    {
        boolean success = false;
        
        // Test the values
        convertHexToBytes(newHash);
        newSeed = newSeed.toLowerCase();

        if(!OTPGenerator.validateSeed(newSeed) ||
           !OTPGenerator.validatePassphrase(pwd) ||
           !OTPGenerator.validateSequence(newSequence))
           throw new SASLException("Unsuitable values for the parameters to init-hex");
        
        // @todo bad toad - clean this up with tuning move
        clearCredential(session, null);

        OTPAuthenticator auth =
            SASLOTPProfile.instance().startAuthentication(authorizeId,
                                                          authenticateId,
                                                          pwd,
                                                          newAlgorithm,
                                                          newHash,
                                                          newSeed,
                                                          newSequence);
        Channel ch = null;
        String startData = null;
        try
        {
            ch = session.startChannel(SASLOTPProfile.uri, false, null,
                                              auth);
            startData = ch.getStartData();
        }
        catch(BEEPException x)
        {
            auth.abort(x.getMessage());
        }

        // @todo EITHER use the Session Event Mechanism once it's detached from
        // the session I/O thread via the Channel Message (EVENT) Queues...
        // OR
        // Embed some tuning profile logic in ChannelZero (hacky) to address stuff.
        // For now, this is ok, the only thread blocked is the users, and it waits
        // until the Authentication succeeds or fails.
        Blob blob = null;
        
        if (startData != null)
        {
            blob = new Blob(startData);
            if( blob.getStatus().equals(SASL_STATUS_ABORT))
                throw new SASLException(ERR_REJECTED);
        }

        auth.started(ch);
        auth.sendIdentity(authorizeId, authenticateId);

        // We'll get notified when either
        // (a) An Error Occurs
        // (b) The authentication succeeds
        // Everything else from here on out is basically on autopilot
        try {
            synchronized (auth) {
                auth.wait();

                SessionCredential cred = session.getLocalCredential();

                if (cred == null) {
                    auth.abort("Authentication Failed");
                } else {
                    success = true;
                    Log.logEntry(Log.SEV_DEBUG, SASL_OTP,
                                 "Wow, cool!!! " + session
                                 + " is valid for\n" + cred.toString());
                }
            }
        } catch (Exception x) {
            auth.abort(x.getMessage());
        }

        return session;
    }

    // See page 17 of RFC 2289 for the validation cases
    // used below.  The output should look like this...
    /*
        Testing MD5 Hash for =>testThis is a test.<=
        [00]FoldW=>INCH SEA ANNE LONG AHEM TOUR
        [01]FoldW=>EASE OIL FUM CURE AWRY AVIS
        [99]FoldW=>BAIL TUFT BITS GANG CHEF THY
        Testing SHA1 Hash
        [00]FoldW=>MILT VARY MAST OK SEES WENT
        [01]FoldW=>CART OTTO HIVE ODE VAT NUT
        [99]FoldW=>GAFF WAIT SKID GIG SKY EYED
        Testing MD5 Hash for =>alpha1AbCdEfGhIjK<=
        [00]FoldW=>FULL PEW DOWN ONCE MORT ARC
        [01]FoldW=>FACT HOOF AT FIST SITE KENT
        [99]FoldW=>BODE HOP JAKE STOW JUT RAP
        Testing SHA1 Hash
        [00]FoldW=>LEST OR HEEL SCOT ROB SUIT
        [01]FoldW=>RITE TAKE GELD COST TUNE RECK
        [99]FoldW=>MAY STAR TIN LYON VEDA STAN
        Testing MD5 Hash for =>correctOTP's are good<=
        [00]FoldW=>ULAN NEW ARMY FUSE SUIT EYED
        [01]FoldW=>SKIM CULT LOB SLAM POE HOWL
        [99]FoldW=>LONG IVY JULY AJAR BOND LEE
        Testing SHA1 Hash
        [00]FoldW=>RUST WELT KICK FELL TAIL FRAU
        [01]FoldW=>FLIT DOSE ALSO MEW DRUM DEFY
        [99]FoldW=>AURA ALOE HURL WING BERG WAIT
        Testing MD5 Hash for =>avalidseedA_Valid_Pass_Phrase<=
        [00]FoldW=>LEO IBIS WHAT OW WILD TROY
        [01]FoldW=>RUTH HOOF SHED DEAF OKAY DARK
        [99]FoldW=>FOWL KID MASH DEAD DUAL OAF
        Testing SHA1 Hash
        [00]FoldW=>JILT WOVE SOFA OWE KING VEND
        [01]FoldW=>OUST KITE MEN NE BEAR JOIN
        [99]FoldW=>ONCE GRAB SOOT CUBE SLAY WAIT
    */
    void testHash() throws SASLException
    {
        String pwd[] = new String[4];
        int limit=100;
        int i=0;
        pwd[i++] = new String("testThis is a test.");
        pwd[i++] = new String("alpha1AbCdEfGhIjK");
        pwd[i++] = new String("correctOTP's are good");
        pwd[i++] = new String("avalidseedA_Valid_Pass_Phrase");

        //      String pwd="correctOTP's are good";
        byte temp[], other[];
        long l = 0;

        for(int j=0; j < pwd.length && pwd[j] != null; j++)
        {
            temp = pwd[j].getBytes();
            System.out.println("Testing MD5 Hash for =>" + pwd[j] + "<=");

            for (i = 0; i < limit; i++) {
                other = md5.generateHash(temp);

                if ((i == 0) || (i == 1) || (i == 99)) {
                    if(i<10)
                        System.out.print("[0" + i + "]");
                    else
                        System.out.print("[" + i + "]");
//                    printHex(other);
                    l = this.convertBytesToLong(other);
                    System.out.println("FoldW=>" + OTPDictionary.convertHashToWords(l));
                }
                temp = other;
            }

            System.out.println("Testing SHA1 Hash");
            temp = pwd[j].getBytes();

            for (i = 0; i < limit; i++) {
                other = sha1.generateHash(temp);

                if ((i == 0) || (i == 1) || (i == 99)) {
                    if(i<10)
                        System.out.print("[0" + i + "]");
                    else
                        System.out.print("[" + i + "]");
//                    printHex(other);
                    l = this.convertBytesToLong(other);
                    System.out.println("FoldW=>" + OTPDictionary.convertHashToWords(l));
                }
                temp = other;
            }
        }
    }

    static void printHex(byte buff[])
    {
        Log.logEntry(Log.SEV_DEBUG, SASL_OTP, convertBytesToHex(buff));
    }

    public static byte[] convertLongToBytes(long l)
    {
        byte hash[] = new byte[8];

        for (int i = 7; i >= 0; i--) {
            hash[i] = (byte) (l & 0xff);
            l >>= 8;
        }

        return hash;
    }
    
    protected long convertBytesToLong(byte hash[]) {
        long l = 0L;

        for(int k=0;k<8;++k) {
             l = (l << 8) | (hash[k] & 0xff);
             }

        return(l);
    }

    public static long convertHexToLong(String hash) throws SASLException
    {
        if (hash.length() != 16) {
            throw new SASLException("Illegal hash" + hash.length());
        }
        long result = 0L;
        result = Long.valueOf(hash,16).longValue();
        return result;
    }

    public static String convertBytesToHex(byte hash[])
    {
        StringBuffer sb = new StringBuffer(16);

        for (int i = 0; i < 8; i++) {
            int val = hash[i] & 0xFF;

            if (val < 16) {
                sb.append('0');
            }

            sb.append(Integer.toHexString(val));
        }
//        Log.logEntry(Log.SEV_DEBUG, "Conversion=>" + sb.toString());
        return sb.toString();
    }
    
    public static byte[] convertHexToBytes(String hash)
        throws SASLException
    {
        byte result[] = new byte[8];
        
        if (hash.length() != 16) 
        {
            throw new SASLException("Illegal hash" + hash.length());
        }
//        hash = hash.toLowerCase();
//        Log.logEntry(Log.SEV_DEBUG, "Converting=>" + hash);
        
        for(int i = 0; i < 16; i+=2)
            result[i/2]=(byte)Integer.parseInt(hash.substring(i,i+2), 16);
//        Log.logEntry(Log.SEV_DEBUG, "Conversion=>" + convertBytesToHex(result));

        return result;
    }

    //Main is for testing..and is obviously cluttered
    // Commenting it out so it doesn't get documented
    public static void main(String argv[])
    {
        ConsoleLog log = new ConsoleLog();
        log.setSeverity(Log.SEV_DEBUG_VERBOSE);
        Log.setLogService(log);

        try
        {
            SASLOTPProfile s = new SASLOTPProfile();
            s.init(new ProfileConfiguration());
            MD5 md5 = new MD5();
            SHA1 sha1 = new SHA1();
/*
            long t = Long.MAX_VALUE;
            System.out.println("ConvertedLongMax=>"+s.convertBytesToHex(s.convertLongToBytes(t)));            
            byte b[] = s.convertHexToBytes("9e876134d90499dd");
            System.out.println("=>"+s.convertBytesToHex(b));            
            long l = s.convertBytesToLong(b);
            System.out.println("Long=>"+l);
            System.out.println("Long=>"+Long.toHexString(l));
            
            for(int k=0;k<100;k++)
            {
                byte c[] = md5.generateHash(b);
                if(k<2 || k>98)
                    System.out.println("Hash["+k+"]=>"+s.convertBytesToHex(c));
                b = c;
            }            
            String whyNot= "TeStThis is a test.";
            byte d[] = md5.generateHash(whyNot);
            System.out.println(s.convertBytesToHex(d));            
            d = sha1.generateHash(whyNot);
            System.out.println(s.convertBytesToHex(d));
            
            String test = "63D936639745385B";
            l = s.convertHexToLong(test);
            System.out.println("L(long)=>"+Long.toHexString(l));
            byte buff[] = s.convertLongToBytes(l);
            test = s.convertBytesToHex(buff);
            System.out.println("L(bytes->String)=>"+test);
            buff = s.convertHexToBytes(test);
            System.out.println("L(bytes->String2)=>"+s.convertBytesToHex(buff));
*/
            s.testHash();
        }
        catch(Exception x)
        {
            x.printStackTrace();
        }
    }
}