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
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JStringListFormField;

/**
 * This will test whether a targeted web page is vulnerable to reflected XSS
 * attacks
 * 
 * @author soapui team
 */

public class SQLInjectionCheck extends AbstractSecurityCheckWithProperties
{

	public static final String TYPE = "SQLInjectionCheck";
	public static final String NAME = "SQL Injection";

	private SQLInjectionCheckConfig sqlInjectionConfig;

	private Map<SecurityCheckedParameter, ArrayList<String>> parameterMutations = new HashMap<SecurityCheckedParameter, ArrayList<String>>();

	String[] defaultSqlInjectionStrings = { "' or '1'='1", "'--", "1'", "admin'--", "/*!10000%201/0%20*/",
			"/*!10000 1/0 */", "1/0", "'%20o/**/r%201/0%20--", "' o/**/r 1/0 --", ";", "'%20and%201=2%20--",
			"' and 1=2 --", "test�%20UNION%20select%201,%20@@version,%201,%201;�",
			"test� UNION select 1, @@version, 1, 1;�" };

	private boolean mutation;

	public SQLInjectionCheck( SecurityCheckConfig config, ModelItem parent, String icon, TestStep testStep )
	{
		super( testStep, config, parent, icon );
		if( config.getConfig() == null || !( config.getConfig() instanceof SQLInjectionCheckConfig ) )
			initSqlInjectionConfig();
		else
			sqlInjectionConfig = ( SQLInjectionCheckConfig )getConfig().getConfig();
	}

	private void initSqlInjectionConfig()
	{
		getConfig().setConfig( SQLInjectionCheckConfig.Factory.newInstance() );
		sqlInjectionConfig = ( SQLInjectionCheckConfig )getConfig().getConfig();

		sqlInjectionConfig.setSqlInjectionStringsArray( defaultSqlInjectionStrings );
	}

	@Override
	public void updateSecurityConfig( SecurityCheckConfig config )
	{
		super.updateSecurityConfig( config );

		if( sqlInjectionConfig != null )
		{
			sqlInjectionConfig = ( SQLInjectionCheckConfig )getConfig().getConfig();
		}
	}

	@Override
	public JComponent getComponent()
	{
		return new JLabel(
				"<html><pre>SQL String  <i>Default strings for SQL injection applied (can be changed under advanced settings)</i></pre></html>" );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		try
		{
			StringToStringMap updatedParams = update( testStep, context );
			MessageExchange message = ( MessageExchange )testStep.run( ( TestCaseRunner )securityTestRunner, context );
			createMessageExchange( updatedParams, message );
		}
		catch( XmlException e )
		{
			SoapUI.logError( e, "[SqlInjectionSecurityCheck]XPath seems to be invalid!" );
			reportSecurityCheckException( "Propety value is not XML or XPath is wrong!" );
		}
		catch( Exception e )
		{
			SoapUI.logError( e, "[SqlInjectionSecurityCheck]Property value is not valid xml!" );
			reportSecurityCheckException( "Propety value is not XML or XPath is wrong!" );
		}
	}

	private StringToStringMap update( TestStep testStep, SecurityTestRunContext context ) throws XmlException, Exception
	{
		StringToStringMap params = new StringToStringMap();

		if( parameterMutations.size() == 0 )
			mutateParameters( testStep, context );

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
						TestProperty property = getTestStep().getProperties().get( param.getName() );
						String value = context.expand( property.getValue() );
						if( param.getXpath() == null || param.getXpath().trim().length() == 0 )
						{
							testStep.getProperties().get( param.getName() )
									.setValue( parameterMutations.get( param ).get( 0 ) );
							params.put( param.getLabel(), parameterMutations.get( param ).get( 0 ) );
							parameterMutations.get( param ).remove( 0 );
						}
						else
						{
							// no value, do nothing.
							if( value == null || value.trim().equals( "" ) )
								continue;
							XmlObjectTreeModel model = new XmlObjectTreeModel( property.getSchemaType().getTypeSystem(),
									XmlObject.Factory.parse( value ) );
							XmlTreeNode[] nodes = model.selectTreeNodes( context.expand( param.getXpath() ) );
							for( XmlTreeNode node : nodes )
								node.setValue( 1, parameterMutations.get( param ).get( 0 ) );
							params.put( param.getLabel(), parameterMutations.get( param ).get( 0 ) );
							parameterMutations.get( param ).remove( 0 );

							testStep.getProperties().get( param.getName() ).setValue( model.getXmlObject().toString() );
						}
						break;
					}
			}
		}
		else
		{
			for( TestProperty property : testStep.getPropertyList() )
			{

				String value = context.expand( property.getValue() );
				XmlObjectTreeModel model = null;
				model = new XmlObjectTreeModel( property.getSchemaType().getTypeSystem(), XmlObject.Factory.parse( value ) );
				for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
				{
					if( param.getXpath() == null || param.getXpath().trim().length() == 0 )
					{
						testStep.getProperties().get( param.getName() ).setValue( parameterMutations.get( param ).get( 0 ) );
						params.put( param.getLabel(), parameterMutations.get( param ).get( 0 ) );
						parameterMutations.get( param ).remove( 0 );
					}
					else
					{
						// no value, do nothing.
						if( value == null || value.trim().equals( "" ) )
							continue;
						if( param.getName().equals( property.getName() ) )
						{
							XmlTreeNode[] nodes = model.selectTreeNodes( context.expand( param.getXpath() ) );
							if( parameterMutations.containsKey( param ) )
								if( parameterMutations.get( param ).size() > 0 )
								{
									for( XmlTreeNode node : nodes )
										node.setValue( 1, parameterMutations.get( param ).get( 0 ) );
									params.put( param.getLabel(), parameterMutations.get( param ).get( 0 ) );
									parameterMutations.get( param ).remove( 0 );
								}
						}
					}
				}
				if( model != null )
					property.setValue( model.getXmlObject().toString() );
			}
		}
		return params;
	}

	private void mutateParameters( TestStep testStep, SecurityTestRunContext context ) throws XmlException, Exception
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
				if( parameter.getXpath() == null || parameter.getXpath().trim().length() == 0 )
				{
					for( String sqlInjectionString : sqlInjectionConfig.getSqlInjectionStringsList() )
					{

						if( !parameterMutations.containsKey( parameter ) )
							parameterMutations.put( parameter, new ArrayList<String>() );
						parameterMutations.get( parameter ).add( sqlInjectionString );

					}
				}
				else
				{
					// we have xpath but do we have xml which need to mutate
					// ignore if there is no value, since than we'll get exception
					if( property.getValue() == null && property.getDefaultValue() == null )
						continue;
					// get value of that property
					String value = context.expand( property.getValue() );

					// we have something that looks like xpath, or hope so.

					XmlObjectTreeModel model = null;

					model = new XmlObjectTreeModel( property.getSchemaType().getTypeSystem(), XmlObject.Factory.parse( value ) );

					XmlTreeNode[] nodes = model.selectTreeNodes( context.expand( parameter.getXpath() ) );

					// for each invalid type set all nodes

					for( String sqlInjectionString : sqlInjectionConfig.getSqlInjectionStringsList() )
					{

						if( nodes.length > 0 )
						{
							if( !parameterMutations.containsKey( parameter ) )
								parameterMutations.put( parameter, new ArrayList<String>() );
							parameterMutations.get( parameter ).add( sqlInjectionString );
						}

					}

				}
			}
		}

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
	public String getConfigDescription()
	{
		return "Configures SQL injection security check";
	}

	@Override
	public String getConfigName()
	{
		return "SQL Injection Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}

	@Override
	public JComponent getAdvancedSettingsPanel()
	{
		JFormDialog dialog = ( JFormDialog )ADialogBuilder.buildDialog( AdvancedSettings.class );
		JStringListFormField stringField = ( JStringListFormField )dialog
				.getFormField( AdvancedSettings.INJECTION_STRINGS );
		stringField.setOptions( sqlInjectionConfig.getSqlInjectionStringsList().toArray() );
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
					sqlInjectionConfig.addSqlInjectionStrings( itemToAdd );
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
								sqlInjectionConfig.removeSqlInjectionStrings( cnt );
								break;
							}
						}
						else
						{
							// this is border case, last lement in array is removed.
							sqlInjectionConfig.removeSqlInjectionStrings( oldOptions.length - 1 );
						}
					}
				}
			}
		} );

		return dialog.getPanel();
	}

	@AForm( description = "SQL Injection Strings", name = "SQL Injection Strings" )
	protected interface AdvancedSettings
	{

		@AField( description = "SQL Strings", name = "###Injection Strings", type = AFieldType.STRINGLIST )
		public final static String INJECTION_STRINGS = "###Injection Strings";

	}
	
	@Override
	protected void clear()
	{
		parameterMutations.clear();
		mutation = false;
	}
}
