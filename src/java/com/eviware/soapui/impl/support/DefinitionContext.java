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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.impl.support.definition.InterfaceDefinition;
import com.eviware.soapui.model.iface.Interface;

public interface DefinitionContext<T extends Interface>
{
	public boolean hasSchemaTypes();

	public boolean isCached();

	public T getInterface();

	public InterfaceDefinition<T> getInterfaceDefinition() throws Exception;

	public String export( String path ) throws Exception;

	public boolean loadIfNecessary() throws Exception;
}
