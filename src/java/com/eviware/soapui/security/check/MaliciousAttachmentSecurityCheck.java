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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MaliciousAttachmentConfig;
import com.eviware.soapui.config.MaliciousAttachmentElementConfig;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.tools.RandomFile;
import com.eviware.soapui.security.ui.MaliciousAttachmentAdvancedSettingsPanel;
import com.eviware.soapui.security.ui.MaliciousAttachmentMutationsPanel;
import com.eviware.soapui.support.UISupport;

public class MaliciousAttachmentSecurityCheck extends AbstractSecurityCheck
{

	public static final String TYPE = "MaliciousAttachmentSecurityCheck";
	public static final String NAME = "Malicious Attachment";

	private MaliciousAttachmentSecurityCheckConfig config;

	private MaliciousAttachmentAdvancedSettingsPanel advancedSettingsPanel;
	private MaliciousAttachmentMutationsPanel mutationsPanel;

	private boolean run = false;

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

	/*
	 * Set attachments
	 */
	private void updateRequestContent( TestStep testStep, SecurityTestRunContext context )
	{
		// add attachments
		for( MaliciousAttachmentElementConfig element : config.getElementList() )
		{
			// remove all attachments
			if( element.getRemove() )
			{
				removeAttachment( testStep, element.getKey() );
			}
			else
			{
				// first, remove original attachments
				removeAttachment( testStep, element.getKey() );

				// then, add specified ones
				addAttachments( testStep, element.getGenerateAttachmentList() );
				addAttachments( testStep, element.getReplaceAttachmentList() );
			}
		}
	}

	private void addAttachments( TestStep testStep, List<MaliciousAttachmentConfig> list )
	{
		for( MaliciousAttachmentConfig element : list )
		{
			File file = new File( element.getFilename() );

			try
			{
				if( !file.exists() )
				{
					file = new RandomFile( element.getSize(), "attachment", element.getContentType() ).next();
				}
				addAttachment( testStep, file, element.getContentType() );
			}
			catch( IOException e )
			{
				UISupport.showErrorMessage( e );
			}
		}
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		try
		{
			updateRequestContent( testStep, context );
			getTestStep().run( ( TestCaseRunner )securityTestRunner,
					( TestCaseRunContext )securityTestRunner.getRunContext() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e, "[MaliciousAttachmentSecurityScan]Property value is not valid xml!" );
			reportSecurityCheckException( "Property value is not XML or XPath is wrong!" );
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

		Attachment attach = request.attachFile( file, true );
		attach.setContentType( contentType );
		file.deleteOnExit();

		return attach;
	}

	private void removeAttachment( TestStep testStep, String name )
	{
		AbstractHttpRequest<?> request = ( AbstractHttpRequest<?> )getRequest( testStep );
		List<Attachment> toRemove = new ArrayList<Attachment>();

		for( Attachment attachment : request.getAttachments() )
		{
			if( attachment.getName().equals( name ) )
			{
				toRemove.add( attachment );
			}
		}
		for( Attachment remove : toRemove )
		{
			request.removeAttachment( remove );
		}

	}

	@Override
	public JComponent getComponent()
	{
		if( mutationsPanel == null )
			mutationsPanel = new MaliciousAttachmentMutationsPanel( config, getTestStep() );

		return mutationsPanel.getPanel();
	}

	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		if( run )
		{
			run = false;
		}
		else
		{
			run = true;
		}
		return run;
	}

	@Override
	public String getConfigDescription()
	{
		return "Configures malicious attachment security scan";
	}

	@Override
	public String getConfigName()
	{
		return "Malicious Attachment Security Scan";
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
