package org.beepcore.beep.core;


import java.util.Enumeration;
import java.util.Hashtable;

import org.beepcore.beep.util.BufferSegment;

public class MimeHeaders {
    /**
     * The default <code>DataStream</code> content type
     * ("application/octet-stream").
     */
    public static final String DEFAULT_CONTENT_TYPE =
        "application/octet-stream";

    /**
     * The default <code>DataStream</code> content transfer encoding
     * ("binary").
     */
    public static final String DEFAULT_CONTENT_TRANSFER_ENCODING = "binary";

    /**
     * <code>DataStream</code> content type ("application/beep+xml");
     */
    public static final String BEEP_XML_CONTENT_TYPE = "application/beep+xml";

    /**
     *
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     *
     */
    public static final String CONTENT_TRANSFER_ENCODING =
        "Content-Transfer-Encoding";

    private static final String NAME_VALUE_SEPARATOR = ": ";
    private static final String HEADER_SUFFIX = "\r\n";
    private static final int HEADER_FORMAT_LENGTH =
        NAME_VALUE_SEPARATOR.length() + HEADER_SUFFIX.length();

    // guessing most hashtables will only contain the 2 default values
    private static final int DEFAULT_HEADER_TABLE_SIZE = 2;

    private static final int MAX_BUFFER_SIZE = 128;

    // length of header portion of data stream
    private int lenHeaders = HEADER_SUFFIX.length();

    private Hashtable mimeHeadersTable =
        new Hashtable(DEFAULT_HEADER_TABLE_SIZE);

    /**
     * Creates <code>MimeHeaders</code> using the default content type
     * <code>DEFAULT_CONTENT_TYPE</code> and default content transfre encoding
     * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
     */
    public MimeHeaders()
    {
        this(DEFAULT_CONTENT_TYPE, DEFAULT_CONTENT_TRANSFER_ENCODING);
    }

    /**
     * Creates <code>MimeHeaders</code> using the specified content type and
     * the <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code> content transfer
     * encoding.
     */
    public MimeHeaders(String contentType)
    {
        this(contentType, DEFAULT_CONTENT_TRANSFER_ENCODING);
    }

    /**
     * Creates <code>MimeHeaders</code> using the specified content type and
     * content transfer encoding.
     */
    public MimeHeaders(String contentType, String transferEncoding)
    {
        this.setContentType(contentType);
        this.setTransferEncoding(transferEncoding);
    }

    /**
     * Returns the value of the MIME entity header <code>Content-Type</code>.
     */
    public String getContentType()
    {
        return this.getHeaderValue(CONTENT_TYPE);
    }

    /**
     * Retrieves the correspoding <code>value</code> to a given a MIME entity
     * header <code>name</code>.
     *
     * @param name Name of the MIME entity header.
     * @return The <code>value</code> of the MIME entity header.
     *
     * @throws BEEPException
     */
    public String getHeaderValue(String name)
    {
        return (String) this.mimeHeadersTable.get(name);
    }

    /**
     * Returns an <code>Enumeration</code> of all the names of the MIME entity
     * headers.
     * Use this call in conjunction with <code>getHeaderValue</code> to iterate
     * through all the corresponding MIME entity header <code>value</code>(s).
     *
     * @return An <code>Enumeration</code> of all the MIME entity header
     * names.
     */
    public Enumeration getHeaderNames()
    {
        return this.mimeHeadersTable.keys();
    }

    /**
     * Returns the value of the MIME entity header
     * <code>Content-Transfer-Encoding</code>.
     */
    public String getTransferEncoding()
    {
        return this.getHeaderValue(CONTENT_TRANSFER_ENCODING);
    }

    /**
     * Removes the <code>name</code> and <code>value</code> of a MIME entity
     * header from the data stream.  Returns <code>true</code> if the
     * <code>name</code> was successfully removed.
     *
     * @param name Name of the header to be removed from the data stream.
     *
     * @return Returns </code>true<code> if header was removed.  Otherwise,
     * returns <code>false</code>.
     */
    public boolean removeHeader(String name)
    {
        String value = (String) mimeHeadersTable.get(name);

        /**
         * @todo change to not allow the removal of content-type and
         * transfer-encoding.
         */
        if (value != null) {
            if (this.mimeHeadersTable.remove(name) != null) {
                if ((name.equals(CONTENT_TYPE) &&
                     value.equals(DEFAULT_CONTENT_TYPE))
                    || (name.equals(CONTENT_TRANSFER_ENCODING)
                        && value.equals(DEFAULT_CONTENT_TRANSFER_ENCODING)))
                {

                    // don't decrement the length, these are default values
                } else {
                    this.lenHeaders -= name.length() + value.length()
                                       + MimeHeaders.HEADER_FORMAT_LENGTH;
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Sets the content type of a <code>DataStream</code>.
     *
     * @param contentType
     */
    public void setContentType(String contentType)
    {
        this.setHeader(CONTENT_TYPE, contentType);
    }

    /**
     * Adds a MIME entity header to this data stream.
     *
     * @param name  Name of the MIME enitity header.
     * @param value Value of the MIME entity header.
     */
    public void setHeader(String name, String value)
    {
        if (this.mimeHeadersTable.containsKey(name)) {
            removeHeader(name);
        }

        this.mimeHeadersTable.put(name, value);

        if ((name.equals(CONTENT_TYPE) && value.equals(DEFAULT_CONTENT_TYPE))
                || (name.equals(CONTENT_TRANSFER_ENCODING)
                    && value.equals(DEFAULT_CONTENT_TRANSFER_ENCODING))) {

            // these are default values, don't add to len
        } else {
            this.lenHeaders += name.length() + value.length()
                               + MimeHeaders.HEADER_FORMAT_LENGTH;
        }
    }

    /**
     * Sets the content transfer encoding of a <code>DataStream</code>
     *
     * @param transferEncoding
     */
    public void setTransferEncoding(String transferEncoding)
    {
        this.setHeader(CONTENT_TRANSFER_ENCODING, transferEncoding);
    }

    public BufferSegment getBufferSegment() {
        byte[] headersBytes = new byte[this.lenHeaders];

        int offsetHeaders = 0;

        // read the headers
        Enumeration headers = mimeHeadersTable.keys();

        while (headers.hasMoreElements()) {
            String name = (String) headers.nextElement();
            String value = (String) mimeHeadersTable.get(name);

            if ((name.equals(CONTENT_TYPE) &&
                 value.equals(DEFAULT_CONTENT_TYPE)) ||
                (name.equals(CONTENT_TRANSFER_ENCODING) &&
                 value.equals(DEFAULT_CONTENT_TRANSFER_ENCODING))) {

                // these are default values that don't need to be added
                // to the payload
                continue;
            }

            // copy name
            System.arraycopy(name.getBytes(), 0, headersBytes,
                             offsetHeaders, name.length());

            offsetHeaders += name.length();

            // ": "
            System.arraycopy(NAME_VALUE_SEPARATOR.getBytes(), 0,
                             headersBytes, offsetHeaders,
                             NAME_VALUE_SEPARATOR.length());

            offsetHeaders += NAME_VALUE_SEPARATOR.length();

            // copy value
            System.arraycopy(value.getBytes(), 0, headersBytes,
                             offsetHeaders, value.length());

            offsetHeaders += value.length();

            // CRLF
            System.arraycopy(MimeHeaders.HEADER_SUFFIX.getBytes(), 0,
                             headersBytes, offsetHeaders,
                             MimeHeaders.HEADER_SUFFIX.length());

            offsetHeaders += MimeHeaders.HEADER_SUFFIX.length();
        }

        // read the CRLF that separates the headers and the data
        System.arraycopy(MimeHeaders.HEADER_SUFFIX.getBytes(), 0,
                         headersBytes, offsetHeaders,
                         MimeHeaders.HEADER_SUFFIX.length());

        offsetHeaders += MimeHeaders.HEADER_SUFFIX.length();

        return new BufferSegment(headersBytes);
    }
}
