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

package com.eviware.soapui.impl.rest.panels.request.inspectors.schema;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wadl.inference.InferredSchema;

/**
 * @author Dain.Nilsson
 * 
 */
public class InferredSchemaManager
{
	private static Map<RestService, InferredSchema> schemas;
	private static Map<RestService, PropertyChangeSupport> propertyChangeSupports;
	private static Map<String, String> filenames;
	private static Map<String, String> rFilenames;

	static
	{
		schemas = new HashMap<RestService, InferredSchema>();
		propertyChangeSupports = new HashMap<RestService, PropertyChangeSupport>();
		filenames = new HashMap<String, String>();
		rFilenames = new HashMap<String, String>();
	}

	public static String filenameForNamespace( String namespace )
	{
		if( !filenames.containsKey( namespace ) )
		{
			filenames.put( namespace, generateFilename( namespace ) );
			rFilenames.put( filenames.get( namespace ), namespace );
		}
		return filenames.get( namespace );
	}

	public static String namespaceForFilename( String filename )
	{
		if( !rFilenames.containsKey( filename ) )
		{
			for( InferredSchema is : schemas.values() )
			{
				for( String ns : is.getNamespaces() )
				{
					if( filenameForNamespace( ns ).equals( filename ) )
						return ns;
				}
			}
		}
		else
			return rFilenames.get( filename );
		return null;
	}

	private static String generateFilename( String namespace )
	{
		if( namespace.equals( "" ) )
			return "unnamed.xsd";
		return namespace.replaceAll( "[^a-zA-Z0-9]", "" ) + ".xsd";
	}

	public static InferredSchema getInferredSchema( RestService service )
	{
		if( !schemas.containsKey( service ) )
		{
			try
			{
				schemas.put( service,
						InferredSchema.Factory.parse( new ByteArrayInputStream( service.getInferredSchema().getBytes() ) ) );
			}
			catch( Exception e )
			{
				schemas.put( service, InferredSchema.Factory.newInstance() );
			}
			propertyChangeSupports.put( service, new PropertyChangeSupport( schemas.get( service ) ) );
		}
		return schemas.get( service );
	}

	public static void save( RestService service )
	{
		if( schemas.containsKey( service ) )
		{
			OutputStream out = new ByteArrayOutputStream();
			String old = service.getInferredSchema();
			try
			{
				schemas.get( service ).save( out );
				service.setInferredSchema( out.toString() );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			propertyChangeSupports.get( service ).firePropertyChange( "inferredSchema", old, out.toString() );
		}
	}

	public static void release( RestService service )
	{
		schemas.remove( service );
		propertyChangeSupports.remove( service );
	}

	public static void delete( RestService service )
	{
		service.setInferredSchema( null );
		if( schemas.containsKey( service ) )
		{
			schemas.remove( service );
		}
		propertyChangeSupports.get( service ).firePropertyChange( "inferredSchema", service.getInferredSchema(), null );
	}

	public static void addPropertyChangeListener( RestService service, PropertyChangeListener listener )
	{
		if( getInferredSchema( service ) != null )
			propertyChangeSupports.get( service ).addPropertyChangeListener( "inferredSchema", listener );
	}

	public static void removePropertyChangeListener( RestService service, PropertyChangeListener listener )
	{
		propertyChangeSupports.get( service ).removePropertyChangeListener( "inferredSchema", listener );
	}

	public static void deleteNamespace( RestService service, String ns )
	{
		getInferredSchema( service ).deleteNamespace( ns );
		save( service );
	}
}
