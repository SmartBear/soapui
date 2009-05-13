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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action for adding a new WsdlTestSuite to a WsdlProject
 * 
 * @author Ole.Matzura
 */

public class AddNewTestSuiteAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "AddNewTestSuiteAction";

	public AddNewTestSuiteAction()
	{
		super( "New TestSuite", "Creates a new TestSuite in this project" );
	}

	public void perform( WsdlProject target, Object param )
	{
		createTestSuite( target );
	}

	public WsdlTestSuite createTestSuite( WsdlProject project )
	{
		String name = UISupport.prompt( "Specify name of TestSuite", "New TestSuite", "TestSuite "
				+ ( project.getTestSuiteCount() + 1 ) );
		if( name == null )
			return null;

		WsdlTestSuite testSuite = project.addNewTestSuite( name );
		UISupport.showDesktopPanel( testSuite );
		return testSuite;
	}
}
