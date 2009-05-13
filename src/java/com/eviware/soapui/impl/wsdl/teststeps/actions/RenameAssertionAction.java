/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames the specified assertion
 * 
 * @author ole.matzura
 */

public class RenameAssertionAction extends AbstractSoapUIAction<WsdlMessageAssertion>
{
	public RenameAssertionAction()
	{
		super( "Rename", "Renames this assertion" );
	}

	public void perform( WsdlMessageAssertion target, Object param )
	{
		String name = UISupport.prompt( "Specify name for this assertion", "Rename Assertion", target.getName() );
		if( name == null || name.equals( target.getName() ) )
			return;

		target.setName( name );
	}
}