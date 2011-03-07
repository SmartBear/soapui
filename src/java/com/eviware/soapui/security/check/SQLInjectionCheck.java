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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SQLInjectionCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

/**
 * This will test whether a targeted web page is vulnerable to reflected XSS
 * attacks
 * 
 * @author soapui team
 */

public class SQLInjectionCheck extends AbstractSecurityCheckWithProperties
{

	public static final String TYPE = "SQLInjectionCheck";

	private SQLInjectionCheckConfig sqlInjectionConfig;

	private boolean generated;

	private boolean hasNext;

	private Map<SecurityCheckedParameter, ArrayList<String>> parameterMutations = new HashMap<SecurityCheckedParameter, ArrayList<String>>();

	String[] defaultSqlInjectionStrings = { "' or '1'='1", "'--", "1'", "admin'--", "/*!10000%201/0%20*/",
			"/*!10000 1/0 */", "1/0", "'%20o/**/r%201/0%20--", "' o/**/r 1/0 --", ";", "'%20and%201=2%20--",
			"' and 1=2 --", "test�%20UNION%20select%201,%20@@version,%201,%201;�",
			"test� UNION select 1, @@version, 1, 1;�" };

	public SQLInjectionCheck( SecurityCheckConfig config, ModelItem parent, String icon, TestStep testStep )
	{
		super( testStep, config, parent, icon );
		if( config.getConfig() == null )
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
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof SamplerTestStep;
	}

	@Override
	public JComponent getComponent()
	{
		return new JLabel( "<html><pre>SQL String  <i>Default strings for SQL injection applied (can be changed under advanced settings)</i></pre></html>" );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		update( testStep );
		testStep.run( ( TestCaseRunner )securityTestRunner, context );
	}

	private void update( TestStep testStep )
	{
		if( !generated )
			mutateParameters();

	}

	private void mutateParameters()
	{
		if( !generated )
		{
			generated = true;
			hasNext = true;

			// for each parameter
			for( SecurityCheckedParameter parameter : getParameterHolder().getParameterList() )
			{

				TestProperty property = getTestStep().getProperties().get( parameter.getName() );
				// ignore if there is no value.
				if( property.getValue() == null && property.getDefaultValue() == null )
					continue;
				// get value of that property
				String value = property.getValue();
				// no xpath, just put values in property than.
				if( value == null )
				{
					for( String sqlInjectionString : sqlInjectionConfig.getSqlInjectionStringsList() )
					{

						if( !parameterMutations.containsKey( parameter ) )
							parameterMutations.put( parameter, new ArrayList<String>() );
						parameterMutations.get( parameter ).add( sqlInjectionString );

					}
				}

				// we have something that looks like xpath
				try
				{

					XmlObjectTreeModel model = new XmlObjectTreeModel( ( ( WsdlTestRequestStep )getTestStep() )
							.getOperation().getInterface().getDefinitionContext().getSchemaTypeSystem(), XmlObject.Factory
							.parse( value ) );

					XmlTreeNode[] nodes = model.selectTreeNodes( parameter.getXPath() );

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
				catch( Exception e1 )
				{
					SoapUI.logError( e1, "[SqlInjection]Failed to select XPath for source property value [" + value + "]" );
				}

			}

		}

	}

	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		boolean oldHasNext = hasNext;
		if( parameterMutations == null )
			hasNext = true;
		else
		{
			boolean haveMore = false;
			for( SecurityCheckedParameter param : parameterMutations.keySet() )
			{
				if( parameterMutations.get( param ).size() > 0 )
				{
					haveMore = true;
					break;
				}
			}
			hasNext = hasNext ? haveMore : true;
		}
		if( oldHasNext && !hasNext )
			generated = false;
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

}
