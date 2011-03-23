package com.eviware.soapui.security.registry;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.InvalidTypesSecurityCheck;

public class InvalidTypesSecurityCheckFactory extends AbstractSecurityCheckFactory
{

	public InvalidTypesSecurityCheckFactory()
	{
		super( InvalidTypesSecurityCheck.TYPE, InvalidTypesSecurityCheck.NAME,
				"Tries to break application and get information on system", "/information_exposure_check.gif" );
	}

	@Override
	public AbstractSecurityCheck buildSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent )
	{
		return new InvalidTypesSecurityCheck( testStep, config, parent, null );
	}

	@Override
	public boolean canCreate( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep;
	}

	@Override
	public SecurityCheckConfig createNewSecurityCheck( String name )
	{
		SecurityCheckConfig securityCheckConfig = SecurityCheckConfig.Factory.newInstance();
		securityCheckConfig.setType( InvalidTypesSecurityCheck.TYPE );
		securityCheckConfig.setName( name );
		return securityCheckConfig;
	}

}
