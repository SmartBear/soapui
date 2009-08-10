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

package com.eviware.soapui.report;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlOptions;

import com.eviware.soapui.junit.Properties;
import com.eviware.soapui.junit.Property;
import com.eviware.soapui.junit.Testcase;
import com.eviware.soapui.junit.Testsuite;
import com.eviware.soapui.junit.TestsuiteDocument;
import com.eviware.soapui.junit.FailureDocument.Failure;

/**
 * Wrapper for a number of Test runs
 */

public class JUnitReport
{
	TestsuiteDocument testsuiteDoc;
	int noofTestCases, noofFailures, noofErrors;
	double totalTime;
	StringBuffer systemOut;
	StringBuffer systemErr;

	public JUnitReport()
	{
		systemOut = new StringBuffer();
		systemErr = new StringBuffer();

		testsuiteDoc = TestsuiteDocument.Factory.newInstance();
		Testsuite testsuite = testsuiteDoc.addNewTestsuite();
		Properties properties = testsuite.addNewProperties();
		setSystemProperties( properties );
	}

	public void setTotalTime( double time )
	{
		testsuiteDoc.getTestsuite().setTime( new BigDecimal( time ) );
	}

	public void setTestSuiteName( String name )
	{
		testsuiteDoc.getTestsuite().setName( name );
	}

	public void setNoofErrorsInTestSuite( int errors )
	{
		testsuiteDoc.getTestsuite().setErrors( errors );
	}

	public void setNoofFailuresInTestSuite( int failures )
	{
		testsuiteDoc.getTestsuite().setFailures( failures );
	}

	public void systemOut( String systemout )
	{
		systemOut.append( systemout );
	}

	public void systemErr( String systemerr )
	{
		systemErr.append( systemerr );
	}

	public void setSystemOut( String systemout )
	{
		testsuiteDoc.getTestsuite().setSystemOut( systemout );
	}

	public void setSystemErr( String systemerr )
	{
		testsuiteDoc.getTestsuite().setSystemErr( systemerr );
	}

	public Testcase addTestCase( String name, double time )
	{
		Testcase testcase = testsuiteDoc.getTestsuite().addNewTestcase();
		testcase.setName( name );
		testcase.setTime( time / 1000 );
		noofTestCases++ ;
		totalTime += time;
		return testcase;
	}

	public Testcase addTestCaseWithFailure( String name, double time, String failure, String stacktrace )
	{
		Testcase testcase = testsuiteDoc.getTestsuite().addNewTestcase();
		testcase.setName( name );
		testcase.setTime( time / 1000 );
		Failure fail = testcase.addNewFailure();
		fail.setType( failure );
		fail.setMessage( failure );
		fail.setStringValue( stacktrace );
		noofTestCases++ ;
		noofFailures++ ;
		totalTime += time;
		return testcase;
	}

	public Testcase addTestCaseWithError( String name, double time, String error, String stacktrace )
	{
		Testcase testcase = testsuiteDoc.getTestsuite().addNewTestcase();
		testcase.setName( name );
		testcase.setTime( time / 1000 );
		com.eviware.soapui.junit.ErrorDocument.Error err = testcase.addNewError();
		err.setType( error );
		err.setMessage( error );
		err.setStringValue( stacktrace );
		noofTestCases++ ;
		noofErrors++ ;
		totalTime += time;
		return testcase;
	}

	private void setSystemProperties( Properties properties )
	{
		Set<?> keys = System.getProperties().keySet();
		for( Object keyO : keys )
		{
			String key = keyO.toString();
			String value = System.getProperty( key );
			Property prop = properties.addNewProperty();
			prop.setName( key );
			prop.setValue( value );
		}
	}

	@SuppressWarnings( "unchecked" )
	public void save( File file ) throws IOException
	{
		finishReport();

		Map prefixes = new HashMap();
		prefixes.put( "", "http://eviware.com/soapui/junit" );

		testsuiteDoc.save( file, new XmlOptions().setSaveOuter().setCharacterEncoding( "utf-8" ).setUseDefaultNamespace()
				.setSaveImplicitNamespaces( prefixes ) );
	}

	public TestsuiteDocument finishReport()
	{
		testsuiteDoc.getTestsuite().setTests( noofTestCases );
		testsuiteDoc.getTestsuite().setFailures( noofFailures );
		testsuiteDoc.getTestsuite().setErrors( noofErrors );
		testsuiteDoc.getTestsuite().setTime( new BigDecimal( totalTime / 1000 ) );

		return testsuiteDoc;
	}
}
