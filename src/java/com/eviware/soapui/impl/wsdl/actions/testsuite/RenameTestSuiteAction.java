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

package com.eviware.soapui.impl.wsdl.actions.testsuite;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlTestSuite
 * 
 * @author Ole.Matzura
 */

public class RenameTestSuiteAction extends AbstractSoapUIAction<WsdlTestSuite>
{
	public RenameTestSuiteAction()
	{
		super( "Rename", "Renames this TestSuite" );
		// putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "F2" ));
	}

	public void perform( WsdlTestSuite testSuite, Object param )
	{
		String name = UISupport.prompt( "Specify name of TestSuite", "Rename TestSuite", testSuite.getName() );
		if( name == null || name.equals( testSuite.getName() ) )
			return;

		testSuite.setName( name );
	}
}
