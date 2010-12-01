/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.check.actions;

import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Configures the specified SecurityCheck
 * 
 * @author dragica.soldo
 */

public class ConfigureSecurityCheckAction extends AbstractSoapUIAction<SecurityCheck>
{
//	public static final String SOAPUI_ACTION_ID = "ConfigureAssertionAction";
	public static final String SOAPUI_ACTION_ID = "ConfigureSecurityCheckAction";

	public ConfigureSecurityCheckAction()
	{
		super( "Configure", "Configures this security check" );
	}

	public void perform( SecurityCheck target, Object param )
	{
		target.configure();
	}
}