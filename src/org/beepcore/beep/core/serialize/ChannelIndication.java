/*
 * ChannelIndication.java  $Revision: 1.1 $ $Date: 2006/02/25 18:02:49 $
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
 * Created on Apr 21, 2004
 */

package org.beepcore.beep.core.serialize;

/**
 *
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2006/02/25 18:02:49 $
 */
public class ChannelIndication {
    public static final int START = 1;
    public static final int CLOSE = 2;
    
    private final int type;
    
    ChannelIndication(int type)
    {
        this.type = type;
    }
    
    public int getType()
    {
        return type;
    }
}
