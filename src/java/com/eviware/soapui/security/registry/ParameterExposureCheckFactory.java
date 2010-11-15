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

package com.eviware.soapui.security.registry;

import com.eviware.soapui.config.ParameterExposureCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.security.check.ParameterExposureCheck;
import com.eviware.soapui.security.check.SecurityCheck;

/**
 * Factory for creation GroovyScript steps
 * 
 * @author soapUI team
 */

public class ParameterExposureCheckFactory extends SecurityCheckFactory
{

	public ParameterExposureCheckFactory()
	{
		super( ParameterExposureCheck.TYPE, "Parameter Exposure Check", "Preforms a check for Parameter Exposure",
				"/parameter_exposure_check_script.gif" );
	}

	public boolean canCreate()
	{
		return true;
	}

	@Override
	public SecurityCheck buildSecurityCheck( SecurityCheckConfig config )
	{
		return new ParameterExposureCheck( config, null, null );
	}

	@Override
	public SecurityCheckConfig createNewSecurityCheck( String name )
	{
		SecurityCheckConfig securityCheckConfig = SecurityCheckConfig.Factory.newInstance();
		securityCheckConfig.setType( ParameterExposureCheck.TYPE );
		securityCheckConfig.setName( name );
		securityCheckConfig.setConfig( ParameterExposureCheckConfig.Factory.newInstance() );
		return securityCheckConfig;
	}

	// @Override
	// public SecurityCheckConfig createNewSecurityCheck( WsdlTestCase testCase,
	// String name )
	// {
	// SecurityCheckConfig securityCheckConfig =
	// SecurityCheckConfig.Factory.newInstance();
	// securityCheckConfig.setType( GROOVY_SECURITY_CHECK_TYPE );
	// securityCheckConfig.setName( name );
	// return securityCheckConfig;
	// }
}
