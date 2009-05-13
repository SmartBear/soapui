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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlString;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.types.StringList;

public class GlobalPropertyResolver implements PropertyResolver
{
	public class EnvironmentPropertyHolder implements TestPropertyHolder
	{
		public void addTestPropertyListener( TestPropertyListener listener )
		{
		}

		public ModelItem getModelItem()
		{
			return null;
		}

		public Map<String, TestProperty> getProperties()
		{
			Map<String, String> properties = System.getenv();
			Map<String, TestProperty> result = new HashMap<String, TestProperty>();

			for( Object key : properties.keySet() )
			{
				result.put( key.toString(), new EnviromentTestProperty( key ) );
			}

			return result;
		}

		public List<TestProperty> getPropertyList()
		{
			List<TestProperty> result = new ArrayList<TestProperty>();

			for( TestProperty property : getProperties().values() )
				result.add( property );

			return result;
		}

		public String getPropertiesLabel()
		{
			return "Environment Properties";
		}

		public TestProperty getProperty( String name )
		{
			Map<String, String> properties = System.getenv();
			return properties.containsKey( name ) ? new EnviromentTestProperty( name ) : null;
		}

		public TestProperty getPropertyAt( int index )
		{
			return getProperty( getPropertyNames()[index] );
		}

		public int getPropertyCount()
		{
			return System.getenv().size();
		}

		public String[] getPropertyNames()
		{
			Set<String> keys = System.getenv().keySet();
			StringList result = new StringList();
			for( Object key : keys )
				result.add( key.toString() );
			return result.toStringArray();
		}

		public String getPropertyValue( String name )
		{
			TestProperty property = getProperty( name );
			return property == null ? null : property.getValue();
		}

		public boolean hasProperty( String name )
		{
			return System.getenv().containsKey( name );
		}

		public void removeTestPropertyListener( TestPropertyListener listener )
		{
		}

		public void setPropertyValue( String name, String value )
		{
		}

		private class EnviromentTestProperty implements TestProperty
		{
			private final Object key;

			public EnviromentTestProperty( Object key )
			{
				this.key = key;
			}

			public String getDefaultValue()
			{
				return null;
			}

			public String getDescription()
			{
				return null;
			}

			public ModelItem getModelItem()
			{
				return null;
			}

			public String getName()
			{
				return key.toString();
			}

			public QName getType()
			{
				return XmlString.type.getName();
			}

			public String getValue()
			{
				return System.getenv( key.toString() );
			}

			public boolean isReadOnly()
			{
				return true;
			}

			public void setValue( String value )
			{
			}
		}
	}

	public class SystemPropertyHolder implements TestPropertyHolder
	{
		public void addTestPropertyListener( TestPropertyListener listener )
		{
		}

		public ModelItem getModelItem()
		{
			return null;
		}

		public Map<String, TestProperty> getProperties()
		{
			Properties properties = System.getProperties();
			Map<String, TestProperty> result = new HashMap<String, TestProperty>();

			for( Object key : properties.keySet() )
			{
				result.put( key.toString(), new SystemTestProperty( key ) );
			}

			return result;
		}

		public String getPropertiesLabel()
		{
			return "System Properties";
		}

		public TestProperty getProperty( String name )
		{
			Properties properties = System.getProperties();
			return properties.containsKey( name ) ? new SystemTestProperty( name ) : null;
		}

		public TestProperty getPropertyAt( int index )
		{
			return getProperty( getPropertyNames()[index] );
		}

		public int getPropertyCount()
		{
			return System.getProperties().size();
		}

		public List<TestProperty> getPropertyList()
		{
			List<TestProperty> result = new ArrayList<TestProperty>();

			for( TestProperty property : getProperties().values() )
				result.add( property );

			return result;
		}

		public String[] getPropertyNames()
		{
			Set<Object> keys = System.getProperties().keySet();
			StringList result = new StringList();
			for( Object key : keys )
				result.add( key.toString() );
			return result.toStringArray();
		}

		public String getPropertyValue( String name )
		{
			TestProperty property = getProperty( name );
			return property == null ? null : property.getValue();
		}

		public boolean hasProperty( String name )
		{
			return System.getProperties().containsKey( name );
		}

		public void removeTestPropertyListener( TestPropertyListener listener )
		{
		}

		public void setPropertyValue( String name, String value )
		{
			System.setProperty( name, value );
		}

		private class SystemTestProperty implements TestProperty
		{
			private final Object key;

			public SystemTestProperty( Object key )
			{
				this.key = key;
			}

			public String getDefaultValue()
			{
				return null;
			}

			public String getDescription()
			{
				return null;
			}

			public ModelItem getModelItem()
			{
				return null;
			}

			public String getName()
			{
				return key.toString();
			}

			public QName getType()
			{
				return XmlString.type.getName();
			}

			public String getValue()
			{
				return System.getProperty( key.toString() );
			}

			public boolean isReadOnly()
			{
				return false;
			}

			public void setValue( String value )
			{
				System.setProperty( key.toString(), value );
			}
		}
	}

	private SystemPropertyHolder systemPropertyHolder;
	private EnvironmentPropertyHolder environmentPropertyHolder;

	public GlobalPropertyResolver()
	{
		systemPropertyHolder = new SystemPropertyHolder();
		environmentPropertyHolder = new EnvironmentPropertyHolder();
	}

	public String resolveProperty( PropertyExpansionContext context, String name, boolean globalOverride )
	{
		String result = ResolverUtils.checkForExplicitReference( name, PropertyExpansion.GLOBAL_REFERENCE,
				PropertyExpansionUtils.getGlobalProperties(), context, false );
		if( result != null )
			return result;

		result = ResolverUtils.checkForExplicitReference( name, PropertyExpansion.SYSTEM_REFERENCE, systemPropertyHolder,
				context, globalOverride );
		if( result != null )
			return result;

		result = ResolverUtils.checkForExplicitReference( name, PropertyExpansion.ENV_REFERENCE,
				environmentPropertyHolder, context, globalOverride );
		if( result != null )
			return result;

		// if not, check for explicit global property (stupid 1.7.6 syntax that
		// should be removed..)
		if( name.length() > 2 && name.charAt( 0 ) == PropertyExpansion.PROPERTY_SEPARATOR
				&& name.charAt( 1 ) == PropertyExpansion.PROPERTY_SEPARATOR )
			return PropertyExpansionUtils.getGlobalProperty( name.substring( 2 ) );
		else
			return PropertyExpansionUtils.getGlobalProperty( name );

	}
}
