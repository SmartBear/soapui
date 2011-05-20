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

package com.eviware.soapui.security.scan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MaliciousAttachmentConfig;
import com.eviware.soapui.config.MaliciousAttachmentElementConfig;
import com.eviware.soapui.config.MaliciousAttachmentSecurityScanConfig;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.tools.RandomFile;
import com.eviware.soapui.security.ui.MaliciousAttachmentAdvancedSettingsPanel;
import com.eviware.soapui.security.ui.MaliciousAttachmentMutationsPanel;
import com.eviware.soapui.support.UISupport;

public class MaliciousAttachmentSecurityScan extends AbstractSecurityScan
{
	
	public static final String TYPE = "MaliciousAttachmentSecurityScan";
	public static final String NAME = "Malicious Attachment";

	private MaliciousAttachmentSecurityScanConfig config;

	private MaliciousAttachmentAdvancedSettingsPanel advancedSettingsPanel;
	private MaliciousAttachmentMutationsPanel mutationsPanel;

	private int elementIndex = -1;
	private int valueIndex = -1;

	public MaliciousAttachmentSecurityScan( SecurityScanConfig newConfig, ModelItem parent, String icon,
			TestStep testStep )
	{
		super( testStep, newConfig, parent, icon );

		if( newConfig.getConfig() == null || !( newConfig.getConfig() instanceof MaliciousAttachmentSecurityScanConfig ) )
		{
			initConfig();
		}
		else
		{
			config = ( ( MaliciousAttachmentSecurityScanConfig )newConfig.getConfig() );
		}
	}

	/**
	 * Default malicious attachment configuration
	 */
	protected void initConfig()
	{
		getConfig().setConfig( MaliciousAttachmentSecurityScanConfig.Factory.newInstance() );
		config = ( MaliciousAttachmentSecurityScanConfig )getConfig().getConfig();
	}

	private void generateFiles()
	{
		if( config != null )
		{
			for( MaliciousAttachmentElementConfig element : config.getElementList() )
			{
				for( MaliciousAttachmentConfig value : element.getGenerateAttachmentList() )
				{
					File file = new File( value.getFilename() );

					try
					{
						if( !file.exists() || file.length() == 0 )
						{
							file = new RandomFile( value.getSize(), value.getFilename(), value.getContentType() ).next();
						}
					}
					catch( IOException e )
					{
						SoapUI.logError( e );
					}
				}
			}
		}
	}

	@Override
	public void updateSecurityConfig( SecurityScanConfig config )
	{
		super.updateSecurityConfig( config );

		if( this.config != null )
		{
			this.config = ( MaliciousAttachmentSecurityScanConfig )getConfig().getConfig();
		}

		if( advancedSettingsPanel != null )
		{
			advancedSettingsPanel.setConfig( ( MaliciousAttachmentSecurityScanConfig )getConfig().getConfig() );
		}

		if( mutationsPanel != null )
		{
			mutationsPanel.updateConfig( ( MaliciousAttachmentSecurityScanConfig )getConfig().getConfig() );
		}
	}

	public MaliciousAttachmentSecurityScanConfig getMaliciousAttachmentSecurityScanConfig()
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
			if( elementIndex == -1 )
			{
				elementIndex++ ;
			}

			if( elementIndex < config.getElementList().size() )
			{

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
			if( elementIndex == -1 )
			{
				elementIndex++ ;
			}

			for( MaliciousAttachmentElementConfig element : config.getElementList() )
			{
				if( element.getRemove() )
				{
					removeAttachments( testStep, element.getKey(), true );
				}

				int valIndex = valueIndex;

				if( valIndex < element.getGenerateAttachmentList().size() + element.getReplaceAttachmentList().size() - 1 )
				{
					valIndex++ ;
					addAttachments( testStep, element, valIndex );
				}
			}
			elementIndex++ ;
			valueIndex++ ;
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
					UISupport.showErrorMessage( "Missing file: " + file.getName() );
					return;
				}

				addAttachment( testStep, file, value.getContentType(), generated, value.getCached() );
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
			generateFiles();
			updateRequestContent( testStep, context );
			MessageExchange message = ( MessageExchange )testStep.run( ( TestCaseRunner )securityTestRunner, context );
			getSecurityScanRequestResult().setMessageExchange( message );
		}
		catch( Exception e )
		{
			SoapUI.logError( e, "[MaliciousAttachmentSecurityScan]Property value is not valid xml!" );
			reportSecurityScanException( "Property value is not XML or XPath is wrong!" );
		}
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	private Attachment addAttachment( TestStep testStep, File file, String contentType, boolean generated, boolean cache )
			throws IOException
	{
		WsdlRequest request = ( WsdlRequest )getRequest( testStep );
		request.setInlineFilesEnabled( false );
		Attachment attach = request.attachFile( file, cache );
		attach.setContentType( contentType );

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
		return "http://soapui.org/Security/malicious-attachment.html";
	}

	@Override
	public JComponent getAdvancedSettingsPanel()
	{
		if( advancedSettingsPanel == null )
			advancedSettingsPanel = new MaliciousAttachmentAdvancedSettingsPanel( config );

		return advancedSettingsPanel.getPanel();
	}

	@Override
	public void copyConfig( SecurityScanConfig config )
	{
		super.copyConfig( config );

		if( advancedSettingsPanel != null )
		{
			advancedSettingsPanel.setConfig( ( MaliciousAttachmentSecurityScanConfig )getConfig().getConfig() );
		}

		if( mutationsPanel != null )
		{
			mutationsPanel.updateConfig( ( MaliciousAttachmentSecurityScanConfig )getConfig().getConfig() );
		}
	}

	@Override
	public void release()
	{
		if( advancedSettingsPanel != null )
			advancedSettingsPanel.release();

		if( mutationsPanel != null )
			mutationsPanel.release();

		super.release();
	}

}
