/*-------------------------------------------------------------------------
 * Copyright (c) 2000, Permabit, Inc.  All rights reserved.
 *
 * $Id: Log4JLog.java,v 1.1 2001/07/19 21:39:22 huston Exp $
 *
 */

package org.beepcore.beep.util;

import java.io.IOException;

import org.beepcore.beep.util.LogService;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.FileAppender;

/** This class serves as a translator from the 
    Beepcore-Java LogService logging mechanism to the 
    Log4J logging package.

    @author Dave Golombek (daveg@permabit.com)

    @see <a href="www.beepcore.org">Beepcore-Java</a>
    @see org.beepcore.beep.util.LogService
    @see <a href="jakarta.apache.org/log4j/">Log4J</a>
*/
public class Log4JLog implements LogService {
  /** Log4j Logging Category */
  private static Category cat = 
    Category.getInstance(Log4JLog.class.getName());

  /** Map the LogService priorities into Log4J priorities.  This is
      not a clean mapping, since there are more LogService priorities
      than Log4J categories.  If a one-to-one mapping is desired, the
      Log4J Priority class could be overridden by a new class
      supporting all desired priorities */
  private static Priority[] mapping = { Priority.FATAL, // SEV_EMERGENCY
					Priority.ERROR, // SEV_ALERT
					Priority.ERROR, // SEV_CRITICAL
					Priority.ERROR, // SEV_ERROR
					Priority.WARN,  // SEV_WARNING
					Priority.WARN,  // SEV_NOTICE
					Priority.INFO,  // SEV_INFORMATIONAL
					Priority.DEBUG, // SEV_DEBUG
					Priority.DEBUG, // SEV_DEBUG_VERBOSE
  };

  public Log4JLog() { 
  }

  public Log4JLog(int severity) { 
    cat.setPriority(mapping[severity]);
  }

  public void logEntry(int severity, String service, String message) {
    cat.log(mapping[severity], service + ": " + message);
  }

  public void logEntry(int severity, String service, Throwable exception) {
    cat.log(mapping[severity], service, exception);
  }

  public boolean isLogged(int severity) {
    return(cat.isEnabledFor(mapping[severity]));
  }

  public void setSeverity(int severity) {
    cat.setPriority(mapping[severity]);
  }
}
  
