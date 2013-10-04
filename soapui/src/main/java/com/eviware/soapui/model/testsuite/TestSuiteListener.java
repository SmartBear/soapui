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

import com.eviware.soapui.security.SecurityTest;

/**
 * Listener for TestSuite-related events
 * 
 * @author Ole.Matzura
 */

public interface TestSuiteListener
{
	void testCaseAdded( TestCase testCase );

	void testCaseRemoved( TestCase testCase );

	void testCaseMoved( TestCase testCase, int index, int offset );

	void loadTestAdded( LoadTest loadTest );

	void loadTestRemoved( LoadTest loadTest );

	void testStepAdded( TestStep testStep, int index );

	void testStepRemoved( TestStep testStep, int index );

	void testStepMoved( TestStep testStep, int fromIndex, int offset );

	void securityTestAdded( SecurityTest securityTest );

	void securityTestRemoved( SecurityTest securityTest );
}
