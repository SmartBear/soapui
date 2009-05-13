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

package com.eviware.x.dialogs;

import java.io.File;

/**
 * 
 * @author Lars
 */
public interface XFileDialogs
{
	File saveAs( Object action, String title, String extension, String fileType, File defaultFile );

	File saveAs( Object action, String title );

	File saveAsDirectory( Object action, String title, File defaultDirectory );

	File open( Object action, String title, String extension, String fileType, String current );

	File openXML( Object action, String title );

	File openDirectory( Object action, String string, File defaultDirectory );

	File openFileOrDirectory( Object action, String title, File defaultDirectory );
}
