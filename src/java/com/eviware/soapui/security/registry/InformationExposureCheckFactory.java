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

package com.eviware.soapui.security.registry;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.security.check.InformationExposureCheck;
import com.eviware.soapui.security.check.SecurityCheck;

/**
 * Factory for creation InformationExposure checks
 * 
 * @author nebojsa.tasic
 */

public class InformationExposureCheckFactory extends AbstractSecurityCheckFactory
{

	public InformationExposureCheckFactory()
	{
		super( InformationExposureCheck.TYPE, "InformationExposureCheck",
				"Executes the specified information exposure for security check", "/information_exposure_check.gif" );
	}

	public boolean canCreate()
	{
		return true;
	}

	@Override
	public SecurityCheck buildSecurityCheck( SecurityCheckConfig config, ModelItem parent )
	{
		return new InformationExposureCheck( config, parent, null );
	}

	@Override
	public SecurityCheckConfig createNewSecurityCheck( String name )
	{
		SecurityCheckConfig securityCheckConfig = SecurityCheckConfig.Factory.newInstance();
		securityCheckConfig.setType( InformationExposureCheck.TYPE );
		securityCheckConfig.setName( name );
		return securityCheckConfig;
	}

	@Override
	public boolean isHttpMonitor()
	{
		return true;
	}
}
