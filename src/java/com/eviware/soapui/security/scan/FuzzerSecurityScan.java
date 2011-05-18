/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.scan;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;

public class FuzzerSecurityScan extends AbstractSecurityScanWithProperties
{
	
	public static final String TYPE = "FuzzingScan";
	public static final String NAME = "Fuzzing Scan";

	public FuzzerSecurityScan( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );
	}

	@Override
	protected void execute( SecurityTestRunner runner, TestStep testStep, SecurityTestRunContext context )
	{

	}

	@Override
	public String getConfigDescription()
	{
		return "Configuration for Fuzzing Security Scan";
	}

	@Override
	public String getConfigName()
	{
		return "Configuration for Fuzzing Security Scan";
	}

	@Override
	public String getHelpURL()
	{
		//TODO: change to proper help url
		return "http://www.soapui.org/Security/boundary-scan.html";
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	protected boolean hasNext( TestStep testStep2, SecurityTestRunContext context )
	{
		return false;
	}

}
