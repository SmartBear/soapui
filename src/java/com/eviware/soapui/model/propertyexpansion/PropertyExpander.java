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

package com.eviware.soapui.model.propertyexpansion;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.resolvers.ContextPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.DynamicPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.EvalPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.GlobalPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.MockRunPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.ModelItemPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.PropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.SubmitPropertyResolver;
import com.eviware.soapui.model.propertyexpansion.resolvers.TestRunPropertyResolver;
import com.eviware.soapui.settings.GlobalPropertySettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Class that can expand properties using property resolvers
 * 
 * @author ole
 */

public class PropertyExpander
{
	private List<PropertyResolver> propertyResolvers = new ArrayList<PropertyResolver>();
	private static List<PropertyResolver> defaultResolvers = new ArrayList<PropertyResolver>();
	private static PropertyExpander defaultExpander;

	static
	{
		// add default resolvers - this should be read from some external config
		// in the future
		defaultResolvers.add( new ModelItemPropertyResolver() );
		defaultResolvers.add( new TestRunPropertyResolver() );
		defaultResolvers.add( new MockRunPropertyResolver() );
		defaultResolvers.add( new SubmitPropertyResolver() );
		defaultResolvers.add( new ContextPropertyResolver() );
		defaultResolvers.add( new DynamicPropertyResolver() );
		defaultResolvers.add( new GlobalPropertyResolver() );
		defaultResolvers.add( new EvalPropertyResolver() );

		defaultExpander = new PropertyExpander( true );
	}

	public PropertyExpander( boolean addDefaultResolvers )
	{
		if( addDefaultResolvers )
		{
			propertyResolvers.addAll( defaultResolvers );
		}
	}

	public static PropertyExpander getDefaultExpander()
	{
		return defaultExpander;
	}

	public static void addDefaultResolver( PropertyResolver resolver )
	{
		defaultResolvers.add( resolver );
		defaultExpander.addResolver( resolver );
	}

	public void addResolver( PropertyResolver propertyResolver )
	{
		propertyResolvers.add( propertyResolver );
	}

	public static String expandProperties( String content )
	{
		return defaultExpander.expand( content );
	}

	public static String expandProperties( PropertyExpansionContext context, String content )
	{
		return defaultExpander.expand( context, content, false );
	}

	public static String expandProperties( PropertyExpansionContext context, String content, boolean entitize )
	{
		return defaultExpander.expand( context, content, entitize );
	}

	public String expand( String content )
	{
		return expand( new PropertyExpansionUtils.GlobalPropertyExpansionContext(), content, false );
	}

	public String expand( PropertyExpansionContext context, String content )
	{
		return expand( context, content, false );
	}

	public String expand( PropertyExpansionContext context, String content, boolean entitize )
	{
		if( StringUtils.isNullOrEmpty( content ) )
			return content;

		int ix = content.indexOf( "${" );
		if( ix == -1 )
			return content;

		StringBuffer buf = new StringBuffer();
		int lastIx = 0;
		while( ix != -1 )
		{
			if( ix > lastIx )
				buf.append( content.substring( lastIx, ix ) );

			int ix2 = content.indexOf( '}', ix + 2 );
			if( ix2 == -1 )
				break;

			// check for nesting
			int ix3 = content.lastIndexOf( "${", ix2 );
			if( ix3 != ix )
			{
				// buf.append( content.substring( ix, ix3 ));
				content = content.substring( 0, ix3 ) + expand( context, content.substring( ix3, ix2 + 1 ) )
						+ content.substring( ix2 + 1 );

				lastIx = ix;
				continue;
			}

			String propertyName = content.substring( ix + 2, ix2 );
			String propertyValue = null;

			if( StringUtils.hasContent( propertyName ) )
			{
				boolean globalOverrideEnabled = SoapUI.getSettings().getBoolean( GlobalPropertySettings.ENABLE_OVERRIDE );

				for( int c = 0; c < propertyResolvers.size() && propertyValue == null; c++ )
				{
					propertyValue = propertyResolvers.get( c )
							.resolveProperty( context, propertyName, globalOverrideEnabled );
				}
			}

			// found a value?
			if( propertyValue != null )
			{
				if( !content.equals( propertyValue ) )
					propertyValue = expand( context, propertyValue );

				if( entitize )
					propertyValue = XmlUtils.entitizeContent( propertyValue );

				buf.append( propertyValue );
			}
			else
			{
				// if( log.isEnabledFor( Priority.WARN ))
				// log.warn( "Missing property value for [" + propertyName + "]" );

				// buf.append( "${" ).append( propertyName ).append( '}' );
			}

			lastIx = ix2 + 1;
			ix = content.indexOf( "${", lastIx );
		}

		if( lastIx < content.length() )
			buf.append( content.substring( lastIx ) );

		return buf.toString();
	}

	public String expand( ModelItem contextModelItem, String content )
	{
		return expand( new DefaultPropertyExpansionContext( contextModelItem ), content );
	}

	public static String expandProperties( ModelItem contextModelItem, String content )
	{
		return defaultExpander.expand( contextModelItem, content );
	}

}
