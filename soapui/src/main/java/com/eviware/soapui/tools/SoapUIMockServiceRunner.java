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

package com.eviware.soapui.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.support.MockRunListenerAdapter;
import com.eviware.soapui.support.DateUtil;

/**
 * Standalone tool-runner used from maven-plugin, can also be used from
 * command-line (see xdocs) or directly from other classes.
 * <p>
 * For standalone usage, set the project file (with setProjectFile) and other
 * desired properties before calling run
 * </p>
 * 
 * @author Ole.Matzura
 */

public class SoapUIMockServiceRunner extends AbstractSoapUIRunner
{
	private String mockService;
	private String port;
	private String path;
	private List<MockRunner> runners = new ArrayList<MockRunner>();
	private boolean block;
	private String projectPassword;
	private WsdlProject project;
	private boolean saveAfterRun;

	public static String TITLE = "SoapUI " + SoapUI.SOAPUI_VERSION + " MockService Runner";

	/**
	 * Runs the specified MockService in the specified SoapUI project file, see
	 * SoapUI xdocs for details.
	 * 
	 * @param args
	 * @throws Exception
	 */

	public static void main( String[] args ) throws Exception
	{
		System.exit( new SoapUIMockServiceRunner().runFromCommandLine( args ) );
	}

	public void setMockService( String mockService )
	{
		this.mockService = mockService;
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public void setPort( String port )
	{
		this.port = port;
	}

	public SoapUIMockServiceRunner()
	{
		super( TITLE );
	}

	public SoapUIMockServiceRunner( String title )
	{
		super( title );
	}

	public boolean runRunner() throws Exception
	{
		initGroovyLog();

		String projectFile = getProjectFile();

		// WsdlProject project = new WsdlProject( projectFile,
		// getProjectPassword() );
		project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew( projectFile,
				getProjectPassword() );
		if( project.isDisabled() )
			throw new Exception( "Failed to load SoapUI project file [" + projectFile + "]" );

		initProject();

		if( mockService == null )
			log.info( "Running all MockServices in project [" + project.getName() + "]" );
		else
			log.info( "Running MockService [" + mockService + "] in project [" + project.getName() + "]" );

		log.info( "Press any key to terminate" );

		long startTime = System.nanoTime();

		for( int c = 0; c < project.getMockServiceCount(); c++ )
		{
			MockService ms = project.getMockServiceAt( c );
			if( mockService == null || ms.getName().equals( mockService ) )
				runMockService( ( WsdlMockService )ms );
		}

		log.info( "Started " + runners.size() + " runner" + ( ( runners.size() == 1 ) ? "" : "s" ) );

		if( block )
		{
			System.out.println( "Press any key to terminate..." );
			while( System.in.available() == 0 )
			{
				Thread.sleep( 1000 );

				// check if runners are still running
				for( int c = 0; c < runners.size(); c++ )
				{
					if( !runners.get( c ).isRunning() )
					{
						runners.remove( c );
						c-- ;
					}
				}

				if( runners.isEmpty() )
					break;
			}

			if( System.in.available() > 0 )
				System.in.read();

			for( MockRunner runner : runners )
				runner.stop();

			if( saveAfterRun && !project.isRemote() )
			{
				try
				{
					project.save();
				}
				catch( Throwable t )
				{
					log.error( "Failed to save project", t );
				}
			}
		}

		long timeTaken = ( System.nanoTime() - startTime ) / 1000000;
		log.info( "time taken: " + timeTaken + "ms" );

		exportReports();

		return block;
	}

	protected void initProject() throws Exception
	{
		initProjectProperties( project );
	}

	protected void exportReports() throws Exception
	{
	}

	/**
	 * Runs the specified MockService
	 * 
	 * @param mockService
	 */

	public void runMockService( WsdlMockService mockService )
	{
		try
		{
			if( path != null )
				mockService.setPath( path );

			if( port != null )
				mockService.setPort( Integer.parseInt( port ) );

			mockService.addMockRunListener( new LogListener() );
			WsdlMockRunner runner = mockService.start();
			runner.setLogEnabled( false );
			runners.add( runner );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public class LogListener extends MockRunListenerAdapter
	{
		private int responseCount;

		public void onMockRunnerStart( MockRunner mockRunner )
		{
			log.info( "MockService started on port " + mockRunner.getMockService().getPort() + " at path ["
					+ mockRunner.getMockService().getPath() + "]" );
		}

		public void onMockRunnerStop( MockRunner mockRunner )
		{
			log.info( "MockService stopped, handled " + responseCount + " requests" );
		}

		public void onMockResult( MockResult result )
		{
			responseCount++ ;
			if( result.getMockResponse() == null )
			{
				log.info( "Handled request " + responseCount + " - [missing mockResponse] in [" + result.getTimeTaken()
						+ "ms] at [" + DateUtil.formatExtraFull( new Date( result.getTimestamp() ) ) + "]" );
			}
			else
			{
				log.info( "Handled request " + responseCount + "; ["
						+ result.getMockResponse().getMockOperation().getName() + "] with ["
						+ result.getMockResponse().getName() + "] in [" + result.getTimeTaken() + "ms] at ["
						+ DateUtil.formatExtraFull( new Date( result.getTimestamp() ) ) + "]" );
			}
		}
	}

	@Override
	protected SoapUIOptions initCommandLineOptions()
	{
		SoapUIOptions options = new SoapUIOptions( "mockservicerunner" );
		options.addOption( "m", true, "Specified the name of the MockService to run" );
		options.addOption( "p", true, "Sets the local port to listen on" );
		options.addOption( "a", true, "Sets the url path to listen on" );
		options.addOption( "s", true, "Sets the soapui-settings.xml file to use" );
		options.addOption( "b", false, "Turns off blocking read for termination" );
		options.addOption( "x", true, "Sets project password for decryption if project is encrypted" );
		options.addOption( "v", true, "Sets password for soapui-settings.xml file" );
		options.addOption( "D", true, "Sets system property with name=value" );
		options.addOption( "G", true, "Sets global property with name=value" );
		options.addOption( "P", true, "Sets or overrides project property with name=value" );
		options.addOption( "S", false, "Saves the project after running the mockService(s)" );

		return options;
	}

	@Override
	protected boolean processCommandLine( CommandLine cmd )
	{
		if( cmd.hasOption( "m" ) )
			setMockService( getCommandLineOptionSubstSpace( cmd, "m" ) );

		if( cmd.hasOption( "a" ) )
			setPath( getCommandLineOptionSubstSpace( cmd, "a" ) );

		if( cmd.hasOption( "p" ) )
			setPort( cmd.getOptionValue( "p" ) );

		if( cmd.hasOption( "s" ) )
			setSettingsFile( getCommandLineOptionSubstSpace( cmd, "s" ) );

		setBlock( !cmd.hasOption( 'b' ) );
		setSaveAfterRun( cmd.hasOption( 'S' ) );

		if( cmd.hasOption( "x" ) )
		{
			setProjectPassword( cmd.getOptionValue( "x" ) );
		}

		if( cmd.hasOption( "v" ) )
		{
			setSoapUISettingsPassword( cmd.getOptionValue( "v" ) );
		}

		if( cmd.hasOption( "D" ) )
		{
			setSystemProperties( cmd.getOptionValues( "D" ) );
		}

		if( cmd.hasOption( "G" ) )
		{
			setGlobalProperties( cmd.getOptionValues( "G" ) );
		}

		if( cmd.hasOption( "P" ) )
		{
			setProjectProperties( cmd.getOptionValues( "P" ) );
		}

		return true;
	}

	public void setProjectPassword( String projectPassword )
	{
		this.projectPassword = projectPassword;
	}

	public String getProjectPassword()
	{
		return projectPassword;
	}

	public void setBlock( boolean block )
	{
		this.block = block;
	}

	public void setSaveAfterRun( boolean saveAfterRun )
	{
		this.saveAfterRun = saveAfterRun;
	}

	public WsdlProject getProject()
	{
		return project;
	}

	public void stopAll()
	{
		for( MockRunner runner : runners )
		{
			runner.stop();
		}
	}
}
