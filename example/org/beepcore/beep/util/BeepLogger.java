package org.beepcore.beep.util;

/*
 * BeepLogger.java
 *
 * Created on August 28, 2002, 5:59 PM
 * 
 * @author Serge Adda
 */

import org.beepcore.beep.util.*;

import java.util.logging.*;

/**
 *
 * @author  sad
 */
public class BeepLogger implements LogService {
    
    private Logger logger_;
    
    /** Creates a new instance of BeepLogger */
    public BeepLogger() {
        this(Log.SEV_ERROR);
    }
    
    /** Creates a new instance of BeepLogger */
    public BeepLogger(int severity) {
        this(Logger.global, severity);
    }
    
    /** Creates a new instance of BeepLogger */
    public BeepLogger(Logger logger, int severity) {
        logger_ = logger;
        setSeverity(severity);
    }
    
    private Level beep2j2se(int sev) {
        switch (sev) {
            case Log.SEV_EMERGENCY:
            case Log.SEV_ALERT:
            case Log.SEV_CRITICAL:
            case Log.SEV_ERROR:
                return Level.SEVERE;
            case Log.SEV_WARNING:
                return Level.WARNING;
            case Log.SEV_NOTICE:
            case Log.SEV_INFORMATIONAL:
                return Level.INFO;
            case Log.SEV_DEBUG:
                return Level.FINE;
            case Log.SEV_DEBUG_VERBOSE:
                return Level.ALL;
            default:
                return Level.ALL;
        }
    }
    
    /** Used to determine if a message of <code>sev</code> will be logged.
     *
     */
    public boolean isLogged(int sev) {
        return logger_.isLoggable(beep2j2se(sev));
    }
    
    /** Used decide if a message of <code>sev</code> will be logged.
     *
     */
    public void setSeverity(int sev) {
        logger_.setLevel(beep2j2se(sev));
    }
    
    /** Method logEntry
     *
     *
     * @param sev
     * @param service
     * @param message
     *
     *
     */
    public void logEntry(int sev, String service, String message) {
        logger_.log(beep2j2se(sev), service + ": " + message);
    }
    
    /** Method logEntry
     *
     *
     * @param sev
     * @param service
     * @param exception
     *
     *
     */
    public void logEntry(int sev, String service, Throwable exception) {
        logger_.log(beep2j2se(sev), service,  exception);
    }
}
