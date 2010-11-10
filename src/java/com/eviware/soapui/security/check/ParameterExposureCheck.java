/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.check;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.testsuite.TestStep;

/**
 * 
 * @author soapui team
 */

public class ParameterExposureCheck extends AbstractSecurityCheck
{

	public static final String SCRIPT_PROPERTY = ParameterExposureCheck.class.getName() + "@script";

	public ParameterExposureCheck( SecurityCheckConfig config )
	{
		super( config );
	}

	@Override
	protected void execute( TestStep testStep )
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void analyze(TestStep testStep) {
		// TODO Auto-generated method stub
		
	}
	
}
