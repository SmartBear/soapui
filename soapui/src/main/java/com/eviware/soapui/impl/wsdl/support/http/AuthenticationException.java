/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.ProtocolException;

/**
 * Signals a failure in authentication process
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @since 2.0
 */
public class AuthenticationException extends ProtocolException {

    /**
     * Creates a new AuthenticationException with a <tt>null</tt> detail message.
     */
    public AuthenticationException() {
        super();
    }

    /**
     * Creates a new AuthenticationException with the specified message.
     *
     * @param message the exception detail message
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Creates a new AuthenticationException with the specified detail message
     * and cause.
     *
     * @param message the exception detail message
     * @param cause   the <tt>Throwable</tt> that caused this exception, or
     *                <tt>null</tt> if the cause is unavailable, unknown, or not a
     *                <tt>Throwable</tt>
     * @since 3.0
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}
