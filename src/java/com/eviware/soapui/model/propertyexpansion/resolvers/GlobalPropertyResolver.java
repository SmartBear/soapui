/*
 *  soapUI, copyright (C) 2004-2008 eviware.com
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

import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;

public class GlobalPropertyResolver implements PropertyResolver
{
	public String resolveProperty( PropertyExpansionContext context, String name, boolean globalOverride )
	{
		//	if not, check for explicit global property (stupid 1.7.6 syntax that should be removed..)
		if(  name.length() > 2 && name.charAt( 0 ) == PropertyExpansion.PROPERTY_SEPARATOR 
					&& name.charAt( 1 ) == PropertyExpansion.PROPERTY_SEPARATOR )
			return PropertyExpansionUtils.getGlobalProperty( name.substring( 2 ) );
		else
			return PropertyExpansionUtils.getGlobalProperty( name );
	}
}
