package com.eviware.soapui.impl.wsdl.panels.assertions;

public class AssertionListEntry implements Comparable<AssertionListEntry>
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

	@Override
	public String toString()
	{
		return name + " " + description;
	}

	@Override
	public int hashCode()
	{
		return name.length() + description.length();
	}

	@Override
	public int compareTo( AssertionListEntry o )
	{
		return name.compareToIgnoreCase( o.getName() );
	}

}
