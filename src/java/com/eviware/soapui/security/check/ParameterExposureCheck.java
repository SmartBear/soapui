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

import java.util.List;
import java.util.Stack;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.SecurityCheckUtil;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

/**
 * This checks whether any parameters sent in the request are included in the
 * response, If they do appear, this is a good parameter to look at as a
 * possible attack vector for XSS
 * 
 * @author nebojsa.tasic
 */

public class ParameterExposureCheck extends AbstractSecurityCheckWithProperties
{
	public static final String TYPE = "ParameterExposureCheck";
	public static final String NAME = "Parameter Exposure";

	StrategyTypeConfig.Enum strategy = StrategyTypeConfig.ONE_BY_ONE;

	public ParameterExposureCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		if( strategy.equals( StrategyTypeConfig.ALL_AT_ONCE ) )
		{
			StringToStringMap stsmap = new StringToStringMap();
			List<PropertyMutation> mutationList = PropertyMutation.popAllMutation( context );
			if( mutationList != null && !mutationList.isEmpty() )
			{
				for( PropertyMutation pm : mutationList )
				{
					pm.updateRequestProperty( testStep );
					stsmap.putAll( pm.getMutatedParameters() );
				}
				MessageExchange message = ( MessageExchange )testStep.run( ( TestCaseRunner )securityTestRunner, context );
				createMessageExchange( stsmap, message );
			}
		}
		else
		{
			PropertyMutation mutation = PropertyMutation.popMutation( context );
			if( mutation != null )
			{
				mutation.updateRequestProperty( testStep );
				MessageExchange message = ( MessageExchange )testStep.run( ( TestCaseRunner )securityTestRunner, context );
				createMessageExchange( mutation.getMutatedParameters(), message );
			}
		}
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

	@SuppressWarnings( "unchecked" )
	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		if( !context.hasProperty( PropertyMutation.REQUEST_MUTATIONS_STACK ) )
		{
			Stack<PropertyMutation> requestMutationsList = new Stack<PropertyMutation>();
			context.put( PropertyMutation.REQUEST_MUTATIONS_STACK, requestMutationsList );
			try
			{
				extractMutations( testStep, context );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
			return true;
		}

		Stack<PropertyMutation> stack = ( Stack<PropertyMutation> )context.get( PropertyMutation.REQUEST_MUTATIONS_STACK );
		if( stack.empty() )
		{
			context.remove( PropertyMutation.REQUEST_MUTATIONS_STACK );
			return false;
		}
		else
		{
			return true;
		}
	}

	private void extractMutations( TestStep testStep, SecurityTestRunContext context )
	{
		strategy = getExecutionStrategy().getStrategy();

		XmlObjectTreeModel model = null;
		List<SecurityCheckedParameter> scpList = getParameterHolder().getParameterList();
		StringToStringMap stsmap = new StringToStringMap();
		for( SecurityCheckedParameter scp : scpList )
		{

			if( strategy.equals( StrategyTypeConfig.ONE_BY_ONE ) )
			{
				stsmap = new StringToStringMap();
				model = SecurityCheckUtil.getXmlObjectTreeModel( testStep, scp );
			}
			else
			{
				if( model == null )
				{
					model = SecurityCheckUtil.getXmlObjectTreeModel( testStep, scp );
				}
			}

			// if parameter is xml
			if( scp.isChecked() && scp.getXpath().trim().length() > 0 )
			{
				XmlTreeNode[] treeNodes = null;

				treeNodes = model.selectTreeNodes( context.expand( scp.getXpath() ) );

				if( treeNodes.length > 0 )
				{
					XmlTreeNode mynode = treeNodes[0];

					// work only for simple types
					if( mynode.getSchemaType().isSimpleType() )
					{
						mynode.setValue( 1, "value!!!!!!!!!!!!!!!!!" );

						PropertyMutation pm = new PropertyMutation();
						pm.setPropertyName( scp.getName() );
						pm.setPropertyValue( model.getXmlObject().toString() );
						stsmap.put( scp.getLabel(), mynode.getNodeText() );
						pm.setMutatedParameters( stsmap );
						pm.addMutation( context );
					}
				}
			}
			// non xml parameter
			else
			{
				PropertyMutation pm = new PropertyMutation();
				pm.setPropertyName( scp.getName() );
				pm.setPropertyValue( "non xml value" );
				stsmap.put( scp.getLabel(), "non xml value" );
				pm.setMutatedParameters( stsmap );
				pm.addMutation( context );
			}

		}
	}

	@Override
	public String getConfigDescription()
	{
		return "Configures parameter exposure security check";
	}

	@Override
	public String getConfigName()
	{
		return "Parameter Exposure Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}
}
