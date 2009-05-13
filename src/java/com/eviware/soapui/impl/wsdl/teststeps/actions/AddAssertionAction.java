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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.UISupport;

/**
 * Adds a WsdlAssertion to a WsdlTestRequest
 * 
 * @author Ole.Matzura
 */

public class AddAssertionAction extends AbstractAction
{
	private final Assertable assertable;

	public AddAssertionAction( Assertable assertable )
	{
		super( "Add Assertion" );
		this.assertable = assertable;

		putValue( Action.SHORT_DESCRIPTION, "Adds an assertion to this item" );
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/addAssertion.gif" ) );
	}

	public void actionPerformed( ActionEvent e )
	{
		String[] assertions = TestAssertionRegistry.getInstance().getAvailableAssertionNames( assertable );

		if( assertions == null || assertions.length == 0 )
		{
			UISupport.showErrorMessage( "No assertions available for this message" );
			return;
		}

		String selection = ( String )UISupport.prompt( "Select assertion to add", "Select Assertion", assertions );
		if( selection == null )
			return;

		if( !TestAssertionRegistry.getInstance().canAddMultipleAssertions( selection, assertable ) )
		{
			UISupport.showErrorMessage( "This assertion can only be added once" );
			return;
		}

		TestAssertion assertion = assertable.addAssertion( selection );
		if( assertion == null )
		{
			UISupport.showErrorMessage( "Failed to add assertion" );
			return;
		}

		if( assertion.isConfigurable() )
		{
			assertion.configure();
		}
	}
}
