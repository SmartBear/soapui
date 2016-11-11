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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DefaultPropertyHolderTableModelTest {

    public static final String FIRST_PARAM_NAME = "Param1";
    public static final String PARAM_VALUE = "ParamValue";
    public static final String EMPTY_STRING = "";
    private RestParamsPropertyHolder methodParams;
    private DirectAccessPropertyHolderTableModel tableHolderModel;
    private RestParamsPropertyHolder requestParams;
    private DirectAccessPropertyHolderTableModel requestTableHolderModel;

    @Before
    public void setUp() throws Exception {
        methodParams = ModelItemFactory.makeRestMethod().getParams();
        methodParams.addProperty(FIRST_PARAM_NAME);
        tableHolderModel = createDefaultPropertyHolderTableModel(methodParams);
        requestParams = ModelItemFactory.makeRestRequest().getParams();
        requestTableHolderModel = createDefaultPropertyHolderTableModel(requestParams);
    }

    @Test
    public void doesNotSetDefaultValueIfModelItemIsRestRequest() throws Exception {
        RestParamProperty property = requestParams.getPropertyAt(0);
        assertThat(property.getValue(), is(PARAM_VALUE));
        assertThat(property.getDefaultValue(), is(EMPTY_STRING));
    }

    @Test
    public void setsDefaultValueIfModelItemIsRestMethod() throws Exception {
        RestParamProperty property = methodParams.getPropertyAt(0);
        assertThat(property.getValue(), is(PARAM_VALUE));
        assertThat(property.getDefaultValue(), is(PARAM_VALUE));
    }

    @Test
    public void handlesParameterMoveCorrectlyForMethodParameters() throws Exception {
        String lastParameterName = "lastOne";
        methodParams.addProperty(lastParameterName);
        tableHolderModel.moveProperty(lastParameterName, 1, 0);

        assertThat(tableHolderModel.getPropertyAtRow(0).getName(), is(lastParameterName));
    }

    @Test
    public void detectsParameterNameChange() throws Exception {
        String newParameterName = "lastOne";
        methodParams.renameProperty(FIRST_PARAM_NAME, newParameterName);

        assertThat(tableHolderModel.getPropertyAtRow(0).getName(), is(newParameterName));
    }

    @Test
    public void handlesParameterMoveCorrectlyForRequestParameters() throws Exception {
        String lastParameterName = "lastOne";
        requestParams.addProperty(lastParameterName);
        requestTableHolderModel.moveProperty(lastParameterName, 1, 0);

        assertThat(requestTableHolderModel.getPropertyAtRow(0).getName(), is(lastParameterName));
    }


	/* helper */

    private DirectAccessPropertyHolderTableModel createDefaultPropertyHolderTableModel(RestParamsPropertyHolder params) {
        params.addProperty(FIRST_PARAM_NAME);
        DirectAccessPropertyHolderTableModel tableHolderModel =
                new DirectAccessPropertyHolderTableModel<RestParamsPropertyHolder>(params);
        tableHolderModel.setValueAt(PARAM_VALUE, 0, 1);
        return tableHolderModel;
    }
}
