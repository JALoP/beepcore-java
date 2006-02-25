/*
 * ChannelZeroParser.java  $Revision: 1.1 $ $Date: 2006/02/25 18:02:49 $
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
 * Created on Apr 14, 2004
 */
package org.beepcore.beep.core.serialize;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



import org.beepcore.beep.core.BEEPError;
import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.core.InputDataStream;
import org.beepcore.beep.core.MimeHeaders;
import org.beepcore.beep.util.StringUtil;

/**
 *
 * @author Huston Franklin
 * @version $Revision: 1.1 $, $Date: 2006/02/25 18:02:49 $
 */
public class ChannelZeroParser {
    public static final int MAX_PROFILE_CONTENT_LENGTH = 4096;
    private static final String ERR_MALFORMED_XML_MSG = "Malformed XML";
    private static final String ERR_UNKNOWN_OPERATION_ELEMENT_MSG =
        "Unknown operation element";

    private DocumentBuilder builder;    // generic XML parser
    
    public ChannelZeroParser() throws BEEPException {
        try {
            builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new BEEPException("Invalid parser configuration");
        }
    }

    private Element processMessage(InputDataStream message) throws BEEPException
    {

        // check the message content type
        if (!message.getInputStream().getContentType().equals(MimeHeaders.BEEP_XML_CONTENT_TYPE)) {
            throw new BEEPException("Invalid content type for this message");
        }

        // parse the stream
        Document doc;

        try {
            doc = builder.parse(message.getInputStream());
        } catch (SAXException e) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG, e);
        } catch (IOException e) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG, e);
        }

        if (doc == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        }

        Element topElement = doc.getDocumentElement();

        if (topElement == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        }

        return topElement;
    }
    
    private StartElement parseStartIndication(Element topElement)
        throws BEEPError
    {
        String channelNumberString = topElement.getAttribute("number");

        if (channelNumberString == null) {
            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                "Malformed <start>: no channel number");
        }
        
        int channelNumber;
        try {
            channelNumber = Integer.parseInt(channelNumberString);
        } catch (NumberFormatException e) {
            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                "Malformed <start>: bad channel number");
        }
        
        if (channelNumber <= 0) {
            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                "Malformed <start>: invalid channel number");
        }

        String serverName = topElement.getAttribute("serverName");
        
        NodeList profiles =
            topElement.getElementsByTagName("profile");

        if (profiles == null) {
            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                "Malformed <start>: no profiles");
        }

        LinkedList profileList = new LinkedList();

        for (int i = 0; i < profiles.getLength(); i++) {
            Element profile = (Element) profiles.item(i);
            String uri = profile.getAttribute("uri");

            if (uri == null) {
                throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                    "no uri in profile");
            }

            String encoding = profile.getAttribute("encoding");
            boolean b64;

            if ((encoding == null) || encoding.equals("")) {
                b64 = false;
            } else if (encoding.equalsIgnoreCase("base64")) {
                b64 = true;
            } else if (encoding.equalsIgnoreCase("none")) {
                b64 = false;
            } else {
                throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                    "unknown encoding in start");
            }

            String data = null;
            Node dataNode = profile.getFirstChild();

            if (dataNode != null) {
                data = dataNode.getNodeValue();

                if (data.length() > MAX_PROFILE_CONTENT_LENGTH) {
                    throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                        "Element's PCDATA exceeds " +
                                        "the maximum size");
                }
            }

            profileList.add(new ProfileElement(uri, b64, data));
        }

        return new StartElement(channelNumber,
                                          serverName, profileList);
    }
    
    private CloseElement parseCloseIndication(Element topElement)
        throws BEEPError
    {
        String channelNumberString = topElement.getAttribute("number");

        int channelNumber = 0;
        
        if (channelNumberString != null) {
            try {
                channelNumber = Integer.parseInt(channelNumberString);
            } catch (NumberFormatException e) {
                throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                    "Malformed <close>: bad channel number");
            }
        }

        String codeString = topElement.getAttribute("code");

        if (codeString == null) {
            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                "Malformed <close>: no code attribute");
        }

        int code;
        try {
            code = Integer.parseInt(codeString);
        } catch (NumberFormatException e) {
            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                "Malformed <close>: bad code");
        }

        // this attribute is implied
        String xmlLang = topElement.getAttribute("xml:lang");
        String data = null;
        Node dataNode = topElement.getFirstChild();

        if (dataNode != null) {
            data = dataNode.getNodeValue();

            if (data.length() > MAX_PROFILE_CONTENT_LENGTH) {
                throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                    "Element's PCDATA exceeds " +
                                    "the maximum size");
            }
        }
        
        return new CloseElement(channelNumber, code, xmlLang, data);
    }

    public ChannelIndication parseIndication(InputDataStream data)
        throws BEEPError
    {
        Element topElement;

        try {
            topElement = processMessage(data);
        } catch (BEEPException e) {
            throw new BEEPError(BEEPError.CODE_GENERAL_SYNTAX_ERROR,
                                ERR_MALFORMED_XML_MSG);
        }

        String elementName = topElement.getTagName();

        if (elementName == null) {
            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                ERR_MALFORMED_XML_MSG);
        }

        if (elementName.equals("start")) {
            return parseStartIndication(topElement);
        } else if (elementName.equals("close")) {
            return parseCloseIndication(topElement);
        } else {
            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
        }
    }

    public GreetingElement parseGreetingConfirmation(InputDataStream data)
        throws BEEPException
    {
        Element topElement = processMessage(data);

        String elementName = topElement.getTagName();

        if (elementName == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        } else if (!elementName.equals("greeting")) {
            throw new BEEPException(ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
        }

        // this attribute is implied
        String features = topElement.getAttribute("features");

        // This attribute has a default value
        String localize = topElement.getAttribute("localize");

        // Read the profiles - note, the greeting is valid
        // with 0 profiles
        NodeList profiles =
            topElement.getElementsByTagName("profile");

        LinkedList profileList = new LinkedList();

        for (int i = 0; i < profiles.getLength(); i++) {
            Element profile = (Element) profiles.item(i);
            String uri = profile.getAttribute("uri");

            if (uri == null) {
                throw new BEEPException("Malformed profile");
            }

            // Since <profile> elements in greetings cannot contain
            // pigggyback'd data there won't be an encoding attribute
            profileList.add(i, uri);
        }
        
        return new GreetingElement(features, localize, profileList);
    }
    
    public ProfileElement parseStartConfirmation(InputDataStream is)
        throws BEEPException
    {
        Element topElement = processMessage(is);

        String elementName = topElement.getTagName();

        if (elementName == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        } 

        if (elementName.equals("profile") == false) {
            throw new BEEPException(ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
        }

        try {
            String uri = topElement.getAttribute("uri");

            if (uri == null) {
                throw new BEEPException("Malformed profile");
            }

            String encoding = topElement.getAttribute("encoding");

            boolean base64 = false;
            if (encoding != null && encoding.equals("base64")) {
                base64 = true;
            }

            Node dataNode = topElement.getFirstChild();
            String data = null;

            if (dataNode != null) {
                data = dataNode.getNodeValue();

                if (data.length() > MAX_PROFILE_CONTENT_LENGTH) {
                    throw new BEEPException("Element's PCDATA " +
                                            "exceeds the " +
                                            "maximum size");
                }
            }

            return new ProfileElement(uri, base64, data);
        } catch (Exception x) {
            throw new BEEPException(x);
        }
    }

    public void parseCloseConfirmation(InputDataStream is)
        throws BEEPException
    {
        Element topElement = processMessage(is);
        String elementName = topElement.getTagName();

        if (elementName == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        }
        
        if (elementName.equals("ok") == false) {
            throw new BEEPException(ERR_UNKNOWN_OPERATION_ELEMENT_MSG);
        }

        return;
    }

    public ErrorElement parseError(InputDataStream is)
        throws BEEPException
    {
        Element topElement = processMessage(is);
        
        if (topElement == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        }
        
        // check for <error>
        String elementName = topElement.getTagName();
        
        if (elementName == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        } else if (!elementName.equals("error")) {
            throw new BEEPException("Unknown operation element");
        }
        
        String codeString = topElement.getAttribute("code");
        
        if (codeString == null) {
            throw new BEEPException(ERR_MALFORMED_XML_MSG);
        }
        
        int code;
        try {
            code = Integer.parseInt(codeString);
        } catch (NumberFormatException e) {
            throw new BEEPError(BEEPError.CODE_PARAMETER_ERROR,
                                "Malformed <close>: bad code");
        }

        // this attribute is implied
        String xmlLang = topElement.getAttribute("xml:lang");
        Node dataNode = topElement.getFirstChild();
        String data = null;
        
        if (dataNode != null) {
            data = dataNode.getNodeValue();
        }

        return new ErrorElement(code, xmlLang, data);
    }

    public byte[] serializeStart(StartElement start)
    {
        // create the message in a buffer and send it
        StringBuffer buf = new StringBuffer();

        buf.append("<start number='");
        buf.append(start.getChannelNumber());
        if (start.getServerName() != null) {
            buf.append("' serverName='");
            buf.append(start.getServerName());
        }
        
        if (start.getProfiles().size() == 0) {
            // @TODO throw exception
        }
        
        buf.append("'>");

        Iterator i = start.getProfiles().iterator();

        while (i.hasNext()) {
            serializeProfile((ProfileElement) i.next(), buf);
        }

        buf.append("</start>");

        return StringUtil.stringBufferToAscii(buf);
    }
    
    public byte[] serializeClose(CloseElement close)
    {
        // Construct Message
        StringBuffer buf = new StringBuffer();

        buf.append("<close number='");
        buf.append(close.getChannelNumber());
        buf.append("' code='");
        buf.append(close.getCode());

        if (close.getDiagnostic() != null) {
            if (close.getXmlLang() != null) {
                buf.append("' xml:lang='");
                buf.append(close.getXmlLang());
            }

            buf.append("'>");
            buf.append(close.getDiagnostic());
            buf.append("</close>");
        } else {
            buf.append("'/>");
        }
        
        return StringUtil.stringBufferToAscii(buf);
    }
    
    private void serializeProfile(ProfileElement profile, StringBuffer buf) {
        buf.append("<profile uri='");
        buf.append(profile.getUri());

        if (profile.getData() != null) {
            if (profile.getBase64Encoding()) {
                buf.append("' encoding='base64");
            }
            buf.append("'><![CDATA[");
            buf.append(profile.getData());
            buf.append("]]></profile>");
        } else {
            buf.append("' />");
        }
    }
    
    public byte[] serializeProfile(ProfileElement profile)
    {
        StringBuffer buf = new StringBuffer();

        serializeProfile(profile, buf);

        return StringUtil.stringBufferToAscii(buf);
    }

    public byte[] serializeGreeting(GreetingElement greeting)
    {
        StringBuffer buf = new StringBuffer();

        buf.append("<greeting");

        if (!greeting.getLocalize().equals(GreetingElement.LOCALIZE_DEFAULT)) {
            buf.append(" localize='");
            buf.append(greeting.getLocalize());
            buf.append('\'');
        }

        if (greeting.getFeatures() != null) {
            buf.append(" features='");
            buf.append(greeting.getFeatures());
            buf.append('\'');
        }

        if (greeting.getProfiles().size() == 0) {
            buf.append("/>");
        } else {
            buf.append('>');

            for (Iterator i = greeting.getProfiles().iterator(); i.hasNext(); ) {
                buf.append("<profile uri='");
                buf.append(i.next());
                buf.append("' />");
            }

            buf.append("</greeting>");
        }

        return StringUtil.stringBufferToAscii(buf);
    }

    private StringBuffer createErrorBuffer(int code, String diagnostic,
                                           String xmlLang)
    {
        StringBuffer buf = new StringBuffer(128);
        
        buf.append("<error code='");
        buf.append(code);
        
        if (xmlLang != null) {
            buf.append("' xml:lang='");
            buf.append(xmlLang);
        }
        
        if (diagnostic != null) {
            buf.append("' >");
            buf.append(diagnostic);
            buf.append("</error>");
        } else {
            buf.append("' />");
        }
        
        return buf;
    }

    public String createErrorMessage(ErrorElement error)
    {
        StringBuffer buf = createErrorBuffer(error.getCode(),
                error.getDiagnostic(),
                error.getXmlLang());

        return buf.toString();
    }

    public byte[] serializeError(ErrorElement error)
    {
        StringBuffer buf = createErrorBuffer(error.getCode(),
                                             error.getDiagnostic(),
                                             error.getXmlLang());
        
        return StringUtil.stringBufferToAscii(buf);
    }
}
