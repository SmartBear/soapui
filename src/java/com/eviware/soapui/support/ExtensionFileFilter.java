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

package com.eviware.soapui.support;

import java.io.File;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

/**
 * FileFilter for a specified extensions
 */

final public class ExtensionFileFilter extends FileFilter
{
	private final String extension;
	private final String description;

	public ExtensionFileFilter( String extension, String description )
	{
		this.extension = extension.toLowerCase();
		this.description = description;
	}

	public boolean accept( File f )
	{
		return f.isDirectory() || "*".equals( extension )
				|| f.getName().toLowerCase( Locale.getDefault() ).endsWith( extension );
	}

	public String getDescription()
	{
		return description;
	}
}