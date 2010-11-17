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

import com.eviware.soapui.config.GroovySecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.security.check.GroovySecurityCheck;
import com.eviware.soapui.security.check.SecurityCheck;

/**
 * Factory for creation GroovyScript steps
 * 
 * @author soapUI team
 */

public class GroovySecurityCheckFactory extends SecurityCheckFactory {

	public GroovySecurityCheckFactory() {
		super(GroovySecurityCheck.TYPE, "GroovySecurityCheck",
				"Executes the specified groovy script for security check",
				"/groovy_security_check_script.gif");
	}

	public boolean canCreate() {
		return true;
	}

	@Override
	public SecurityCheck buildSecurityCheck(SecurityCheckConfig config) {
		return new GroovySecurityCheck(config, null, null);
	}

	@Override
	public SecurityCheckConfig createNewSecurityCheck(String name) {
		SecurityCheckConfig securityCheckConfig = SecurityCheckConfig.Factory
				.newInstance();
		securityCheckConfig.setType(GroovySecurityCheck.TYPE);
		securityCheckConfig.setName(name);
		GroovySecurityCheckConfig groovyscc = GroovySecurityCheckConfig.Factory
				.newInstance();
		groovyscc.addNewScript();
		// securityCheckConfig.changeType( GroovySecurityCheckConfig.type );
		securityCheckConfig.setConfig(groovyscc);
		return securityCheckConfig;
	}

	@Override
	public boolean isHttpMonitor() {
		return true;
	}
}
