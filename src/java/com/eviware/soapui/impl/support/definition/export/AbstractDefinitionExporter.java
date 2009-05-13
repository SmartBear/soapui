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

package com.eviware.soapui.impl.support.definition.export;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.support.definition.InterfaceDefinition;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringMap;

public abstract class AbstractDefinitionExporter<T extends Interface> implements DefinitionExporter
{
	private InterfaceDefinition<T> definition;

	public AbstractDefinitionExporter( InterfaceDefinition<T> definition )
	{
		this.definition = definition;
	}

	public InterfaceDefinition<T> getDefinition()
	{
		return definition;
	}

	public void setDefinition( InterfaceDefinition<T> definition )
	{
		this.definition = definition;
	}

	public String export( String folderName ) throws Exception
	{
		if( definition.getDefinitionCache() == null || !definition.getDefinitionCache().validate() )
			throw new Exception( "Definition is not cached for export" );

		File outFolder = new File( folderName );
		if( !outFolder.exists() && !outFolder.mkdirs() )
			throw new Exception( "Failed to create directory [" + folderName + "]" );

		Map<String, String> urlToFileMap = new HashMap<String, String>();

		setFilenameForPart( definition.getDefinitionCache().getRootPart(), urlToFileMap, null );

		List<InterfaceDefinitionPart> partList = definition.getDefinitionCache().getDefinitionParts();
		for( InterfaceDefinitionPart part : partList )
		{
			setFilenameForPart( part, urlToFileMap, null );
		}

		for( InterfaceDefinitionPart part : partList )
		{
			XmlObject obj = XmlObject.Factory.parse( part.getContent() );
			replaceImportsAndIncludes( obj, urlToFileMap, part.getUrl() );
			obj.save( new File( outFolder, urlToFileMap.get( part.getUrl() ) ) );
		}

		return folderName + File.separatorChar
				+ urlToFileMap.get( definition.getDefinitionCache().getRootPart().getUrl() );
	}

	public StringToStringMap createFilesForExport( String urlPrefix ) throws Exception
	{
		StringToStringMap result = new StringToStringMap();
		Map<String, String> urlToFileMap = new HashMap<String, String>();

		if( urlPrefix == null )
			urlPrefix = "";

		setFilenameForPart( definition.getDefinitionCache().getRootPart(), urlToFileMap, urlPrefix );

		List<InterfaceDefinitionPart> partList = definition.getDefinitionCache().getDefinitionParts();
		for( InterfaceDefinitionPart part : partList )
		{
			if( !part.isRootPart() )
				setFilenameForPart( part, urlToFileMap, urlPrefix );
		}

		for( InterfaceDefinitionPart part : partList )
		{
			XmlObject obj = XmlObject.Factory.parse( part.getContent() );
			replaceImportsAndIncludes( obj, urlToFileMap, part.getUrl() );
			String urlString = urlToFileMap.get( part.getUrl() );
			if( urlString.startsWith( urlPrefix ) )
				urlString = urlString.substring( urlPrefix.length() );

			result.put( urlString, obj.xmlText() );

			if( part.isRootPart() )
				result.put( "#root#", urlString );
		}

		return result;
	}

	private void setFilenameForPart( InterfaceDefinitionPart part, Map<String, String> urlToFileMap, String urlPrefix )
			throws MalformedURLException
	{

		String path = part.getUrl();

		try
		{
			URL url = new URL( path );
			path = url.getPath();
		}
		catch( MalformedURLException e )
		{
		}

		int ix = path.lastIndexOf( '/' );
		String fileName = ix == -1 ? path : path.substring( ix + 1 );

		ix = fileName.lastIndexOf( '.' );
		if( ix != -1 )
			fileName = fileName.substring( 0, ix );

		String type = part.getType();

		if( type.equals( Constants.WSDL11_NS ) )
			fileName += ".wsdl";
		else if( part.getType().equals( Constants.XSD_NS ) )
			fileName += ".xsd";
		else if( part.getType().equals( Constants.WADL10_NS ) )
			fileName += ".wadl";
		else
			fileName += ".xml";

		if( urlPrefix != null )
			fileName = urlPrefix + fileName;

		int cnt = 1;
		while( urlToFileMap.containsValue( fileName ) )
		{
			ix = fileName.lastIndexOf( '.' );
			fileName = fileName.substring( 0, ix ) + "_" + cnt + fileName.substring( ix );
			cnt++ ;
		}

		urlToFileMap.put( part.getUrl(), fileName );
	}

	private void replaceImportsAndIncludes( XmlObject xmlObject, Map<String, String> urlToFileMap, String baseUrl )
			throws Exception
	{
		String[] paths = getLocationXPathsToReplace();

		for( String path : paths )
		{
			XmlObject[] locations = xmlObject.selectPath( path );

			for( int i = 0; i < locations.length; i++ )
			{
				SimpleValue wsdlImport = ( ( SimpleValue )locations[i] );
				replaceLocation( urlToFileMap, baseUrl, wsdlImport );
			}
		}
	}

	protected abstract String[] getLocationXPathsToReplace();

	private void replaceLocation( Map<String, String> urlToFileMap, String baseUrl, SimpleValue wsdlImport )
			throws Exception
	{
		String location = wsdlImport.getStringValue();
		if( location != null )
		{
			if( location.startsWith( "file:" ) || location.indexOf( "://" ) > 0 )
			{
				String newLocation = urlToFileMap.get( location );
				if( newLocation != null )
					wsdlImport.setStringValue( newLocation );
				else
					throw new Exception( "Missing local file for [" + newLocation + "]" );
			}
			else
			{
				String loc = Tools.joinRelativeUrl( baseUrl, location );
				String newLocation = urlToFileMap.get( loc );
				if( newLocation != null )
					wsdlImport.setStringValue( newLocation );
				else
					throw new Exception( "Missing local file for [" + loc + "]" );
			}
		}
	}

}
