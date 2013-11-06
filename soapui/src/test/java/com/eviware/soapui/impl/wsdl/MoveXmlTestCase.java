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

package com.eviware.soapui.impl.wsdl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.apache.xmlbeans.XmlCursor;
import org.junit.Test;

import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.config.TestStepConfig;

public class MoveXmlTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( MoveXmlTestCase.class );
	}

	@Test
	public void testMoveXml() throws Exception
	{
		TestCaseConfig testCase = TestCaseConfig.Factory.newInstance();
		TestStepConfig step1 = testCase.addNewTestStep();
		TestStepConfig step2 = testCase.addNewTestStep();
		TestStepConfig step3 = testCase.addNewTestStep();

		List<TestStepConfig> testSteps = testCase.getTestStepList();
		assertEquals( 3, testSteps.size() );
		assertEquals( testSteps.get( 0 ), step1 );
		assertEquals( testSteps.get( 1 ), step2 );
		assertEquals( testSteps.get( 2 ), step3 );

		XmlCursor cursor1 = step3.newCursor();
		XmlCursor cursor2 = step2.newCursor();

		cursor1.moveXml( cursor2 );

		cursor1.dispose();
		cursor2.dispose();

		assertEquals( testSteps.get( 0 ), step1 );
		// assertEquals( testSteps.get( 1 ), step3 );
		assertEquals( testSteps.get( 2 ), step2 );
	}
}
