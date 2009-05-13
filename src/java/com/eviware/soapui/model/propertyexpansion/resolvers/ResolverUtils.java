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

package com.eviware.soapui.model.propertyexpansion.resolvers;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.xml.XmlUtils;

public class ResolverUtils
{
	public static String checkForExplicitReference( String propertyName, String prefix, TestPropertyHolder holder,
			PropertyExpansionContext context, boolean globalOverride )
	{
		if( holder == null )
			return null;

		if( propertyName.startsWith( prefix ) )
			propertyName = propertyName.substring( prefix.length() );
		else
			return null;

		return ResolverUtils.parseProperty( propertyName, holder, context, globalOverride );
	}

	public static String parseProperty( String name, TestPropertyHolder holder, PropertyExpansionContext context,
			boolean globalOverride )
	{
		int sepIx = name.indexOf( PropertyExpansion.PROPERTY_SEPARATOR );
		if( sepIx != -1 )
		{
			String xpath = name.substring( sepIx + 1 );
			name = name.substring( 0, sepIx );

			if( globalOverride )
			{
				String value = PropertyExpansionUtils.getGlobalProperty( name );
				if( value != null )
					return value;
			}

			TestProperty property = holder.getProperty( name );

			if( property != null )
			{
				return context == null ? ResolverUtils.extractXPathPropertyValue( property, xpath ) : ResolverUtils
						.extractXPathPropertyValue( property, PropertyExpansionUtils.expandProperties( context, xpath ) );
			}
		}
		else
		{
			if( globalOverride )
			{
				String value = PropertyExpansionUtils.getGlobalProperty( name );
				if( value != null )
					return value;
			}

			TestProperty property = holder.getProperty( name );
			if( property != null )
			{
				return property.getValue();
			}
		}

		return null;
	}

	public static String extractXPathPropertyValue( Object property, String xpath )
	{
		try
		{
			String value = property instanceof TestProperty ? ( ( TestProperty )property ).getValue() : property
					.toString();
			XmlObject xmlObject = XmlObject.Factory.parse( value );
			String ns = xpath.trim().startsWith( "declare namespace" ) ? "" : XmlUtils.declareXPathNamespaces( xmlObject );
			Node domNode = XmlUtils.selectFirstDomNode( xmlObject, ns + xpath );
			return domNode == null ? null : XmlUtils.getValueForMatch( domNode, false );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		return null;
	}

}
