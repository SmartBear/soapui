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

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SQLInjectionCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.config.XPathInjectionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JStringListFormField;

public class XPathInjectionSecurityCheck extends AbstractSecurityCheckWithProperties
{

	public static final String TYPE = "XPathInjectionSecurityCheck";

	private XPathInjectionConfig xpathList;

	private Map<SecurityCheckedParameter, ArrayList<String>> parameterMutations = new HashMap<SecurityCheckedParameter, ArrayList<String>>();

	String[] defaultXPathInjectionStrings = { " or name(//users/LoginID[1]) = 'LoginID' or 'a'='b", "' or '1'='1",
			"1/0", "'%20o/**/r%201/0%20--", "' o/**/r 1/0 --", ";", "'%20and%201=2%20--", "' and 1=2 --",
			"test�%20UNION%20select%201,%20@@version,%201,%201;�", "test� UNION select 1, @@version, 1, 1;�" };

	private boolean mutation;

	public XPathInjectionSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );

		if( config.getConfig() == null || !( config.getConfig() instanceof SQLInjectionCheckConfig ) )
			initXPathInjectionConfig();
		else
			xpathList = ( XPathInjectionConfig )getConfig().getConfig();
	}

	private void initXPathInjectionConfig()
	{
		getConfig().setConfig( XPathInjectionConfig.Factory.newInstance() );
		xpathList = ( XPathInjectionConfig )getConfig().getConfig();

		xpathList.setXpathListArray( defaultXPathInjectionStrings );
	}


	@Override
	public JComponent getComponent()
	{
		return new JLabel(
				"<html><pre>XPath String  <i>Default strings for XPath injection applied (can be changed under advanced settings)</i></pre></html>" );
	}

	@Override
	protected void execute( SecurityTestRunner runner, TestStep testStep, SecurityTestRunContext context )
	{
		update( testStep );
		testStep.run( ( TestCaseRunner )runner, context );
		createMessageExchange( testStep );
	}

	private void createMessageExchange( TestStep testStep )
	{
		MessageExchange messageExchange = null;
		if( messageExchange instanceof WsdlTestRequestStep )
			messageExchange = new WsdlResponseMessageExchange( ( ( WsdlTestRequestStep )testStep ).getTestRequest() );
		else
			messageExchange = new RestResponseMessageExchange( ( ( RestTestRequestStep )testStep ).getTestRequest() );
		getSecurityCheckRequestResult().setMessageExchange( messageExchange );
	}

	private void update( TestStep testStep )
	{
		if( parameterMutations.size() == 0 )
			mutateParameters( testStep );

		if( getExecutionStrategy().getStrategy() == StrategyTypeConfig.ONE_BY_ONE )
		{
			/*
			 * Idea is to drain for each parameter mutations.
			 */
			for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
			{
				if( parameterMutations.containsKey( param ) )
					if( parameterMutations.get( param ).size() > 0 )
					{
						try
						{
							TestProperty property = getTestStep().getProperties().get( param.getName() );
							String value = property.getValue();
							if( param.getXPath() == null || param.getXPath().trim().length() == 0 )
							{
								testStep.getProperties().get( param.getName() ).setValue(
										parameterMutations.get( param ).get( 0 ) );
								parameterMutations.get( param ).remove( 0 );
							}
							else
							{
								// no value, do nothing.
								if( value == null || value.trim().equals( "" ) )
									continue;
								XmlObjectTreeModel model = new XmlObjectTreeModel( ( ( WsdlTestRequestStep )getTestStep() )
										.getOperation().getInterface().getDefinitionContext().getSchemaTypeSystem(),
										XmlObject.Factory.parse( value ) );
								XmlTreeNode[] nodes = model.selectTreeNodes( param.getXPath() );
								for( XmlTreeNode node : nodes )
									node.setValue( 1, parameterMutations.get( param ).get( 0 ) );
								parameterMutations.get( param ).remove( 0 );

								testStep.getProperties().get( param.getName() ).setValue( model.getXmlObject().toString() );
							}

						}
						catch( Exception e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
			}
		}
		else
		{
			for( TestProperty property : testStep.getPropertyList() )
			{

				String value = property.getValue();
				XmlObjectTreeModel model = null;
				try
				{
					model = new XmlObjectTreeModel( ( ( WsdlTestRequestStep )getTestStep() ).getOperation().getInterface()
							.getDefinitionContext().getSchemaTypeSystem(), XmlObject.Factory.parse( value ) );
					for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
					{
						if( param.getXPath() == null || param.getXPath().trim().length() == 0 )
						{
							testStep.getProperties().get( param.getName() )
									.setValue( parameterMutations.get( param ).get( 0 ) );
							parameterMutations.get( param ).remove( 0 );
						}
						else
						{
							// no value, do nothing.
							if( value == null || value.trim().equals( "" ) )
								continue;
							if( param.getName().equals( property.getName() ) )
							{
								XmlTreeNode[] nodes = model.selectTreeNodes( param.getXPath() );
								if( parameterMutations.containsKey( param ) )
									if( parameterMutations.get( param ).size() > 0 )
									{
										for( XmlTreeNode node : nodes )
											node.setValue( 1, parameterMutations.get( param ).get( 0 ) );
										parameterMutations.get( param ).remove( 0 );
									}
							}
						}
					}
					if( model != null )
						property.setValue( model.getXmlObject().toString() );
				}
				catch( XmlException e )
				{
					// TODO Auto-generated catch block
					// e.printStackTrace();
					continue;
				}
				catch( Exception e )
				{
					// TODO Auto-generated catch block
					// e.printStackTrace();
					continue;
				}

			}
		}
	}

	private void mutateParameters( TestStep testStep )
	{
		mutation = true;
		// for each parameter
		for( SecurityCheckedParameter parameter : getParameterHolder().getParameterList() )
		{
			if( parameter.isChecked() )
			{
				TestProperty property = testStep.getProperties().get( parameter.getName() );
				// check parameter does not have any xpath
				// than mutate whole parameter
				if( parameter.getXPath() == null || parameter.getXPath().trim().length() == 0 )
				{
					for( String xpathInjectionString : xpathList.getXpathListList() )
					{

						if( !parameterMutations.containsKey( parameter ) )
							parameterMutations.put( parameter, new ArrayList<String>() );
						parameterMutations.get( parameter ).add( xpathInjectionString );

					}
				}
				else
				{
					// we have xpath but do we have xml which need to mutate
					// ignore if there is no value, since than we'll get exception
					if( property.getValue() == null && property.getDefaultValue() == null )
						continue;
					// get value of that property
					String value = property.getValue();

					// we have something that looks like xpath, or hope so.
					try
					{

						XmlObjectTreeModel model = null;

						if( testStep instanceof WsdlTestRequestStep )
							model = new XmlObjectTreeModel( ( ( WsdlTestRequestStep )testStep ).getOperation().getInterface()
									.getDefinitionContext().getSchemaTypeSystem(), XmlObject.Factory.parse( value ) );
						if( testStep instanceof RestTestRequestStep || testStep instanceof HttpTestRequestStep )
							model = new XmlObjectTreeModel( XmlObject.Factory.parse( value ) );

						XmlTreeNode[] nodes = model.selectTreeNodes( parameter.getXPath() );

						// for each invalid type set all nodes

						for( String xpathInjectionString : xpathList.getXpathListList() )
						{

							if( nodes.length > 0 )
							{
								if( !parameterMutations.containsKey( parameter ) )
									parameterMutations.put( parameter, new ArrayList<String>() );
								parameterMutations.get( parameter ).add( xpathInjectionString );
							}

						}
					}
					catch( Exception e1 )
					{
						SoapUI.logError( e1, "[XPathInjection]Failed to select XPath for source property value [" + value
								+ "]" );
					}

				}
			}
		}

	}

	@Override
	public String getConfigDescription()
	{
		return "Configures XPath Injection Security Check";
	}

	@Override
	public String getConfigName()
	{
		return "XPath Injection Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		boolean hasNext = false;
		if( ( parameterMutations == null || parameterMutations.size() == 0 ) && !mutation )
		{
			if( getParameterHolder().getParameterList().size() > 0 )
				hasNext = true;
			else
				hasNext = false;
		}
		else
		{
			for( SecurityCheckedParameter param : parameterMutations.keySet() )
			{
				if( parameterMutations.get( param ).size() > 0 )
				{
					hasNext = true;
					break;
				}
			}
		}
		if( !hasNext )
		{
			parameterMutations.clear();
			mutation = false;
		}
		return hasNext;
	}

	@Override
	public JComponent getAdvancedSettingsPanel()
	{
		JFormDialog dialog = ( JFormDialog )ADialogBuilder.buildDialog( AdvancedSettings.class );
		JStringListFormField stringField = ( JStringListFormField )dialog
				.getFormField( AdvancedSettings.INJECTION_STRINGS );
		stringField.setOptions( xpathList.getXpathListList().toArray() );
		stringField.setProperty( "dimension", new Dimension( 470, 150 ) );
		stringField.getComponent().addPropertyChangeListener( "options", new PropertyChangeListener()
		{

			@Override
			public void propertyChange( PropertyChangeEvent evt )
			{
				String[] newOptions = ( String[] )evt.getNewValue();
				String[] oldOptions = ( String[] )evt.getOldValue();
				// added
				if( newOptions.length > oldOptions.length )
				{
					// new element is always added to the end
					String[] newValue = ( String[] )evt.getNewValue();
					String itemToAdd = newValue[newValue.length - 1];
					xpathList.addXpathList( itemToAdd );
				}
				// removed
				if( newOptions.length < oldOptions.length )
				{
					/*
					 * items with same index should me same. first one in oldOptions
					 * that does not match is element that is removed.
					 */
					for( int cnt = 0; cnt < oldOptions.length; cnt++ )
					{
						if( cnt < newOptions.length )
						{
							if( newOptions[cnt] != oldOptions[cnt] )
							{
								xpathList.removeXpathList( cnt );
								break;
							}
						}
						else
						{
							// this is border case, last lement in array is removed.
							xpathList.removeXpathList( oldOptions.length - 1 );
						}
					}
				}
			}
		} );

		return dialog.getPanel();
	}

	@AForm( description = "XPath Injection Strings", name = "XPath Injection Strings" )
	protected interface AdvancedSettings
	{

		@AField( description = "XPath Strings", name = "###Injection Strings", type = AFieldType.STRINGLIST )
		public final static String INJECTION_STRINGS = "###Injection Strings";

	}
}
