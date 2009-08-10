/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.actions.iface.tools.wsi;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.wsI.testing.x2003.x03.common.AddStyleSheet;
import org.wsI.testing.x2004.x07.analyzerConfig.AssertionResults;
import org.wsI.testing.x2004.x07.analyzerConfig.Configuration;
import org.wsI.testing.x2004.x07.analyzerConfig.ConfigurationDocument;
import org.wsI.testing.x2004.x07.analyzerConfig.ReportFile;
import org.wsI.testing.x2004.x07.analyzerConfig.WsdlElementReference;
import org.wsI.testing.x2004.x07.analyzerConfig.WsdlElementType;
import org.wsI.testing.x2004.x07.analyzerConfig.WsdlReference;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.actions.SoapUIPreferencesAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.RunnerContext;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.WSISettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

/**
 * Invokes WS-I Analyzer Tool
 * 
 * @author Ole.Matzura
 */

public class WSIAnalyzeAction extends AbstractToolsAction<Interface>
{
	public final static String SOAPUI_ACTION_ID = "WSIAnalyzeAction";
	public final static Logger log = Logger.getLogger( WSIAnalyzeAction.class );

	private String configFile;
	private String wsiDir;

	public WSIAnalyzeAction()
	{
		super( "Check WSI Compliance", "Validate this WSDL for WSI Basic Profile compliance" );
	}

	protected void generate( StringToStringMap values, ToolHost toolHost, Interface modelItem ) throws Exception
	{
		wsiDir = SoapUI.getSettings().getString( WSISettings.WSI_LOCATION,
				System.getProperty( "wsi.dir", System.getenv( "WSI_HOME" ) ) );
		if( Tools.isEmpty( wsiDir ) )
		{
			UISupport.showErrorMessage( "WSI Test Tools directory must be set in global preferences" );

			if( UISupport.getMainFrame() != null )
			{
				if( SoapUIPreferencesAction.getInstance().show( SoapUIPreferencesAction.WS_I_SETTINGS ) )
				{
					wsiDir = SoapUI.getSettings().getString( WSISettings.WSI_LOCATION, null );
				}
			}
		}

		if( Tools.isEmpty( wsiDir ) )
			return;

		ProcessBuilder builder = new ProcessBuilder();

		File reportFile = File.createTempFile( "wsi-report", ".xml" );

		File wsiToolDir = new File( wsiDir + File.separatorChar + "cs" + File.separatorChar + "bin" );
		if( !wsiToolDir.exists() )
			wsiToolDir = new File( wsiDir + File.separatorChar + "java" + File.separatorChar + "bin" );

		ArgumentBuilder args = buildArgs( wsiToolDir, reportFile, modelItem );
		builder.command( args.getArgs() );
		builder.directory( wsiToolDir );

		toolHost.run( new WSIProcessToolRunner( builder, reportFile, modelItem ) );
	}

	private ArgumentBuilder buildArgs( File wsiToolDir, File reportFile, Interface modelItem ) throws IOException
	{
		Settings settings = modelItem.getSettings();

		ConfigurationDocument configDoc = createConfigFile( reportFile, settings, ( WsdlInterface )modelItem );
		configFile = configDoc.toString();

		File file = File.createTempFile( "wsi-analyzer-config", ".xml" );

		configDoc.save( file );

		ArgumentBuilder builder = new ArgumentBuilder( new StringToStringMap() );
		builder.startScript( wsiToolDir.getAbsolutePath() + File.separator + "Analyzer", ".bat", ".sh" );

		builder.addArgs( "-config", file.getAbsolutePath() );

		// add this to command-line due to bug in wsi-tools (?)
		if( settings.getBoolean( WSISettings.ASSERTION_DESCRIPTION ) )
			builder.addArgs( "-assertionDescription", "true" );

		return builder;
	}

	private ConfigurationDocument createConfigFile( File reportFile, Settings settings, WsdlInterface iface )
	{
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
		stylesheet.setHref( "./../common/Profiles/SSBP10_BP11_TAD.xml" );
		stylesheet.setType( "text/xsl" );
		stylesheet.setAlternate( false );

		config.setTestAssertionsFile( "../../common/profiles/SSBP10_BP11_TAD.xml" );

		WsdlReference wsdlRef = config.addNewWsdlReference();

		StringToStringMap values = new StringToStringMap();
		values.put( WSDL, iface.getDefinition() );
		values.put( CACHED_WSDL, Boolean.toString( iface.isCached() ) );

		wsdlRef.setWsdlURI( getWsdlUrl( values, iface ) );
		WsdlElementReference wsdlElement = wsdlRef.addNewWsdlElement();
		wsdlElement.setType( WsdlElementType.BINDING );
		wsdlElement.setStringValue( iface.getBindingName().getLocalPart() );
		wsdlElement.setNamespace( iface.getBindingName().getNamespaceURI() );
		return configDoc;
	}

	protected void showReport( File reportFile, String configFile ) throws Exception
	{
		WSIReportPanel panel = new WSIReportPanel( reportFile, configFile, null, true );
		panel.setPreferredSize( new Dimension( 600, 400 ) );

		UISupport.showDesktopPanel( new DefaultDesktopPanel( "WS-I Report", "WS-I Report for Interface ["
				+ getModelItem().getName() + "]", panel ) );
	}

	public static File transformReport( File reportFile ) throws Exception
	{
		String dir = SoapUI.getSettings().getString( WSISettings.WSI_LOCATION, null );
		File xsltFile = new File( dir + File.separatorChar + "common" + File.separatorChar + "xsl" + File.separatorChar
				+ "report.xsl" );

		Source xmlSource = new StreamSource( reportFile );
		Source xsltSource = new StreamSource( xsltFile );

		TransformerFactory transFact = TransformerFactory.newInstance();
		Transformer trans = transFact.newTransformer( xsltSource );

		String outputFolder = SoapUI.getSettings().getString( WSISettings.OUTPUT_FOLDER, null );
		File output = outputFolder == null || outputFolder.trim().length() == 0 ? null : new File( outputFolder );

		File tempFile = File.createTempFile( "wsi-report", ".html", output );
		trans.transform( xmlSource, new StreamResult( new FileWriter( tempFile ) ) );

		log.info( "WSI Report created at [" + tempFile.getAbsolutePath() + "]" );

		return tempFile;
	}

	private class WSIProcessToolRunner extends ProcessToolRunner
	{
		private File reportFile;
		private final Interface modelItem;

		public WSIProcessToolRunner( ProcessBuilder builder, File reportFile, Interface modelItem )
		{
			super( builder, "WSI Analyzer", modelItem );
			this.reportFile = reportFile;
			this.modelItem = modelItem;
		}

		public String getDescription()
		{
			return "Running WSI Analysis tools..";
		}

		protected void afterRun( int exitCode, RunnerContext context )
		{
			if( exitCode == 0 && context.getStatus() == RunnerContext.RunnerStatus.FINISHED )
			{
				try
				{
					reportFile = transformReport( reportFile );
				}
				catch( Exception e1 )
				{
					SoapUI.logError( e1 );
				}

				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						try
						{
							showReport( reportFile, configFile );
						}
						catch( Exception e )
						{
							UISupport.showErrorMessage( e );
						}
					}
				} );
			}

			closeDialog( modelItem );
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
