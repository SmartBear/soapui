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
import com.eviware.soapui.config.StrategyTypeConfig;
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

	private int elementIndex = -1;
	private int valueIndex = -1;

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
	 * Set attachments. Strategy determines the number of existing attachments
	 * used (one/all)
	 */
	private void updateRequestContent( TestStep testStep, SecurityTestRunContext context )
	{
		if( config.getRequestTimeout() > 0 )
		{
			setRequestTimeout( testStep, config.getRequestTimeout() );
		}

		if( getExecutionStrategy().getStrategy() == StrategyTypeConfig.ONE_BY_ONE )
		{
			if( elementIndex < config.getElementList().size() )
			{
				if( elementIndex == -1 )
				{
					elementIndex++ ;
				}

				MaliciousAttachmentElementConfig element = config.getElementList().get( elementIndex );

				removeAttachments( testStep, element.getKey(), false );
				if( element.getRemove() )
				{
					removeAttachments( testStep, element.getKey(), true );
				}

				if( valueIndex < element.getGenerateAttachmentList().size() + element.getReplaceAttachmentList().size() - 1 )
				{
					valueIndex++ ;

					addAttachments( testStep, element, valueIndex );
				}

				if( valueIndex == element.getGenerateAttachmentList().size() + element.getReplaceAttachmentList().size()
						- 1 )
				{
					valueIndex = -1;
					elementIndex++ ;
				}
			}
		}
		else if( getExecutionStrategy().getStrategy() == StrategyTypeConfig.ALL_AT_ONCE )
		{
			for( MaliciousAttachmentElementConfig element : config.getElementList() )
			{
				if( elementIndex == -1 )
				{
					elementIndex++ ;
				}

				if( element.getRemove() )
				{
					removeAttachments( testStep, element.getKey(), true );
				}

				if( valueIndex < element.getGenerateAttachmentList().size() + element.getReplaceAttachmentList().size() - 1 )
				{
					valueIndex++ ;

					addAttachments( testStep, element, valueIndex );
				}

				if( valueIndex == element.getGenerateAttachmentList().size() + element.getReplaceAttachmentList().size()
						- 1 )
				{
					valueIndex = -1;
					elementIndex++ ;
				}
			}
		}
	}

	private void addAttachments( TestStep testStep, MaliciousAttachmentElementConfig element, int counter )
	{
		if( counter == -1 )
		{
			return;
		}

		boolean generated = false;
		List<MaliciousAttachmentConfig> list = null;

		if( counter < element.getGenerateAttachmentList().size() )
		{
			generated = true;
			list = element.getGenerateAttachmentList();
		}
		else
		{
			list = element.getReplaceAttachmentList();
			counter = counter - element.getGenerateAttachmentList().size();
		}

		MaliciousAttachmentConfig value = list.get( counter );
		File file = new File( value.getFilename() );

		if( value.getEnabled() )
		{
			try
			{
				if( !file.exists() )
				{
					if( generated )
					{
						file = new RandomFile( value.getSize(), "attachment", value.getContentType() ).next();
					}
					else
					{
						UISupport.showErrorMessage( "Missing file: " + file.getName() );
						return;
					}
				}

				addAttachment( testStep, file, value.getContentType(), generated );
			}
			catch( IOException e )
			{
				SoapUI.logError( e );
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

	private void removeAttachments( TestStep testStep, String key, boolean equals )
	{
		WsdlRequest request = ( WsdlRequest )getRequest( testStep );
		List<Attachment> toRemove = new ArrayList<Attachment>();

		for( Attachment attachment : request.getAttachments() )
		{
			if( equals )
			{
				if( attachment.getId().equals( key ) )
				{
					toRemove.add( attachment );
				}
			}
			else
			{
				if( !attachment.getId().equals( key ) )
				{
					toRemove.add( attachment );
				}
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
		boolean hasNext = elementIndex < config.getElementList().size();

		if( !hasNext )
		{
			elementIndex = -1;
			valueIndex = -1;
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
