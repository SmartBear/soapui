package com.eviware.soapui.impl.wsdl.panels.assertions;

public class AssertionListEntry
{
	private String name;
	private String description;

	public AssertionListEntry( String name, String description )
	{
		this.name = name;
		this.description = description;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}
}
