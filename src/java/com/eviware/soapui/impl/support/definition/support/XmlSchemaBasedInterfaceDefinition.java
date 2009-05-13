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

package com.eviware.soapui.impl.support.definition.support;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaException;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;

public abstract class XmlSchemaBasedInterfaceDefinition<T extends AbstractInterface<?>> extends
		AbstractInterfaceDefinition<T>
{
	private SchemaTypeSystem schemaTypes;
	private SchemaTypeLoader schemaTypeLoader;

	public XmlSchemaBasedInterfaceDefinition( T iface )
	{
		super( iface );
	}

	public SchemaTypeLoader getSchemaTypeLoader()
	{
		return schemaTypeLoader;
	}

	public SchemaTypeSystem getSchemaTypeSystem()
	{
		return schemaTypes;
	}

	public boolean hasSchemaTypes()
	{
		return schemaTypes != null;
	}

	public Collection<String> getDefinedNamespaces() throws Exception
	{
		Set<String> namespaces = new HashSet<String>();

		SchemaTypeSystem schemaTypes = getSchemaTypeSystem();
		if( schemaTypes != null )
		{
			namespaces.addAll( SchemaUtils.extractNamespaces( getSchemaTypeSystem(), true ) );
		}

		namespaces.add( getTargetNamespace() );

		return namespaces;
	}

	public SchemaType findType( QName typeName )
	{
		return getSchemaTypeLoader().findType( typeName );
	}

	public void loadSchemaTypes( DefinitionLoader loader ) throws SchemaException
	{
		schemaTypes = SchemaUtils.loadSchemaTypes( loader.getBaseURI(), loader );
		schemaTypeLoader = XmlBeans.typeLoaderUnion( new SchemaTypeLoader[] { schemaTypes,
				XmlBeans.getBuiltinTypeSystem() } );
	}
}
