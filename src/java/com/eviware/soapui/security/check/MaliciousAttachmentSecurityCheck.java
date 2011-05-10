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
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
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

	private int currentIndex = 0;

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

		getExecutionStrategy().setImmutable( true );
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

		if( advancedSettingsPanel != null )
		{
			advancedSettingsPanel.setConfig( ( MaliciousAttachmentSecurityCheckConfig )getConfig().getConfig() );
		}

		if( mutationsPanel != null )
		{
			mutationsPanel.updateConfig( ( MaliciousAttachmentSecurityCheckConfig )getConfig().getConfig() );
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
		setRequestTimeout( testStep, config.getRequestTimeout() );

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
				addAttachments( testStep, element.getGenerateAttachmentList(), true );
				addAttachments( testStep, element.getReplaceAttachmentList(), false );
			}
		}
	}

	private void addAttachments( TestStep testStep, List<MaliciousAttachmentConfig> list, boolean generated )
	{
		for( MaliciousAttachmentConfig element : list )
		{
			File file = new File( element.getFilename() );

			if( element.getEnabled() )
			{
				try
				{
					if( !file.exists() )
					{
						if( generated )
						{
							file = new RandomFile( element.getSize(), "attachment", element.getContentType() ).next();
						}
						else
						{
							UISupport.showErrorMessage( "Missing file: " + file.getName() );
							return;
						}
					}

					addAttachment( testStep, file, element.getContentType(), generated );
				}
				catch( IOException e )
				{
					SoapUI.logError( e );
				}
			}
		}
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		try
		{
			updateRequestContent( testStep, context );
			WsdlTestRequestStepResult message = ( WsdlTestRequestStepResult )testStep.run(
					( TestCaseRunner )securityTestRunner, context );
			message.setRequestContent( "", false );
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

	private Attachment addAttachment( TestStep testStep, File file, String contentType, boolean generated )
			throws IOException
	{
		WsdlRequest request = ( WsdlRequest )getRequest( testStep );
		request.setInlineFilesEnabled( false );
		Attachment attach = request.attachFile( file, false );
		attach.setContentType( contentType );

		if( generated )
		{
			file.deleteOnExit();
		}

		return attach;
	}

	private void removeAttachment( TestStep testStep, String id )
	{
		WsdlRequest request = ( WsdlRequest )getRequest( testStep );
		List<Attachment> toRemove = new ArrayList<Attachment>();

		for( Attachment attachment : request.getAttachments() )
		{
			if( attachment.getId().equals( id ) )
			{
				toRemove.add( attachment );
			}
		}
		for( Attachment remove : toRemove )
		{
			request.removeAttachment( remove );
		}

	}

	private void setRequestTimeout( TestStep testStep, int timeout )
	{
		WsdlRequest request = ( WsdlRequest )getRequest( testStep );
		request.setTimeout( String.valueOf( timeout ) );

	}

	@Override
	public JComponent getComponent()
	{
		if( mutationsPanel == null )
			mutationsPanel = new MaliciousAttachmentMutationsPanel( config, ( WsdlRequest )getRequest( getTestStep() ) );

		return mutationsPanel.getPanel();
	}

	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		boolean hasNext = currentIndex++ < 1;

		if( !hasNext )
		{
			currentIndex = 0;
		}

		return hasNext;
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

	@Override
	public void copyConfig( SecurityCheckConfig config )
	{
		super.copyConfig( config );

		if( advancedSettingsPanel != null )
		{
			advancedSettingsPanel.setConfig( ( MaliciousAttachmentSecurityCheckConfig )getConfig().getConfig() );
		}

		if( mutationsPanel != null )
		{
			mutationsPanel.updateConfig( ( MaliciousAttachmentSecurityCheckConfig )getConfig().getConfig() );
		}
	}
}
