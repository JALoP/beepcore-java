/*
 * SessionTuningProperties.java  $Revision: 1.4 $ $Date: 2001/11/08 05:51:34 $
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
package org.beepcore.beep.core;


import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Class SessionTuningProperties
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Rev$, $Date: 2001/11/08 05:51:34 $
 */
public class SessionTuningProperties {

    // Standard settings
    public final static String ENCRYPTION = "ENCRYPTION";
    public final static String[] STANDARD_PROPERTIES = { ENCRYPTION };

    // Pretty Print Crap
    public static final String LEFT = "[";
    public static final String RIGHT = "]";
    public static final String MIDDLE = "=>";
    public static final String NEWLINE = "\n";
    public static final String NO_PROPERTIES =
        "SessionTuningProperties: No properties";

    static final SessionTuningProperties emptyTuningProperties =
        new SessionTuningProperties();

    // Data
    private Hashtable properties;

    /**
     * Constructor SessionTuningProperties
     *
     *
     */
    public SessionTuningProperties()
    {
        properties = new Hashtable();
    }

    /**
     * Constructor SessionCredential
     *
     *
     * @param props
     *
     */
    public SessionTuningProperties(Hashtable props)
    {
        properties = props;
    }

    /**
     * generic way to get the value of a property.  It is recommended
     * to use the standard methods for standard properties and that
     * these methods only be used for non standard properties.
     */
    public Object getProperty(String property)
    {
        return properties.get(property);
    }

    /**
     * generic way to set the value of a property.  It is recommended
     * to use the standard methods for standard properties and that
     * these methods only be used for non standard properties.
     */
    public void setProperty(String property, String value)
    {
        properties.put(property, value);
    }

    /**
     * gets the status of encryption
     */
    public boolean getEncrypted()
    {
        return ((String) properties.get(ENCRYPTION)).equals("true");
    }

    /**
     * sets the status of encryption
     */
    public void setEncrypted()
    {
        properties.put(ENCRYPTION, "true");
    }

    /**
     * Method toString
     *
     */
    public String toString()
    {
        StringBuffer b = new StringBuffer(1024);
        String key, value;
        int i = 0;

        if (properties.size() == 0) {
            return NO_PROPERTIES;
        }

        Enumeration e = properties.keys();

        while (e.hasMoreElements()) {
            key = (String) e.nextElement();
            value = (String) properties.get(key);

            b.append(LEFT);
            b.append(i);
            b.append(RIGHT);
            b.append(key);
            b.append(MIDDLE);
            b.append(value);
            b.append(NEWLINE);
        }

        return b.toString();
    }

    boolean isEmpty() {
        return properties.isEmpty();
    }
}
