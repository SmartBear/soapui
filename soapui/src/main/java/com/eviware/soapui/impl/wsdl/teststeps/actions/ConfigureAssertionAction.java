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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Configures the specified WsdlMessageAssertion
 * 
 * @author ole.matzura
 */

public class ConfigureAssertionAction extends AbstractSoapUIAction<WsdlMessageAssertion>
{
	public static final String SOAPUI_ACTION_ID = "ConfigureAssertionAction";

	public ConfigureAssertionAction()
	{
		super( "Configure", "Configures this assertion" );
	}

	public void perform( WsdlMessageAssertion target, Object param )
	{
		target.configure();
	}
}
