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

package com.eviware.soapui.support.xml;

import org.apache.xmlbeans.XmlObject;

/**
 * Marker interface for objects that can be saved/restored to/from an XmlObject
 * 
 * @author Ole.Matzura
 */

public interface XmlObjectPersistable
{
	/**
	 * Persisits this object to an XmlObject
	 * 
	 * @return the persisted XmlObject
	 */

	public XmlObject save();

	/**
	 * Restores this object from the specified XmlObject
	 * 
	 * @param xmlObject
	 *           the xmlObject to restore from
	 */

	public void restore( XmlObject xmlObject );
}
