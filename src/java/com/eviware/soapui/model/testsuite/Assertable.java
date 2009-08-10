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

package com.eviware.soapui.model.testsuite;

import java.util.List;
import java.util.Map;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;

/**
 * Behaviour for an object that can be asserted
 * 
 * @author ole.matzura
 */

public interface Assertable
{
	public TestAssertion addAssertion( String selection );

	public void addAssertionsListener( AssertionsListener listener );

	public int getAssertionCount();

	public TestAssertion getAssertionAt( int c );

	public void removeAssertionsListener( AssertionsListener listener );

	public void removeAssertion( TestAssertion assertion );

	public AssertionStatus getAssertionStatus();

	public enum AssertionStatus
	{
		UNKNOWN, VALID, FAILED
	}

	public String getAssertableContent();

	public String getDefaultAssertableContent();

	public AssertableType getAssertableType();

	public List<TestAssertion> getAssertionList();

	public TestAssertion getAssertionByName( String name );

	public ModelItem getModelItem();

	public Interface getInterface();

	public TestAssertion cloneAssertion( TestAssertion source, String name );

	public Map<String, TestAssertion> getAssertions();
	
	public TestAssertion moveAssertion( int ix, int offset );
}
