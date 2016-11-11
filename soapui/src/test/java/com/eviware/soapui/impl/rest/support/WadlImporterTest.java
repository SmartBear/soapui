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

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.junit.Test;

import static com.eviware.soapui.utils.CommonMatchers.anEmptyString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class WadlImporterTest {

    @Test
    public void testWadlImporter() throws Exception {
        WsdlProject project = new WsdlProject();
        RestService service = (RestService) project.addNewInterface("REST Service", RestServiceFactory.REST_TYPE);
        WadlImporter importer = new WadlImporter(service);
        importer.initFromWadl(WadlImporter.class.getResource("/wadl/YahooSearch.wadl").toString());
        assertEquals(service.getName(), "REST Service");
        assertEquals(1, service.getResourceList().size());
        assertEquals(0, service.getResourceList().get(0).getChildResourceCount());
        assertEquals(1, service.getResourceList().get(0).getRestMethodCount());
    }

    @Test
    public void importsWadl() throws Exception {
        WsdlProject project = new WsdlProject();
        RestService service = (RestService) project.addNewInterface("Test", RestServiceFactory.REST_TYPE);

        new WadlImporter(service).initFromWadl(RestUtilsTest.class.getResource("/wadl/YahooSearch.wadl").toURI().toString());

        assertEquals(1, service.getOperationCount());
        assertEquals("/NewsSearchService/V1/", service.getBasePath());

        RestResource resource = service.getOperationAt(0);

        assertEquals(1, resource.getPropertyCount());
        assertEquals("appid", resource.getPropertyAt(0).getName());
        assertNotNull(resource.getProperty("appid"));
        assertEquals(1, resource.getRequestCount());

        RestRequest request = resource.getRequestAt(0);
        assertEquals(RestRequestInterface.HttpMethod.GET, request.getMethod());
        assertEquals(9, request.getPropertyCount());
    }

    @Test
    public void removesPropertyExpansions() throws Exception {
        WsdlProject project = new WsdlProject();
        RestService service = (RestService) project.addNewInterface("Test", RestServiceFactory.REST_TYPE);

        new WadlImporter(service).initFromWadl(RestUtilsTest.class.getResource(
                "/wadl/YahooSearchWithExpansions.wadl").toURI().toString());
        RestResource operation = (RestResource) service.getAllOperations()[0];
        RestMethod restMethod = operation.getRestMethodAt(0);
        RestRequest request = restMethod.getRequestAt(0);
        assertThat(request.getParams().getProperty("language").getDefaultValue(), is(anEmptyString()));
    }
}
