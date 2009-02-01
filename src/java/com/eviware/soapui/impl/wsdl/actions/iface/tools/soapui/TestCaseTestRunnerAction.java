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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.soapui;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class TestCaseTestRunnerAction extends AbstractSoapUIAction<WsdlTestCase>
{
	public TestCaseTestRunnerAction()
	{
		super( "Launch TestRunner", "Launch the soapUI commandline TestRunner for this TestCase" );
	}

	public void perform( WsdlTestCase target, Object param )
	{
		SoapUIAction<ModelItem> action = SoapUI.getActionRegistry().getAction( TestRunnerAction.SOAPUI_ACTION_ID );
		action.perform( target.getTestSuite().getProject(), target );
	}
}
