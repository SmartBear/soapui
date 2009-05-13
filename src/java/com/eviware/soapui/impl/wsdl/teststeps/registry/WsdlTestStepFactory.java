/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;

/**
 * Abstract factory behaviour for WsdlTestStep factories
 * 
 * @author Ole.Matzura
 */

public abstract class WsdlTestStepFactory
{
	private final String typeName;
	private final String name;
	private final String description;
	private final String pathToIcon;

	public WsdlTestStepFactory( String typeName, String name, String description, String pathToIcon )
	{
		this.typeName = typeName;
		this.name = name;
		this.description = description;
		this.pathToIcon = pathToIcon;
	}

	public abstract WsdlTestStep buildTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest );

	public String getType()
	{
		return typeName;
	}

	public abstract TestStepConfig createNewTestStep( WsdlTestCase testCase, String name );

	public abstract boolean canCreate();

	public String getTestStepName()
	{
		return name;
	}

	public String getTestStepDescription()
	{
		return description;
	}

	public String getTestStepIconPath()
	{
		return pathToIcon;
	}
}
