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

import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones the specified SecurityCheck
 * 
 * @author dragica.soldo
 */

public class CloneSecurityCheckAction extends AbstractSoapUIAction<SecurityCheck>
{
	// public static final String SOAPUI_ACTION_ID = "CloneAssertionAction";
	public static final String SOAPUI_ACTION_ID = "CloneSecurityCheckAction";

	public CloneSecurityCheckAction()
	{
		super( "Clone", "Clones this securityCheck" );
	}

	public void perform( SecurityCheck target, Object param )
	{
		String name = target.getName();

		while( target.getName().equals( name ) )
		{
			name = UISupport.prompt( "Specify unique name for cloned security check", "Clone SecurityCheck", target
					.getName() );
			if( name == null )
				return;
		}

		SecurityCheck securityCheck = target.getSecurable().cloneSecurityCheck( target, name );

		// if( securityCheck.isConfigurable() )
		securityCheck.configure();
	}
}