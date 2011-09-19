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

	public void removeProperty( String name )
	{
	}

	public void changePropertyName( String name, String value )
	{
	}

	public void changePropertyValue( String name, String value )
	{
	}

}
