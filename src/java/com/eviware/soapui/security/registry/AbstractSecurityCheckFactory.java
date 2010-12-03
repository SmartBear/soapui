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
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.check.SecurityCheck;

/**
 * Abstract factory behaviour for SecurityCheck factories
 * 
 * @author soapui team
 */

public abstract class AbstractSecurityCheckFactory 
{
	private final String typeName;
	private final String name;
	private final String description;
	private final String pathToIcon;

	public AbstractSecurityCheckFactory( String typeName, String name, String description, String pathToIcon )
	{
		this.typeName = typeName;
		this.name = name;
		this.description = description;
		this.pathToIcon = pathToIcon;
	}

	public abstract SecurityCheckConfig createNewSecurityCheck( String name );
	public abstract SecurityCheck buildSecurityCheck( SecurityCheckConfig config);
	public String getType()
	{
		return typeName;
	}

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

	public boolean isHttpMonitor()
	{
		return false;
	}
	public boolean canDoSecurityCheck( Securable securable ) {
		//TODO implement appropriately for specific checks implementation 
		return true;
	}
}
