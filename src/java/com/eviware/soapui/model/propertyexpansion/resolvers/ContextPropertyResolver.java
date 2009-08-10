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

import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;

public class ContextPropertyResolver implements PropertyResolver
{
	public String resolveProperty( PropertyExpansionContext context, String propertyName, boolean globalOverride )
	{
		Object property = null;
		String xpath = null;

		int sepIx = propertyName.indexOf( PropertyExpansion.PROPERTY_SEPARATOR );
		if( sepIx == 0 )
		{
			propertyName = propertyName.substring( 1 );
			sepIx = propertyName.indexOf( PropertyExpansion.PROPERTY_SEPARATOR );
		}

		if( sepIx > 0 )
		{
			xpath = propertyName.substring( sepIx + 1 );
			propertyName = propertyName.substring( 0, sepIx );
		}

		if( globalOverride )
			property = PropertyExpansionUtils.getGlobalProperty( propertyName );

		if( property == null )
			property = context.getProperty( propertyName );

		if( property != null && xpath != null )
		{
			property = ResolverUtils.extractXPathPropertyValue( property, PropertyExpander.expandProperties( context,
					xpath ) );
		}

		return property == null ? null : property.toString();
	}

}
