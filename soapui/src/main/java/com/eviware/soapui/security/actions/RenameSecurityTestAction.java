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
 * Renames a SecurityTest
 * 
 * @author Ole.Matzura
 */

public class RenameSecurityTestAction extends AbstractSoapUIAction<SecurityTest>
{
	public RenameSecurityTestAction()
	{
		super( "Rename", "Renames this SecurityTest" );
	}

	public void perform( SecurityTest securityTest, Object param )
	{
		String name = UISupport.prompt( "Specify name of SecurityTest", "Rename SecurityTest", securityTest.getName() );
		if( StringUtils.isNullOrEmpty( name ) || name.equals( securityTest.getName() ) )
			return;

		while( securityTest.getTestCase().getSecurityTestByName( name.trim() ) != null )
		{
			name = UISupport.prompt( "Specify unique name of SecurityTest", "Rename SecurityTest", securityTest.getName() );
			if( StringUtils.isNullOrEmpty( name ) || name.equals( securityTest.getName() ) )
				return;
		}

		securityTest.setName( name );
	}
}
