package com.eviware.soapui.impl.wadl.inference.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;
import com.eviware.soapui.impl.wadl.inference.InferredSchema;
import com.eviware.soapui.impl.wadl.inference.schema.SchemaSystem;
import com.eviware.soapui.inferredSchema.SchemaSetConfig;

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

public class InferredSchemaImpl implements InferredSchema
{
	private SchemaSystem ss;

	public InferredSchemaImpl()
	{
		ss = new SchemaSystem();
	}

	public InferredSchemaImpl( InputStream is ) throws XmlException, IOException
	{
		ss = new SchemaSystem( SchemaSetConfig.Factory.parse( is ) );
	}

	public String[] getNamespaces()
	{
		return ss.getNamespaces().toArray( new String[0] );
	}

	public SchemaTypeSystem getSchemaTypeSystem()
	{
		return getSchemaTypeSystem( XmlBeans.getBuiltinTypeSystem() );
	}

	public SchemaTypeSystem getSchemaTypeSystem( SchemaTypeSystem sts )
	{
		List<XmlObject> schemas = new ArrayList<XmlObject>();
		try
		{
			for( String namespace : getNamespaces() )
			{
				schemas.add( XmlObject.Factory.parse( getXsdForNamespace( namespace ).toString() ) );
			}
			return XmlBeans.compileXsd( schemas.toArray( new XmlObject[0] ), sts, null );
		}
		catch( XmlException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public String getXsdForNamespace( String namespace )
	{
		return ss.getSchemaForNamespace( namespace ).toString();
	}

	public void learningValidate( XmlObject xml, ConflictHandler handler ) throws XmlException
	{
		ss.validate( xml, handler );
	}

	public void processValidXml( XmlObject xml ) throws XmlException
	{
		ss.validate( xml, new AllowAll() );
	}

	public void save( OutputStream os ) throws IOException
	{
		SchemaSetConfig xml = SchemaSetConfig.Factory.newInstance();
		ss.save( xml );
		xml.save( os );
	}

	public boolean validate( XmlObject xml )
	{
		try
		{
			ss.validate( xml, new DenyAll() );
			return true;
		}
		catch( XmlException e )
		{
			return false;
		}
	}

	public void deleteNamespace( String ns )
	{
		ss.deleteNamespace( ns );
	}

}
