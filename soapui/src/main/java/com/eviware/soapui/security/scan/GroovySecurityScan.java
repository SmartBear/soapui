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

package com.eviware.soapui.security.scan;

import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.GroovySecurityScanConfig;
import com.eviware.soapui.config.ScriptConfig;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.ui.GroovySecurityScanPanel;
import com.eviware.soapui.security.ui.SecurityScanConfigPanel;
import com.eviware.soapui.support.SecurityScanUtil;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

/**
 * 
 * @author soapui team
 */

public class GroovySecurityScan extends AbstractSecurityScanWithProperties
{
	
	public static final String SCRIPT_PROPERTY = GroovySecurityScan.class.getName() + "@script";
	public static final String TYPE = "GroovySecurityScan";
	public static final String NAME = "Custom Script";
	private GroovySecurityScanConfig groovyscc;
	private Boolean hasNext = true;
	private Object scriptResult;
	private SoapUIScriptEngine scriptEngine;

	private StringToStringMap parameters;
	// private TestStepResult stepResult;

	// private TestProperty response;

	private static final String PARAMETERS_INITIALIZED = "parameterInitialized";

	public GroovySecurityScan( TestStep testStep, SecurityScanConfig config, ModelItem parent, String icon )
	{

		super( testStep, config, parent, icon );
		if( config.getConfig() == null )
		{
			groovyscc = GroovySecurityScanConfig.Factory.newInstance();
			groovyscc.setExecuteScript( ScriptConfig.Factory.newInstance() );
			groovyscc.getExecuteScript().setLanguage( "groovy" );
			groovyscc.getExecuteScript().setStringValue( "" );
			config.setConfig( groovyscc );
		}
		else
		{
			groovyscc = ( GroovySecurityScanConfig )config.getConfig();
			if( groovyscc.getExecuteScript() == null )
			{
				groovyscc.setExecuteScript( ScriptConfig.Factory.newInstance() );
				groovyscc.getExecuteScript().setLanguage( "groovy" );
				groovyscc.getExecuteScript().setStringValue( "" );
			}
		}

		scriptEngine = SoapUIScriptEngineRegistry.create( this );

		getExecutionStrategy().setImmutable( true );
	}

	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		if( !context.hasProperty( PARAMETERS_INITIALIZED ) )
		{
			parameters = new StringToStringMap();
			initParameters( parameters );
			context.put( PARAMETERS_INITIALIZED, "true" );
			hasNext = true;
		}

		if( !hasNext )
		{
			context.remove( PARAMETERS_INITIALIZED );
			scriptEngine.clearVariables();
		}

		return hasNext;
	}

	private void initParameters( StringToStringMap parameters2 )
	{
		List<SecurityCheckedParameter> scpList = getParameterHolder().getParameterList();
		for( SecurityCheckedParameter scp : scpList )
		{
			parameters.put( scp.getLabel(), null );
		}
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		scriptEngine.setScript( groovyscc.getExecuteScript().getStringValue() );
		scriptEngine.setVariable( "context", context );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "securityScan", this );
		scriptEngine.setVariable( "parameters", parameters );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );

		try
		{
			scriptResult = scriptEngine.run();
			hasNext = castResultToBoolean( scriptResult );
			XmlObjectTreeModel model = null;
			for( SecurityCheckedParameter scp : getParameterHolder().getParameterList() )
			{
				if( parameters.containsKey( scp.getLabel() ) && parameters.get( scp.getLabel() ) != null )
				{
					if( scp.isChecked() && scp.getXpath().trim().length() > 0 )
					{
						model = SecurityScanUtil.getXmlObjectTreeModel( testStep, scp );
						XmlTreeNode[] treeNodes = null;
						treeNodes = model.selectTreeNodes( context.expand( scp.getXpath() ) );
						if( treeNodes.length > 0 )
						{
							XmlTreeNode mynode = treeNodes[0];
							mynode.setValue( 1, parameters.get( scp.getLabel() ) );
						}
						updateRequestProperty( testStep, scp.getName(), model.getXmlObject().toString() );

					}
					else
					{
						updateRequestProperty( testStep, scp.getName(), parameters.get( scp.getLabel() ) );
					}
				}
				else if( parameters.containsKey( scp.getLabel() ) && parameters.get( scp.getLabel() ) == null )
				{// clears null values form parameters
					parameters.remove( scp.getLabel() );
				}

			}

			MessageExchange message = ( MessageExchange )testStep.run( ( TestCaseRunner )securityTestRunner, context );
			createMessageExchange( clearNullValues( parameters ), message, context );

		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			hasNext = false;
		}
		finally
		{
			// if( scriptResult != null )
			// {
			// getTestStep().getProperty( "Request" ).setValue( ( String
			// )scriptResult );
			//
			// getTestStep().run( ( TestCaseRunner )securityTestRunner,
			// ( TestCaseRunContext )securityTestRunner.getRunContext() );
			// }

		}

	}

	private Boolean castResultToBoolean( Object scriptResult2 )
	{
		try
		{
			hasNext = ( Boolean )scriptResult2;
			if( hasNext == null )
			{
				hasNext = false;
				SoapUI.ensureGroovyLog().error( "You must return Boolean value from groovy script!" );
			}
		}
		catch( Exception e )
		{
			hasNext = false;
			SoapUI.ensureGroovyLog().error( "You must return Boolean value from groovy script!" );
		}
		return hasNext;
	}

	private StringToStringMap clearNullValues( StringToStringMap parameters )
	{
		StringToStringMap params = new StringToStringMap();
		for( String key : parameters.keySet() )
		{
			if( parameters.get( key ) != null )
				params.put( key, parameters.get( key ) );
		}
		return params;
	}

	private void updateRequestProperty( TestStep testStep, String propertyName, String propertyValue )
	{
		testStep.getProperty( propertyName ).setValue( propertyValue );

	}

	public void setExecuteScript( String script )
	{
		String old = getExecuteScript();
		groovyscc.getExecuteScript().setStringValue( script );
		notifyPropertyChanged( SCRIPT_PROPERTY, old, script );
	}

	public String getExecuteScript()
	{
		return groovyscc.getExecuteScript().getStringValue();
	}

	@Override
	public SecurityScanConfigPanel getComponent()
	{
		return new GroovySecurityScanPanel( this );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public String getConfigDescription()
	{
		return "Configuration for Custom Script Security Scan";
	}

	@Override
	public String getConfigName()
	{
		return "Configuration for Custom Script Security Scan";
	}

	@Override
	public String getHelpURL()
	{
		return "http://soapui.org/Security/script-custom-scan.html";
	}

}
