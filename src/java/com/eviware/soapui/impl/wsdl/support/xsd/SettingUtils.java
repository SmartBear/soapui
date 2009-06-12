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
package com.eviware.soapui.impl.wsdl.support.xsd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.StringListConfig;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringList;

public class SettingUtils
{
	public static Collection<? extends QName> string2QNames( String string )
	{
		List<QName> result = new ArrayList<QName>();
		if( string != null && string.trim().length() > 0 )
		{
			try
			{
				StringList names = StringList.fromXml( string );
				for( String name : names )
				{
					QName qname = string2qname( name );
					result.add( qname );
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}

		return result;
	}

	public static String qnames2String(Collection<? extends QName> qnames)
   {
	   StringList names = new StringList();
	   for(QName qname : qnames)
	   {
	      String string = qname2string(qname);
	      names.add(string);
	   }
	   return names.toXml();
   }
	
	private static QName string2qname( String name )
	{
		int ix = name.indexOf( '@' );
		if( ix >= 0 )
			return new QName( name.substring( ix + 1 ), name.substring( 0, ix ) );
		else
			return new QName( name );
	}

	private static String qname2string( QName qname )
	{
		String ns = qname.getNamespaceURI();
		String localPart = qname.getLocalPart();
		if( ns != null && ns.length() > 0 )
			return localPart + "@" + ns;
		else
			return localPart;
	}

	public static String qnameValues2String( Map<QName, String[]> valueMap )
	{
		StringListConfig config = StringListConfig.Factory.newInstance();
		for( QName qname : valueMap.keySet() )
		{
			String[] values = valueMap.get( qname );
			String nameAndValues = qname2string( qname ) + "=" + StringUtils.join( values, "," );
			config.addEntry( nameAndValues );
		}
		return config.toString();
	}

	public static Map<QName, String[]> string2QNameValues( String string )
	{
		LinkedHashMap<QName, String[]> result = new LinkedHashMap<QName, String[]>();
		if( string != null && string.trim().length() > 0 )
		{
			try
			{
				StringList list = StringList.fromXml( string );
				for( String s : list )
				{
					String[] words = s.split( "=" );
					if( words.length == 2 )
					{
						String name = words[0];
						String[] values = words[1].split( "," );
						if( name.length() > 0 && values.length > 0 )
						{
							QName qname = string2qname( name );
							result.put( qname, values );
						}
					}
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}

		return result;
	}
}
