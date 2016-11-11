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

package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.support.SoapUIException;

/**
 * Exception thrown during assertion
 *
 * @author Ole.Matzura
 */

public class AssertionException extends SoapUIException {
    private AssertionError[] errors;

    public AssertionException(AssertionError error) {
        this(new AssertionError[]{error});
    }

    public AssertionException(AssertionError[] errors) {
        this.errors = new AssertionError[errors.length];
        for (int c = 0; c < errors.length; c++) {
            this.errors[c] = errors[c];
        }
    }

    public int getErrorCount() {
        return errors.length;
    }

    public AssertionError getErrorAt(int c) {
        return errors[c];
    }

    public AssertionError[] getErrors() {
        return errors;
    }

    public String getMessage() {
        StringBuffer result = new StringBuffer();
        for (int c = 0; c < errors.length; c++) {
            if (c > 0) {
                result.append('\n');
            }
            result.append(errors[c].getMessage());
        }

        return result.toString();
    }

}
