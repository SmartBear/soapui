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

package com.eviware.soapui.impl.rest.actions.oauth;

/**
 * Created with IntelliJ IDEA.
 * User: manne
 * Date: 2/18/14
 * Time: 9:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class JavaScriptValidationError {
    private final String errorMessage;
    private final int lineNumber;

    public JavaScriptValidationError(String errorMessage, int lineNumber) {
        this.errorMessage = errorMessage;
        this.lineNumber = lineNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }


    public int getLineNumber() {
        return lineNumber;
    }
}
