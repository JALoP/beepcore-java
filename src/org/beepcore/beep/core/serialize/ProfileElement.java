/*
 * ProfileElement.java  $Revision: 1.1 $ $Date: 2006/02/25 18:02:49 $
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
 * Created on Apr 24, 2004
 */
package org.beepcore.beep.core.serialize;

/**
 *
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2006/02/25 18:02:49 $
 */
public class ProfileElement {
    String uri;
    boolean base64Encoding;
    String data;

    public ProfileElement(String uri, boolean base64Encoding, String data) {
        this.uri = uri;
        this.base64Encoding = base64Encoding;
        this.data = data;
    }

    public ProfileElement(String uri) {
        this(uri, false, null);
    }

    public String getUri() {
        return uri;
    }

    public boolean getBase64Encoding() {
        return base64Encoding;
    }

    public String getData() {
        return data;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setBase64Encoding(boolean base64Encoding) {
        this.base64Encoding = base64Encoding;
    }

    public void setData(String data) {
        this.data = data;
    }
}
