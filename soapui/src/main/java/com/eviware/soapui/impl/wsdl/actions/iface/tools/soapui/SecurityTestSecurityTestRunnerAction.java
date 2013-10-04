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
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SecurityTestRunnerAction;

public class SecurityTestSecurityTestRunnerAction extends AbstractSoapUIAction<SecurityTest>
{

	public SecurityTestSecurityTestRunnerAction()
	{
		super( "Launch SecurityTestRunner", "Launch command-line SecurityTestRunner for this SecurityTest" );
	}

	@Override
	public void perform( SecurityTest target, Object param )
	{
		SoapUIAction<ModelItem> action = SoapUI.getActionRegistry().getAction( SecurityTestRunnerAction.SOAPUI_ACTION_ID );
		SoapUI.setLaunchedTestRunner( true );
		action.perform( target.getTestCase().getTestSuite().getProject(), target );
	}

}
