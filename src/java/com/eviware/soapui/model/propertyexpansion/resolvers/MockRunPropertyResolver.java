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

import com.eviware.soapui.model.mock.MockRunContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;

public class MockRunPropertyResolver implements PropertyResolver
{
	public String resolveProperty( PropertyExpansionContext context, String propertyName, boolean globalOverride )
	{
		if( propertyName.charAt( 0 ) != PropertyExpansion.SCOPE_PREFIX || !( context instanceof MockRunContext ) )
			return null;

		MockRunContext mrc = ( MockRunContext )context;

		// explicit item reference?
		String value = ResolverUtils.checkForExplicitReference( propertyName, PropertyExpansion.PROJECT_REFERENCE, mrc
				.getMockService().getProject(), mrc, globalOverride );
		if( value != null )
			return value;

		value = ResolverUtils.checkForExplicitReference( propertyName, PropertyExpansion.MOCKSERVICE_REFERENCE, mrc
				.getMockService(), mrc, globalOverride );
		if( value != null )
			return value;

		return ResolverUtils.checkForExplicitReference( propertyName, PropertyExpansion.MOCKRESPONSE_REFERENCE, mrc
				.getMockResponse(), mrc, globalOverride );
	}
}
