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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SchemaUtilsDefaultNSTest {

    @Test
    public void testLoadNS() throws Exception {
        SoapUI.initDefaultCore();
        String wsdlUriString = SchemaUtilsDefaultNSTest.class.getResource("/chameleon/chameleon.wsdl").toString();
        SchemaTypeSystem sts = SchemaUtils.loadSchemaTypes(wsdlUriString, new UrlWsdlLoader(wsdlUriString));
        assertNotNull(sts);
    }
}
