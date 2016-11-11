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

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Test;

import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.MATRIX;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.QUERY;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.TEMPLATE;
import static com.eviware.soapui.impl.rest.support.RestUtils.TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS;
import static com.eviware.soapui.impl.rest.support.RestUtils.TemplateExtractionOption.IGNORE_TEMPLATE_PARAMETERS;
import static com.eviware.soapui.utils.ModelItemFactory.makeRestRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RestUtilsTest {

    @Test
    public void extractsTemplateParams() throws Exception {
        String path = "/{id}/test/{test}/test";

        String[] params = RestUtils.extractTemplateParams(path);
        assertEquals(params.length, 2);
        assertEquals("id", params[0]);
        assertEquals("test", params[1]);
    }

    @Test
    public void extractsTemplateParameterFromCurlyBrackets() throws Exception {
        String path = "/{id}/42";

        RestParamsPropertyHolder params = ModelItemFactory.makeRestRequest().getParams();
        String extractedPath = RestUtils.extractParams(path, params, true, EXTRACT_TEMPLATE_PARAMETERS);
        assertThat(extractedPath, is(path));
        assertEquals(params.getPropertyCount(), 1);
        RestParamProperty id = params.getProperty("id");
        assertThat(id.getStyle(), is(TEMPLATE));
        assertThat(id.getValue(), is("id"));
    }

    @Test
    public void extractsTemplateParameterFromInteger() throws Exception {
        String path = "/{id}/42";

        RestParamsPropertyHolder params = ModelItemFactory.makeRestRequest().getParams();
        String extractedPath = RestUtils.extractParams(path, params, true, IGNORE_TEMPLATE_PARAMETERS);
        assertThat(extractedPath, is("/{id}/42"));
        assertEquals(params.getPropertyCount(), 0);
    }

    @Test
    public void extractsEmbeddedTemplateParameters() throws Exception {
        String path = "/conversation/date-{date}/time-{time}?userId=1234";

        RestParamsPropertyHolder params = ModelItemFactory.makeRestRequest().getParams();
        String extractedPath = RestUtils.extractParams(path, params, true, EXTRACT_TEMPLATE_PARAMETERS);
        assertThat(extractedPath, is("/conversation/date-{date}/time-{time}"));
        assertThat(params.getProperty("date").getStyle(), is(TEMPLATE));
        assertThat(params.getProperty("time").getStyle(), is(TEMPLATE));
    }

    @Test
    public void extractsEmbeddedTemplateAndMatrixParameters() throws Exception {
        String path = "/{templateParam};matrixParam=matrixValue?queryParam=value";

        RestParamsPropertyHolder params = ModelItemFactory.makeRestRequest().getParams();
        String extractedPath = RestUtils.extractParams(path, params, true, EXTRACT_TEMPLATE_PARAMETERS);
        assertThat(extractedPath, is("/{templateParam}"));
        assertThat(params.getProperty("templateParam").getStyle(), is(TEMPLATE));
        assertThat(params.getProperty("matrixParam").getStyle(), is(MATRIX));
        assertThat(params.getProperty("queryParam").getStyle(), is(QUERY));
    }

    @Test
    public void expandsRestRequestPathsWithoutTemplateParameters() throws Exception {
        RestRequest restRequest = makeRestRequest();
        restRequest.getResource().setPath("/the/path");
        addParameter(restRequest, RestParamsPropertyHolder.ParameterStyle.QUERY, "queryName", "queryValue");
        addParameter(restRequest, RestParamsPropertyHolder.ParameterStyle.MATRIX, "matrixName", "theMatrixValue");
        addParameter(restRequest, TEMPLATE, "templateName", "templateValue");
        addParameter(restRequest, RestParamsPropertyHolder.ParameterStyle.MATRIX, "matrixName2", "theMatrixValue2");
        addParameter(restRequest, RestParamsPropertyHolder.ParameterStyle.QUERY, "queryName2", "queryValue2");

        assertThat(RestUtils.expandPath("/the/path", restRequest.getParams(), restRequest),
                is("/the/path;matrixName=theMatrixValue;matrixName2=theMatrixValue2?queryName=queryValue&queryName2=queryValue2"));
    }

    @Test
    public void expandsRestRequestPathsWithTemplateParameter() throws Exception {
        RestRequest restRequest = makeRestRequest();
        String templateParameterName = "templateName";
        String templateParameterValue = "templateValue";
        restRequest.getResource().setPath("/the/{" + templateParameterName + "}/path");
        addParameter(restRequest, TEMPLATE, templateParameterName, templateParameterValue);

        assertThat(RestUtils.expandPath(restRequest.getResource().getFullPath(), restRequest.getParams(), restRequest),
                is("/the/" + templateParameterValue + "/path"));
    }

    @Test
    public void expandsPathWithPropertyExpansionOnPathAndTemplateParameter() throws Exception {
        RestRequest restRequest = makeRestRequest();
        restRequest.getProject().setPropertyValue("version", "xml");
        String templateParameterName = "templateName";
        String templateParameterValue = "templateValue";
        restRequest.getResource().setPath("/the/{" + templateParameterName + "}/path/${#Project#version}");
        addParameter(restRequest, TEMPLATE, templateParameterName, templateParameterValue);

        assertThat(RestUtils.getExpandedPath(restRequest.getResource().getFullPath(), restRequest.getParams(), restRequest),
                is("/the/" + templateParameterValue + "/path/xml"));
    }

    private void addParameter(RestRequestInterface restRequest, RestParamsPropertyHolder.ParameterStyle style, String name, String value) {
        RestParamsPropertyHolder params = restRequest.getParams();
        RestParamProperty restParamProperty = params.addProperty(name);
        restParamProperty.setStyle(style);
        restParamProperty.setValue(value);
    }
}
