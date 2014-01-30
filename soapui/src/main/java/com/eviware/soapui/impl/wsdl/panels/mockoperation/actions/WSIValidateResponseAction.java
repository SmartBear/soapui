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

package com.eviware.soapui.impl.wsdl.panels.mockoperation.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.*;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wsi.WSIReportPanel;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.WSISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;
import org.wsI.testing.x2003.x03.common.AddStyleSheet;
import org.wsI.testing.x2003.x03.log.*;
import org.wsI.testing.x2004.x07.analyzerConfig.*;
import org.wsI.testing.x2004.x07.analyzerConfig.LogFile.CorrelationType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Validates the current request/response exchange of a WsdlMockResponse with
 * the WS-I tools
 * 
 * @author Ole.Matzura
 */

public class WSIValidateResponseAction extends AbstractToolsAction<MockResponse>
{
	private String configFile;
	private File logFile;
	private String wsiDir;

	public WSIValidateResponseAction()
	{
		super( "Check WS-I Compliance", "Validates the current request/response againt the WS-I Basic Profile" );
		// putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "alt W" ));

		// setEnabled( request != null && request.getMockResult() != null );
	}

	protected void generate( StringToStringMap values, ToolHost toolHost, MockResponse modelItem ) throws Exception
	{
		if( modelItem.getMockResult() == null )
		{
			UISupport.showErrorMessage( "Request/Response required for WS-I validations" );
			return;
		}

		wsiDir = SoapUI.getSettings().getString( WSISettings.WSI_LOCATION,
				System.getProperty( "wsi.dir", System.getenv( "WSI_HOME" ) ) );
		if( wsiDir == null )
		{
			UISupport.showErrorMessage( "WSI Test Tools directory must be set in global preferences" );
			return;
		}

		if( modelItem.getAttachmentCount() > 0 )
		{
			if( !UISupport.confirm( "Response contains attachments which is not supported by "
					+ "validation tools, validate anyway?", "Validation Warning" ) )
				return;
		}

		ProcessBuilder builder = new ProcessBuilder();

		File reportFile = File.createTempFile( "wsi-report", ".xml" );

		ArgumentBuilder args = buildArgs( reportFile, modelItem );
		builder.command( args.getArgs() );
		builder.directory( new File( wsiDir + File.separatorChar + "java" + File.separatorChar + "bin" ) );

		toolHost.run( new WSIProcessToolRunner( builder, reportFile, modelItem ) );
	}

	private ArgumentBuilder buildArgs( File reportFile, MockResponse modelItem ) throws Exception
	{
		File logFile = buildLog( modelItem );
		File file = buildConfig( reportFile, logFile, modelItem );
		Settings settings = modelItem.getSettings();

		ArgumentBuilder builder = new ArgumentBuilder( new StringToStringMap() );
		builder.startScript( "Analyzer", ".bat", ".sh" );

		builder.addArgs( "-config", file.getAbsolutePath() );

		// add this to command-line due to bug in wsi-tools (?)
		if( settings.getBoolean( WSISettings.ASSERTION_DESCRIPTION ) )
			builder.addArgs( "-assertionDescription", "true" );

		return builder;
	}

	private File buildLog( MockResponse modelItem ) throws Exception
	{
		LogDocument logDoc = LogDocument.Factory.newInstance();
		Log log = logDoc.addNewLog();
		log.setTimestamp( Calendar.getInstance() );

		addMonitorConfig( log );
		addMessageConfig( log, modelItem );

		logFile = File.createTempFile( "wsi-analyzer-log", ".xml" );
		logDoc.save( logFile );
		return logFile;
	}

	private File buildConfig( File reportFile, File logFile, MockResponse modelItem ) throws IOException
	{
		Settings settings = modelItem.getSettings();

		ConfigurationDocument configDoc = ConfigurationDocument.Factory.newInstance();
		Configuration config = configDoc.addNewConfiguration();

		config.setVerbose( settings.getBoolean( WSISettings.VERBOSE ) );
		AssertionResults results = config.addNewAssertionResults();
		results.setType( AssertionResults.Type.Enum.forString( settings.getString( WSISettings.RESULTS_TYPE,
				AssertionResults.Type.ONLY_FAILED.toString() ) ) );

		results.setMessageEntry( settings.getBoolean( WSISettings.MESSAGE_ENTRY ) );
		results.setFailureMessage( settings.getBoolean( WSISettings.FAILURE_MESSAGE ) );
		results.setAssertionDescription( settings.getBoolean( WSISettings.ASSERTION_DESCRIPTION ) );

		ReportFile report = config.addNewReportFile();
		report.setLocation( reportFile.getAbsolutePath() );
		report.setReplace( true );

		AddStyleSheet stylesheet = report.addNewAddStyleSheet();
		stylesheet.setHref( ".\\..\\common\\Profiles\\SSBP10_BP11_TAD.xml" );
		stylesheet.setType( "text/xsl" );
		stylesheet.setAlternate( false );

		config.setTestAssertionsFile( "../../common/profiles/SSBP10_BP11_TAD.xml" );

		LogFile logFileConfig = config.addNewLogFile();
		logFileConfig.setStringValue( logFile.getAbsolutePath() );
		logFileConfig.setCorrelationType( CorrelationType.ENDPOINT );

		/*
		 * WsdlInterface iface = (WsdlInterface)
		 * modelItem.getOperation().getInterface();
		 * 
		 * WsdlReferenceConfig wsdlRef = config.addNewWsdlReference();
		 * wsdlRef.setWsdlURI( iface.getWsdlDefinition() );
		 * WsdlElementReferenceConfig wsdlElement = wsdlRef.addNewWsdlElement();
		 * wsdlElement.setType( WsdlElementTypeConfig.BINDING );
		 * wsdlElement.setStringValue( iface.getBindingName().getLocalPart() );
		 * wsdlElement.setNamespace( iface.getBindingName().getNamespaceURI() );
		 * wsdlRef.setServiceLocation( modelItem.getEndpoint() );
		 */

		configFile = configDoc.toString();

		File file = File.createTempFile( "wsi-analyzer-config", ".xml" );

		configDoc.save( file );
		return file;
	}

	private void addMessageConfig( Log log, MockResponse modelItem ) throws MalformedURLException
	{
		HttpMessageEntry requestMessage = HttpMessageEntry.Factory.newInstance();
		MockRequest mockRequest = modelItem.getMockResult().getMockRequest();
		requestMessage.addNewMessageContent().setStringValue( mockRequest.getRequestContent() );
		requestMessage.setConversationID( "1" );
		requestMessage.setTimestamp( Calendar.getInstance() );
		requestMessage.setID( "1" );
		MockService mockService = modelItem.getMockOperation().getMockService();
		URL endpoint = new URL( "http://127.0.0.1:" + mockService.getPort() + mockService.getPath() );
		requestMessage.setSenderHostAndPort( "localhost" );

		if( endpoint.getPort() > 0 )
			requestMessage.setReceiverHostAndPort( endpoint.getHost() + ":" + endpoint.getPort() );
		else
			requestMessage.setReceiverHostAndPort( endpoint.getHost() );

		requestMessage.setType( TcpMessageType.REQUEST );

		HttpMessageEntry responseMessage = HttpMessageEntry.Factory.newInstance();
		responseMessage.addNewMessageContent().setStringValue( modelItem.getMockResult().getResponseContent() );
		responseMessage.setConversationID( "1" );
		responseMessage.setType( TcpMessageType.RESPONSE );
		responseMessage.setTimestamp( Calendar.getInstance() );
		responseMessage.setID( "2" );
		responseMessage.setSenderHostAndPort( requestMessage.getReceiverHostAndPort() );
		responseMessage.setReceiverHostAndPort( requestMessage.getSenderHostAndPort() );

		String requestHeaders = buildHttpHeadersString( mockRequest.getRequestHeaders() );
		requestMessage.setHttpHeaders( "POST " + mockRequest.getPath() + " " + mockRequest.getProtocol() + "\r\n"
				+ requestHeaders );

		responseMessage.setHttpHeaders( "HTTP/1.1 200 OK"
				+ buildHttpHeadersString( modelItem.getMockResult().getResponseHeaders() ) );

		log.setMessageEntryArray( new MessageEntry[] { requestMessage, responseMessage } );
	}

	private void addMonitorConfig( Log log ) throws Exception
	{
		Monitor monitor = log.addNewMonitor();

		monitor.setVersion( "1.5" );
		monitor.setReleaseDate( Calendar.getInstance() );

		org.wsI.testing.x2003.x03.monitorConfig.Configuration conf = monitor.addNewConfiguration();
		conf.setCleanupTimeoutSeconds( 0 );
		conf.setLogDuration( 0 );

		org.wsI.testing.x2003.x03.monitorConfig.LogFile logFileConf = conf.addNewLogFile();
		logFileConf.setLocation( "report.xml" );
		logFileConf.setReplace( true );

		/*
		 * ArrayOfRedirectConfig mintConf = conf.addNewManInTheMiddle();
		 * RedirectConfig redirect = mintConf.addNewRedirect();
		 * redirect.setListenPort( 9999 ); redirect.setMaxConnections( 10 );
		 * redirect.setReadTimeoutSeconds( 10 );
		 * 
		 * URL endpoint = new URL( modelItem.getEndpoint()); if(
		 * endpoint.getPort() > 0 ) redirect.setSchemeAndHostPort(
		 * endpoint.getHost() + ":" + endpoint.getPort()); else
		 * redirect.setSchemeAndHostPort( endpoint.getHost() );
		 */

		Environment env = monitor.addNewEnvironment();
		NameVersionPair osConf = env.addNewOperatingSystem();
		osConf.setName( "Windows" );
		osConf.setVersion( "2003" );

		NameVersionPair rtConf = env.addNewRuntime();
		rtConf.setName( "java" );
		rtConf.setVersion( "1.5" );

		NameVersionPair xpConf = env.addNewXmlParser();
		xpConf.setName( "xmlbeans" );
		xpConf.setVersion( "2.2.0" );

		Implementation implConf = monitor.addNewImplementer();
		implConf.setName( "soapui" );
		implConf.setLocation( "here" );
	}

	private String buildHttpHeadersString( StringToStringsMap headers )
	{
		StringBuffer buffer = new StringBuffer();

		if( headers.containsKey( "#status#" ) )
		{
			buffer.append( headers.get( "#status#" ) ).append( "\r\n" );
		}

		for( Map.Entry<String, List<String>> headerEntry : headers.entrySet() )
		{
			if( !headerEntry.getKey().equals( "#status#" ) )
			{
				for( String value : headerEntry.getValue() )
					buffer.append( headerEntry.getKey() ).append( ": " ).append( value ).append( "\r\n" );
			}
		}

		return buffer.toString();
	}

	private class WSIProcessToolRunner extends ProcessToolRunner
	{
		private final File reportFile;
		private final MockResponse modelItem;

		public WSIProcessToolRunner( ProcessBuilder builder, File reportFile, MockResponse modelItem )
		{
			super( builder, "WSI Message Validation", modelItem );
			this.reportFile = reportFile;
			this.modelItem = modelItem;
		}

		public String getDescription()
		{
			return "Running WSI Analysis tools..";
		}

		protected void afterRun( int exitCode, RunnerContext context )
		{
			try
			{
				if( exitCode == 0 && context.getStatus() == RunnerContext.RunnerStatus.FINISHED )
				{
					WSIReportPanel panel = new WSIReportPanel( reportFile, configFile, logFile, true );
					panel.setPreferredSize( new Dimension( 600, 400 ) );

					UISupport.showDesktopPanel( new DefaultDesktopPanel( "WS-I Report",
							"WS-I Report for validation of messages in MockResponse [" + modelItem.getName() + "]", panel ) );
				}
			}
			catch( Exception e )
			{
				UISupport.showErrorMessage( e );
			}
		}

		public boolean showLog()
		{
			return modelItem.getSettings().getBoolean( WSISettings.SHOW_LOG );
		}

		@Override
		protected void beforeProcess( ProcessBuilder processBuilder, RunnerContext context )
		{
			processBuilder.environment().put( "WSI_HOME", wsiDir );
		}
	}
}
