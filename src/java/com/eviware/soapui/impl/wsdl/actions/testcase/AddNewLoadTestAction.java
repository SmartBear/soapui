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

package com.eviware.soapui.impl.wsdl.actions.testcase;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a new WsdlLoadTest to a WsdlTestCase
 * 
 * @author Ole.Matzura
 */

public class AddNewLoadTestAction extends AbstractSoapUIAction<WsdlTestCase>
{
	public static final String SOAPUI_ACTION_ID = "AddNewLoadTestAction";

	public AddNewLoadTestAction()
	{
		super( "New LoadTest", "Creates a new LoadTest for this TestCase" );
	}

	public void perform( WsdlTestCase testCase, Object param )
	{
		String name = UISupport.prompt( "Specify name of LoadTest", "New LoadTest", "LoadTest "
				+ ( testCase.getLoadTestCount() + 1 ) );
		if( name == null )
			return;

		WsdlLoadTest loadTest = testCase.addNewLoadTest( name );
		UISupport.selectAndShow( loadTest );
	}
}
