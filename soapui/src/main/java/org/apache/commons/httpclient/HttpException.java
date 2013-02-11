/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/HttpException.java,v 1.19 2004/09/30 18:53:20 olegk Exp $
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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * Signals that an HTTP or HttpClient exception has occurred.
 * 
 * @author Laura Werner
 * 
 * @version $Revision: 480424 $ $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 */
public class HttpException extends IOException {

    /**
     * Creates a new HttpException with a <tt>null</tt> detail message.
     */
    public HttpException() {
        super();
        this.cause = null;
    }

    /**
     * Creates a new HttpException with the specified detail message.
     *
     * @param message the exception detail message
     */
    public HttpException(String message) {
        super(message);
        this.cause = null;
    }

    /**
     * Creates a new HttpException with the specified detail message and cause.
     * 
     * @param message the exception detail message
     * @param cause the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     * if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     * 
     * @since 3.0
     */
    public HttpException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
        
        // If we're running on JDK 1.4 or later, tell Throwable what the cause was
        try {
            Class[] paramsClasses = new Class[] { Throwable.class };
            Method initCause = Throwable.class.getMethod("initCause", paramsClasses);
            initCause.invoke(this, new Object[] { cause });
        } catch (Exception e) {
            // The setCause method must not be available
        }
    }

    /**
     * Return the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     *         if the cause is unavailable, unknown, or not a <tt>Throwable</tt>.
     * 
     * @return the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     *         if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     * 
     * @since 3.0
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Print this HttpException and its stack trace to the standard error stream.
     * 
     * @since 3.0
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Print this HttpException and its stack trace to the specified print stream.
     * 
     * @param s the <tt>PrintStream</tt> to which the exception and its stack trace
     * should be written
     * 
     * @since 3.0
     */
    public void printStackTrace(PrintStream s) {
        try {
            // JDK 1.4 has a nice printStackTrace method that prints the cause's stack
            // trace too and prunes out duplicate stack frames.  Call it if possible,
            // which is determined by checking whether JDK 1.4's getStackTrace method is present 
            Class[] paramsClasses = new Class[] {  };
            this.getClass().getMethod("getStackTrace", paramsClasses);
            super.printStackTrace(s);
        } catch (Exception ex) {
            // If that didn't work, print it out ourselves
            // First print this exception's stack trace.
            super.printStackTrace(s);
            if (cause != null) {
                // Print out the exception that caused this one.
                // This will recurse if the cause is another HttpException.
                s.print("Caused by: ");
                cause.printStackTrace(s);
            }
        }
    }

    /**
     * Print this HttpException and its stack trace to the specified print writer.
     * 
     * @param s the <tt>PrintWriter</tt> to which the exception and its stack trace
     * should be written
     * 
     * @since 3.0
     */
    public void printStackTrace(PrintWriter s) {
        try {
            // JDK 1.4 has a nice printStackTrace method that prints the cause's stack
            // trace too and prunes out duplicate stack frames.  Call it if possible,
            // which is determined by checking whether JDK 1.4's getStackTrace method is present 
            Class[] paramsClasses = new Class[] {  };
            this.getClass().getMethod("getStackTrace", paramsClasses);
            super.printStackTrace(s);
        } catch (Exception ex) {
            // If that didn't work, print it out ourselves
            // First print this exception's stack trace.
            super.printStackTrace(s);
            if (cause != null) {
                // Print out the exception that caused this one.
                // This will recurse if the cause is another HttpException.
                s.print("Caused by: ");
                cause.printStackTrace(s);
            }
        }
    }

    /**
     * Sets the text description of the reason for an exception.
     *
     * @param reason The reason for the exception.
     *
     * @deprecated HttpClient no longer uses this for itself.  It is only
     * provided for compatibility with existing clients, and will be removed
     * in a future release.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Get the text description of the reason for an exception.
     *
     * @deprecated HttpClient no longer uses this for itself.  It is only
     * provided for compatibility with existing clients, and will be removed
     * in a future release.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the status code description of the reason for an exception.
     *
     * @param code The reason for the exception.  This is intended to be an
     *  HTTP status code.
     *
     * @deprecated HttpClient no longer uses this for itself.  It is only
     * provided for compatibility with existing clients, and will be removed
     * in a future release.
     */
    public void setReasonCode(int code) {
        reasonCode = code;
    }

    /**
     * Get the status code description of the reason for an exception.
     *
     * @deprecated HttpClient no longer uses this for itself.  It is only
     * provided for compatibility with existing clients, and will be removed
     * in a future release.
     */
    public int getReasonCode() {
        return this.reasonCode;
    }

    /**
     * A "reason" string provided for compatibility with older clients.
     *
     * @deprecated HttpClient no longer uses this field for itself.  It
     * is only provided for compatibility with existing clients.
     */
    private String reason;

    /**
     * Reason code for compatibility with older clients.
     *
     * @deprecated  HttpClient no longer uses this field for itself.
     *  It is only provided for compatibility with existing clients.
     */
    private int reasonCode = HttpStatus.SC_OK;

    /** The original Throwable representing the cause of this error */
    private final Throwable cause;
}
