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
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.BoundarySecurityCheck;

/**
 * Factory for creation Boundary checks
 * 
 * @author nebojsa.tasic
 */

public class BoundarySecurityCheckFactory extends AbstractSecurityCheckFactory
{

	public BoundarySecurityCheckFactory()
	{
		super( BoundarySecurityCheck.TYPE, BoundarySecurityCheck.NAME, "Executes the specified boundary security scan",
				"/boundary_security_check.gif" );
	}

	@Override
	public SecurityCheckConfig createNewSecurityCheck( String name )
	{
		SecurityCheckConfig securityCheckConfig = SecurityCheckConfig.Factory.newInstance();
		securityCheckConfig.setType( BoundarySecurityCheck.TYPE );
		securityCheckConfig.setName( name );
		return securityCheckConfig;
	}

	@Override
	public AbstractSecurityCheck buildSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent )
	{
		return new BoundarySecurityCheck( testStep, config, parent, null );
	}

	@Override
	public boolean canCreate( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep || testStep instanceof RestTestRequestStep;
	}
}
