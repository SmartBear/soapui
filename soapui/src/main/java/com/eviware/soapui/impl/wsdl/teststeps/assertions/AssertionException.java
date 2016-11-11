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

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import com.eviware.soapui.model.testsuite.AssertionError;

/**
 * Exception thrown during assertion
 *
 * @author Ole.Matzura
 * @deprecated moved to com.eviware.soapui.model.testsuite.AssertionException
 */

public class AssertionException extends com.eviware.soapui.model.testsuite.AssertionException {
    public AssertionException(AssertionError[] errors) {
        super(errors);
    }

    public AssertionException(AssertionError error) {
        super(error);
    }
}
