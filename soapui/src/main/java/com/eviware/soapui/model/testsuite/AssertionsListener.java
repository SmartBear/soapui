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

package com.eviware.soapui.model.testsuite;

/**
 * Listener for assertion events.. should be added to core model
 * 
 * @author Ole.Matzura
 */

public interface AssertionsListener
{
	public void assertionAdded( TestAssertion assertion );

	public void assertionRemoved( TestAssertion assertion );

	public void assertionMoved( TestAssertion assertion, int ix, int offset );
}
