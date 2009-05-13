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

package com.eviware.soapui.impl.support.definition;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;

import com.eviware.soapui.model.iface.Interface;

public interface InterfaceDefinition<T extends Interface>
{
	public String getTargetNamespace();

	public SchemaTypeLoader getSchemaTypeLoader();

	public SchemaTypeSystem getSchemaTypeSystem();

	public boolean hasSchemaTypes();

	public Collection<String> getDefinedNamespaces() throws Exception;

	public SchemaType findType( QName name );

	public DefinitionCache getDefinitionCache();

	public T getInterface();
}
