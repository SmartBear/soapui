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

package com.eviware.soapui.monitor;

import java.beans.PropertyDescriptor;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;

public class PropertySupport
{
	public static void applySystemProperties( Object target, String scope, ModelItem modelItem )
	{
		PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors( target );
		DefaultPropertyExpansionContext context = new DefaultPropertyExpansionContext( modelItem );
		Properties properties = System.getProperties();

		for( PropertyDescriptor descriptor : descriptors )
		{
			String name = descriptor.getName();
			String key = scope + "." + name;
			if( PropertyUtils.isWriteable( target, name ) && properties.containsKey( key ) )
			{
				try
				{
					String value = context.expand( String.valueOf( properties.get( key ) ) );
					BeanUtils.setProperty( target, name, value );
					SoapUI.log.info( "Set property [" + name + "] to [" + value + "] in scope [" + scope + "]" );
				}
				catch( Throwable e )
				{
					SoapUI.logError( e );
				}
			}
		}
	}

}
