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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.teststeps.AbstractPathPropertySupport;

public class PathPropertyExternalDependency implements ExternalDependency
{
	private final AbstractPathPropertySupport pathProperty;
	private final Type type;

	public PathPropertyExternalDependency( AbstractPathPropertySupport pathProperty )
	{
		this( pathProperty, Type.FILE );
	}

	public PathPropertyExternalDependency( AbstractPathPropertySupport pathProperty, Type type )
	{
		this.pathProperty = pathProperty;
		this.type = type;
	}

	@Override
	public String getPath()
	{
		return pathProperty.expand();
	}

	@Override
	public Type getType()
	{
		return type;
	}

	@Override
	public void updatePath( String path )
	{
		pathProperty.set( path, true );
	}
}
