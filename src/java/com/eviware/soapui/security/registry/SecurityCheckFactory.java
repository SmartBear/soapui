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
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.check.AbstractSecurityCheck;

public interface SecurityCheckFactory
{
	public boolean canDoSecurityCheck( Securable securable );

	// TODO check if should be changed to commented
	// public abstract SecurityCheck buildSecurityCheck( SecurityCheckConfig
	// config, Securable securable );
	public AbstractSecurityCheck buildSecurityCheck( SecurityCheckConfig config );

	// public Class<? extends WsdlMessageAssertion> getAssertionClassType();

	// public String getAssertionId();

	public String getSecurityCheckName();
}
