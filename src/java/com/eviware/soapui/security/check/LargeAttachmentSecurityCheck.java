package com.eviware.soapui.security.check;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.LargeAttachmentSecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.tools.InfiniteAttachment;
import com.eviware.soapui.security.ui.LargeAttachmentSecurityCheckConfigPanel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.types.StringToObjectMap;

public class LargeAttachmentSecurityCheck extends AbstractSecurityCheck
{

	public static final String TYPE = "LargeAttachmentSecurityCheck";

	public LargeAttachmentSecurityCheck( SecurityCheckConfig config, ModelItem parent, String icon, TestStep testStep )
	{
		super( testStep, config, parent, icon );
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
			LargeAttachmentSecurityCheckConfig mascc = LargeAttachmentSecurityCheckConfig.Factory.newInstance();
			mascc.setSize( 4 * 1024 * 1024 * 1024 );
			config.setConfig( mascc );
		}
		if( config.getConfig() == null )
		{
			LargeAttachmentSecurityCheckConfig mascc = LargeAttachmentSecurityCheckConfig.Factory.newInstance();
			mascc.setSize( 4 * 1024 * 1024 * 1024 );
			config.setConfig( mascc );
		}
	}

	@Override
	protected void execute( TestStep testStep, SecurityTestRunContext context )
	{
		SecurityTestRunnerImpl securityTestRunner = new SecurityTestRunnerImpl( ( SecurityTest )getParent(),
				new StringToObjectMap() );

		String originalResponse = getOriginalResult( securityTestRunner, testStep ).getResponse().getRequestContent();

		AbstractHttpRequest<?> request = ( AbstractHttpRequest<?> )getRequest( testStep );

		request.setAttachmentAt( 0, new InfiniteAttachment( AttachmentConfig.Factory.newInstance(), request,
				( long )( ( LargeAttachmentSecurityCheckConfig )getConfig().getConfig() ).getSize() ) );

		Logger.getLogger( SoapUI.class ).info( "Disabling logs during Large Attachment Check" );
		Logger.getLogger( "httpclient.wire" ).setLevel( Level.OFF );
		// runCheck( testStep, context, securityTestLog, testCaseRunner,
		// originalResponse,
		// "Large attachment vulnerability detected" );
		Logger.getLogger( SoapUI.class ).info( "Re-enabling logs" );
		Logger.getLogger( "httpclient.wire" ).setLevel( Level.DEBUG );

	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return true;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		return new LargeAttachmentSecurityCheckConfigPanel( this );
	}

	public long getMaxSize()
	{
		return ( long )( ( LargeAttachmentSecurityCheckConfig )getConfig().getConfig() ).getSize();
	}

	public void setMaxSize( long size )
	{
		( ( LargeAttachmentSecurityCheckConfig )getConfig().getConfig() ).setSize( size );
	}

	public int getMaxTime()
	{
		return ( ( LargeAttachmentSecurityCheckConfig )getConfig().getConfig() ).getTime();
	}

	public void setMaxTime( int time )
	{
		( ( LargeAttachmentSecurityCheckConfig )getConfig().getConfig() ).setTime( time );
	}


	@Override
	protected boolean hasNext()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String getConfigDescription()
	{
		return "Configures invalid type security check";
	}

	@Override
	public String getConfigName()
	{
		return "Invalid Types Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}

}
