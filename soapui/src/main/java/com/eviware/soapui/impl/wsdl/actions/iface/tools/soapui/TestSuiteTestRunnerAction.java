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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.soapui;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class TestSuiteTestRunnerAction extends AbstractSoapUIAction<WsdlTestSuite>
{
	public TestSuiteTestRunnerAction()
	{
		super( "Launch TestRunner", "Launch the SoapUI commandline TestRunner for this TestSuite" );
	}

	public void perform( WsdlTestSuite target, Object param )
	{
		SoapUIAction<ModelItem> action = SoapUI.getActionRegistry().getAction( TestRunnerAction.SOAPUI_ACTION_ID );
		SoapUI.setLaunchedTestRunner( true );
		action.perform( target.getProject(), target );
	}
}
