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

package com.eviware.soapui.security.actions;

import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.StringUtils;
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
		if( StringUtils.isNullOrEmpty( name ) )
			return;

		while( securityTest.getTestCase().getSecurityTestByName( name.trim() ) != null )
		{
			name = UISupport.prompt( "Specify unique name of SecurityTest", "Clone SecurityTest", name );
			if( StringUtils.isNullOrEmpty( name ) )
				return;
		}

		SecurityTest newSecurityTest = securityTest.getTestCase().cloneSecurityTest( securityTest, name );
		UISupport.selectAndShow( newSecurityTest );
	}
}
