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

package com.eviware.soapui.support.editor.xml;

import org.apache.xmlbeans.SchemaTypeSystem;

import com.eviware.soapui.support.PropertyChangeNotifier;
import com.eviware.soapui.support.editor.EditorDocument;

/**
 * Document class used by XmlEditors
 * 
 * @author ole.matzura
 */

public interface XmlDocument extends PropertyChangeNotifier, EditorDocument
{
	public final static String XML_PROPERTY = XmlDocument.class.getName() + "@xml";

	public String getXml();

	public void setXml( String xml );

	public SchemaTypeSystem getTypeSystem();
}
