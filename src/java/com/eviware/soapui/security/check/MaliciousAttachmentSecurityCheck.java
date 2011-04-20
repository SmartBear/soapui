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

import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.ui.MaliciousAttachmentAdvancedSettingsPanel;
import com.eviware.soapui.security.ui.MaliciousAttachmentMutationsPanel;
import com.eviware.soapui.support.types.StringToStringMap;

public class MaliciousAttachmentSecurityCheck extends AbstractSecurityCheck
{

	public static final String TYPE = "MaliciousAttachmentSecurityCheck";
	public static final String NAME = "Malicious Attachment";

	private MaliciousAttachmentSecurityCheckConfig config;

	private MaliciousAttachmentAdvancedSettingsPanel advancedSettingsPanel;
	private MaliciousAttachmentMutationsPanel mutationsPanel;

	public MaliciousAttachmentSecurityCheck( SecurityCheckConfig newConfig, ModelItem parent, String icon,
			TestStep testStep )
	{
		super( testStep, newConfig, parent, icon );

		if( newConfig.getConfig() == null || !( newConfig.getConfig() instanceof MaliciousAttachmentSecurityCheckConfig ) )
		{
			initConfig();
		}
		else
		{
			config = ( ( MaliciousAttachmentSecurityCheckConfig )newConfig.getConfig() );
		}
	}

	/**
	 * Default malicious attachment configuration
	 */
	protected void initConfig()
	{
		getConfig().setConfig( MaliciousAttachmentSecurityCheckConfig.Factory.newInstance() );
		config = ( MaliciousAttachmentSecurityCheckConfig )getConfig().getConfig();
	}

	@Override
	public void updateSecurityConfig( SecurityCheckConfig config )
	{
		super.updateSecurityConfig( config );

		if( this.config != null )
		{
			this.config = ( MaliciousAttachmentSecurityCheckConfig )getConfig().getConfig();
		}
	}

	public MaliciousAttachmentSecurityCheckConfig getMaliciousAttachmentSecurityCheckConfig()
	{
		return config;
	}

	protected StringToStringMap update( TestStep testStep, SecurityTestRunContext context ) throws Exception
	{
		return null;

	}

	@Override
	protected void execute( SecurityTestRunner runner, TestStep testStep, SecurityTestRunContext context )
	{
		try
		{
			StringToStringMap paramsUpdated = update( testStep, context );
			MessageExchange message = ( MessageExchange )testStep.run( ( TestCaseRunner )runner, context );
			// createMessageExchange( paramsUpdated, message );
		}
		catch( Exception e )
		{
			SoapUI.logError( e, "[MaliciousAttachmentSecurityCheck]Property value is not valid xml!" );
			reportSecurityCheckException( "Propety value is not XML or XPath is wrong!" );
		}
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	private Attachment addAttachment( TestStep testStep, File file, String contentType ) throws IOException
	{
		AbstractHttpRequest<?> request = ( AbstractHttpRequest<?> )getRequest( testStep );

		Attachment attach = request.attachFile( file, false );
		attach.setContentType( contentType );

		return attach;
	}

	@Override
	public JComponent getComponent()
	{
		if( mutationsPanel == null )
			mutationsPanel = new MaliciousAttachmentMutationsPanel( config );

		return mutationsPanel.getPanel();
	}

	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getConfigDescription()
	{
		return "Configures malicious attachment security check";
	}

	@Override
	public String getConfigName()
	{
		return "Malicious Attachment Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}

	@Override
	public JComponent getAdvancedSettingsPanel()
	{
		if( advancedSettingsPanel == null )
			advancedSettingsPanel = new MaliciousAttachmentAdvancedSettingsPanel( config );

		return advancedSettingsPanel.getPanel();
	}

}
