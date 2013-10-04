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

import java.util.HashSet;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIMultiAction;

public class MultiAssertionDeleteAction extends AbstractSoapUIMultiAction<ModelItem>
{
	public static final String SOAPUI_ACTION_ID = "MultiAssertionDeleteAction";

	public MultiAssertionDeleteAction()
	{
		super( SOAPUI_ACTION_ID, "Delete Assertions", "Delete selected Assertions" );
	}

	public void perform( ModelItem[] targets, Object param )
	{
		if( UISupport.confirm( "Delete selected Assertions?", "Delete Assertions" ) )
		{
			if ( SoapUI.getTestMonitor().hasRunningTestCase( ( TestCase )targets[0].getParent().getParent() ) ) {
				UISupport.showInfoMessage( "Can not remove assertion(s) while test case is running" );
				return;
			}
			// remove duplicates
			Set<TestAssertion> assertions = new HashSet<TestAssertion>();

			for( ModelItem target : targets )
			{
				assertions.add( ( TestAssertion )target );
			}

			for( TestAssertion assertion : assertions )
			{
				( ( Assertable )assertion.getParent() ).removeAssertion( assertion );
			}
		}
	}


	public boolean applies( ModelItem target )
	{
		return( target instanceof TestAssertion );
	}
	
}
