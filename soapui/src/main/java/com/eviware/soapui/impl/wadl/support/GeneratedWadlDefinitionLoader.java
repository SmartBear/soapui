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

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.WadlGenerator;
import com.eviware.soapui.impl.rest.panels.request.inspectors.schema.InferredSchemaManager;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionLoader;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class GeneratedWadlDefinitionLoader extends AbstractDefinitionLoader {
    private RestService restService;

    public GeneratedWadlDefinitionLoader(RestService restService) {
        this.restService = restService;
    }

    public XmlObject loadXmlObject(String wsdlUrl, XmlOptions options) throws Exception {
        if (wsdlUrl.toLowerCase().endsWith(".xsd"))
        // return XmlObject.Factory.parse(
        // InferredSchemaManager.getInferredSchema( restService
        // ).getXsdForNamespace(
        // InferredSchemaManager.namespaceForFilename( wsdlUrl ) ) );
        {
            return XmlUtils.createXmlObject(InferredSchemaManager.getInferredSchema(restService).getXsdForNamespace(
                    InferredSchemaManager.namespaceForFilename(wsdlUrl)));
        }
        return new WadlGenerator(restService).generateWadl();
    }

    public String getBaseURI() {
        return restService.getName() + ".wadl";
    }

    public void setNewBaseURI(String uri) {
        // not implemented
    }

    public String getFirstNewURI() {
        return getBaseURI();
    }
}
