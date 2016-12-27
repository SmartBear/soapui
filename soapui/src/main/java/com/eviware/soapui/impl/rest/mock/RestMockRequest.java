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

package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.impl.support.AbstractMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestMockRequest extends AbstractMockRequest {
    public RestMockRequest(HttpServletRequest request, HttpServletResponse response, WsdlMockRunContext context) throws Exception {
        super(request, response, context);
    }

    @Override
    public XmlObject getContentElement() throws XmlException {
        return null;
    }
}
