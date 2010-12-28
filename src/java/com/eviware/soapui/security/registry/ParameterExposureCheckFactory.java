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
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.security.check.ParameterExposureCheck;
import com.eviware.soapui.security.check.SecurityCheck;

/**
 * Factory for creation GroovyScript steps
 * 
 * @author soapUI team
 */

public class ParameterExposureCheckFactory extends AbstractSecurityCheckFactory
{

	public ParameterExposureCheckFactory()
	{
		super( ParameterExposureCheck.TYPE, "ParameterExposureCheck", "Preforms a check for Parameter Exposure",
				"/parameter_exposure_check_script.gif" );
	}

	public boolean canCreate()
	{
		return true;
	}

	@Override
	public SecurityCheck buildSecurityCheck( SecurityCheckConfig config, ModelItem parent )
	{
		return new ParameterExposureCheck( config, parent, null );
	}

	@Override
	public SecurityCheckConfig createNewSecurityCheck( String name )
	{
		SecurityCheckConfig securityCheckConfig = SecurityCheckConfig.Factory.newInstance();
		securityCheckConfig.setType( ParameterExposureCheck.TYPE );
		securityCheckConfig.setName( name );
		ParameterExposureCheckConfig pecc = ParameterExposureCheckConfig.Factory.newInstance();
		pecc.setMinimumLength( ParameterExposureCheck.DEFAULT_MINIMUM_CHARACTER_LENGTH );
		securityCheckConfig.setConfig( pecc );
		return securityCheckConfig;
	}

	@Override
	public boolean isHttpMonitor()
	{
		return true;
	}
}
