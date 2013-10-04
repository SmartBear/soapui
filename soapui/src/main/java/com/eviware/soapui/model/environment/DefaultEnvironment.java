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
package com.eviware.soapui.model.environment;

import com.eviware.soapui.config.ServiceConfig;
import com.eviware.soapui.model.project.Project;

public class DefaultEnvironment implements Environment
{

	public static final String NAME = "Default";

	private DefaultEnvironment()
	{
	}

	private static class DefaultEnvironmentHolder
	{
		public static final DefaultEnvironment instance = new DefaultEnvironment();
	}

	public static DefaultEnvironment getInstance()
	{
		return DefaultEnvironmentHolder.instance;
	}

	public String getName()
	{
		return NAME;
	}

	@Override
	public boolean equals( Object obj )
	{
		return( obj instanceof DefaultEnvironment );
	}

	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}

	public void setProject( Project project )
	{
	}

	public Project getProject()
	{
		return null;
	}

	public void release()
	{
	}

	public Service addNewService( String name, ServiceConfig.Type.Enum serviceType )
	{
		return null;
	}

	public void removeService( Service service )
	{
	}

	public Property addNewProperty( String name, String value )
	{
		return null;
	}

	public void removeProperty( Property property )
	{
	}

	public void changePropertyName( String name, String value )
	{
	}

	public void moveProperty( String name, int idx )
	{
	}

	@Override
	public void setName( String name )
	{
		// TODO Auto-generated method stub
	}

}
