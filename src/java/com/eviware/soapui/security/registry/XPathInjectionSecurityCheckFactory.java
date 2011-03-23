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
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.XPathInjectionSecurityCheck;

public class XPathInjectionSecurityCheckFactory extends AbstractSecurityCheckFactory
{

	public XPathInjectionSecurityCheckFactory()
	{
		super( XPathInjectionSecurityCheck.TYPE, XPathInjectionSecurityCheck.NAME,
				"Preforms a check for XPath Injection Vulerabilities", null );
	}

	@Override
	public AbstractSecurityCheck buildSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent )
	{
		return new XPathInjectionSecurityCheck( testStep, config, parent, null );
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
		securityCheckConfig.setType( XPathInjectionSecurityCheck.TYPE );
		securityCheckConfig.setName( name );
		return securityCheckConfig;
	}

}
