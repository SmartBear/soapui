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

package com.eviware.soapui.model.util;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.WorkspaceImplPanelBuilder;
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
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.panels.iface.WsdlInterfacePanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.loadtest.WsdlLoadTestPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.mock.WsdlMockServicePanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockOperationPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResponsePanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.operation.WsdlOperationPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.project.WsdlProjectPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.request.WsdlRequestPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.testcase.WsdlTestCasePanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.DelayTestStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.GotoStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.GroovyScriptStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.HttpTestRequestPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.JdbcRequestTestStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.ManualTestStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.MockResponseStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.PropertiesStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.PropertyTransfersTestStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.RestTestRequestPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.WsdlRunTestCaseTestStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.WsdlTestRequestPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFRequestTestStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.testsuite.WsdlTestSuitePanelBuilder;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.ManualTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlDelayTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGotoTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlPropertiesTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.panels.SecurityTestPanelBuilder;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistryListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of PanelBuilders
 *
 * @author ole.matzura
 */

public class PanelBuilderRegistry {
    private static Map<Class<? extends ModelItem>, PanelBuilder<? extends ModelItem>> builders = new HashMap<Class<? extends ModelItem>, PanelBuilder<? extends ModelItem>>();

    @SuppressWarnings("unchecked")
    public static <T extends ModelItem> PanelBuilder<T> getPanelBuilder(T modelItem) {
        return (PanelBuilder<T>) builders.get(modelItem.getClass());
    }

    public static <T extends ModelItem> void register(Class<T> modelItemClass, PanelBuilder<T> panelBuilder) {
        builders.put(modelItemClass, panelBuilder);
    }

    static {
        register(WorkspaceImpl.class, new WorkspaceImplPanelBuilder());
        register(WsdlProject.class, new WsdlProjectPanelBuilder());
        register(WsdlInterface.class, new WsdlInterfacePanelBuilder());
        register(RestService.class, new RestServicePanelBuilder());
        register(WsdlOperation.class, new WsdlOperationPanelBuilder());
        register(RestResource.class, new RestResourcePanelBuilder());
        register(RestMethod.class, new RestMethodPanelBuilder());
        register(WsdlRequest.class, new WsdlRequestPanelBuilder());
        register(RestRequest.class, new RestRequestPanelBuilder());
        register(WsdlTestSuite.class, new WsdlTestSuitePanelBuilder<WsdlTestSuite>());
        register(WsdlTestCase.class, new WsdlTestCasePanelBuilder<WsdlTestCase>());
        register(WsdlLoadTest.class, new WsdlLoadTestPanelBuilder<WsdlLoadTest>());
        register(WsdlMockService.class, new WsdlMockServicePanelBuilder());
        register(WsdlMockOperation.class, new WsdlMockOperationPanelBuilder());
        register(WsdlMockResponse.class, new WsdlMockResponsePanelBuilder());
        register(RestMockService.class, new RestMockServicePanelBuilder());
        register(RestMockAction.class, new RestMockActionPanelBuilder());
        register(RestMockResponse.class, new RestMockResponsePanelBuilder());
        register(WsdlGotoTestStep.class, new GotoStepPanelBuilder());
        register(WsdlDelayTestStep.class, new DelayTestStepPanelBuilder());
        register(ManualTestStep.class, new ManualTestStepPanelBuilder());
        register(RestTestRequestStep.class, new RestTestRequestPanelBuilder());
        register(HttpTestRequestStep.class, new HttpTestRequestPanelBuilder());
        register(WsdlTestRequestStep.class, new WsdlTestRequestPanelBuilder());
        register(WsdlPropertiesTestStep.class, new PropertiesStepPanelBuilder());
        register(WsdlGroovyScriptTestStep.class, new GroovyScriptStepPanelBuilder());
        register(PropertyTransfersTestStep.class, new PropertyTransfersTestStepPanelBuilder());
        register(WsdlRunTestCaseTestStep.class, new WsdlRunTestCaseTestStepPanelBuilder());
        register(WsdlMockResponseTestStep.class, new MockResponseStepPanelBuilder());
        register(JdbcRequestTestStep.class, new JdbcRequestTestStepPanelBuilder());
        register(AMFRequestTestStep.class, new AMFRequestTestStepPanelBuilder());
        register(SecurityTest.class, new SecurityTestPanelBuilder<SecurityTest>());

        for (PanelBuilderFactory factory : SoapUI.getFactoryRegistry().getFactories(PanelBuilderFactory.class)) {
            register(factory.getTargetModelItem(), factory.createPanelBuilder());
        }

        SoapUI.getFactoryRegistry().addFactoryRegistryListener( new SoapUIFactoryRegistryListener() {
            @Override
            public void factoryAdded(Class<?> factoryType, Object factory) {
                if( factoryType.equals( PanelBuilderFactory.class )) {
                    PanelBuilderFactory panelBuilderFactory = (PanelBuilderFactory) factory;
                    register(panelBuilderFactory.getTargetModelItem(), panelBuilderFactory.createPanelBuilder());
                }
            }

            @Override
            public void factoryRemoved(Class<?> factoryType, Object factory) {
                if( factoryType.equals( PanelBuilderFactory.class )) {
                    unregister((PanelBuilderFactory) factory);
                }
            }
        });
    }

    public static void unregister(PanelBuilderFactory factory) {
        builders.remove( factory.getTargetModelItem() );
    }
}
