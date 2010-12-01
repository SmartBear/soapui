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
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes the specified SecurityCheck from its TestStep
 * 
 * @author dragica.soldo
 */

public class DeleteSecurityCheckAction extends AbstractSoapUIAction<SecurityCheck>
{
	public DeleteSecurityCheckAction()
	{
		super( "Remove", "Removes this assertion from its request" );
	}

	public void perform( SecurityCheck target, Object param )
	{
		if( UISupport.confirm( "Remove securityCheck [" + target.getName() + "] from [" + target.getName() + "]",
				"Remove SecurityCheck" ) )
		{
			target.getSecurable().removeSecurityCheck( target );
		}
	}
}