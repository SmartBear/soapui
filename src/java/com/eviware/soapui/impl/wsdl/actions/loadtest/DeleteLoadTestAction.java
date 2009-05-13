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

package com.eviware.soapui.impl.wsdl.actions.loadtest;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Removes a WsdlLoadTest from its WsdlTestCase
 * 
 * @author Ole.Matzura
 */

public class DeleteLoadTestAction extends AbstractSoapUIAction<WsdlLoadTest>
{
	public DeleteLoadTestAction()
	{
		super( "Remove", "Removes this Test Schedule from the test-case" );
	}

	public void perform( WsdlLoadTest loadTest, Object param )
	{
		if( loadTest.isRunning() )
		{
			UISupport.showErrorMessage( "Can not remove running LoadTest" );
			return;
		}

		if( UISupport.confirm( "Remove LoadTest [" + loadTest.getName() + "] from test-casee", "Remove LoadTest" ) )
		{
			( ( WsdlTestCase )loadTest.getTestCase() ).removeLoadTest( loadTest );
		}
	}
}