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

package com.eviware.soapui.tools;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import org.apache.commons.cli.*;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.io.OutputStreamWriter;

public abstract class AbstractSoapUIRunner
{
	private boolean groovyLogInitialized;
	private String projectFile;
	protected final Logger log = Logger.getLogger( getClass() );
	private String settingsFile;
	private String soapUISettingsPassword;

	private boolean enableUI;
	private String outputFolder;
	private String[] projectProperties;

	public AbstractSoapUIRunner( String title )
	{
		if( title != null )
			System.out.println( title );
	}

	protected void initGroovyLog()
	{
		if( !groovyLogInitialized )
		{
			Logger logger = Logger.getLogger( "groovy.log" );

			ConsoleAppender appender = new ConsoleAppender();
			appender.setWriter( new OutputStreamWriter( System.out ) );
			appender.setLayout( new PatternLayout( "%d{ABSOLUTE} %-5p [%c{1}] %m%n" ) );
			logger.addAppender( appender );

			groovyLogInitialized = true;
		}
	}

	public int runFromCommandLine( String[] args )
	{
		try
		{
			if( initFromCommandLine( args, true ) )
			{
				if( run() )
				{
					return 0;
				}
			}
		}
		catch( Throwable e )
		{
			log.error( e );
			SoapUI.logError( e );
		}
		
		return -1;
	}

	public boolean initFromCommandLine( String[] args, boolean printHelp ) throws Exception
	{
		SoapUIOptions options = initCommandLineOptions();

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args );

		if( options.requiresProject() )
		{
			args = cmd.getArgs();

			if( args.length != 1 )
			{
				if( printHelp )
				{
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp( options.getRunnerName() + " [options] <soapui-project-file>", options );
				}

				System.err.println( "Missing soapUI project file.." );
				return false;
			}

			setProjectFile( args[0] );
		}

		return processCommandLine( cmd );
	}

	/**
	 * Main method to use for running the configured tests. Call after setting
	 * properties, etc as desired.
	 * 
	 * @return true if execution should be blocked
	 * @throws Exception
	 *            if an error or failure occurs during test execution
	 */

	public final boolean run() throws Exception
	{
		SoapUI.setSoapUICore( createSoapUICore() );
		return runRunner();
	}

	protected SoapUICore createSoapUICore()
	{
		if( enableUI )
		{
			StandaloneSoapUICore core = new StandaloneSoapUICore( settingsFile );
			log.info( "Enabling UI Components" );
			core.prepareUI();
			UISupport.setMainFrame( null );
			return core;
		}
		else
		{
			return new DefaultSoapUICore( null, settingsFile, soapUISettingsPassword );
		}
	}

	protected abstract boolean processCommandLine( CommandLine cmd );

	protected abstract SoapUIOptions initCommandLineOptions();

	protected abstract boolean runRunner() throws Exception;

	protected String getCommandLineOptionSubstSpace( CommandLine cmd, String key )
	{
		return cmd.getOptionValue( key ).replaceAll( "%20", " " );
	}

	public String getProjectFile()
	{
		return projectFile;
	}

	public String getSettingsFile()
	{
		return settingsFile;
	}

	public void setOutputFolder( String outputFolder )
	{
		this.outputFolder = outputFolder;
	}

	public String getOutputFolder()
	{
		return this.outputFolder;
	}

	public String getAbsoluteOutputFolder( ModelItem modelItem )
	{
		String folder = outputFolder;

		if( StringUtils.isNullOrEmpty( folder ) )
		{
			folder = PathUtils.getExpandedResourceRoot( modelItem );
		}
		else if( PathUtils.isRelativePath( folder ) )
		{
			folder = PathUtils.resolveResourcePath( folder, modelItem );
		}

		return folder;
	}

	protected void ensureOutputFolder( ModelItem modelItem )
	{
		ensureFolder( getAbsoluteOutputFolder( modelItem ) );
	}

	public void ensureFolder( String path )
	{
		if( path == null )
			return;

		File folder = new File( path );
		if( !folder.exists() || !folder.isDirectory() )
			folder.mkdirs();
	}

	/**
	 * Sets the soapUI project file containing the tests to run
	 * 
	 * @param projectFile
	 *           the soapUI project file containing the tests to run
	 */

	public void setProjectFile( String projectFile )
	{
		this.projectFile = projectFile;
	}

	/**
	 * Sets the soapUI settings file containing the tests to run
	 * 
	 * @param settingsFile
	 *           the soapUI settings file to use
	 */

	public void setSettingsFile( String settingsFile )
	{
		this.settingsFile = settingsFile;
	}

	public void setEnableUI( boolean enableUI )
	{
		this.enableUI = enableUI;
	}

	public static class SoapUIOptions extends Options
	{
		private final String runnerName;

		public SoapUIOptions( String runnerName )
		{
			this.runnerName = runnerName;
		}

		public String getRunnerName()
		{
			return runnerName;
		}

		public boolean requiresProject()
		{
			return true;
		}
	}

	public String getSoapUISettingsPassword()
	{
		return soapUISettingsPassword;
	}

	public void setSoapUISettingsPassword( String soapUISettingsPassword )
	{
		this.soapUISettingsPassword = soapUISettingsPassword;
	}

	public void setSystemProperties( String[] optionValues )
	{
		for( String option : optionValues )
		{
			int ix = option.indexOf( '=' );
			if( ix != -1 )
			{
				System.setProperty( option.substring( 0, ix ), option.substring( ix + 1 ) );
			}
		}
	}

	public void setGlobalProperties( String[] optionValues )
	{
		for( String option : optionValues )
		{
			int ix = option.indexOf( '=' );
			if( ix != -1 )
			{
				String name = option.substring( 0, ix );
				String value = option.substring( ix + 1 );
				log.info( "Setting global property [" + name + "] to [" + value + "]" );
				PropertyExpansionUtils.getGlobalProperties().setPropertyValue( name, value );
			}
		}
	}

	public void setProjectProperties( String[] projectProperties )
	{
		this.projectProperties = projectProperties;
	}

	protected void initProjectProperties( WsdlProject project )
	{
		if( projectProperties != null )
		{
			for( String option : projectProperties )
			{
				int ix = option.indexOf( '=' );
				if( ix != -1 )
				{
					String name = option.substring( 0, ix );
					String value = option.substring( ix + 1 );
					log.info( "Setting project property [" + name + "] to [" + value + "]" );
					project.setPropertyValue( name, value );
				}
				}
			}
		}

	protected boolean isEnableUI()
	{
		return enableUI;
	}
}
