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

package com.eviware.soapui.impl.wadl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.wadl.WadlDefinitionContext;
import com.eviware.soapui.impl.wsdl.submit.RestMessageExchange;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class WadlValidator {
    public WadlValidator(WadlDefinitionContext context) {
    }

    public AssertionError[] assertResponse(RestMessageExchange messageExchange) {
        RestRequestInterface restRequest = messageExchange.getRestRequest();
        if (restRequest != null) {
            if (messageExchange.getResponseStatusCode() >= 400) {
                return assertResponse(messageExchange, RestRepresentation.Type.FAULT);
            } else {
                return assertResponse(messageExchange, RestRepresentation.Type.RESPONSE);
            }
        }

        return new AssertionError[0];
    }

    private AssertionError[] assertResponse(RestMessageExchange messageExchange, RestRepresentation.Type type) {
        List<AssertionError> result = new ArrayList<AssertionError>();
        QName responseBodyElementName = getResponseBodyElementName(messageExchange);
        RestRequestInterface restRequest = messageExchange.getRestRequest();
        boolean asserted = false;

        for (RestRepresentation representation : restRequest.getRepresentations(type,
                messageExchange.getResponseContentType())) {
            if (representation.getStatus().isEmpty()
                    || representation.getStatus().contains(messageExchange.getResponseStatusCode())) {
                SchemaType schemaType = representation.getSchemaType();
                if (schemaType != null && representation.getElement().equals(responseBodyElementName)) {
                    try {
                        XmlObject xmlObject = schemaType.getTypeSystem().parse(messageExchange.getResponseContentAsXml(),
                                schemaType, new XmlOptions());

                        // create internal error list
                        List<?> list = new ArrayList<Object>();

                        XmlOptions xmlOptions = new XmlOptions();
                        xmlOptions.setErrorListener(list);
                        xmlOptions.setValidateTreatLaxAsSkip();
                        xmlObject.validate(xmlOptions);

                        for (Object o : list) {
                            if (o instanceof XmlError) {
                                result.add(new AssertionError((XmlError) o));
                            } else {
                                result.add(new AssertionError(o.toString()));
                            }
                        }

                        asserted = true;
                    } catch (XmlException e) {
                        SoapUI.logError(e);
                    }
                } else {
                    asserted = true;
                }
            }
        }

        if (!asserted && result.isEmpty()) {
            result.add(new AssertionError("Missing matching representation for request with contentType ["
                    + messageExchange.getResponseContentType() + "]"));
        }

        return result.toArray(new AssertionError[result.size()]);
    }

    private QName getResponseBodyElementName(RestMessageExchange messageExchange) {
        try {
            // XmlObject xmlObject = XmlObject.Factory.parse(
            // messageExchange.getResponseContentAsXml() );
            XmlObject xmlObject = XmlUtils.createXmlObject(messageExchange.getResponseContentAsXml());
            Element docElement = ((Document) xmlObject.getDomNode()).getDocumentElement();

            return new QName(docElement.getNamespaceURI(), docElement.getLocalName());
        } catch (XmlException e) {
            SoapUI.logError(e);
        }

        return null;
    }
}
