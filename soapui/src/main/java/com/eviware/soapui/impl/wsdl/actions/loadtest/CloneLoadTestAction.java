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

package com.eviware.soapui.impl.wsdl.actions.loadtest;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a WsdlLoadTest
 * 
 * @author Ole.Matzura
 */

public class CloneLoadTestAction extends AbstractSoapUIAction<WsdlLoadTest>
{
	public CloneLoadTestAction()
	{
		super( "Clone LoadTest", "Clones this LoadTest" );
	}

	public void perform( WsdlLoadTest loadTest, Object param )
	{
		String name = UISupport.prompt( "Specify name of cloned LoadTest", "Clone LoadTest",
				"Copy of " + loadTest.getName() );
		if( name == null )
			return;

		WsdlLoadTest newLoadTest = loadTest.getTestCase().cloneLoadTest( loadTest, name );
		UISupport.selectAndShow( newLoadTest );
	}
}
