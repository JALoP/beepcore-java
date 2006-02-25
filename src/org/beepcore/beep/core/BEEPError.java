/*
 * BEEPError.java  $Revision: 1.10 $ $Date: 2006/02/25 17:48:37 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 * Copyright (c) 2002,2004 Huston Franklin.  All rights reserved.
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
package org.beepcore.beep.core;


/**
 * Class BEEPError
 *
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.10 $, $Date: 2006/02/25 17:48:37 $
 */
public class BEEPError extends BEEPException {

    private static final String ERR_MALFORMED_XML_MSG = "Malformed XML";

    private int code;
    private String xmlLang = null;

    /**
     * Constructor BEEPError
     *
     *
     * @param code
     * @param diagnostic
     * @param xmlLang
     *
     */
    public BEEPError(int code, String diagnostic, String xmlLang)
    {
        super(diagnostic);

        this.xmlLang = xmlLang;
        this.code = code;
    }

    /**
     * Constructor BEEPError
     *
     *
     * @param code
     * @param diagnostic
     *
     */
    public BEEPError(int code, String diagnostic)
    {
        super(diagnostic);

        this.code = code;
    }

    /**
     * Constructor BEEPError
     *
     *
     * @param code
     *
     */
    public BEEPError(int code)
    {

        /**
         * @todo change this and the other constructors to call
         *  super("<error code=" + code + "/>") or something similar.
         */
        super("");

        this.code = code;
    }

    /**
     * Method getCode
     *
     *
     * @return the BEEP ERR code
     *
     */
    public int getCode()
    {
        return this.code;
    }

    /**
     * Method getXMLLang
     *
     *
     * @return the BEEP ERR xmllang
     *
     */
    public String getXMLLang()
    {
        return this.xmlLang;
    }

    /**
     * Method getDiagnostic
     *
     *
     * @return the BEEP ERR diagnostic message
     *
     */
    public String getDiagnostic()
    {
        return this.getMessage();
    }

    /** Success */
    public static final int CODE_SUCCESS = 200;

    /** Service not available */
    public static final int CODE_SERVICE_NOT_AVAILABLE = 421;

    /** Requested action not taken (e.g., lock already in use) */
    public static final int CODE_REQUESTED_ACTION_NOT_TAKEN = 450;

    /** Requested action aborted (e.g., local error in processing) */
    public static final int CODE_REQUESTED_ACTION_ABORTED = 451;

    /** Temporary authentication failure */
    public static final int CODE_TEMPORARY_AUTHENTICATION_FAILURE = 454;

    /** General syntax error (e.g., poorly-formed XML) */
    public static final int CODE_GENERAL_SYNTAX_ERROR = 500;

    /** Syntax error in parameters (e.g., non-valid XML) */
    public static final int CODE_PARAMETER_ERROR = 501;

    /** Parameter not implemented */
    public static final int CODE_PARAMETER_NOT_IMPLEMENTED = 504;

    /** Authentication required */
    public static final int CODE_AUTHENTICATION_REQUIRED = 530;

    /**
     * Authentication mechanism insufficient (e.g., too weak,
     * sequence exhausted, etc.)
     */
    public static final int CODE_AUTHENTICATION_INSUFFICIENT = 534;

    /** Authentication failure */
    public static final int CODE_AUTHENTICATION_FAILURE = 535;

    /** Action not authorized for user */
    public static final int CODE_ACTION_NOT_AUTHORIZED = 537;

    /** Authentication mechanism requires encryption */
    public static final int CODE_AUTHENTICATION_REQUIRES_ENCRYPTION = 538;

    /**
     * requested action not taken (e.g., no requested profiles are
     * acceptable)
     */
    public static final int CODE_REQUESTED_ACTION_NOT_TAKEN2 = 550;

    /** Parameter invalid */
    public static final int CODE_PARAMETER_INVALID = 553;

    /** Transaction failed (e.g. policy violation). */
    public static final int CODE_TRANSACTION_FAILED = 554;
}
