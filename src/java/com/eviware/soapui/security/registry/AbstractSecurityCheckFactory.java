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
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityCheck;

/**
 * Abstract factory behaviour for SecurityCheck factories
 * 
 * @author soapui team
 */

public abstract class AbstractSecurityCheckFactory implements SecurityCheckFactory
{
	private final String type;
	private final String name;
	private final String description;
	protected final String pathToIcon;

	public AbstractSecurityCheckFactory( String typeName, String name, String description, String pathToIcon )
	{
		this.type = typeName;
		this.name = name;
		this.description = description;
		this.pathToIcon = pathToIcon;
	}

	public abstract SecurityCheckConfig createNewSecurityCheck( String name );

	public abstract AbstractSecurityCheck buildSecurityCheck( TestStep testStep, SecurityCheckConfig config,
			ModelItem parent );

	public String getSecurityCheckType()
	{
		return type;
	}

	/**
	 * True for test step on which this check could be aplied.
	 * 
	 * @return
	 */
	public abstract boolean canCreate( TestStep testStep );

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
