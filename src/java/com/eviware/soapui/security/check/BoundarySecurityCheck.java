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

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
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
import com.eviware.soapui.security.boundary.AbstractBoundary;
import com.eviware.soapui.security.boundary.enumeration.EnumerationValues;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class BoundarySecurityCheck extends AbstractSecurityCheckWithProperties
{

	public static final String TYPE = "BoundaryCheck";
	public static final String NAME = "Boundary Check";
	private static final String REQUEST_MUTATIONS_STACK = "RequestMutationsStack";

	StrategyTypeConfig.Enum strategy;

	public BoundarySecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );
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
		String mutatedRequest = popMutation( context );
		if( mutatedRequest != null )
		{
			updateTestStepRequest( testStep, mutatedRequest );
			testStep.run( ( TestCaseRunner )securityTestRunner, context );
			createMessageExchange( testStep );
		}
	}

	@SuppressWarnings( "unchecked" )
	private String popMutation( SecurityTestRunContext context )
	{
		Stack<String> requestMutationsStack = ( Stack<String> )context.get( REQUEST_MUTATIONS_STACK );
		return requestMutationsStack.empty() ? null : requestMutationsStack.pop();
	}

	private void createMessageExchange( TestStep testStep )
	{
		MessageExchange messageExchange = new WsdlResponseMessageExchange( ( ( WsdlTestRequestStep )testStep )
				.getTestRequest() );
		getSecurityCheckRequestResult().setMessageExchange( messageExchange );
	}

	private void extractMutations( TestStep testStep, SecurityTestRunContext context ) throws XmlException, Exception
	{
		strategy = getExecutionStrategy().getStrategy();

		XmlObjectTreeModel model = getXmlObjectTreeModel( testStep );
		List<SecurityCheckedParameter> scpList = getParameterHolder().getParameterList();
		for( SecurityCheckedParameter scp : scpList )
		{
			if( scp.isChecked() )
			{
				
				// a more generic solution for getting schematype, schematypesystem and value is probably needed for supporting
				// REST also, something in the line of:
				//typeSystem = scp.getTestProperty().getSchemaType().getTypeSystem()
				//model = new XmlObjectTreeModel( typeSystem, XmlObject.Factory.parse( scp.getTestProperty().getValue() ) );
				
				
				XmlTreeNode[] treeNodes = null;
				if( strategy.equals( StrategyTypeConfig.ONE_BY_ONE ) )
				{
					model = getXmlObjectTreeModel( testStep );
					treeNodes = model.selectTreeNodes( context.expand( scp.getXPath() ));
				}
				else
				{
					treeNodes = model.selectTreeNodes( context.expand(scp.getXPath()) );
				}

				if( treeNodes.length > 0 )
				{
					XmlTreeNode mynode = treeNodes[0];

					// !!!!!!!!!!!!!!work only for simple types
					if( mynode.getSchemaType().isSimpleType() )
					{
						if( mynode.getSchemaType() != null && mynode.getSchemaType().getEnumerationValues() != null
								&& mynode.getSchemaType().getEnumerationValues().length > 0 )
						{
							EnumerationValues nodeInfo = new EnumerationValues( mynode.getSchemaType().getBaseType()
									.getShortJavaName() );
							for( XmlAnySimpleType s : mynode.getSchemaType().getEnumerationValues() )
							{
								nodeInfo.addValue( s.getStringValue() );
							}
							updateEnumNodeValue( mynode, nodeInfo );
							if( strategy.equals( StrategyTypeConfig.ONE_BY_ONE ) )
								addMutation( context, model.getXmlObject().toString() );
						}
						else
						{
							SchemaTypeImpl simpleType = ( SchemaTypeImpl )mynode.getSchemaType();
							XmlObjectTreeModel model2 = new XmlObjectTreeModel( simpleType.getTypeSystem(), simpleType
									.getParseObject() );
							extractRestrictions( model2, context, mynode, model );
						}
					}
				}
			}
		}
		if( strategy.equals( StrategyTypeConfig.ALL_AT_ONCE ) )
			addMutation( context, model.getXmlObject().toString() );
	}

	private XmlObjectTreeModel getXmlObjectTreeModel( TestStep testStep )
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
		return model;
	}

	@SuppressWarnings( "unchecked" )
	private void addMutation( SecurityTestRunContext context, String request )
	{
		Stack<String> stack = ( Stack<String> )context.get( REQUEST_MUTATIONS_STACK );
		stack.push( request );
	}

	private void updateTestStepRequest( TestStep testStep, String updatedRequest )
	{
		( ( WsdlTestRequestStep )testStep ).getTestRequest().setRequestContent( updatedRequest );

	}

	public String extractRestrictions( XmlObjectTreeModel model2, SecurityTestRunContext context,
			XmlTreeNode nodeToUpdate, XmlObjectTreeModel model ) throws XmlException, Exception
	{
		getNextChild( model2.getRootNode(), context, nodeToUpdate, model );

		return nodeToUpdate.getXmlObject().toString();
	}

	private void getNextChild( XmlTreeNode node, SecurityTestRunContext context, XmlTreeNode nodeToUpdate,
			XmlObjectTreeModel model )
	{
		String baseType = null;
		for( int i = 0; i < node.getChildCount(); i++ )
		{
			XmlTreeNode mynode = node.getChild( i );

			if( "xsd:restriction".equals( mynode.getParent().getNodeName() ) )
			{
				if( mynode.getNodeName().equals( "@base" ) )
				{
					baseType = mynode.getNodeText();
				}
				else
				{
					createMutation( baseType, mynode, context, nodeToUpdate, model );
				}
			}
			getNextChild( mynode, context, nodeToUpdate, model );
		}
	}

	private void createMutation( String baseType, XmlTreeNode mynode, SecurityTestRunContext context,
			XmlTreeNode nodeToUpdate, XmlObjectTreeModel model )
	{
		String value = null;
		String nodeName = mynode.getNodeName();
		String nodeValue = mynode.getChild( 0 ).getNodeText();
		value = AbstractBoundary.outOfBoundaryValue( baseType, nodeName, nodeValue );

		if( value != null )
		{
			nodeToUpdate.setValue( 1, value );
			if( strategy.equals( StrategyTypeConfig.ONE_BY_ONE ) )
				addMutation( context, model.getXmlObject().toString() );
		}
		else
		{
			SoapUI.log.warn( "No out of boundary value is created for restriction " + nodeName + " of baseType:"
					+ baseType );
		}
	}

	public void updateEnumNodeValue( XmlTreeNode mynode, EnumerationValues enumerationValues )
	{
		int size = EnumerationValues.maxLengthStringSize( enumerationValues.getValuesList() );
		String value = EnumerationValues.createOutOfBoundaryValue( enumerationValues, size );
		if( value != null )
			mynode.setValue( 1, value );
	}

	/**
	 * this method uses context to handle list of mutated request
	 * 
	 */
	@SuppressWarnings( "unchecked" )
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		if( !context.hasProperty( REQUEST_MUTATIONS_STACK ) )
		{
			Stack<String> requestMutationsList = new Stack<String>();
			context.put( REQUEST_MUTATIONS_STACK, requestMutationsList );
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

		Stack<String> stack = ( Stack<String> )context.get( REQUEST_MUTATIONS_STACK );
		if( stack.empty() )
		{
			context.remove( REQUEST_MUTATIONS_STACK );
			return false;
		}
		else
		{
			return true;
		}
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