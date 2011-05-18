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
import com.eviware.soapui.config.XmlBombSecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityScan;
import com.eviware.soapui.security.scan.XmlBombSecurityScan;

/**
 * Factory for creation GroovyScript steps
 * 
 * @author soapUI team
 */

public class XmlBombSecurityCheckFactory extends AbstractSecurityCheckFactory
{

	public XmlBombSecurityCheckFactory()
	{
		super( XmlBombSecurityScan.TYPE, XmlBombSecurityScan.NAME, "Performs a scan for XML Bomb Vulnerabilities",
				"/xml_bomb_security_check_script.gif" );
	}

	public boolean canCreate( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep;
	}

	@Override
	public AbstractSecurityScan buildSecurityScan( TestStep testStep, SecurityCheckConfig config, ModelItem parent )
	{
		return new XmlBombSecurityScan( config, parent, null, testStep );
	}

	@Override
	public SecurityCheckConfig createNewSecurityScan( String name )
	{
		SecurityCheckConfig securityCheckConfig = SecurityCheckConfig.Factory.newInstance();
		securityCheckConfig.setType( XmlBombSecurityScan.TYPE );
		securityCheckConfig.setName( name );
		return securityCheckConfig;
	}

}
