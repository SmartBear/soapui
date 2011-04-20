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
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.TestStep;

public interface SecurityCheckFactory
{

	public SecurityCheckConfig createNewSecurityCheck( String name );

	public SecurityCheck buildSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent );

	public String getSecurityCheckType();

	/**
	 * True for test step on which this check could be applied.
	 * 
	 * @return
	 */
	public boolean canCreate( TestStep testStep );

	public String getSecurityCheckName();

	public String getSecurityCheckDescription();

	public String getSecurityCheckIconPath();

	public boolean isHttpMonitor();

}