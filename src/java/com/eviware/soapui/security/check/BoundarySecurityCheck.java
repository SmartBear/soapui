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
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.boundary.AbstractBoundary;
import com.eviware.soapui.security.boundary.BoundaryRestrictionUtill;
import com.eviware.soapui.security.boundary.enumeration.EnumerationValues;
import com.eviware.soapui.support.SecurityCheckUtil;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

public class BoundarySecurityCheck extends AbstractSecurityCheckWithProperties
{

	public static final String TYPE = "BoundaryCheck";
	public static final String NAME = "Boundary Check";
	private static final String REQUEST_MUTATIONS_STACK = "RequestMutationsStack";
	private RestrictionLabel restrictionLabel = new RestrictionLabel();

	StrategyTypeConfig.Enum strategy = StrategyTypeConfig.ONE_BY_ONE;

	public BoundarySecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );
	}

	@Override
	public JComponent getComponent()
	{
		return restrictionLabel.getJLabel();
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		PropertyMutation mutation = popMutation( context );
		if( mutation != null )
		{

			updateRequestProperty( testStep, mutation );
			MessageExchange message = ( MessageExchange )testStep.run( ( TestCaseRunner )securityTestRunner, context );
			createMessageExchange( mutation.getMutatedParameters(), message );
		}
	}

	@SuppressWarnings( "unchecked" )
	private PropertyMutation popMutation( SecurityTestRunContext context )
	{
		Stack<PropertyMutation> requestMutationsStack = ( Stack<PropertyMutation> )context.get( REQUEST_MUTATIONS_STACK );
		return requestMutationsStack.empty() ? null : requestMutationsStack.pop();
	}

	private void extractMutations( TestStep testStep, SecurityTestRunContext context ) throws XmlException, Exception
	{
		strategy = getExecutionStrategy().getStrategy();

		XmlObjectTreeModel model = null;// getXmlObjectTreeModel( testStep );
		List<SecurityCheckedParameter> scpList = getParameterHolder().getParameterList();
		StringToStringMap stsmap = new StringToStringMap();
		for( SecurityCheckedParameter scp : scpList )
		{
			if( scp.isChecked() && scp.getXpath().trim().length() > 0 )
			{
				XmlTreeNode[] treeNodes = null;

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
				treeNodes = model.selectTreeNodes( context.expand( scp.getXpath() ) );

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
							stsmap.put( scp.getLabel(), mynode.getNodeText() );
							// addToUpdated( context, scp.getLabel(),
							// mynode.getNodeText() );
							if( strategy.equals( StrategyTypeConfig.ONE_BY_ONE ) )
							{
								PropertyMutation pm = new PropertyMutation();
								pm.setPropertyName( scp.getName() );
								pm.setPropertyValue( model.getXmlObject().toString() );
								stsmap = new StringToStringMap();
								stsmap.put( scp.getLabel(), mynode.getNodeText() );
								pm.setMutatedParameters( stsmap );
								addMutation( context, pm );
							}
						}
						else
						{
							SchemaTypeImpl simpleType = ( SchemaTypeImpl )mynode.getSchemaType();
							XmlObjectTreeModel model2 = new XmlObjectTreeModel( simpleType.getTypeSystem(), simpleType
									.getParseObject() );
							extractRestrictions( model2, context, mynode, model, scp, stsmap );
						}
					}
				}
			}
		}

		if( model != null && strategy.equals( StrategyTypeConfig.ALL_AT_ONCE ) )
		{
			PropertyMutation pm = new PropertyMutation();
			pm.setPropertyName( "Request" );
			pm.setPropertyValue( model.getXmlObject().toString() );
			pm.setMutatedParameters( stsmap );
			addMutation( context, pm );
		}
	}

//	private XmlObjectTreeModel getXmlObjectTreeModel( TestStep testStep, SecurityCheckedParameter scp )
//	{
//		try
//		{
//			TestProperty tp = testStep.getProperty( scp.getName() );
//			if( tp.getSchemaType() != null )
//			{
//				return new XmlObjectTreeModel( tp.getSchemaType().getTypeSystem(), XmlObject.Factory.parse( tp.getValue() ) );
//			}
//		}
//		catch( Exception e )
//		{
//			SoapUI.logError( e );
//		}
//		return null;
//	}

	@SuppressWarnings( "unchecked" )
	private void addMutation( SecurityTestRunContext context, PropertyMutation mutation )
	{
		Stack<PropertyMutation> stack = ( Stack<PropertyMutation> )context.get( REQUEST_MUTATIONS_STACK );
		stack.push( mutation );
	}

	private void updateRequestProperty( TestStep testStep, PropertyMutation mutation )
	{
		testStep.getProperty( mutation.getPropertyName() ).setValue( mutation.getPropertyValue() );

	}

	public String extractRestrictions( XmlObjectTreeModel model2, SecurityTestRunContext context,
			XmlTreeNode nodeToUpdate, XmlObjectTreeModel model, SecurityCheckedParameter scp, StringToStringMap stsmap )
			throws XmlException, Exception
	{
		getNextChild( model2.getRootNode(), context, nodeToUpdate, model, scp, stsmap );

		return nodeToUpdate.getXmlObject().toString();
	}

	private void getNextChild( XmlTreeNode node, SecurityTestRunContext context, XmlTreeNode nodeToUpdate,
			XmlObjectTreeModel model, SecurityCheckedParameter scp, StringToStringMap stsmap )
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
					createMutation( baseType, mynode, context, nodeToUpdate, model, scp, stsmap );
				}
			}
			getNextChild( mynode, context, nodeToUpdate, model, scp, stsmap );
		}
	}

	private void createMutation( String baseType, XmlTreeNode mynode, SecurityTestRunContext context,
			XmlTreeNode nodeToUpdate, XmlObjectTreeModel model, SecurityCheckedParameter scp, StringToStringMap stsmap )
	{

		String value = null;
		String nodeName = mynode.getNodeName();
		String nodeValue = mynode.getChild( 0 ).getNodeText();
		value = AbstractBoundary.outOfBoundaryValue( baseType, nodeName, nodeValue );

		if( value != null )
		{
			nodeToUpdate.setValue( 1, value );
			PropertyMutation pm = new PropertyMutation();
			pm.setPropertyName( scp.getName() );
			pm.setPropertyValue( model.getXmlObject().toString() );

			if( strategy.equals( StrategyTypeConfig.ONE_BY_ONE ) )
			{
				stsmap = new StringToStringMap();
				stsmap.put( scp.getLabel() + " (" + nodeName + "='" + nodeValue + "') ", value );
				pm.setMutatedParameters( stsmap );
				addMutation( context, pm );
			}
			else
			{
				stsmap.put( scp.getLabel() + " (" + nodeName + "='" + nodeValue + "') ", value );
			}

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
		{
			mynode.setValue( 1, value );
		}
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
			Stack<PropertyMutation> requestMutationsList = new Stack<PropertyMutation>();
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

		Stack<PropertyMutation> stack = ( Stack<PropertyMutation> )context.get( REQUEST_MUTATIONS_STACK );
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

	
	public class RestrictionLabel
	{
		private String text = "<html><pre>    </pre></html>";
		private JLabel jlabel = new JLabel();
		private int limit = 70;
		{
			setJlabel( text );
		}

		public void setJlabel( String text )
		{
			text = text.replace( "[", "" );
			text = text.replace( "]", "" );
			if( text.length() > limit )
			{
				jlabel.setToolTipText( text.length() < 250 ? text : text.substring( 0, 249 ) + " ... " );
				jlabel.setText( text.substring( 0, limit - 5 ) + " ... " );
			}
			else
			{
				jlabel.setText( text );
			}
		}

		public JLabel getJLabel()
		{
			return jlabel;
		}

	}

	public void refreshRestrictionLabel( int row )
	{
		if( row == -1 )
		{
			restrictionLabel.setJlabel( "<html><pre>    </pre></html>" );
			return;
		}
		SecurityCheckedParameter parameter = getParameterAt( row );
		String name = parameter.getName();
		String xpath = parameter.getXpath();
		TestProperty tp = getTestStep().getProperty( name );
		XmlObjectTreeModel xmlObjectTreeModel = null;
		if( tp.getSchemaType() != null && XmlUtils.seemsToBeXml( tp.getValue() ) )
		{
			try
			{
				xmlObjectTreeModel = new XmlObjectTreeModel( tp.getSchemaType().getTypeSystem(), XmlObject.Factory
						.parse( tp.getValue() ) );
			}
			catch( XmlException e )
			{
				SoapUI.logError( e );
			}

			XmlTreeNode[] treeNodes = xmlObjectTreeModel.selectTreeNodes( xpath );

			if( treeNodes.length == 0 )
			{
				restrictionLabel.setJlabel( "<html><pre>    </pre></html>" );
				return;
			}
			List<String> list = null;
			if( treeNodes[0].getSchemaType() != null && treeNodes[0].getSchemaType().getEnumerationValues() != null )
			{
				list = BoundaryRestrictionUtill.extractEnums( treeNodes[0] );
				restrictionLabel.setJlabel( list.toString().replaceFirst( ",", "" ) );
			}
			else
			{
				SchemaTypeImpl simpleType = ( SchemaTypeImpl )treeNodes[0].getSchemaType();
				XmlObjectTreeModel model2 = new XmlObjectTreeModel( simpleType.getTypeSystem(), simpleType.getParseObject() );
				list = BoundaryRestrictionUtill.getRestrictions( model2.getRootNode(), new ArrayList<String>() );
				if( list.isEmpty() )
				{
					list.add( "No restrictions in schema are specified for this parameter!" );
				}
				restrictionLabel.setJlabel( list.toString() );
			}

		}
		else
		{
			restrictionLabel.setJlabel( "<html><pre>    </pre></html>" );
		}
	}

}