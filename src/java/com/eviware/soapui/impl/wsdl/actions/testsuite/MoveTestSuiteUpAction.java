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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Moves the specified WsdlTestCase up one step in the WsdlTestSuites list of
 * WsdlTestCases
 * 
 * @author ole.matzura
 */

public class MoveTestSuiteUpAction extends AbstractSoapUIAction<WsdlTestSuite>
{
	public MoveTestSuiteUpAction()
	{
		super( "Move TestSuite Up", "Moves this TestSuite up" );
	}

	public void perform( WsdlTestSuite testSuite, Object param )
	{
		WsdlProject project = testSuite.getProject();
		int ix = project.getIndexOfTestSuite( testSuite );
		if( ix == -1 || ix == 0 )
			return;

		project.moveTestSuite( ix, -1 );
		UISupport.select( testSuite );
	}
}
