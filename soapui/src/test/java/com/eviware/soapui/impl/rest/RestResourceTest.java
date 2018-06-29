/*
 * SoapUI, Copyright (C) 2004-2017 SmartBear Software
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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.RestParameterConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.eviware.soapui.utils.CommonMatchers.anEmptyArray;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class RestResourceTest {

    private RestResource restResource;
    private RestService parentService;

    @Before
    public void setUp() throws XmlException, IOException, SoapUIException {
        WsdlProject project = new WsdlProject();
        RestService restService = (RestService) project.addNewInterface("Test", RestServiceFactory.REST_TYPE);
        restResource = restService.addNewResource("Resource", "/test");
        parentService = restResource.getService();
    }

    @Test
    public void shouldGetTemplateParams() throws Exception {
        assertThat(restResource.getDefaultParams(), is(anEmptyArray()));

        restResource.setPath("/{id}/test");
        assertThat(restResource.getDefaultParams(), is(anEmptyArray()));
        assertThat(restResource.getFullPath(), is("/{id}/test"));

        RestResource subResource = restResource.addNewChildResource("Child", "{test}/test");
        assertThat(subResource.getFullPath(), is("/{id}/test/{test}/test"));
    }

    @Test
    public void ignoresMatrixParamsOnPath() throws Exception {
        String matrixParameterString = ";Param2=matrixValue2;address=16";
        restResource.setPath("/maps/api/geocode/xml" + matrixParameterString);

        assertThat(restResource.getFullPath(), not(containsString(matrixParameterString)));

        String childResourceParameterString = ";ver=2";
        RestResource childResource = restResource.addNewChildResource("Child", "{test}/test/version" + childResourceParameterString);
        assertThat(childResource.getPath(), not(containsString(childResourceParameterString)));

        assertThat(childResource.getFullPath(), not(containsString(matrixParameterString)));
        assertThat(childResource.getFullPath(), not(containsString(childResourceParameterString)));
    }

    @Test
    public void ignoresMatrixParamsWithoutValueOnPath() throws Exception {
        String matrixParameterString = ";Param2=1;address=";
        restResource.setPath("/maps/api/geocode/xml" + matrixParameterString);

        assertThat(restResource.getFullPath(), not(containsString(matrixParameterString)));

        String childResourceParameterString = ";ver=";
        RestResource childResource = restResource.addNewChildResource("Child", "{test}/test/version" + childResourceParameterString);
        assertThat(childResource.getPath(), not(containsString(childResourceParameterString)));

        assertThat(childResource.getFullPath(), not(containsString(matrixParameterString)));
        assertThat(childResource.getFullPath(), not(containsString(childResourceParameterString)));
    }

    @Test
    public void listensToChangesInConfiguredParameters() throws Exception {
        RestResourceConfig config = RestResourceConfig.Factory.newInstance();
        RestParametersConfig restParametersConfig = config.addNewParameters();
        RestParameterConfig parameterConfig = restParametersConfig.addNewParameter();
        String parameterName = "theName";
        parameterConfig.setName(parameterName);
        parameterConfig.setStyle(RestParameterConfig.Style.Enum.forInt(RestParamsPropertyHolder.ParameterStyle.QUERY.ordinal()));
        config.setPath("/actual_path");

        RestResource restResource = new RestResource(parentService, config);
        restResource.getParams().getProperty(parameterName).setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        assertThat(restResource.getPath(), containsString(parameterName));

    }

    @Test
    public void removesFormerTemplateParametersFromPath() throws Exception {
        RestResourceConfig config = RestResourceConfig.Factory.newInstance();
        RestParametersConfig restParametersConfig = config.addNewParameters();
        RestParameterConfig parameterConfig = restParametersConfig.addNewParameter();
        String parameterName = "theName";
        parameterConfig.setName(parameterName);
        parameterConfig.setStyle(RestParameterConfig.Style.Enum.forInt(RestParamsPropertyHolder.ParameterStyle.TEMPLATE.ordinal()));
        config.setPath("/actual_path");

        RestResource restResource = new RestResource(parentService, config);
        restResource.getParams().getProperty(parameterName).setStyle(RestParamsPropertyHolder.ParameterStyle.QUERY);
        assertThat(restResource.getPath(), not(containsString(parameterName)));

    }

    @Test
    public void considersBasePathWhenAddingTemplateParameter() throws Exception {
        String parameterName = "version";
        String parameterInPath = "{" + parameterName + "}";
        parentService.setBasePath("/base/" + parameterInPath);
        RestParamProperty parameter = restResource.addProperty(parameterName);
        parameter.setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);

        assertThat(restResource.getPath(), not(containsString(parameterInPath)));
    }

    @Test
    public void deletingResourceDeletesAllChildResources() throws Exception {
        // restResource -> childResourceA, childResourceB
        RestResource childResourceA = restResource.addNewChildResource("ChildA", "/childPathA");
        restResource.addNewChildResource("ChildB", "/childPathB");

        // childResourceA -> grandChildAA, grandChildAB
        RestResource grandChildAA = childResourceA.addNewChildResource("GrandChildAA", "/grandChildPathAA");
        childResourceA.addNewChildResource("GrandChildAB", "/grandChildPathAB");

        // grandChildAA -> greatGrandChildAAA
        grandChildAA.addNewChildResource("GreatGrandChildAAA", "/greatGrandChildAAA");

        restResource.deleteResource(childResourceA);

        assertThat(restResource.getChildResourceList().size(), is(1)); // ensure it does not delete the sibling
        assertThat(childResourceA.getChildResourceList().size(), is(0));
        assertThat(grandChildAA.getChildResourceList().size(), is(0));

    }

}
