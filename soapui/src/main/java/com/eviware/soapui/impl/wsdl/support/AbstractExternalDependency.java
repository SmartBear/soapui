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

import java.io.File;

public abstract class AbstractExternalDependency implements ExternalDependency
{
	private final String path;

	public AbstractExternalDependency( String path )
	{
		this.path = path;
	}

	@Override
	public String getPath()
	{
		return path;
	}

	@Override
	public Type getType()
	{
		File file = new File( path );
		if( file.exists() )
		{
			return file.isDirectory() ? Type.FOLDER : Type.FILE;
		}

		return Type.UNKNOWN;
	}
}
