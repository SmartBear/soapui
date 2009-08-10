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

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;

public class SubmitPropertyResolver implements PropertyResolver
{
	public String resolveProperty( PropertyExpansionContext context, String propertyName, boolean globalOverride )
	{
		if( propertyName.charAt( 0 ) == PropertyExpansion.SCOPE_PREFIX
				&& context.getModelItem() instanceof AbstractHttpRequestInterface<?> )
		{
			return ResolverUtils.checkForExplicitReference( propertyName, PropertyExpansion.PROJECT_REFERENCE,
					( ( AbstractHttpRequest<?> )context.getModelItem() ).getOperation().getInterface().getProject(), context,
					globalOverride );
		}

		return null;
	}

}
