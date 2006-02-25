/*
 * GreetingElement.java  $Revision: 1.1 $ $Date: 2006/02/25 18:02:49 $
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
 * Created on Apr 19, 2004
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
public class GreetingElement {
    public static final String LOCALIZE_DEFAULT = "i-default";

    private static final Collection emptyCollection =
        Collections.unmodifiableCollection(new LinkedList());

    private String features;
    private String localize;
    private Collection profiles;

    public GreetingElement(String features, String localize,
                                Collection profiles)
    {
        this.features = features;
        if (localize != null) {
            this.localize = localize;
        } else {
            this.localize = LOCALIZE_DEFAULT;
        }

        this.profiles = Collections.unmodifiableCollection(profiles);
    }

    public GreetingElement(String features, Collection profiles) {
        this(features, null, profiles);
    }

    public GreetingElement(Collection profiles) {
        this(null, null, profiles);
    }

    public GreetingElement() {
        features = null;
        localize = LOCALIZE_DEFAULT;
        profiles = emptyCollection;
    }

    public String getFeatures() {
        return features;
    }

    public String getLocalize() {
        return localize;
    }

    public Collection getProfiles() {
        return profiles;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public void setLocalize(String localize) {
        this.localize = localize;
    }

    public void setProfiles(Collection profiles) {
        if (profiles == null) {
            this.profiles = emptyCollection;
        } else {
            this.profiles = Collections.unmodifiableCollection(profiles);
        }
    }
}
