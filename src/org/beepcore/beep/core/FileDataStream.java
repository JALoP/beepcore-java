
/*
 * FileDataStream.java            $Revision: 1.1 $ $Date: 2001/04/02 08:56:06 $
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
package org.beepcore.beep.core;


import java.io.FileInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;

import java.lang.SecurityException;


/**
 * <code>FileDataStream</code> represents a BEEP message's payload.
 * Allows the implementor to treat a <code>File</code> or
 * <code>FileDescriptor</code> as a <code>DataSream</code>.
 * <p>
 * <b>Note that this implementation
 * is not synchronized.</b> If multiple threads access a
 * <code>FileDataStream</code> concurrently, data may be inconsistent or lost.
 *
 * @see org.beepcore.beep.core.DataStream
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision, $Date: 2001/04/02 08:56:06 $
 */
public class FileDataStream extends DataStream {

    private FileInputStream data = null;

    /**
     * Creates a <code>FileDataStream</code> by opening a connection to an
     * actual file, the file named by the <code>File</code> object file in
     * the file system, and with a content type of
     * <code>DEFAULT_CONTENT_TYPE</code>.
     *
     * @param file  the file to be opened for reading.
     *
     * @exception FileNotFoundException if the file does not exist, is a
     * directory rather than a regular file, or for some other reason cannot be
     * opened for reading.
     * @exception SecurityException if a security manager exists and its
     * <code>checkRead</code> method denies read access to the file.
     */
    public FileDataStream(File file)
            throws FileNotFoundException, SecurityException
    {
        super();

        this.data = new FileInputStream(file);
    }

    /**
     * Creates a <code>FileDataStream</code> by opening a connection to an
     * actual file, the file named by the <code>File</code> object file in
     * the file system, and
     * with a specified content type.
     *
     * @param contentType Content type of <code>data</code>.
     * @param file  the file to be opened for reading.
     *
     * @exception FileNotFoundException if the file does not exist, is a
     * directory rather than a regular file, or for some other reason cannot be
     * opened for reading.
     * @exception SecurityException if a security manager exists and its
     * <code>checkRead</code> method denies read access to the file.
     */
    public FileDataStream(String contentType, File file)
            throws FileNotFoundException, SecurityException
    {
        super(contentType, DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING);

        this.data = new FileInputStream(file);
    }

    /**
     * Creates a <code>FileDataStream</code> by using the file descriptor
     * <code>fdObj</code>, which represents an existing connection to an
     * actual file in the file system
     * with a content type of <code>DEFAULT_CONTENT_TYPE</code>.
     *
     * @param fdObj  The file descriptor to be opened for reading.
     * @exception SecurityException if a security manager exists and its
     * <code>checkRead</code> method denies read access to the file.
     */
    public FileDataStream(FileDescriptor fdObj) throws SecurityException
    {
        super();

        this.data = new FileInputStream(fdObj);

        // length is unknown
    }

    /**
     * Creates a <code>FileDataStream</code> by using the file descriptor
     * <code>fdObj</code>, which represents an existing connection to an
     * actual file in the file system
     * with a specified content type.
     *
     * @param contentType Content type of <code>byte[]</code>.
     * @param fdObj  The file descriptor to be opened for reading.
     * @exception SecurityException if a security manager exists and its
     * <code>checkRead</code> method denies read access to the file.
     */
    public FileDataStream(String contentType, FileDescriptor fdObj)
            throws SecurityException
    {
        super(contentType, DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING);

        this.data = new FileInputStream(fdObj);
    }

    /**
     * Creates a <code>FileDataStream</code> by opening a connection to an
     * actual file, the file named by the <code>File</code> object file in
     * the file system, and with a content type of
     * <code>DEFAULT_CONTENT_TYPE</code>.
     *
     * @param name  The system-dependent file name.
     *
     * @exception FileNotFoundException if the file does not exist, is a
     * directory rather than a regular file, or for some other reason cannot be
     * opened for reading.
     * @exception SecurityException if a security manager exists and its
     * <code>checkRead</code> method denies read access to the file.
     */
    public FileDataStream(String name)
            throws FileNotFoundException, SecurityException
    {
        super();

        this.data = new FileInputStream(name);
    }

    /**
     * Creates a <code>FileDataStream</code> by opening a connection to an
     * actual file, the file named by the <code>File</code> object file in
     * the file system, and
     * with a specified content type.
     *
     * @param contentType Content type of the file.
     * @param name  The system-dependent file name.
     *
     * @exception FileNotFoundException if the file does not exist, is a
     * directory rather than a regular file, or for some other reason cannot be
     * opened for reading.
     * @exception SecurityException if a security manager exists and its
     * <code>checkRead</code> method denies read access to the file.
     */
    public FileDataStream(String contentType, String name)
            throws FileNotFoundException, SecurityException
    {
        super(contentType, DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING);

        this.data = new FileInputStream(name);
    }

    /**
     * Returns this data stream as an <code>InputStream</code>
     */
    public InputStream getInputStream()
    {
        return this.data;
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from this
     * data stream without blocking by the next caller of a method for this
     * input stream.  See <code>isComplete</code> to detect end of stream
     * without blocking.
     *
     * @return Number of bytes available.
     *
     * @throws IOException
     */
    int available() throws BEEPException
    {
        try {
            return data.available();
        } catch (IOException e) {
            throw new BEEPException("threw IOException");
        }
    }

    /**
     * Reads a byte of data from this data stream.
     * This method blocks if no input is yet available..
     * Note that this implementation is not synchronized.
     *
     * @return The next byte.
     *
     * @exception IOException Throws <code>IOException</code>,
     * if an I/O error occurs.
     */
    int read() throws BEEPException
    {
        try {
            return this.data.read();
        } catch (IOException e) {
            throw new BEEPException("threw IOException");
        }
    }

    /**
     * Reads some number of bytes from the input stream and stores them into
     * the buffer array <code>buf</code>.
     * This method blocks until some input is available.
     * Note that this implementation is not synchronized.
     *
     * @param buf The buffer into which the data is read.
     * @return The total number of bytes read into the buffer, or -1 if
     *    there is no more data because the end of the file has been reached.
     *
     * @exception IOException Throws <code>IOException</code>,
     * if an I/O error occurs.
     */
    int read(byte[] buf) throws BEEPException
    {
        try {
            return this.data.read(buf);
        } catch (IOException e) {
            throw new BEEPException("threw IOException");
        }
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of
     * bytes. This method blocks until some input is available.
     * Note that this implementation is not synchronized.
     *
     * @param buf The buffer into which the data is read.
     * @param off The start offset of the data.
     * @param len The maximum number of bytes read.
     * @return The number of bytes read.  Returns -1 if no more can be
     *    read.
     * @exception IOException Throws <code>IOException</code>,
     * if an I/O error occurs.
     */
    int read(byte[] buf, int off, int len) throws BEEPException
    {
        try {
            return this.data.read(buf, off, len);
        } catch (IOException e) {
            throw new BEEPException("threw IOException");
        }
    }

    /**
     * Skips over and discards n bytes of data from this input stream.
     *
     * @param n Number of bytes to skip.
     * @return Number of bytes actually skipped.
     *
     * @exception IOException Throws <code>IOException</code>,
     * if an I/O error occurs.
     */
    long skip(long n) throws BEEPException
    {
        try {
            return this.data.skip(n);
        } catch (IOException e) {
            throw new BEEPException("threw IOException");
        }
    }

    void reset() throws BEEPException
    {
        try {
            this.data.reset();
        } catch (IOException e) {
            throw new BEEPException("threw IOException");
        }
    }

    boolean markSupported()
    {
        return this.data.markSupported();
    }

    void mark(int readlimit)
    {
        this.data.mark(readlimit);
    }

    void close() throws BEEPException
    {
        try {
            this.data.close();
        } catch (IOException e) {
            throw new BEEPException("threw IOException");
        }
    }
}
