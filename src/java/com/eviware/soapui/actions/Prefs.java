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

package com.eviware.soapui.actions;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Interface for a preferences page
 * 
 * @author ole.matzura
 */

public interface Prefs
{
	public SimpleForm getForm();

	public void setFormValues( Settings settings );

	public void getFormValues( Settings settings );

	public void storeValues( StringToStringMap values, Settings settings );

	public StringToStringMap getValues( Settings settings );

	public String getTitle();
}