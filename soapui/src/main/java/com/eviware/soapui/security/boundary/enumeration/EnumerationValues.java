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
package com.eviware.soapui.security.boundary.enumeration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.eviware.soapui.security.boundary.BoundaryUtils;
import com.eviware.soapui.security.boundary.StringBoundary;

public class EnumerationValues
{
	private String type;
	private List<String> valuesList = new ArrayList<String>();

	public EnumerationValues( String type )
	{
		this.type = type;
	}

	public static int maxLengthStringSize( Collection<String> values )
	{
		int max = 0;
		for( String str : values )
		{
			if( max < str.length() )
				max = str.length();
		}
		return max;
	}

	public static String createOutOfBoundaryValue( EnumerationValues enumValues, int size )
	{
		if( "XmlString".equals( enumValues.getType() ) )
		{
			String value = null;
			do
			{
				value = BoundaryUtils.createCharacterArray( StringBoundary.AVAILABLE_VALUES, size );
			}
			while( enumValues.getValuesList().contains( value ) );
			return value;
		}
		return null;
	}

	public String getType()
	{
		return type;
	}

	public void addValue( String value )
	{
		valuesList.add( value );
	}

	public List<String> getValuesList()
	{
		return valuesList;
	}

}