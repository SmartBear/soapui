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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.support.xsd.SchemaLoader;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.net.URL;

public class UrlSchemaLoader implements SchemaLoader {
    private String baseURI;

    public UrlSchemaLoader(String baseURI) {
        this.baseURI = baseURI;
    }

    public XmlObject loadXmlObject(String wsdlUrl, XmlOptions options) throws Exception {
        // return XmlObject.Factory.parse( new URL( wsdlUrl ), options );
        return XmlUtils.createXmlObject(new URL(wsdlUrl), options);
    }

    public String getBaseURI() {
        return baseURI;
    }
}
