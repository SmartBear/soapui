/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.registry;

import com.eviware.soapui.config.GroovySecurityScanConfig;
import com.eviware.soapui.config.ScriptConfig;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityScan;
import com.eviware.soapui.security.scan.GroovySecurityScan;

/**
 * Factory for creation GroovyScript steps
 * 
 * @author SoapUI team
 */

public class GroovySecurityScanFactory extends AbstractSecurityScanFactory
{

	public GroovySecurityScanFactory()
	{
		super( GroovySecurityScan.TYPE, GroovySecurityScan.NAME,
				"Executes the specified groovy script for security scan", "/groovy_script_scan.gif" );
	}

	public boolean canCreate( TestStep testStep )
	{
		return true;
	}

	@Override
	public AbstractSecurityScan buildSecurityScan( TestStep testStep, SecurityScanConfig config, ModelItem parent )
	{
		return new GroovySecurityScan( testStep, config, parent, "/groovy_script_scan.gif" );
	}

	@Override
	public SecurityScanConfig createNewSecurityScan( String name )
	{
		SecurityScanConfig securityCheckConfig = SecurityScanConfig.Factory.newInstance();
		securityCheckConfig.setType( GroovySecurityScan.TYPE );
		securityCheckConfig.setName( name );
		GroovySecurityScanConfig groovyscc = GroovySecurityScanConfig.Factory.newInstance();
		groovyscc.setExecuteScript( ScriptConfig.Factory.newInstance() );
		// securityCheckConfig.changeType( GroovySecurityScanConfig.type );
		securityCheckConfig.setConfig( groovyscc );
		return securityCheckConfig;
	}

}
