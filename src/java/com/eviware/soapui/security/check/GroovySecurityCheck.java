/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.GroovySecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.monitor.HttpSecurityAnalyser;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.SimpleForm;

/**
 * 
 * @author soapui team
 */

public class GroovySecurityCheck extends AbstractSecurityCheck implements HttpSecurityAnalyser
{
	public static final String SCRIPT_PROPERTY = GroovySecurityCheck.class.getName() + "@script";
	public static final String TYPE = "GroovySecurityCheck";
	//if this is a text area document listener doesn't work, WHY? !!
	protected JTextField scriptTextArea;
	protected static final String SCRIPT_FIELD = "Script";
	private static final String checkTitle = "GroovyScript Check";

	public GroovySecurityCheck( SecurityCheckConfig config, ModelItem parent, String icon)
	{
		super( config, parent, icon );
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
			GroovySecurityCheckConfig groovyscc = GroovySecurityCheckConfig.Factory.newInstance();
			config.setConfig( groovyscc );
		}

	}

	@Override
	protected void execute( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog )
	{
		scriptEngine.setScript( getScript() );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		scriptEngine.setVariable( "context", context );

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			scriptEngine.clearVariables();
		}
	}

	public void setScript( String script )
	{
		String old = getScript();
		if( getConfig().getConfig() == null )
		{
			getConfig().addNewConfig();
		}
		GroovySecurityCheckConfig groovyscc = GroovySecurityCheckConfig.Factory.newInstance();
		groovyscc.addNewScript();
		groovyscc.getScript().setStringValue( script );
		getConfig().setConfig( groovyscc );
		notifyPropertyChanged( SCRIPT_PROPERTY, old, script );
	}

	private String getScript()
	{
		GroovySecurityCheckConfig groovyscc = null;
		if( getConfig().getConfig() != null )
		{
			groovyscc = ( GroovySecurityCheckConfig )getConfig().getConfig();
			if( groovyscc.getScript() != null )
				return groovyscc.getScript().getStringValue();
		}
		return "";
	}

	@Override
	public void analyze( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog )
	{
		

	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return true;
	}

	@Override
	public JComponent getComponent()
	{
		// if (panel == null) {
		panel = new JPanel( new BorderLayout() );

		form = new SimpleForm();
		form.addSpace( 5 );

		// form.setDefaultTextFieldColumns( 50 );

		scriptTextArea = form.appendTextField( SCRIPT_FIELD, "Script to use" );
		scriptTextArea.setSize( new Dimension( 400, 600 ) );
		scriptTextArea.setText( getScript() );
		scriptTextArea.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{

			@Override
			public void update( Document document )
			{
				String scriptStr = form.getComponentValue( SCRIPT_FIELD );
				if( !StringUtils.isNullOrEmpty( scriptStr ) )
				{
					setScript( scriptStr );
				}
			}
		} );
		panel.add( form.getPanel() );
		// }
		return panel;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public void analyzeHttpConnection(MessageExchange messageExchange,
			JSecurityTestRunLog securityTestLog) {
		scriptEngine.setScript( getScript() );
		scriptEngine.setVariable( "testStep", null );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		scriptEngine.setVariable( "context", null );
		scriptEngine.setVariable("messageExchange", messageExchange);

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			scriptEngine.clearVariables();
		}
		
	}

	@Override
	public boolean canRun() {

		return true;
	}
	
	@Override
	public String getTitle()
	{
		return checkTitle;
	}
}
