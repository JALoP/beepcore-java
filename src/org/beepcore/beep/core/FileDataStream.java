
/*
 * FileDataStream.java            $Revision: 1.3 $ $Date: 2001/04/24 22:52:02 $
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
 * @version $Revision, $Date: 2001/04/24 22:52:02 $
 */
public class FileDataStream extends InputStreamDataStream {
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
        super(new FileInputStream(file));
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
        super(contentType, new FileInputStream(file));
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
        super(new FileInputStream(fdObj));
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
        super(contentType, new FileInputStream(fdObj));
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
        super(new FileInputStream(name));
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
        super(contentType, new FileInputStream(name));
    }

    /**
     * Returns this data stream as an <code>InputStream</code>
     */
    public InputStream getInputStream()
    {
        return this.data;
    }

    /**
     * Returns <code>true</code> if no more bytes will be added to those
     * currently available, if any, on this stream.  Returns
     * <code>false</code> if more bytes are expected.
     */
    public boolean isComplete()
    {
      return true;
    }
}
