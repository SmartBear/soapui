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

/**
 * Authentication credentials required to respond to a authentication challenge
 * are not available
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @since 3.0
 */
public class CredentialsNotAvailableException extends AuthenticationException {
    /**
     * Creates a new CredentialsNotAvailableException with a <tt>null</tt> detail
     * message.
     */
    public CredentialsNotAvailableException() {
        super();
    }

    /**
     * Creates a new CredentialsNotAvailableException with the specified message.
     *
     * @param message the exception detail message
     */
    public CredentialsNotAvailableException(String message) {
        super(message);
    }

    /**
     * Creates a new CredentialsNotAvailableException with the specified detail
     * message and cause.
     *
     * @param message the exception detail message
     * @param cause   the <tt>Throwable</tt> that caused this exception, or
     *                <tt>null</tt> if the cause is unavailable, unknown, or not a
     *                <tt>Throwable</tt>
     */
    public CredentialsNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
