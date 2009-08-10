/*
 *  soapUI, copyright (C) 2004-2009 eviware.com
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.model.propertyexpansion.resolvers;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestMockService;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.TestProperty;

public class ModelItemPropertyResolver implements PropertyResolver
{
	public String resolveProperty( PropertyExpansionContext context, String pe, boolean globalOverride )
	{
		if( pe.charAt( 0 ) == PropertyExpansion.SCOPE_PREFIX )
			return getScopedProperty( context, pe, globalOverride );

		ModelItem modelItem = context.getModelItem();
		if( modelItem instanceof WsdlLoadTest )
			modelItem = ( ( WsdlLoadTest )modelItem ).getTestCase();
		else if( modelItem instanceof TestRequest )
			modelItem = ( ( TestRequest )modelItem ).getTestStep();
		else if( modelItem instanceof WsdlMockResponse
				&& ( ( WsdlMockResponse )modelItem ).getMockOperation().getMockService() instanceof WsdlTestMockService )
			modelItem = ( ( WsdlTestMockService )( ( WsdlMockResponse )modelItem ).getMockOperation().getMockService() )
					.getMockResponseStep();

		if( modelItem instanceof WsdlTestStep || modelItem instanceof WsdlTestCase )
		{
			WsdlTestStep testStep = ( WsdlTestStep )( modelItem instanceof WsdlTestStep ? modelItem : null );
			WsdlTestCase testCase = ( WsdlTestCase )( testStep == null ? modelItem : testStep.getTestCase() );

			int sepIx = pe.indexOf( PropertyExpansion.PROPERTY_SEPARATOR );
			Object property = null;

			if( sepIx > 0 )
			{
				String step = pe.substring( 0, sepIx );
				String name = pe.substring( sepIx + 1 );
				String xpath = null;

				sepIx = name.indexOf( PropertyExpansion.PROPERTY_SEPARATOR );
				WsdlTestStep ts = testCase.getTestStepByName( step );

				if( sepIx != -1 )
				{
					xpath = name.substring( sepIx + 1 );
					name = name.substring( 0, sepIx );
				}

				if( step != null )
				{
					if( ts != null )
					{
						TestProperty p = ts.getProperty( name );
						if( p != null )
							property = p.getValue();
					}
				}
				else
				{
					property = context.getProperty( name );
				}

				if( property != null && xpath != null )
				{
					property = ResolverUtils.extractXPathPropertyValue( property, PropertyExpander.expandProperties(
							context, xpath ) );
				}
			}

			if( property != null )
				return property.toString();
		}

		return null;
	}

	private String getScopedProperty( PropertyExpansionContext context, String pe, boolean globalOverride )
	{
		ModelItem modelItem = context.getModelItem();

		WsdlTestStep testStep = null;
		WsdlTestCase testCase = null;
		WsdlTestSuite testSuite = null;
		WsdlProject project = null;
		WsdlMockService mockService = null;
		WsdlMockResponse mockResponse = null;

		if( modelItem instanceof WsdlTestStep )
		{
			testStep = ( WsdlTestStep )modelItem;
			testCase = testStep.getTestCase();
			testSuite = testCase.getTestSuite();
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlTestCase )
		{
			testCase = ( WsdlTestCase )modelItem;
			testSuite = testCase.getTestSuite();
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlLoadTest )
		{
			testCase = ( ( WsdlLoadTest )modelItem ).getTestCase();
			testSuite = testCase.getTestSuite();
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlTestSuite )
		{
			testSuite = ( WsdlTestSuite )modelItem;
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlInterface )
		{
			project = ( ( WsdlInterface )modelItem ).getProject();
		}
		else if( modelItem instanceof WsdlProject )
		{
			project = ( WsdlProject )modelItem;
		}
		else if( modelItem instanceof WsdlMockService )
		{
			mockService = ( WsdlMockService )modelItem;
			project = mockService.getProject();
		}
		else if( modelItem instanceof AbstractHttpRequestInterface<?> )
		{
			project = ( ( AbstractHttpRequest<?> )modelItem ).getOperation().getInterface().getProject();
		}
		else if( modelItem instanceof WsdlMockOperation )
		{
			mockService = ( ( WsdlMockOperation )modelItem ).getMockService();
			project = mockService.getProject();
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			mockResponse = ( WsdlMockResponse )modelItem;
			mockService = mockResponse.getMockOperation().getMockService();
			project = mockService.getProject();
		}

		// no project -> nothing
		if( project == null )
			return null;

		// explicit item reference?
		String result = ResolverUtils.checkForExplicitReference( pe, PropertyExpansion.PROJECT_REFERENCE, project,
				context, globalOverride );
		if( result != null )
			return result;

		result = ResolverUtils.checkForExplicitReference( pe, PropertyExpansion.TESTSUITE_REFERENCE, testSuite, context,
				globalOverride );
		if( result != null )
			return result;

		result = ResolverUtils.checkForExplicitReference( pe, PropertyExpansion.TESTCASE_REFERENCE, testCase, context,
				globalOverride );
		if( result != null )
			return result;

		result = ResolverUtils.checkForExplicitReference( pe, PropertyExpansion.MOCKSERVICE_REFERENCE, mockService,
				context, globalOverride );
		if( result != null )
			return result;

		result = ResolverUtils.checkForExplicitReference( pe, PropertyExpansion.MOCKRESPONSE_REFERENCE, mockResponse,
				context, globalOverride );
		if( result != null )
			return result;

		return null;
	}
}
