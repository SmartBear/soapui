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

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.security.SecurityCheck;

/**
 * Abstract factory behaviour for SecurityCheck factories
 * 
 * @author soapui team
 */

public abstract class SecurityCheckFactory
{
	private final String typeName;
	private final String name;
	private final String description;
	private final String pathToIcon;

	public SecurityCheckFactory( String typeName, String name, String description, String pathToIcon )
	{
		this.typeName = typeName;
		this.name = name;
		this.description = description;
		this.pathToIcon = pathToIcon;
	}

	public abstract SecurityCheck buildSecurityCheck( SecurityCheckConfig config );

	public String getType()
	{
		return typeName;
	}

	// public abstract SecurityCheckConfig createNewSecurityCheck( WsdlTestCase testCase, String name );

	public abstract boolean canCreate();

	public String getSecurityCheckName()
	{
		return name;
	}

	public String getSecurityCheckDescription()
	{
		return description;
	}

	public String getSecurityCheckIconPath()
	{
		return pathToIcon;
	}
}
