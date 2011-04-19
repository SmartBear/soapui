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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;

public class MaliciousAttachmentSecurityCheck extends AbstractSecurityCheck
{

	public static final String TYPE = "MaliciousAttachmentSecurityCheck";
	public static final String NAME = "Malicious Attachment";

	private MaliciousAttachmentSecurityCheckConfig config;

	public MaliciousAttachmentSecurityCheck( SecurityCheckConfig config, ModelItem parent, String icon, TestStep testStep )
	{
		super( testStep, config, parent, icon );

		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
			MaliciousAttachmentSecurityCheckConfig mascc = MaliciousAttachmentSecurityCheckConfig.Factory.newInstance();
			config.setConfig( mascc );
		}
		if( config.getConfig() == null )
		{
			MaliciousAttachmentSecurityCheckConfig mascc = MaliciousAttachmentSecurityCheckConfig.Factory.newInstance();
			config.setConfig( mascc );
		}
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

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{

		String originalResponse = getOriginalResult( ( SecurityTestRunnerImpl )securityTestRunner, testStep )
				.getResponse().getContentAsXml();

		// First, lets see what happens when we just attach a plain text file
		File textFile;
		try
		{
			textFile = File.createTempFile( "test", ".txt" );
			BufferedWriter writer = new BufferedWriter( new FileWriter( textFile ) );
			writer.write( "This is just a text file, nothing to see here, just a harmless text file" );
			writer.flush();
			Attachment attach = addAttachment( testStep, textFile, "text/plain" );
			// runCheck(testStep, context, securityTestLog, testCaseRunner,
			// originalResponse,
			// "Possible Malicious Attachment Vulnerability Detected");
			( ( AbstractHttpRequest<?> )getRequest( testStep ) ).removeAttachment( attach );

			// Try with setting the wrong content type
			attach = addAttachment( testStep, textFile, "multipart/mixed" );
			// runCheck(testStep, context, securityTestLog, testCaseRunner,
			// originalResponse,
			// "Possible Malicious Attachment Vulnerability Detected");
			( ( AbstractHttpRequest<?> )getRequest( testStep ) ).removeAttachment( attach );

		}
		catch( IOException e )
		{
			SoapUI.logError( e );
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
	public SecurityCheckConfigPanel getComponent()
	{
		// TODO Auto-generated method stub
		return null;
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
}
