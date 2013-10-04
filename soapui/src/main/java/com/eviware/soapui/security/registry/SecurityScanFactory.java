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

import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestStep;

public interface SecurityScanFactory
{
	public SecurityScanConfig createNewSecurityScan( String name );

	public SecurityScan buildSecurityScan( TestStep testStep, SecurityScanConfig config, ModelItem parent );

	public String getSecurityScanType();

	/**
	 * True for test step on which this check could be applied.
	 * 
	 * @return
	 */
	public boolean canCreate( TestStep testStep );

	public String getSecurityScanName();

	public String getSecurityScanDescription();

	public String getSecurityScanIconPath();
}