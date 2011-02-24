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

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.boundary.EnumerationValuesExtractor;
import com.eviware.soapui.security.boundary.enumeration.EnumerationValues;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class BoundarySecurityCheck extends AbstractSecurityCheck
{

	private int propertiesCounter = 0;
	public static final String TYPE = "BoundaryCheck";
	public static final String LABEL = "Boundary";

	public BoundarySecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );
		List<String> selected = getSelectedList();
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

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
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

			testStep.run( ( TestCaseRunner )securityTestRunner, context );
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
		XmlObjectTreeModel model = null;
		WsdlRequest request = ( ( WsdlTestRequestStep )testStep ).getTestRequest();
		try
		{
			model = new XmlObjectTreeModel( request.getOperation().getInterface().getDefinitionContext()
					.getSchemaTypeSystem(), XmlObject.Factory.parse( request.getRequestContent() ) );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		List<SecurityCheckedParameter> scpList = getParameterHolder().getParameterList();
		for( SecurityCheckedParameter scp : scpList )
		{
			if( scp.isChecked() )
			{
				XmlTreeNode[] treeNodes = model.selectTreeNodes( scp.getXPath() );
				if( treeNodes.length > 0 )
				{
					XmlTreeNode mynode = treeNodes[0];

					if( mynode.getSchemaType() != null && mynode.getSchemaType().getEnumerationValues() != null
							&& mynode.getSchemaType().getEnumerationValues().length > 0 )
					{
						EnumerationValues nodeInfo = new EnumerationValues( mynode.getSchemaType().getBaseType()
								.getShortJavaName() );
						for( XmlAnySimpleType s : mynode.getSchemaType().getEnumerationValues() )
						{
							nodeInfo.addValue( s.getStringValue() );
						}
						updateNodeValue( mynode, nodeInfo );
					}
				}
			}
		}
		( ( WsdlTestRequestStep )testStep ).getTestRequest().setRequestContent( model.getXmlObject().toString() );
	}

	public  void updateNodeValue( XmlTreeNode mynode, EnumerationValues enumerationValues )
	{
		int size = EnumerationValues.maxLengthStringSize( enumerationValues.getValuesList() );
		String value = EnumerationValues.createOutOfBoundaryValue( enumerationValues, size );
		if( value != null )
			mynode.setValue( 1, value );
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