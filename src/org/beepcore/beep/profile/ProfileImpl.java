
/*
 * ProfileImpl.java    $Revision: 1.1.1.1 $ $Date: 2001/04/02 08:45:29 $
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
package org.beepcore.beep.profile;


import org.beepcore.beep.core.*;


/**
 * A class provided for it's utility and shared code,
 * as a base class that all profiles can extend.
 */
public abstract class ProfileImpl implements Profile {

    // Data
    protected ProfileConfiguration configuration;

    /**
     * Constructor ProfileImpl
     *
     *
     */
    public ProfileImpl()
    {
        configuration = null;
    }

    public void init(ProfileConfiguration config) throws BEEPException
    {
        configuration = config;
    }

    public ProfileConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * Method getProperty
     *
     *
     * @param name
     *
     */
    public String getProperty(String name)
    {
        if (configuration != null) {
            return configuration.getProperty(name);
        }

        return null;
    }

    /**
     * Method setProperty
     *
     *
     * @param name
     * @param value
     *
     */
    public void setProperty(String name, String value)
    {
        if (configuration != null) {
            configuration.setProperty(name, value);
        }
    }
}
