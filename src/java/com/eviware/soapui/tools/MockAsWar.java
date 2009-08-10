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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.eviware.x.dialogs.Worker.WorkerAdapter;

public class MockAsWar
{
	private static final String SOAPUI_SETTINGS = "[soapUISettings]";
	private static final String PROJECT_FILE_NAME = "[ProjectFileName]";
	private File projectFile;
	private File settingsFile;
	private File warDir;
	private File warFile;
	private File webInf;
	private File lib;
	private File soapuiDir;

	private Logger log = Logger.getLogger( MockAsWar.class );
	private boolean includeExt;
	private boolean includeActions;
	private boolean includeListeners;
	private File actionsDir;
	private File listenersDir;

	public MockAsWar(String projectPath, String settingsPath,  String warDir, String warFile,
			boolean includeExt, boolean actions, boolean listeners )
	{
		this.projectFile = new File( projectPath );
		this.settingsFile = new File( settingsPath );
		this.warDir = warDir.length() > 0 ? new File( warDir ) : new File( System.getProperty( "java.io.tmpdir" ),
				"warasmock" );
		if( !this.warDir.exists() )
		{
			this.warDir.mkdir();
		}
		this.warFile = warFile.length() == 0 ? null : new File( warFile );
		this.includeExt = includeExt;
		this.includeActions = actions;
		this.includeListeners = listeners;
	}

	public void createMockAsWarArchive()
	{
		XProgressDialog progressDialog = UISupport.getDialogs().createProgressDialog( "Creating War File", 3,
				"Filling war file..", false );
		WorkerAdapter warWorker = new WorkerAdapter()
		{

			public Object construct( XProgressMonitor monitor )
			{
				if( prepareWarFile() )
				{
					createWebXml();

					if( warFile != null )
					{
						File[] filez = getAllFilesFrom( webInf ).toArray( new File[0] );
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

	private void createWebXml()
	{
		URL url = SoapUI.class.getResource( "/com/eviware/soapui/resources/mockaswar/web.xml" );
		try
		{
			BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );
			String inputLine;
			StringBuilder content = new StringBuilder();

			while( ( inputLine = in.readLine() ) != null )
				content.append( inputLine + "\n" );

			content.replace( content.indexOf( PROJECT_FILE_NAME ), content.indexOf( PROJECT_FILE_NAME )
					+ PROJECT_FILE_NAME.length(), projectFile.getName() );
			content.replace( content.indexOf( SOAPUI_SETTINGS ), content.indexOf( SOAPUI_SETTINGS )
					+ SOAPUI_SETTINGS.length(), settingsFile.getAbsolutePath() );
			
			if ( !includeActions )
				content.replace( content.indexOf( "WEB-INF/actions" ), content.indexOf( "WEB-INF/actions" )
						+ "WEB-INF/actions".length(), "" );
			if ( !includeListeners )
				content.replace( content.indexOf( "WEB-INF/listeners" ), content.indexOf( "WEB-INF/listeners" )
						+ "WEB-INF/listeners".length(), "" );

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

	private boolean prepareWarFile()
	{
		// create file system first
		if( createWarFileSystem() )
		{
			// copy all from bin/../lib to soapui.home/war/WEB-INF/lib/
			File fromDir = new File( System.getProperty( "soapui.home" ), ".." + File.separator + "lib" );
			JarPackager.copyAllFromTo( fromDir, lib );
			if( includeExt )
			{
				// copy all from bin/ext to soapui.home/war/WEB-INF/lib/
				fromDir = new File( System.getProperty( "soapui.home" ), "ext" );
				JarPackager.copyAllFromTo( fromDir, lib );
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
			JarPackager.copyFileToDir( projectFile, soapuiDir );
			JarPackager.copyFileToDir( settingsFile, soapuiDir );
			// actions
			if( includeActions )
			{
				fromDir = new File( System.getProperty( "soapui.ext.actions" ) );
				JarPackager.copyAllFromTo( fromDir, actionsDir );
			}
			// listeners
			if( includeListeners )
			{
				fromDir = new File( System.getProperty( "soapui.ext.listeners" ) );
				JarPackager.copyAllFromTo( fromDir, listenersDir );
			}
			return true;
		}
		return false;
	}

	private boolean createWarFileSystem()
	{
		if( warDir.isDirectory() )
		{
			webInf = new File( warDir, "WEB-INF" );
			if( !( webInf.mkdir() || webInf.exists() ) )
			{
				UISupport.showErrorMessage( "Could not create directory " + webInf.getAbsolutePath() );
				return false;
			}
			else
			{
				clearDir(webInf);
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
				
			// this shouldn't be required, class is in main jar anyway!?
//				File classesDir = new File( webInf, "classes" );
//				
//				File packageDir = new File( classesDir, "com" + File.separator + "eviware" + File.separator + "soapui"
//						+ File.separator + "mockaswar" );
//				if( !( packageDir.mkdirs() || packageDir.exists() ) )
//				{
//					UISupport.showErrorMessage( "Could not create directory " + packageDir.getAbsolutePath() );
//					return false;
//				}
//				clearDir( packageDir );
//				extractServletClass( packageDir );
				
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
	 * @param dir
	 */
	private void clearDir( File dir)
	{
		for( File file : dir.listFiles())
			if( file.isFile())
				file.delete();
	}

	@SuppressWarnings( "unused" )
	private void extractServletClass( File packageDir )
	{
		try
		{
			JarFile soapJar = new JarFile( new File( System.getProperty( "soapui.home" ), "soapui-3.0.jar" ) );
			Enumeration<JarEntry> classList = soapJar.entries();
			while( classList.hasMoreElements() )
			{
				JarEntry soapClass = classList.nextElement();
				if( soapClass.getName().contains( "MockAsWarServlet.class" ) )
				{
					InputStream in = soapJar.getInputStream( soapClass );
					OutputStream out = new FileOutputStream( new File( packageDir, "MockAsWarServlet.class" ) );
					byte[] buffer = new byte[4096];
					int len =0;
					while( (len = in.read( buffer )) > 0 )
					{
						out.write( buffer, 0, len);
						out.flush();
					}
					in.close();
					out.close();
				}
			}
		}
		catch( IOException e )
		{
			log.error( e.getMessage(), e );
		}
	}
}
