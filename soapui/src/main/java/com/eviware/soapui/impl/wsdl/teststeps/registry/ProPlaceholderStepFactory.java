/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepWithProperties;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.UISupport;

/**
 * Factory for creation of placeholder steps
 * 
 * @author Ole.Matzura
 */

public class ProPlaceholderStepFactory extends WsdlTestStepFactory
{
	public ProPlaceholderStepFactory( String type, String name, String image )
	{
		super( type, name, "Placeholder for SoapUI Pro " + name + " TestStep", image );
	}

	public WsdlTestStep buildTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		return new WsdlProPlaceholderTestStep( testCase, config, forLoadTest, getTestStepIconPath(),
				getTestStepDescription() );
	}

	public TestStepConfig createNewTestStep( WsdlTestCase testCase, String name )
	{
		return null;
	}

	public boolean canCreate()
	{
		return false;
	}

	public static class WsdlProPlaceholderTestStep extends WsdlTestStepWithProperties
	{
		private final String description;

		protected WsdlProPlaceholderTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest,
				String iconPath, String description )
		{
			super( testCase, config, false, forLoadTest );
			this.description = description;

			if( !forLoadTest )
			{
				setIcon( UISupport.createImageIcon( iconPath ) );
			}
		}

		public TestStepResult run( TestCaseRunner testRunner, TestCaseRunContext testRunContext )
		{
			return new WsdlTestStepResult( this );
		}

		@Override
		public String getDescription()
		{
			return description;
		}
	}
}
