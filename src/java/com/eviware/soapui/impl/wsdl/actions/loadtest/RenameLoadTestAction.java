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

package com.eviware.soapui.impl.wsdl.actions.loadtest;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlLoadTest
 * 
 * @author Ole.Matzura
 */

public class RenameLoadTestAction extends AbstractSoapUIAction<WsdlLoadTest>
{
	public RenameLoadTestAction()
	{
		super( "Rename", "Renames this LoadTest" );
	}

	public void perform( WsdlLoadTest loadTest, Object param )
	{
		String name = UISupport.prompt( "Specify name of LoadTest", "Rename LoadTest", loadTest.getName() );
		if( name == null || name.equals( loadTest.getName() ) )
			return;

		loadTest.setName( name );
	}
}
