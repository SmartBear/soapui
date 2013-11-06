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
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityScan;

/**
 * Abstract factory behaviour for SecurityScan factories
 * 
 * @author soapui team
 */

public abstract class AbstractSecurityScanFactory implements SecurityScanFactory
{
	private final String type;
	private final String name;
	private final String description;
	protected final String pathToIcon;

	public AbstractSecurityScanFactory( String typeName, String name, String description, String pathToIcon )
	{
		this.type = typeName;
		this.name = name;
		this.description = description;
		this.pathToIcon = pathToIcon;
	}

	public abstract SecurityScanConfig createNewSecurityScan( String name );

	public abstract AbstractSecurityScan buildSecurityScan( TestStep testStep, SecurityScanConfig config,
			ModelItem parent );

	public String getSecurityScanType()
	{
		return type;
	}

	/**
	 * True for test step on which this scan could be aplied.
	 * 
	 * @return
	 */
	public abstract boolean canCreate( TestStep testStep );

	public String getSecurityScanName()
	{
		return name;
	}

	public String getSecurityScanDescription()
	{
		return description;
	}

	public String getSecurityScanIconPath()
	{
		return pathToIcon;
	}

}
