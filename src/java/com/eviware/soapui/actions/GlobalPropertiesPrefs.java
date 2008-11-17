/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import java.awt.Dimension;

import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

public class GlobalPropertiesPrefs implements Prefs
{
   public final static String ENABLE_OVERRIDE = "Enable Override";
	
	public SimpleForm getForm()
	{
		SimpleForm form = new SimpleForm();
		
		PropertyHolderTable propertyHolderTable = new PropertyHolderTable( PropertyExpansionUtils.getGlobalProperties() );
		propertyHolderTable.setPreferredSize( new Dimension( 200, 300 ) );
		form.addComponent( propertyHolderTable);
		form.addSpace();
		form.appendCheckBox( ENABLE_OVERRIDE, "Enables overriding of any property-reference with global properties", false );
		
		return form;
	}

	public void getFormValues( Settings settings )
	{
	}

	public String getTitle()
	{
		return "Global Properties";
	}

	public StringToStringMap getValues( Settings settings )
	{
		return null;
	}

	public void setFormValues( Settings settings )
	{
      
	}

	public void storeValues( StringToStringMap values, Settings settings )
	{
	}
}
