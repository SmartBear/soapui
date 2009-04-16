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

package com.eviware.soapui.impl.wsdl;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.config.PropertiesTypeConfig;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;

public abstract class AbstractTestPropertyHolderWsdlModelItem<T extends ModelItemConfig> extends
		AbstractWsdlModelItem<T> implements MutableTestPropertyHolder
{
	private XmlBeansPropertiesTestPropertyHolder propertyHolderSupport;
	private final static Logger log = Logger.getLogger( AbstractTestPropertyHolderWsdlModelItem.class );

	protected AbstractTestPropertyHolderWsdlModelItem( T config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
	}

	protected void setPropertiesConfig( PropertiesTypeConfig config )
	{
		if( propertyHolderSupport == null )
			propertyHolderSupport = new XmlBeansPropertiesTestPropertyHolder( this, config );
		else
			propertyHolderSupport.resetPropertiesConfig( config );

		String propertyName = createPropertyName( getName() );
		if( StringUtils.hasContent( propertyName ) )
		{
			String propFile = System.getProperty( "soapui.properties." + propertyName );
			if( StringUtils.hasContent( propFile ) )
			{
				int result = propertyHolderSupport.addPropertiesFromFile( propFile );
				if( result > 0 )
				{
					log.info( "Overriding " + result + " properties from [" + propFile + "] in [" + getName() + "]" );
				}
			}
		}
	}

	private String createPropertyName( String str )
	{
		if( str == null )
			return null;

		StringBuffer result = new StringBuffer();
		for( char ch : str.toCharArray() )
		{
			if( Character.isLetterOrDigit( ch ) )
				result.append( ch );
		}

		return result.toString();
	}

	public int addPropertiesFromFile( String propFile )
	{
		return propertyHolderSupport.addPropertiesFromFile( propFile );
	}

	public void saveProperties( Properties props )
	{
		propertyHolderSupport.saveTo( props );
	}

	public TestProperty addProperty( String name )
	{
		return propertyHolderSupport.addProperty( name );
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		propertyHolderSupport.addTestPropertyListener( listener );
	}

	public TestProperty getProperty( String name )
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

	public TestProperty removeProperty( String propertyName )
	{
		return propertyHolderSupport.removeProperty( propertyName );
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		propertyHolderSupport.removeTestPropertyListener( listener );
	}

	public void setPropertyValue( String name, String value )
	{
		propertyHolderSupport.setPropertyValue( name, value );
	}

	public boolean renameProperty( String name, String newName )
	{
		return PropertyExpansionUtils.renameProperty( propertyHolderSupport.getProperty( name ), newName, this ) != null;
	}

	public Map<String, TestProperty> getProperties()
	{
		return propertyHolderSupport.getProperties();
	}

	public boolean hasProperty( String name )
	{
		return propertyHolderSupport.hasProperty( name );
	}

	public TestProperty getPropertyAt( int index )
	{
		return propertyHolderSupport.getPropertyAt( index );
	}

	public int getPropertyCount()
	{
		return propertyHolderSupport.getPropertyCount();
	}

	public void moveProperty( String propertyName, int targetIndex )
	{
		propertyHolderSupport.moveProperty( propertyName, targetIndex );
	}

	public ModelItem getModelItem()
	{
		return this;
	}

	public String getPropertiesLabel()
	{
		return "Test Properties";
	}
}
