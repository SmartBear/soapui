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

package com.eviware.soapui.impl.wsdl.mock;

/**
 * Exception thrown during dispatching of HTTP Requests to a WsdlMockService
 *
 * @author ole.matzura
 */

public class DispatchException extends Exception {
    public DispatchException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public DispatchException(String arg0) {
        super(arg0);
    }

    public DispatchException(Throwable arg0) {
        super(arg0);
    }
}
