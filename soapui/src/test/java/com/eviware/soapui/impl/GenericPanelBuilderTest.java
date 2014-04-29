/*
 * Copyright 2004-2014 SmartBear Software
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
package com.eviware.soapui.impl;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.rest.panels.method.RestMethodPanelBuilder;
import com.eviware.soapui.impl.rest.panels.mock.RestMockActionPanelBuilder;
import com.eviware.soapui.impl.rest.panels.mock.RestMockResponsePanelBuilder;
import com.eviware.soapui.impl.rest.panels.mock.RestMockServicePanelBuilder;
import com.eviware.soapui.impl.rest.panels.request.RestRequestPanelBuilder;
import com.eviware.soapui.impl.rest.panels.resource.RestResourcePanelBuilder;
import com.eviware.soapui.impl.rest.panels.service.RestServicePanelBuilder;
import com.eviware.soapui.impl.wsdl.*;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.panels.iface.WsdlInterfacePanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.mock.WsdlMockServicePanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockOperationPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResponsePanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.operation.WsdlOperationPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.project.WsdlProjectPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.request.WsdlRequestPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.testcase.WsdlTestCasePanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.*;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFRequestTestStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.testsuite.WsdlTestSuitePanelBuilder;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.*;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.support.components.JPropertiesTable;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.fail;
import static org.junit.runners.Parameterized.Parameters;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class GenericPanelBuilderTest {
    private static final int VALUE_INDEX = 1;

    private PanelBuilder panelBuilder;
    private ModelItem modelItem;

    public GenericPanelBuilderTest(Class<? extends PanelBuilder> panelBuilderClass, Class<? extends ModelItem> modelClass) throws Exception {
        this.panelBuilder = panelBuilderClass.newInstance();
        this.modelItem = mock(modelClass, RETURNS_MOCKS);
    }

    @Test
    public void builderValuesShouldMatchModel() throws Exception {
        JPropertiesTable table = (JPropertiesTable) panelBuilder.buildOverviewPanel(modelItem);
        JPropertiesTable.PropertiesTableModel tableModel = table.getTableModel();

        int numberOfProperties = tableModel.getRowCount();
        for (int i = 0; i < numberOfProperties; i++) {
            assertOneProperty(tableModel, i);
        }
    }

    private void assertOneProperty(JPropertiesTable.PropertiesTableModel tableModel, int index) throws Exception {
        String key = tableModel.getPropertyDescriptorAt(index).getName();
        Object propertyValue = tableModel.getValueAt(index, VALUE_INDEX);

        // at this point I was expecting an exception in case of failure but
        // the exception is swallowed and null is returned

        if (propertyValue == null && !isEnum(key)) {
            fail(failureMessage(key));
        }
    }

    private boolean isEnum(String key) {
        try {
            Method getter = modelItem.getClass().getMethod("get" + StringUtils.capitalize(key));
            boolean isEnum = getter.getReturnType().isEnum();
            return isEnum;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private String failureMessage(Object key) {
        String builderName = this.panelBuilder.getClass().getName();
        return "The panel builder " + builderName + " fails for the property " + key;
    }

    @Parameters(name= "{index} - {0}")
    public static Collection<Object[]> panelModelCombinations() {
        return Arrays.asList(new Object[][]{
                {AMFRequestTestStepPanelBuilder.class, AMFRequestTestStep.class},
                {DelayTestStepPanelBuilder.class, WsdlDelayTestStep.class},
                {HttpTestRequestPanelBuilder.class, HttpTestRequestStep.class},
                {JdbcRequestTestStepPanelBuilder.class, JdbcRequestTestStep.class},
                {MockResponseStepPanelBuilder.class, WsdlMockResponseTestStep.class},
                {PropertiesStepPanelBuilder.class, WsdlPropertiesTestStep.class},
                {RestMethodPanelBuilder.class, RestMethod.class},
                {RestMockActionPanelBuilder.class, RestMockAction.class},
                {RestMockResponsePanelBuilder.class, RestMockResponse.class},
                {RestMockServicePanelBuilder.class, RestMockService.class},
                {RestRequestPanelBuilder.class, RestRequest.class},
                {RestResourcePanelBuilder.class, RestResource.class},
                {RestServicePanelBuilder.class, RestService.class},
                {RestTestRequestPanelBuilder.class, RestTestRequestStep.class},
                {WorkspaceImplPanelBuilder.class, WorkspaceImpl.class},
                {WsdlInterfacePanelBuilder.class, WsdlInterface.class},
                {WsdlMockOperationPanelBuilder.class, WsdlMockOperation.class},
                {WsdlMockResponsePanelBuilder.class, WsdlMockResponse.class},
                {WsdlMockServicePanelBuilder.class, WsdlMockService.class},
                {WsdlOperationPanelBuilder.class, WsdlOperation.class},
                {WsdlProjectPanelBuilder.class, WsdlProject.class},
                {WsdlRequestPanelBuilder.class, WsdlRequest.class},
                {WsdlRunTestCaseTestStepPanelBuilder.class, WsdlRunTestCaseTestStep.class},
                {WsdlTestCasePanelBuilder.class, WsdlTestCase.class},
                {WsdlTestRequestPanelBuilder.class, WsdlTestRequestStep.class},
                {WsdlTestSuitePanelBuilder.class, WsdlTestSuite.class}
                // add more panel builders here
        });
    }
}
