
/*
 * LogService.java            $Revision: 1.3 $ $Date: 2001/11/08 05:51:35 $
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
package org.beepcore.beep.util;


/**
 * Interface LogService
 *
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.3 $, $Date: 2001/11/08 05:51:35 $
 */
public interface LogService {

    /**
     * Method logEntry
     *
     *
     * @param sev
     * @param service
     * @param message
     *
     */
    public void logEntry(int sev, String service, String message);

    /**
     * Method logEntry
     *
     *
     * @param sev
     * @param service
     * @param exception
     *
     */
    public void logEntry(int sev, String service, Throwable exception);

    /**
     * Used to determine if a message of <code>sev</code> will be logged.
     */
    public boolean isLogged(int sev);
}
