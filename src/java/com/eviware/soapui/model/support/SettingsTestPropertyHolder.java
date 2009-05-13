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

package com.eviware.soapui.model.support;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.PropertiesTypeConfig;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder.PropertiesStepProperty;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.settings.GlobalPropertySettings;
import com.eviware.soapui.support.StringUtils;

public class SettingsTestPropertyHolder implements MutableTestPropertyHolder, Map<String, TestProperty>
{
	public final static Logger log = Logger.getLogger( SettingsTestPropertyHolder.class );
	private XmlBeansPropertiesTestPropertyHolder propertyHolderSupport;
	private PropertiesTypeConfig config;
	private final ModelItem modelItem;
	private String propertiesLabel = "Test Properties";

	public SettingsTestPropertyHolder( Settings settings, ModelItem modelItem )
	{
		this.modelItem = modelItem;
		config = PropertiesTypeConfig.Factory.newInstance();
		try
		{
			String str = settings.getString( GlobalPropertySettings.PROPERTIES, null );
			if( StringUtils.hasContent( str ) )
				config = PropertiesTypeConfig.Factory.parse( str );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		propertyHolderSupport = new XmlBeansPropertiesTestPropertyHolder( null, config );
	}

	public TestProperty addProperty( String name )
	{
		return propertyHolderSupport.addProperty( name );
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		propertyHolderSupport.addTestPropertyListener( listener );
	}

	public Map<String, TestProperty> getProperties()
	{
		return propertyHolderSupport.getProperties();
	}

	public PropertiesStepProperty getProperty( String name )
	{
		return propertyHolderSupport.getProperty( name );
	}

	public String[] getPropertyNames()
	{
		return propertyHolderSupport.getPropertyNames();
	}

	public List<TestProperty> getPropertyList()
	{
		return propertyHolderSupport.getPropertyList();
	}

	public String getPropertyValue( String name )
	{
		return propertyHolderSupport.getPropertyValue( name );
	}

	public boolean hasProperty( String name )
	{
		return propertyHolderSupport.hasProperty( name );
	}

	public TestProperty removeProperty( String propertyName )
	{
		return propertyHolderSupport.removeProperty( propertyName );
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		propertyHolderSupport.removeTestPropertyListener( listener );
	}

	public boolean renameProperty( String name, String newName )
	{
		return propertyHolderSupport.renameProperty( name, newName );
	}

	public void saveTo( Settings settings )
	{
		settings.setString( GlobalPropertySettings.PROPERTIES, config.toString() );
	}

	public void setPropertyValue( String name, String value )
	{
		propertyHolderSupport.setPropertyValue( name, value );
	}

	public int addPropertiesFromFile( String propFile )
	{
		return propertyHolderSupport.addPropertiesFromFile( propFile );
	}

	public ModelItem getModelItem()
	{
		return modelItem;
	}

	public void moveProperty( String propertyName, int targetIndex )
	{
		propertyHolderSupport.moveProperty( propertyName, targetIndex );
	}

	public TestProperty getPropertyAt( int index )
	{
		return propertyHolderSupport.getPropertyAt( index );
	}

	public int getPropertyCount()
	{
		return propertyHolderSupport.getPropertyCount();
	}

	public void clear()
	{
		propertyHolderSupport.clear();
	}

	public boolean containsKey( Object key )
	{
		return propertyHolderSupport.containsKey( key );
	}

	public boolean containsValue( Object value )
	{
		return propertyHolderSupport.containsValue( value );
	}

	public Set<java.util.Map.Entry<String, TestProperty>> entrySet()
	{
		return propertyHolderSupport.entrySet();
	}

	public TestProperty get( Object key )
	{
		return propertyHolderSupport.get( key );
	}

	public boolean isEmpty()
	{
		return propertyHolderSupport.isEmpty();
	}

	public Set<String> keySet()
	{
		return propertyHolderSupport.keySet();
	}

	public TestProperty put( String key, TestProperty value )
	{
		return propertyHolderSupport.put( key, value );
	}

	public void putAll( Map<? extends String, ? extends TestProperty> m )
	{
		propertyHolderSupport.putAll( m );
	}

	public TestProperty remove( Object key )
	{
		return propertyHolderSupport.remove( key );
	}

	public int size()
	{
		return propertyHolderSupport.size();
	}

	public Collection<TestProperty> values()
	{
		return propertyHolderSupport.values();
	}

	public String getPropertiesLabel()
	{
		return propertiesLabel;
	}

	public void setPropertiesLabel( String propertiesLabel )
	{
		this.propertiesLabel = propertiesLabel;
	}
}