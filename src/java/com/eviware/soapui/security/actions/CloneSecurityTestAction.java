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

package com.eviware.soapui.security.actions;

import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a SecurityTest
 * 
 * @author Ole.Matzura
 */

public class CloneSecurityTestAction extends AbstractSoapUIAction<SecurityTest>
{
	public CloneSecurityTestAction()
	{
		super( "Clone SecurityTest", "Clones this SecurityTest" );
	}

	public void perform( SecurityTest securityTest, Object param )
	{
		String name = UISupport.prompt( "Specify name of cloned SecurityTest", "Clone SecurityTest", "Copy of "
				+ securityTest.getName() );
		if( name == null )
			return;

		SecurityTest newSecurityTest = securityTest.getTestCase().cloneSecurityTest( securityTest, name );
		UISupport.selectAndShow( newSecurityTest );
	}
}
