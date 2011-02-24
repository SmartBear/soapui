/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.check;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.boundary.EnumerationValuesExtractor;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

public class BoundarySecurityCheck extends AbstractSecurityCheck
{

	private int propertiesCounter = 0;
	public static final String TYPE = "BoundaryCheck";
	public static final String LABEL = "Boundary";
	private EnumerationValuesExtractor enumerationValuesExtractor;

	public BoundarySecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );
		enumerationValuesExtractor = new EnumerationValuesExtractor( ( ( WsdlTestRequestStep )testStep ).getTestRequest() );
		List<String> selected = getSelectedList();
		enumerationValuesExtractor.setSelectedEnumerationParameters( selected );
		propertiesCounter = selected.size();
	}

	private List<String> getSelectedList()
	{
		List<String> selected = new ArrayList<String>();
		for( SecurityCheckedParameter cpc : getParameterHolder().getParameterList() )
		{
			if( cpc.isChecked() )
				selected.add( cpc.getName() );
		}
		return selected;
	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{

		return null;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	protected void execute( SecurityTestRunner  securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		if( acceptsTestStep( testStep ) )
		{
			try
			{
				updateRequestContent( testStep );
			}
			catch( Exception e )
			{
				SoapUI.log.error( "Error extracting enumeration values from message", e );
			}

			testStep.run( (TestCaseRunner)securityTestRunner, context );
			createMessageExchange( testStep );
			propertiesCounter-- ;

		}
	}

	private void createMessageExchange( TestStep testStep )
	{
		MessageExchange messageExchange = new WsdlResponseMessageExchange( ( ( WsdlTestRequestStep )testStep )
				.getTestRequest() );
		getSecurityCheckRequestResult().setMessageExchange( messageExchange );
	}

	private void updateRequestContent( TestStep testStep ) throws XmlException, Exception
	{
		( ( WsdlTestRequestStep )testStep ).getTestRequest().setRequestContent( enumerationValuesExtractor.extract() );
	}

	protected boolean hasNext()
	{
		if( propertiesCounter == 0 )
		{
			propertiesCounter = getSelectedList().size();
			return false;
		}
		return true;
	}

	@Override
	public boolean isConfigurable()
	{
		return true;
	}

	@AForm( description = "Configure Out of Boundary Check", name = "Configure Out of Boundary Check", helpUrl = HelpUrls.HELP_URL_ROOT )
	protected interface BoundaryConfigDialog
	{

		@AField( description = "Parameters to Check", name = "Select parameters to check", type = AFieldType.MULTILIST )
		public final static String PARAMETERS = "Select parameters to check";

		@AField( description = "Assertions", name = "Select assertions to apply", type = AFieldType.COMPONENT )
		public final static String ASSERTIONS = "Select assertions to apply";

	}

	@Override
	public String getConfigDescription()
	{
		return "Configuration for Boundary Security Check";
	}

	@Override
	public String getConfigName()
	{
		return "Configuration for Boundary Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}

}