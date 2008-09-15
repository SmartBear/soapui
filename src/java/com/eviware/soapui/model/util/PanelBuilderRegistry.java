/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.model.util;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.WorkspaceImplPanelBuilder;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.panels.request.RestRequestPanelBuilder;
import com.eviware.soapui.impl.rest.panels.resource.RestResourcePanelBuilder;
import com.eviware.soapui.impl.rest.panels.service.RestServicePanelBuilder;
import com.eviware.soapui.impl.wsdl.*;
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
import com.eviware.soapui.impl.wsdl.panels.teststeps.*;
import com.eviware.soapui.impl.wsdl.panels.testsuite.WsdlTestSuitePanelBuilder;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.*;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of PanelBuilders
 * 
 * @author ole.matzura
 */

public class PanelBuilderRegistry
{
	private static Map<Class<? extends ModelItem>, PanelBuilder<? extends ModelItem> > builders = 
		new HashMap<Class<? extends ModelItem>,PanelBuilder<? extends ModelItem> >();
	
	@SuppressWarnings("unchecked")
	public static <T extends ModelItem> PanelBuilder<T> getPanelBuilder( T modelItem )
	{
		return ( PanelBuilder<T> ) builders.get( modelItem.getClass() );
	}
	
	public static <T extends ModelItem> void register( Class<T> modelItemClass, 
				PanelBuilder<T> panelBuilder )
	{
		builders.put( modelItemClass, panelBuilder );
	}
	
	static
	{
		register( WorkspaceImpl.class, new WorkspaceImplPanelBuilder() );
		register( WsdlProject.class, new WsdlProjectPanelBuilder() );
		register( WsdlInterface.class, new WsdlInterfacePanelBuilder() );
		register( RestService.class, new RestServicePanelBuilder() );
		register( WsdlOperation.class, new WsdlOperationPanelBuilder() );
		register( RestResource.class, new RestResourcePanelBuilder() );
		register( WsdlRequest.class, new WsdlRequestPanelBuilder() );
		register( RestRequest.class, new RestRequestPanelBuilder() );
		register( WsdlTestSuite.class, new WsdlTestSuitePanelBuilder() );
		register( WsdlTestCase.class, new WsdlTestCasePanelBuilder() );
		register( WsdlLoadTest.class, new WsdlLoadTestPanelBuilder() );
		register( WsdlMockService.class, new WsdlMockServicePanelBuilder() );
		register( WsdlMockOperation.class, new WsdlMockOperationPanelBuilder() );
		register( WsdlMockResponse.class, new WsdlMockResponsePanelBuilder() );
		register( WsdlGotoTestStep.class, new GotoStepPanelBuilder() );
		register( WsdlDelayTestStep.class, new DelayTestStepPanelBuilder() );
      register( RestTestRequestStep.class, new RestTestRequestPanelBuilder() );
      register( HttpTestRequestStep.class, new HttpTestRequestPanelBuilder() );
		register( WsdlTestRequestStep.class, new WsdlTestRequestPanelBuilder() );
		register( WsdlPropertiesTestStep.class, new PropertiesStepPanelBuilder() );
		register( WsdlGroovyScriptTestStep.class, new GroovyScriptStepPanelBuilder() );
		register( PropertyTransfersTestStep.class, new PropertyTransfersTestStepPanelBuilder() );
		register( WsdlRunTestCaseTestStep.class, new WsdlRunTestCaseTestStepPanelBuilder() );
		register( WsdlAsyncResponseTestStep.class, new AsyncResponseStepPanelBuilder() );
	}
}
