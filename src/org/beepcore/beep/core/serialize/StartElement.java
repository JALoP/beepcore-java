/*
 * StartElement.java  $Revision: 1.1 $ $Date: 2006/02/25 18:02:49 $
 *
 * Copyright (c) 2004 Huston Franklin.  All rights reserved.
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
/*
 * Created on Apr 18, 2004
 */
package org.beepcore.beep.core.serialize;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2006/02/25 18:02:49 $
 */
public class StartElement extends ChannelIndication {
    private static final Collection emptyCollection =
        Collections.unmodifiableCollection(new LinkedList());
    private int channelNumber;
    private String serverName;
    private Collection profiles;
    
    public StartElement(int channelNumber, String serverName,
                                  Collection profiles)
    {
        super(ChannelIndication.START);

        this.channelNumber = channelNumber;
        this.serverName = serverName;
        this.profiles = Collections.unmodifiableCollection(profiles);
    }
    
    public StartElement(int channelNumber, Collection profiles) {
        this(channelNumber, null, profiles);
    }
    
    public StartElement() {
        super(ChannelIndication.START);

        channelNumber = 0;
        serverName = null;
        profiles = emptyCollection;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public String getServerName() {
        return serverName;
    }

    public Collection getProfiles() {
        return profiles;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setProfiles(Collection profiles) {
        this.profiles = Collections.unmodifiableCollection(profiles);
    }
}
