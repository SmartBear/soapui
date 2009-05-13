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
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlTestStep
 * 
 * @author Ole.Matzura
 */

public class RenameTestStepAction extends AbstractSoapUIAction<WsdlTestStep>
{
	public RenameTestStepAction()
	{
		super( "Rename", "Renames this TestStep" );
	}

	public void perform( WsdlTestStep testStep, Object param )
	{
		String name = UISupport.prompt( "Specify unique name of TestStep", "Rename TestStep", testStep.getName() );
		if( name == null || name.equals( testStep.getName() ) )
			return;

		while( testStep.getTestCase().getTestStepByName( name ) != null )
		{
			name = UISupport.prompt( "Specify unique name of TestStep", "Rename TestStep", testStep.getName() );
			if( name == null || name.equals( testStep.getName() ) )
				return;
		}

		testStep.setName( name );
	}
}
