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

package com.eviware.soapui.impl.wsdl.actions.testcase;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a new SecurityTest to a WsdlTestCase
 * 
 */

public class AddNewSecurityTestAction extends AbstractSoapUIAction<WsdlTestCase>
{
	public static final String SOAPUI_ACTION_ID = "AddNewSecurityTestAction";

	public AddNewSecurityTestAction()
	{
		super( "New SecurityTest", "Creates a new SecurityTest for this TestCase" );
	}

	public void perform( WsdlTestCase testCase, Object param )
	{
		String name = UISupport.prompt( "Specify name of SecurityTest", "New SecurityTest",
				"SecurityTest " + ( testCase.getSecurityTestCount() + 1 ) );
		if( StringUtils.isNullOrEmpty( name ) )
			return;

		while( testCase.getSecurityTestByName( name.trim() ) != null )
		{
			name = UISupport.prompt( "Specify unique name of SecurityTest", "Rename SecurityTest", name );
			if( StringUtils.isNullOrEmpty( name ) )
				return;
		}

		SecurityTest securityTest = testCase.addNewSecurityTest( name );
		UISupport.selectAndShow( securityTest );
	}
}
