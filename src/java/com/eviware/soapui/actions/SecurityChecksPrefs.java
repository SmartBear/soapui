/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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

import javax.swing.JLabel;

import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

public class SecurityChecksPrefs implements Prefs
{

	private SimpleForm securityChecksForm;
	private final String title;

	public SecurityChecksPrefs( String title )
	{
		this.title = title;
	}

	public SimpleForm getForm()
	{
		if( securityChecksForm == null )
		{
			securityChecksForm = new SimpleForm();

			PropertyHolderTable propertyHolderTable = new PropertyHolderTable( PropertyExpansionUtils
					.getSecurityGlobalProperties() );
			propertyHolderTable.setPreferredSize( new Dimension( 200, 300 ) );
			securityChecksForm.append( new JLabel( title ) );
			securityChecksForm.addSpace();
			securityChecksForm.addComponent( propertyHolderTable );
		}

		return securityChecksForm;
	}

	public void getFormValues( Settings settings )
	{
	}

	public String getTitle()
	{
		return "Security Checks Properties";
	}

	public StringToStringMap getValues( Settings settings )
	{
		return null;
	}

	public void setFormValues( Settings settings )
	{
		PropertyExpansionUtils.saveSecurityGlobalProperties();
	}

	public void storeValues( StringToStringMap values, Settings settings )
	{
		
	}
}
