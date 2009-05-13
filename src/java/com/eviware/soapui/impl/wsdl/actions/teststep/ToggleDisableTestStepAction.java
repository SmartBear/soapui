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

package com.eviware.soapui.impl.wsdl.actions.teststep;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Toggles the disabled state of WsdlTestStep
 * 
 * @author Ole.Matzura
 */

public class ToggleDisableTestStepAction extends AbstractSoapUIAction<WsdlTestStep>
{
	public static final String SOAPUI_ACTION_ID = "ToggleDisableTestStepAction";

	public ToggleDisableTestStepAction()
	{
		super( "Disable", "Disables this TestStep" );
	}

	public void perform( WsdlTestStep testStep, Object param )
	{
		testStep.setDisabled( !testStep.isDisabled() );
	}
}
