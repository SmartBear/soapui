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

package com.eviware.soapui.model.propertyexpansion;

import com.eviware.soapui.model.testsuite.TestProperty;

public interface PropertyExpansion
{
	// scope specifiiers
	public static final String SYSTEM_REFERENCE = "#System#";
	public static final String ENV_REFERENCE = "#Env#";
	public static final String GLOBAL_REFERENCE = "#Global#";
	public static final String PROJECT_REFERENCE = "#Project#";
	public static final String TESTSUITE_REFERENCE = "#TestSuite#";
	public static final String TESTCASE_REFERENCE = "#TestCase#";
	public static final String MOCKSERVICE_REFERENCE = "#MockService#";
	public static final String MOCKRESPONSE_REFERENCE = "#MockResponse#";

	public static final char PROPERTY_SEPARATOR = '#';
	public static final char XPATH_SEPARATOR = '#';
	public static final char SCOPE_PREFIX = '#';

	public TestProperty getProperty();

	public String toString();

	public String getXPath();

	public String getContainerInfo();
}
