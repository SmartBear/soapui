/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/URIException.java,v 1.12 2004/09/30 18:53:20 olegk Exp $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient;

/**
 * The URI parsing and escape encoding exception.
 *
 * @author <a href="mailto:jericho at apache.org">Sung-Gu</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @version $Revision: 480424 $ $Date: 2002/03/14 15:14:01 
 */
public class URIException extends HttpException {

    // ----------------------------------------------------------- constructors

    /**
     * Default constructor.
     */
    public URIException() {
    }


    /**
     * The constructor with a reason code argument.
     *
     * @param reasonCode the reason code
     */
    public URIException(int reasonCode) {
        this.reasonCode = reasonCode;
    }


    /**
     * The constructor with a reason string and its code arguments.
     *
     * @param reasonCode the reason code
     * @param reason the reason
     */
    public URIException(int reasonCode, String reason) {
        super(reason); // for backward compatibility of Throwable
        this.reason = reason;
        this.reasonCode = reasonCode;
    }


    /**
     * The constructor with a reason string argument.
     *
     * @param reason the reason
     */
    public URIException(String reason) {
        super(reason); // for backward compatibility of Throwable
        this.reason = reason;
        this.reasonCode = UNKNOWN;
    }

    // -------------------------------------------------------------- constants

    /**
     * No specified reason code.
     */
    public static final int UNKNOWN = 0;


    /**
     * The URI parsing error.
     */
    public static final int PARSING = 1;


    /**
     * The unsupported character encoding.
     */
    public static final int UNSUPPORTED_ENCODING = 2;


    /**
     * The URI escape encoding and decoding error.
     */
    public static final int ESCAPING = 3;


    /**
     * The DNS punycode encoding or decoding error.
     */
    public static final int PUNYCODE = 4;

    // ------------------------------------------------------------- properties

    /**
     * The reason code.
     */
    protected int reasonCode;


    /**
     * The reason message.
     */
    protected String reason;

    // ---------------------------------------------------------------- methods

    /**
     * Get the reason code.
     *
     * @return the reason code
     */
    public int getReasonCode() {
        return reasonCode;
    }

    /**
     * Set the reason code.
     *
     * @param reasonCode the reason code
     *
     * @deprecated Callers should set the reason code as a parameter to the
     *  constructor.
     */
    public void setReasonCode(int reasonCode) {
        this.reasonCode = reasonCode;
    }


    /**
     * Get the reason message.
     *
     * @return the reason message
     *
     * @deprecated You should instead call {@link #getMessage()}.
     */
    public String getReason() {
        return reason;
    }


    /**
     * Set the reason message.
     *
     * @param reason the reason message
     *
     * @deprecated Callers should instead set this via a parameter to the constructor.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }


}

