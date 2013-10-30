/*
 *  SoapUI, copyright (C) 2004-2011 smartbear.com
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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker.WorkerAdapter;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MockAsWar
{
	protected static final String SOAPUI_SETTINGS = "[SoapUISettings]";
	protected static final String PROJECT_FILE_NAME = "[ProjectFileName]";
	protected static final String MOCKSERVICE_ENDPOINT = "[mockServiceEndpoint]";

	private static final String SOAPUI_HOME = "soapui.home";

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
				content.append( inputLine ).append( "\n" );

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
		{
			String actionsString = "WEB-INF/actions";
			content.delete( content.indexOf( actionsString ), content.indexOf( actionsString ) + actionsString.length() );
		}
		if( !includeListeners )
		{
			String listenersString = "WEB-INF/listeners";
			content.delete( content.indexOf( listenersString ), content.indexOf( listenersString )
					+ listenersString.length() );
		}
		if( !enableWebUI )
		{
			String webUIEnabled = "<param-value>true</param-value>";
			String webUIDisabled = "<param-value>false</param-value>";
			content.replace( content.indexOf( webUIEnabled ),
					content.indexOf( webUIEnabled ) + webUIEnabled.length(),
					webUIDisabled );
		}
	}

	protected boolean prepareWarFile()
	{
		// create file system first
		if( createWarFileSystem() )
		{
			// copy all from bin/../lib to soapui.home/war/WEB-INF/lib/
			File fromDir = new File( System.getProperty( SOAPUI_HOME ), ".." + File.separator + "lib" );


			JarPackager.copyAllFromTo( fromDir, lib, new CaseInsensitiveFileFilter() );

			if( includeExt )
			{
				// copy all from bin/ext to soapui.home/war/WEB-INF/lib/
				fromDir = new File( System.getProperty( SOAPUI_HOME ), "ext" );
				JarPackager.copyAllFromTo( fromDir, lib, null );
			}

			// copy soapui jar to soapui.home/war/WEB-INF/lib/
			File soapUIHome = new File( System.getProperty( SOAPUI_HOME ) );
			String[] mainJar = soapUIHome.list( new FilenameFilter()
			{
				public boolean accept( File dir, String name )
				{
					return name.toLowerCase().startsWith( "soapui" ) && name.toLowerCase().endsWith( ".jar" );
				}
			} );

			fromDir = new File( System.getProperty( SOAPUI_HOME ), mainJar[0] );
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
			if( !directoryIsUsable( webInf ) )
			{
				return false;
			}
			else
			{
				clearDir( webInf );
				lib = new File( webInf, "lib" );

				if( !directoryIsUsable( lib ) )
				{
					return false;
				}

				soapuiDir = new File( webInf, "soapui" );
				if( !directoryIsUsable( soapuiDir ) )
				{
					return false;
				}
				clearDir( soapuiDir );

				if( includeActions )
				{
					actionsDir = new File( webInf, "actions" );
					if( !directoryIsUsable( actionsDir ) )
					{
						return false;
					}
					clearDir( actionsDir );
				}
				if( includeListeners )
				{
					listenersDir = new File( webInf, "listeners" );
					if( !directoryIsUsable( listenersDir ) )
					{
						return false;
					}
					clearDir( listenersDir );
				}

				return true;
			}
		}
		else
		{
			UISupport.showErrorMessage( warDir.getName() + " needs to be a directory!" );
			return false;
		}
	}

	private boolean directoryIsUsable( File dir )
	{
		if( !( dir.mkdir() || dir.exists() ) )
		{
			UISupport.showErrorMessage( "Could not create directory " + dir.getAbsolutePath() );
			return false;
		}
		return true;
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

	protected static class CaseInsensitiveFileFilter implements FileFilter
	{
		protected static final ArrayList<String> excludes = Lists.newArrayList( "servlet", "xulrunner", "Mozilla", "l2fprod", "tuxpack", "winpack", "ActiveQueryBuilder", "jxbrowser", "protection" );

		public boolean accept( final File file )
		{

			boolean pathNameExcluded = FluentIterable.from( excludes ).anyMatch( new Predicate<String>()
			{
				@Override
				public boolean apply( @Nullable String s )
				{
					if (file == null || s == null || file.getName().isEmpty()){
						return true;
					}

					return file.getName().toLowerCase().contains( s.toLowerCase() );
				}
			} );
			return !pathNameExcluded;
		}
	}
}
