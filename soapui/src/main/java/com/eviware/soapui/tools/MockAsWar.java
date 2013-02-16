/*
 *  soapUI, copyright (C) 2004-2011 smartbear.com 
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker.WorkerAdapter;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;

public class MockAsWar
{
	protected static final String SOAPUI_SETTINGS = "[soapUISettings]";
	protected static final String PROJECT_FILE_NAME = "[ProjectFileName]";
	protected static final String MOCKSERVICE_ENDPOINT = "[mockServiceEndpoint]";

	protected File projectFile;
	protected File settingsFile;
	protected File warDir;
	private File warFile;
	protected File webInf;
	private File lib;
	protected File soapuiDir;

	protected Logger log = Logger.getLogger( MockAsWar.class );

	private boolean includeExt;
	protected boolean includeActions;
	protected boolean includeListeners;
	private File actionsDir;
	private File listenersDir;
	protected final String localEndpoint;
	protected boolean enableWebUI;

	public MockAsWar( String projectPath, String settingsPath, String warDir, String warFile, boolean includeExt,
			boolean actions, boolean listeners, String localEndpoint, boolean enableWebUI )
	{
		this.localEndpoint = localEndpoint;
		this.projectFile = new File( projectPath );
		this.settingsFile = StringUtils.hasContent( settingsPath ) ? new File( settingsPath ) : null;
		this.warDir = StringUtils.hasContent( warDir ) ? new File( warDir ) : new File(
				System.getProperty( "java.io.tmpdir" ), "warasmock" );
		if( !this.warDir.exists() )
		{
			this.warDir.mkdir();
		}
		this.warFile = !StringUtils.hasContent( warFile ) ? null : new File( warFile );
		this.includeExt = includeExt;
		this.includeActions = actions;
		this.includeListeners = listeners;
		this.enableWebUI = enableWebUI;
	}

	public void createMockAsWarArchive()
	{
		XProgressDialog progressDialog = UISupport.getDialogs().createProgressDialog( "Creating War File", 3,
				"Building war file..", false );
		WorkerAdapter warWorker = new WorkerAdapter()
		{

			public Object construct( XProgressMonitor monitor )
			{
				if( prepareWarFile() )
				{
					createWebXml();

					if( warFile != null )
					{
						ArrayList<File> files = getAllFilesFrom( webInf );
						files.add( new File( warDir, "stylesheet.css" ) );
						files.add( new File( warDir, "header_logo.jpg" ) );

						File[] filez = files.toArray( new File[files.size()] );
						JarPackager.createJarArchive( warFile, warDir, filez );
					}
				}
				return null;
			}
		};
		try
		{
			progressDialog.run( warWorker );
		}
		catch( Exception e )
		{
			log.error( e.getMessage(), e );
		}

	}

	private ArrayList<File> getAllFilesFrom( File dir )
	{
		ArrayList<File> result = new ArrayList<File>();
		if( dir.isDirectory() )
		{
			result.addAll( Arrays.asList( dir.listFiles() ) );
			ArrayList<File> toAdd = new ArrayList<File>();
			for( File f : result )
			{
				if( f.isDirectory() )
					toAdd.addAll( getAllFilesFrom( f ) );
			}
			result.addAll( toAdd );
		}
		return result;
	}

	protected void createWebXml()
	{
		URL url = SoapUI.class.getResource( "/com/eviware/soapui/resources/mockaswar/web.xml" );
		try
		{
			BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );
			String inputLine;
			StringBuilder content = new StringBuilder();

			while( ( inputLine = in.readLine() ) != null )
				content.append( inputLine + "\n" );

			createContent( content );

			BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( new File( webInf,
					"web.xml" ) ) ) );
			out.write( content.toString() );
			out.flush();
			out.close();
		}
		catch( IOException e )
		{
			log.error( e.getMessage(), e );
		}
	}

	protected void createContent( StringBuilder content )
	{
		content.replace( content.indexOf( PROJECT_FILE_NAME ),
				content.indexOf( PROJECT_FILE_NAME ) + PROJECT_FILE_NAME.length(), projectFile.getName() );

		content.replace(
				content.indexOf( SOAPUI_SETTINGS ),
				content.indexOf( SOAPUI_SETTINGS ) + SOAPUI_SETTINGS.length(),
				settingsFile != null && settingsFile.exists() && settingsFile.isFile() ? "WEB-INF/soapui/"
						+ settingsFile.getName() : "" );
		content.replace( content.indexOf( MOCKSERVICE_ENDPOINT ), content.indexOf( MOCKSERVICE_ENDPOINT )
				+ MOCKSERVICE_ENDPOINT.length(), localEndpoint );

		if( !includeActions )
			content.replace( content.indexOf( "WEB-INF/actions" ), content.indexOf( "WEB-INF/actions" )
					+ "WEB-INF/actions".length(), "" );
		if( !includeListeners )
			content.replace( content.indexOf( "WEB-INF/listeners" ), content.indexOf( "WEB-INF/listeners" )
					+ "WEB-INF/listeners".length(), "" );
		if( !enableWebUI )
			content.replace( content.indexOf( "<param-value>true</param-value>" ),
					content.indexOf( "<param-value>true</param-value>" ) + "<param-value>true</param-value>".length(),
					"<param-value>false</param-value>" );
	}

	protected boolean prepareWarFile()
	{
		// create file system first
		if( createWarFileSystem() )
		{
			// copy all from bin/../lib to soapui.home/war/WEB-INF/lib/
			File fromDir = new File( System.getProperty( "soapui.home" ), ".." + File.separator + "lib" );
			JarPackager.copyAllFromTo( fromDir, lib, new FileFilter()
			{
				public boolean accept( File pathname )
				{
					return pathname.getName().indexOf( "servlet" ) == -1 && pathname.getName().indexOf( "xulrunner" ) == -1
							&& pathname.getName().indexOf( "Mozilla" ) == -1 && pathname.getName().indexOf( "l2fprod" ) == -1
							&& pathname.getName().indexOf( "tuxpack" ) == -1
							&& pathname.getName().indexOf( "winpack" ) == -1
							//	&& pathname.getName().indexOf( "rsyntax" ) == -1
							&& pathname.getName().indexOf( "ActiveQueryBuilder" ) == -1
							&& pathname.getName().indexOf( "jxbrowser" ) == -1
							&& pathname.getName().toLowerCase().indexOf( "protection" ) == -1;
				}
			} );

			if( includeExt )
			{
				// copy all from bin/ext to soapui.home/war/WEB-INF/lib/
				fromDir = new File( System.getProperty( "soapui.home" ), "ext" );
				JarPackager.copyAllFromTo( fromDir, lib, null );
			}

			// copy soapui jar to soapui.home/war/WEB-INF/lib/
			File soapUIHome = new File( System.getProperty( "soapui.home" ) );
			String[] mainJar = soapUIHome.list( new FilenameFilter()
			{
				public boolean accept( File dir, String name )
				{
					if( name.toLowerCase().startsWith( "soapui" ) && name.toLowerCase().endsWith( ".jar" ) )
						return true;
					return false;
				}
			} );

			fromDir = new File( System.getProperty( "soapui.home" ), mainJar[0] );
			JarPackager.copyFileToDir( fromDir, lib );
			// copy project and settings file to bin/war/WEB-INF/soapui/
			copyProjectFile();
			if( settingsFile != null && settingsFile.exists() && settingsFile.isFile() )
				JarPackager.copyFileToDir( settingsFile, soapuiDir );

			// actions
			if( includeActions )
			{
				fromDir = new File( System.getProperty( "soapui.ext.actions" ) );
				JarPackager.copyAllFromTo( fromDir, actionsDir, null );
			}
			// listeners
			if( includeListeners )
			{
				fromDir = new File( System.getProperty( "soapui.ext.listeners" ) );
				JarPackager.copyAllFromTo( fromDir, listenersDir, null );
			}

			copyWarResource( "header_logo.jpg" );
			copyWarResource( "stylesheet.css" );

			return true;
		}
		return false;
	}

	protected void copyProjectFile()
	{
		JarPackager.copyFileToDir( projectFile, soapuiDir );
	}

	private void copyWarResource( String resource )
	{
		try
		{
			Tools.writeAll( new FileOutputStream( new File( warDir, resource ) ),
					SoapUI.class.getResourceAsStream( "/com/eviware/soapui/resources/mockaswar/" + resource ) );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	protected boolean createWarFileSystem()
	{
		if( warDir.isDirectory() )
		{
			log.info( "Creating WAR directory in [" + warDir.getAbsolutePath() + "]" );
			webInf = new File( warDir, "WEB-INF" );
			if( !( webInf.mkdir() || webInf.exists() ) )
			{
				UISupport.showErrorMessage( "Could not create directory " + webInf.getAbsolutePath() );
				return false;
			}
			else
			{
				clearDir( webInf );
				lib = new File( webInf, "lib" );
				if( !( lib.mkdir() || lib.exists() ) )
				{
					UISupport.showErrorMessage( "Could not create directory " + lib.getAbsolutePath() );
					return false;
				}
				soapuiDir = new File( webInf, "soapui" );
				if( !( soapuiDir.mkdir() || soapuiDir.exists() ) )
				{
					UISupport.showErrorMessage( "Could not create directory " + soapuiDir.getAbsolutePath() );
					return false;
				}
				clearDir( soapuiDir );

				if( includeActions )
				{
					actionsDir = new File( webInf, "actions" );
					if( !( actionsDir.mkdirs() || actionsDir.exists() ) )
					{
						UISupport.showErrorMessage( "Could not create directory " + actionsDir.getAbsolutePath() );
						return false;
					}
					clearDir( actionsDir );
				}
				if( includeListeners )
				{
					listenersDir = new File( webInf, "listeners" );
					if( !( listenersDir.mkdirs() || listenersDir.exists() ) )
					{
						UISupport.showErrorMessage( "Could not create directory " + listenersDir.getAbsolutePath() );
						return false;
					}
					clearDir( listenersDir );
				}

				return true;
			}
		}
		else
		{
			UISupport.showErrorMessage( warDir.getName() + " need to be directory!" );
			return false;
		}
	}

	/**
	 * Deletes all files, just files, in directory
	 * 
	 * @param dir
	 */
	protected void clearDir( File dir )
	{
		for( File file : dir.listFiles() )
			if( file.isFile() )
				file.delete();
	}

}
