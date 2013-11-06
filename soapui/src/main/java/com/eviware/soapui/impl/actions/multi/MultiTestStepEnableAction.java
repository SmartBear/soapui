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

package com.eviware.soapui.impl.actions.multi;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.support.AbstractSoapUIMultiAction;

public class MultiTestStepEnableAction extends AbstractSoapUIMultiAction<ModelItem>
{
	public static final String SOAPUI_ACTION_ID = "MultiTestStepEnableAction";

	public MultiTestStepEnableAction()
	{
		super( SOAPUI_ACTION_ID, "Enable", "Enables the selected items" );
	}

	public void perform( ModelItem[] targets, Object param )
	{
		for( ModelItem target : targets )
		{
			if( target instanceof WsdlTestStep )
				( ( WsdlTestStep )target ).setDisabled( false );
			else if( target instanceof WsdlTestCase )
				( ( WsdlTestCase )target ).setDisabled( false );
			else if( target instanceof WsdlTestSuite )
				( ( WsdlTestSuite )target ).setDisabled( false );
		}
	}

	public boolean applies( ModelItem target )
	{
		return ( ( target instanceof WsdlTestStep ) && ( ( WsdlTestStep )target ).isDisabled() )
				|| ( ( target instanceof WsdlTestCase ) && ( ( WsdlTestCase )target ).isDisabled() )
				|| ( ( target instanceof WsdlTestSuite ) && ( ( WsdlTestSuite )target ).isDisabled() );
	}
}
