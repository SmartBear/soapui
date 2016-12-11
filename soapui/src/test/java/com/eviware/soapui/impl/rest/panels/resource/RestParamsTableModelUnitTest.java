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

package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation.METHOD;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.QUERY;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the RestParamsTableModel class
 */
public class RestParamsTableModelUnitTest {
    private static final int NAME_COLUMN_INDEX = 0;
    public static final int VALUE_COLUMN_INDEX = 1;
    private static final int STYLE_COLUMN_INDEX = 2;
    private static final int LOCATION_COLUMN_INDEX = 3;
    public static final String PARAM_NAME_1 = "ParamName1";
    public static final String PARAM_NAME_2 = "ParamName2";

    private RestParamsTableModel restParamsTableModel;
    private RestParamsPropertyHolder params;

    @Before
    public void setUp() throws SoapUIException {
        RestRequest restRequest = ModelItemFactory.makeRestRequest();
        params = restRequest.getParams();
        RestParamProperty param = params.addProperty(PARAM_NAME_1);
        param.setParamLocation(METHOD);
        RestParamProperty param2 = params.addProperty(PARAM_NAME_2);
        param2.setParamLocation(METHOD);

        restParamsTableModel = new RestParamsTableModel(params);
    }


    @Test
    public void removesAndAddThePropertyListenerAgainWhenParamIsSet() {
        mockParams();
        restParamsTableModel = new RestParamsTableModel(params);
        restParamsTableModel.setParams(params);
        verify(params, times(1)).removeTestPropertyListener(any(TestPropertyListener.class));
        verify(params, times(2)).addTestPropertyListener(any(TestPropertyListener.class));
    }

    @Test
    public void removesListenerOnRelease() {
        mockParams();
        restParamsTableModel.setParams(params);
        restParamsTableModel.release();
        verify(params, times(1)).addTestPropertyListener(any(TestPropertyListener.class));
        verify(params, times(1)).removeTestPropertyListener(any(TestPropertyListener.class));

    }

    @Test
    public void setsValueToPropertyWhenSetValueAtIsInvoked() {
        String value = "New value";
        restParamsTableModel.setValueAt(value, 0, VALUE_COLUMN_INDEX);
        assertThat((String) restParamsTableModel.getValueAt(0, VALUE_COLUMN_INDEX), is(value));
    }

    @Test
    public void renamesThePropertyIfSetValueIsInvokedOnFirstColumn() {
        String value = "New Name";
        restParamsTableModel.setValueAt(value, 0, NAME_COLUMN_INDEX);
        assertThat((String) restParamsTableModel.getValueAt(0, NAME_COLUMN_INDEX), is(value));
    }

    @Test
    public void changesPropertyStyleWhenSetValueIsInvokedonStyleColumn() {
        restParamsTableModel.setValueAt(QUERY, 0, STYLE_COLUMN_INDEX);
        assertThat((RestParamsPropertyHolder.ParameterStyle) restParamsTableModel.getValueAt(0, STYLE_COLUMN_INDEX),
                is(QUERY));
    }

    @Test
    public void givenModelWithParamsWhenSetLocationAndGetLocationThenShouldReturnSameValue() {
        restParamsTableModel.setValueAt(METHOD, 0, LOCATION_COLUMN_INDEX);
        assertThat((NewRestResourceActionBase.ParamLocation) restParamsTableModel.getValueAt(0, LOCATION_COLUMN_INDEX),
                is(METHOD));
    }

    @Test
    public void retainsParameterOrderWhenChangingLocation() throws Exception {
        restParamsTableModel.setValueAt(METHOD, 0, LOCATION_COLUMN_INDEX);

        assertThat((String) restParamsTableModel.getValueAt(0, NAME_COLUMN_INDEX), is(PARAM_NAME_1));
    }

    private void mockParams() {
        params = mock(RestParamsPropertyHolder.class);
        RestRequest restRequest = mock(RestRequest.class);

        RestResource resource = mock(RestResource.class);
        RestParamsPropertyHolder resourceParams = mock(XmlBeansRestParamsTestPropertyHolder.class);
        when(resource.getParams()).thenReturn(resourceParams);
        when(restRequest.getResource()).thenReturn(resource);

        RestMethod restMethod = mock(RestMethod.class);
        RestParamsPropertyHolder methodParams = mock(XmlBeansRestParamsTestPropertyHolder.class);
        when(restMethod.getParams()).thenReturn(methodParams);
        when(restRequest.getRestMethod()).thenReturn(restMethod);

        when(params.getModelItem()).thenReturn(restRequest);

    }
}
