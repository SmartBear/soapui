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

package com.eviware.soapui.impl.wsdl.support;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;

public class PathUtils
{
	public static String getAbsoluteFolder( String path )
	{
		File folder = new File( path );

		if( !folder.exists() )
			return null;

		if( folder.isDirectory() )
			return folder.getAbsolutePath();

		File parentFile = folder.getParentFile();
		return parentFile == null ? null : parentFile.getAbsolutePath();
	}

	public static String expandPath( String path, AbstractWsdlModelItem<?> modelItem )
	{
		return expandPath( path, modelItem, null );
	}

	public static String expandPath( String path, AbstractWsdlModelItem<?> modelItem, PropertyExpansionContext context )
	{
		path = context == null ? PropertyExpansionUtils.expandProperties( modelItem, path ) : PropertyExpansionUtils
				.expandProperties( context, path );

		if( !isRelativePath( path ) )
			return path;

		String root = getExpandedResourceRoot( modelItem, context );
		if( StringUtils.isNullOrEmpty( root ) || StringUtils.isNullOrEmpty( root ) )
			return path;

		if( isHttpPath( root ) )
			root += "/";
		else
			root += File.separatorChar;

		return Tools.joinRelativeUrl( root, path );
	}

	public static String adjustRelativePath( String str, String root, ModelItem contextModelItem )
	{
		if( StringUtils.isNullOrEmpty( root ) || StringUtils.isNullOrEmpty( str ) )
			return str;

		if( !isRelativePath( str ) )
			return str;

		root = PropertyExpansionUtils.expandProperties( contextModelItem, root );

		if( isHttpPath( root ) )
			root += "/";
		else
			root += File.separatorChar;

		return Tools.joinRelativeUrl( root, str );

		// if( isHttpPath( str ))
		// return root + '/' + str;
		// else
		// return root + File.separatorChar + str;
	}

	public static boolean isHttpPath( String str )
	{
		if( StringUtils.isNullOrEmpty( str ) )
			return false;

		str = str.toLowerCase();

		return str.startsWith( "http:/" ) || str.startsWith( "https:/" );
	}

	public static boolean isRelativePath( String str )
	{
		if( StringUtils.isNullOrEmpty( str ) )
			return false;

		str = str.toLowerCase();

		return !str.startsWith( "/" ) && !str.startsWith( "\\" ) && !str.startsWith( "http:/" )
				&& !str.startsWith( "https:/" ) && str.indexOf( ":\\" ) != 1 && !str.startsWith( "file:" )
				&& str.indexOf( ":/" ) != 1;
	}

	public static String createRelativePath( String path, String root, ModelItem contextModelItem )
	{
		if( StringUtils.isNullOrEmpty( root ) )
			return path;

		root = PropertyExpansionUtils.expandProperties( contextModelItem, root );

		return relativize( path, root );
	}

	public static String relativizeResourcePath( String path, ModelItem modelItem )
	{
		if( modelItem == null || StringUtils.isNullOrEmpty( path ) || isRelativePath( path ) || isHttpPath( path ) )
			return path;

		Project project = ModelSupport.getModelItemProject( modelItem );
		if( project == null )
			return path;

		if( StringUtils.isNullOrEmpty( project.getPath() ) && project.getResourceRoot().indexOf( "${projectDir}" ) >= 0 )
		{
			if( UISupport.confirm( "Save project before setting path?", "Project has not been saved" ) )
			{
				try
				{
					project.save();
				}
				catch( IOException e )
				{
					SoapUI.logError( e );
					UISupport.showErrorMessage( e );
					return path;
				}
			}
		}

		String projectPath = PropertyExpansionUtils.expandProperties( project, project.getResourceRoot() );
		if( StringUtils.isNullOrEmpty( projectPath ) )
			return path;

		return PathUtils.relativize( path, projectPath );
	}

	public static String resolveResourcePath( String path, ModelItem modelItem )
	{
		if( path == null || modelItem == null )
			return path;

		path = PathUtils.denormalizePath( path );
		path = PropertyExpansionUtils.expandProperties( new DefaultPropertyExpansionContext( modelItem ), path );

		String prefix = "";

		if( path.startsWith( "file:" ) )
		{
			prefix = path.substring( 0, 5 );
			path = path.substring( 5 );
		}

		if( PathUtils.isAbsolutePath( path ) )
			return prefix + path;

		WsdlProject project = ( WsdlProject )ModelSupport.getModelItemProject( modelItem );
		if( project == null )
			return prefix + path;

		String resourceRoot = getExpandedResourceRoot( modelItem );

		if( StringUtils.hasContent( resourceRoot ) && !resourceRoot.endsWith( File.separator ) )
			resourceRoot += File.separator;

		String result = Tools.joinRelativeUrl( resourceRoot, path );
		return prefix + result;
	}

	public static String relativize( String path, String rootPath )
	{
		if( StringUtils.isNullOrEmpty( path ) || StringUtils.isNullOrEmpty( rootPath ) )
			return path;

		if( path.toLowerCase().startsWith( "http:/" ) || path.toLowerCase().startsWith( "https:/" ) )
		{
			String prefix = "";

			while( rootPath != null )
			{
				if( path.startsWith( rootPath ) )
				{
					path = path.substring( rootPath.length() );
					if( path.startsWith( "/" ) )
						path = path.substring( 1 );

					break;
				}
				else
				{
					int ix = rootPath.lastIndexOf( '/' );
					rootPath = ix == -1 ? null : rootPath.substring( 0, ix );
					prefix += "../";
				}
			}

			return prefix + path;
		}
		else
		{
			String prefix = "";

			// file url?
			if( path.toLowerCase().startsWith( "file:" ) )
			{
				try
				{
					path = new File( new URL( path ).toURI() ).getAbsolutePath();
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

			if( rootPath.startsWith( "file:" ) )
			{
				try
				{
					rootPath = new File( new URL( rootPath ).toURI() ).getAbsolutePath();
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

			// different drives on windows? (can't relativize)
			if( rootPath.toUpperCase().charAt( 0 ) != path.toUpperCase().charAt( 0 ) && rootPath.indexOf( ":\\" ) == 1
					&& path.indexOf( ":\\" ) == 1 )
			{
				return path;
			}

			while( rootPath != null )
			{
				if( path.startsWith( rootPath ) )
				{
					path = path.substring( rootPath.length() );
					if( path.startsWith( File.separator ) )
						path = path.substring( 1 );

					break;
				}
				else
				{
					File file = new File( rootPath );
					rootPath = file.getParent();
					prefix += ".." + File.separatorChar;
				}
			}

			return prefix + path;
		}
	}

	public static boolean isAbsolutePath( String path )
	{
		return !isRelativePath( path );
	}

	public static boolean isFilePath( String path )
	{
		if( StringUtils.isNullOrEmpty( path ) )
			return false;

		return !isHttpPath( path );
	}

	public static String normalizePath( String path )
	{
		if( StringUtils.isNullOrEmpty( path ) )
			return path;

		return File.separatorChar == '/' ? path : path.replace( File.separatorChar, '/' );
	}

	public static String denormalizePath( String path )
	{
		if( StringUtils.isNullOrEmpty( path ) )
			return path;

		if( isHttpPath( path ) )
			return path;

		return File.separatorChar == '/' ? path.replace( '\\', File.separatorChar ) : path.replace( '/',
				File.separatorChar );
	}

	public static String getExpandedResourceRoot( ModelItem modelItem )
	{
		return getExpandedResourceRoot( modelItem, null );
	}

	public static String getExpandedResourceRoot( ModelItem modelItem, PropertyExpansionContext context )
	{
		if( !( modelItem instanceof AbstractWsdlModelItem<?> ) )
			return null;

		WsdlProject project = ( WsdlProject )ModelSupport.getModelItemProject( modelItem );
		if( project == null )
			return null;

		String docroot = project.getResourceRoot();
		if( !StringUtils.hasContent( docroot ) )
			return new File( "" ).getAbsolutePath();

		docroot = context == null ? PropertyExpansionUtils.expandProperties( modelItem, docroot )
				: PropertyExpansionUtils.expandProperties( context, docroot );

		return docroot;
	}

	public static String ensureFilePathIsUrl( String url )
	{
		if( isFilePath( url ) && !url.startsWith( "file:" ) )
		{
			try
			{
				return new File( url ).toURI().toURL().toString();
			}
			catch( MalformedURLException e )
			{
				e.printStackTrace();
			}
		}

		return url;
	}
}
