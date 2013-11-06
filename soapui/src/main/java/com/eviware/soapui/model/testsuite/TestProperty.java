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

package com.eviware.soapui.model.testsuite;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.model.ModelItem;

public interface TestProperty
{
	public String getName();

	public String getDescription();

	public String getValue();

	public String getDefaultValue();

	public void setValue( String value );

	public boolean isReadOnly();

	public QName getType();

	/**
	 * Gets the modelItem containing this property
	 * 
	 * @return the modelItem containing this property
	 */

	public ModelItem getModelItem();

	/**
	 * defines if specific property belongs to request part
	 */
	public boolean isRequestPart();

	public SchemaType getSchemaType();
}
