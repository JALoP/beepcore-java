/*
 * Blob.java            $Revision: 1.10 $ $Date: 2003/11/18 14:03:08 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2002 Huston Franklin.  All rights reserved.
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
package org.beepcore.beep.profile.sasl;

import java.io.IOException;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The Blob class tries to abstract some of the complexity inherent in
 * dealing with the XML <blob> elements that are used in the SASL 
 * profiles.  This class may eventually belong in CORE, but until then.
 * One other developmental issue - it might be simpler to just use 
 * strings all the time.  I just try to avoid string comparison, but
 * I'm not sure this effort is buying anything.  I need to examine
 * how the status is used...and may well go back to String representation
 * of status alone.
 * @todo move Blob to core
 * 
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.10 $, $Date: 2003/11/18 14:03:08 $
 *
 */
public class Blob
{
    // Constants
    // Status Constants
    public final static String ABORT    = SASLProfile.SASL_STATUS_ABORT;
    public final static String COMPLETE = SASLProfile.SASL_STATUS_COMPLETE;
    public final static String CONTINUE = SASLProfile.SASL_STATUS_CONTINUE;
    public final static String NONE     = SASLProfile.SASL_STATUS_NONE;
    public final static int STATUS_NONE     = 0;
    public final static int STATUS_ABORT    = 1;
    public final static int STATUS_CONTINUE = 2;
    public final static int STATUS_COMPLETE = 3;   
    public final static int STATUS_LIMIT    = 4;
    // Data Constants
    public final static int DEFAULT_BLOB_SIZE = 1024;
    // Error Constants
    private final static String ERR_INVALID_STATUS_VALUE =
        "Invalid SASL Status for Blob";
    private final static String ERR_XML_PARSE_FAILURE = "Failed to parse xml";
    // XML Fragment Constants


    // Class data
    private static String statusMappings[];
    private static BASE64Decoder decoder;
    private static BASE64Encoder encoder;
    private static boolean initialized = false;
    
    // Data
    private Log log = LogFactory.getLog(this.getClass());

    private int status;
    private String blobData;
    private String stringified;
    private byte[] decodedData;
    private DocumentBuilder builder;    // generic XML parser

    /**
     * This is the Constructor for those that want to create and send a blob.
     * 
     * @param status the status to construct the blob with (see the constants
     * in this class).
     *
     * @throws SASLException
     */
    public Blob(int status)
        throws SASLException
    {
        if (log.isDebugEnabled()) {
            log.debug("Created blob=>"+status);
        }
        if (!initialized) {
            init();
        }

        try {
            builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SASLException(ERR_XML_PARSE_FAILURE);
        }

        // Validate status
        if (!validateStatus(status)) {
            throw new SASLException(ERR_INVALID_STATUS_VALUE);
        }

        this.status = status;

        StringBuffer buff = new StringBuffer(DEFAULT_BLOB_SIZE);
        buff.append("<blob ");

        if (status != STATUS_NONE) {
            buff.append("status='");
            buff.append(statusMappings[status]);
            buff.append('\'');
        }

        if (blobData == null) {
            buff.append("/>");
        } else {
            buff.append(">");
            buff.append(blobData);
            buff.append("</blob>");
        }

        stringified = buff.toString();
        if (log.isDebugEnabled()) {
            log.debug("Created blob=>" + stringified);
        }
    }

    /**
     * This is the Constructor for those that want to create and send a blob.
     *
     * @param status the status to construct the blob with (see the constants
     * in this class).
     * @param data the data to be embedded in the blob element
     *
     * @throws SASLException
     */
    public Blob(int status, String data)
        throws SASLException
    {
        if (log.isDebugEnabled()) {
            log.debug("Created blob=>" + status + "," + data);
        }
        if (!initialized) {
            init();
        }

        try {
            builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SASLException(ERR_XML_PARSE_FAILURE);
        }

        // Validate status
        if (!validateStatus(status)) {
            throw new SASLException(ERR_INVALID_STATUS_VALUE);
        }

        this.status = status;
        if (data != null) {
          try {
            this.decodedData = data.getBytes("UTF-8");
          } catch (java.io.UnsupportedEncodingException e) {
            throw new
                java.lang.RuntimeException("UTF-8 encoding not supported");
          }

          this.blobData = encoder.encodeBuffer(this.decodedData);
        }

        StringBuffer buff = new StringBuffer(DEFAULT_BLOB_SIZE);
        buff.append("<blob ");

        if (status != STATUS_NONE) {
            buff.append("status='");
            buff.append(statusMappings[status]);
            buff.append('\'');
        }

        if (blobData == null) {
            buff.append("/>");
        } else {
            buff.append(">");
            buff.append(blobData);
            buff.append("</blob>");
        }

        stringified = buff.toString();
        if (log.isDebugEnabled()) {
            log.debug("Created blob=>" + stringified);
        }
    }

    /**
     * This is the Constructor for those that want to create and send a blob.
     *
     * @param status the status to construct the blob with (see the constants
     * in this class).
     * @param data the data to be embedded in the blob element
     *
     * @throws SASLException
     */
    public Blob(int status, byte[] data )
        throws SASLException
    {
        if (!initialized) {
            init();
        }

        try {
            builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SASLException(ERR_XML_PARSE_FAILURE);
        }

        // Validate status
        if (!validateStatus(status)) {
            throw new SASLException(ERR_INVALID_STATUS_VALUE);
        }

        this.status = status;
        if (data != null) {
            this.decodedData = data;
            this.blobData = encoder.encodeBuffer(data);
        }
        
        StringBuffer buff = new StringBuffer(DEFAULT_BLOB_SIZE);
        buff.append("<blob ");

        if (status != STATUS_NONE) {
            buff.append("status='");
            buff.append(statusMappings[status]);
            buff.append('\'');
        }

        if (blobData == null) {
            buff.append("/>");
        } else {
            buff.append(">");
            buff.append(blobData);
            buff.append("</blob>");
        }
        
        stringified = buff.toString();
        if (log.isDebugEnabled()) {
            log.debug("Created blob=>" + stringified);
        }
    }

    /**
     * Constructor for those that want to 'receive' or 'digest' a blob.
     * 
     * @param blob The data to digest.
     * 
     * @throws SASLException in the event that errors occur during the
     * parsing of the blob passed in.
     */
    public Blob(String blob) throws SASLException
    {
        if (log.isDebugEnabled()) {
            log.debug("Receiving blob of=>"+blob);
        }
        if (!initialized) {
            init();
        }

        try {
            builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SASLException(ERR_XML_PARSE_FAILURE);
        }
        
        stringified = blob;
        // Parse out the status if there is any
        String statusString = extractStatusFromBlob(blob);
        this.status = STATUS_NONE;
        if (statusString != null) { /// @TODO && statusString.equals("") == false
            for (int i=0; i<STATUS_LIMIT; i++) {
                if (statusMappings[i].equals(statusString)) {
                    status = i;
                    break;
                }
            }
            statusString = statusMappings[status];
        }
        
        // Parse out the data if there is any
        blobData = extractDataFromBlob(blob);
        if (blobData != null) {
            try {
                decodedData = decoder.decodeBuffer(blobData);
            } catch (IOException x) {
                throw new SASLException(x.getMessage());
            }
        } else {
            decodedData = null;
        }

        if (status == STATUS_NONE && blobData == null) {
            throw new SASLException("No valid data in blob");
        }

        if (log.isDebugEnabled()) {
            log.debug("Received Blob of =>" + stringified);
        }
    }
    
    /**
     * Method init is a static method that prepares some of the
     * XML parsing and Base64Encoding/Decoding machinery needed
     * to understand blobs.  This functionality pops up elsewhere
     * in the library and should probably be moved ot some XML
     * utility class.  It isn't there yet though.
     * 
     */
    private static void init() throws SASLException
    {
        if (decoder == null) {
            decoder = new BASE64Decoder();
        }

        if (encoder == null) {
            encoder = new BASE64Encoder();
        }

        if(statusMappings == null) {
            statusMappings = new String[STATUS_LIMIT];
            statusMappings[STATUS_NONE]     = NONE;
            statusMappings[STATUS_ABORT]    = ABORT;
            statusMappings[STATUS_CONTINUE] = CONTINUE;
            statusMappings[STATUS_COMPLETE] = COMPLETE;
        }
        initialized = true;
    }

    /**
     * Returns the status used in the blob - can be 'none'.
     */
    public String getStatus()
    {
        return statusMappings[status];
    }
    
    /**
     * Returns the blob data as a <code>String</code>.
     */
    public String getData()
    {
        if (decodedData == null) {
            return "";
        }
        return new String(decodedData);
    }

    /**
     * Returns the blob data as bytes.
     */
    public byte[] getDataBytes()
    {
        return decodedData;
    }
    
    /**
     * Returns a representation of the Blob as it would be sent
     * out on the wire (with the data encoded).
     *
     */
    public String toString()
    {
        return stringified;
    }
    
    /**
     * Routines to parse XML blobs and extract either status
     * attributes or data (which is sometimes clear, sometimes Base64,
     * and potentially sometimes in other formats).  This routine
     * doesn't decode anything
     * 
     * @todo move this to some util class
     *
     * @param blob the data to be processed
     *
     * @return the top <code>Element</code>
     *
     * @throws SASLException
     */
    private Element processMessage(String blob) 
        throws SASLException
    {

        // parse the stream
        Document doc = null;

        try {
            log.debug("Tuning Profile Parse Routine");

            doc = builder.parse(new java.io.ByteArrayInputStream(blob.getBytes()));

            log.debug("parsed message");
        } catch (Exception e) {
            log.debug("Error parsing", e);
            throw new SASLException(ERR_XML_PARSE_FAILURE);
        }

        if (doc == null) {
            throw new SASLException(ERR_XML_PARSE_FAILURE);
        }

        Element topElement = doc.getDocumentElement();

        if (topElement == null) {
            throw new SASLException(ERR_XML_PARSE_FAILURE);
        }

        return topElement;
    }

    /**
     * Method extractStatusFromBlob
     *
     * @param blob is the data to extract from
     * @return String the value of the status attribute
     *
     * @throws SASLException if a parsing error occurs
     * or the input is invalid in some other way.
     *
     */
    private String extractStatusFromBlob(String blob)
            throws SASLException
    {
        if ((blob == null) || (blob.indexOf("<blob ") == -1)) {
            return null;
        }

        Element top = processMessage(blob);

        if (!top.getTagName().equals("blob")) {
            throw new SASLException(ERR_XML_PARSE_FAILURE);
        }

        return top.getAttribute("status");
    }

    /**
     * Method extractStatusFromBlob
     *
     * @param blob is the data to extract from
     * @return String the value of the blob element
     *
     * @throws SASLException if a parsing error occurs
     * or the input is invalid in some other way.
     *
     */
    private String extractDataFromBlob(String blob) throws SASLException
    {
        if (blob == null) {
            return null;
        }

        String result;
        Element top = processMessage(blob);

        if (!top.getTagName().equals("blob")) {
            throw new SASLException(ERR_XML_PARSE_FAILURE);
        }

        Node dataNode = top.getFirstChild();

        if (dataNode == null) {
            return null;
        }

        return top.getFirstChild().getNodeValue();
    }

    /**
     * Base64 Help routines so we don't have to expose them
     * to subclasses and others.
     *
     * @todo move this to some util class
     *
     * @param data the data to encode
     * @return String the encoded data.
     * 
     * @throws SASLException
     */
    private static String encodeData(String data) 
        throws SASLException
    {
        try {
            return new String(encoder.encodeBuffer(data.getBytes()));
        } catch (Exception x) {
            throw new SASLException("Base64 encode or decode failure");
        }
    }

    /**
     * Method decodeData simply checks values of the blob's
     * status attribute against 'valid' values.
     * 
     * @param status the string to be checked
     * @return boolean whether or not it's valid or not
     *
     * @throws SASLException
     *
     */
    private static boolean validateStatus(String status)
    {
        if (!status.equals(ABORT)
            &&!status.equals(NONE)
            &&!status.equals(CONTINUE)
            &&!status.equals(COMPLETE)) {
            return false;
        }

        return true;
    }
    
    /**
     * Method validateStatus validates a status value for an instance
     * of the blob class.
     *
     * @param data
     *
     * @throws SASLException
     *
     */
    private static boolean validateStatus(int status)
    {
        if (status < STATUS_NONE || status > STATUS_COMPLETE) {
            return false;
        }

        return true;
    }
}
