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

package com.eviware.soapui.impl.rest.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestParameterConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.testsuite.RenameableTestProperty;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;

public class XmlBeansRestParamsTestPropertyHolder implements MutableTestPropertyHolder, Map<String, TestProperty>
{
	private RestParametersConfig config;
	private List<RestParamProperty> properties = new ArrayList<RestParamProperty>();
	private Map<String, RestParamProperty> propertyMap = new HashMap<String, RestParamProperty>();
	private Set<TestPropertyListener> listeners = new HashSet<TestPropertyListener>();
	private ModelItem modelItem;
	private Properties overrideProperties;
	private String propertiesLabel = "Test Properties";

	public XmlBeansRestParamsTestPropertyHolder( ModelItem modelItem, RestParametersConfig config )
	{
		this.modelItem = modelItem;
		this.config = config;

		for( RestParameterConfig propertyConfig : config.getParameterList() )
		{
			addProperty( propertyConfig, false );
		}
	}

	protected RestParamProperty addProperty( RestParameterConfig propertyConfig, boolean notify )
	{
		RestParamProperty propertiesStepProperty = new RestParamProperty( propertyConfig );
		properties.add( propertiesStepProperty );
		propertyMap.put( propertiesStepProperty.getName().toUpperCase(), propertiesStepProperty );

		if( notify )
		{
			firePropertyAdded( propertiesStepProperty.getName() );
		}

		return propertiesStepProperty;
	}

	private void firePropertyAdded( String name )
	{
		TestPropertyListener[] listenersArray = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : listenersArray )
		{
			listener.propertyAdded( name );
		}
	}

	private void firePropertyRemoved( String name )
	{
		TestPropertyListener[] listenersArray = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : listenersArray )
		{
			listener.propertyRemoved( name );
		}
	}

	private void firePropertyMoved( String name, int oldIndex, int newIndex )
	{
		TestPropertyListener[] listenersArray = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : listenersArray )
		{
			listener.propertyMoved( name, oldIndex, newIndex );
		}
	}

	private void firePropertyRenamed( String oldName, String newName )
	{
		TestPropertyListener[] listenersArray = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : listenersArray )
		{
			listener.propertyRenamed( oldName, newName );
		}
	}

	private void firePropertyValueChanged( String name, String oldValue, String newValue )
	{
		TestPropertyListener[] listenersArray = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : listenersArray )
		{
			listener.propertyValueChanged( name, oldValue, newValue );
		}
	}

	public RestParamProperty addProperty( String name )
	{
		RestParameterConfig propertyConfig = config.addNewParameter();
		propertyConfig.setName( name );
		return addProperty( propertyConfig, true );
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		listeners.add( listener );
	}

	public RestParamProperty getProperty( String name )
	{
		return propertyMap.get( name.toUpperCase() );
	}

	public String[] getPropertyNames()
	{
		String[] result = new String[properties.size()];
		for( int c = 0; c < properties.size(); c++ )
			result[c] = properties.get( c ).getName();

		return result;
	}

	public String getPropertyValue( String name )
	{
		TestProperty property = getProperty( name );
		return property == null ? null : property.getValue();
	}

	public RestParamProperty removeProperty( String propertyName )
	{
		RestParamProperty property = getProperty( propertyName );
		if( property != null )
		{
			int ix = properties.indexOf( property );
			propertyMap.remove( propertyName.toUpperCase() );
			properties.remove( ix );
			config.removeParameter( ix );

			firePropertyRemoved( propertyName );
			return property;
		}

		return null;
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		listeners.remove( listener );
	}

	public void setPropertyValue( String name, String value )
	{
		RestParamProperty property = getProperty( name );
		if( property != null )
			property.setValue( value );
		else
			addProperty( name ).setValue( value );
	}

	public void resetValues()
	{
		for( RestParamProperty property : properties )
		{
			property.reset();
		}
	}

	public void resetPropertiesConfig( RestParametersConfig config )
	{
		this.config = config;

		for( int c = 0; c < config.sizeOfParameterArray(); c++ )
		{
			properties.get( c ).setConfig( config.getParameterArray( c ) );
		}
	}

	public boolean renameProperty( String name, String newName )
	{
		if( getProperty( newName ) != null )
			return false;

		RestParamProperty property = getProperty( name );
		if( property == null )
			return false;

		property.setName( newName );
		return true;
	}

	public int getPropertyIndex( String name )
	{
		for( int c = 0; c < properties.size(); c++ )
		{
			if( properties.get( c ).getName().equals( name ) )
			{
				return c;
			}
		}

		return -1;
	}

	/**
	 * Internal property class
	 * 
	 * @author ole
	 */

	public enum ParameterStyle
	{
		MATRIX, HEADER, QUERY, TEMPLATE, PLAIN
	}

	public class RestParamProperty implements RenameableTestProperty, RestParameter
	{
		private RestParameterConfig propertyConfig;
		private PropertyChangeSupport propertySupport;

		public RestParamProperty( RestParameterConfig propertyConfig )
		{
			this.propertyConfig = propertyConfig;

			propertySupport = new PropertyChangeSupport( this );
		}

		public void addPropertyChangeListener( PropertyChangeListener listener )
		{
			propertySupport.addPropertyChangeListener( listener );
		}

		public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
		{
			propertySupport.addPropertyChangeListener( propertyName, listener );
		}

		public void removePropertyChangeListener( PropertyChangeListener listener )
		{
			propertySupport.removePropertyChangeListener( listener );
		}

		public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
		{
			propertySupport.removePropertyChangeListener( propertyName, listener );
		}

		public void setConfig( RestParameterConfig restParameterConfig )
		{
			this.propertyConfig = restParameterConfig;
		}

		public String getName()
		{
			return propertyConfig.getName();
		}

		public void setName( String name )
		{
			String oldName = getName();
			propertyConfig.setName( name );

			propertyMap.remove( oldName.toUpperCase() );
			propertyMap.put( name.toUpperCase(), this );

			firePropertyRenamed( oldName, name );
		}

		public String getDescription()
		{
			return propertyConfig.getDescription();
		}

		public void setDescription( String description )
		{
			String old = getDescription();
			propertyConfig.setDescription( description );
			propertySupport.firePropertyChange( "description", old, description );
		}

		public ParameterStyle getStyle()
		{
			if( propertyConfig.xgetStyle() == null )
				propertyConfig.setStyle( RestParameterConfig.Style.QUERY );

			return ParameterStyle.valueOf( propertyConfig.getStyle().toString() );
		}

		public void setStyle( ParameterStyle style )
		{
			ParameterStyle old = getStyle();

			propertyConfig.setStyle( RestParameterConfig.Style.Enum.forString( style.name() ) );
			propertySupport.firePropertyChange( "style", old, style );
		}

		public String getValue()
		{
			if( overrideProperties != null && overrideProperties.containsKey( getName() ) )
				return overrideProperties.getProperty( getName() );

			return propertyConfig.getValue() == null ? "" : propertyConfig.getValue();
		}

		public void setValue( String value )
		{
			String oldValue = getValue();
			propertyConfig.setValue( value );

			if( overrideProperties != null && overrideProperties.containsKey( getName() ) )
			{
				overrideProperties.remove( getName() );
				if( overrideProperties.isEmpty() )
					overrideProperties = null;
			}

			firePropertyValueChanged( getName(), oldValue, value );
		}

		public boolean isReadOnly()
		{
			return false;
		}

		public ModelItem getModelItem()
		{
			return modelItem;
		}

		public String getDefaultValue()
		{
			return propertyConfig.isSetDefault() ? propertyConfig.getDefault() : "";
		}

		public String[] getOptions()
		{
			return propertyConfig.getOptionList().toArray( new String[propertyConfig.sizeOfOptionArray()] );
		}

		public boolean getRequired()
		{
			return propertyConfig.getRequired();
		}

		public boolean isDisableUrlEncoding()
		{
			return propertyConfig.getDisableUrlEncoding();
		}

		public void setDisableUrlEncoding( boolean encode )
		{
			boolean old = isDisableUrlEncoding();
			if( old == encode )
				return;

			propertyConfig.setDisableUrlEncoding( encode );
			propertySupport.firePropertyChange( "disableUrlEncoding", old, encode );
		}

		public QName getType()
		{
			return propertyConfig.isSetType() ? propertyConfig.getType() : XmlString.type.getName();
		}

		public void setOptions( String[] arg0 )
		{
			String[] old = getOptions();
			propertyConfig.setOptionArray( arg0 );
			propertySupport.firePropertyChange( "options", old, arg0 );
		}

		public void setRequired( boolean arg0 )
		{
			boolean old = getRequired();
			if( old == arg0 )
				return;
			propertyConfig.setRequired( arg0 );
			propertySupport.firePropertyChange( "required", old, arg0 );
		}

		public void setType( QName arg0 )
		{
			propertyConfig.setType( arg0 );
		}

		public void setDefaultValue( String default1 )
		{
			String old = default1;
			propertyConfig.setDefault( default1 );
			propertySupport.firePropertyChange( "defaultValue", old, default1 );
		}

		public RestParameterConfig getConfig()
		{
			return propertyConfig;
		}

		@Override
		public boolean equals( Object obj )
		{
			if( obj instanceof RestParamProperty )
			{
				return propertyConfig.toString().equals( ( ( RestParamProperty )obj ).propertyConfig.toString() );
			}

			return super.equals( obj );
		}

		public void reset()
		{
			setValue( getDefaultValue() );
		}
	}

	public void saveTo( Properties props )
	{
		int cnt = 0;
		for( RestParamProperty p : properties )
		{
			String name = p.getName();
			String value = p.getValue();
			if( value == null )
				value = "";

			props.setProperty( name, value );
			cnt++ ;
		}
	}

	public int getPropertyCount()
	{
		return properties.size();
	}

	public RestParamProperty getPropertyAt( int index )
	{
		return properties.get( index );
	}

	public List<TestProperty> getPropertyList()
	{
		List<TestProperty> result = new ArrayList<TestProperty>();

		for( TestProperty property : properties )
			result.add( property );

		return result;
	}

	public Map<String, TestProperty> getProperties()
	{
		Map<String, TestProperty> result = new HashMap<String, TestProperty>();
		for( TestProperty property : propertyMap.values() )
		{
			result.put( property.getName(), property );
		}

		return result;
	}

	public boolean hasProperty( String name )
	{
		return propertyMap.containsKey( name.toUpperCase() );
	}

	public int addPropertiesFromFile( String propFile )
	{
		if( !StringUtils.hasContent( propFile ) )
			return 0;

		try
		{
			InputStream input = null;

			File file = new File( propFile );
			if( file.exists() )
			{
				input = new FileInputStream( file );
			}
			else if( propFile.toLowerCase().startsWith( "http://" ) || propFile.toLowerCase().startsWith( "https://" ) )
			{
				UrlWsdlLoader loader = new UrlWsdlLoader( propFile, getModelItem() );
				loader.setUseWorker( false );
				input = loader.load();
			}

			if( input != null )
			{
				if( overrideProperties == null )
					overrideProperties = new Properties();

				int sz = overrideProperties.size();
				overrideProperties.load( input );

				for( Object key : overrideProperties.keySet() )
				{
					String name = key.toString();
					if( !hasProperty( name ) )
						addProperty( name );
				}

				return overrideProperties.size() - sz;
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		return 0;
	}

	public ModelItem getModelItem()
	{
		return modelItem;
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

		return result.toArray( new PropertyExpansion[result.size()] );
	}

	public void moveProperty( String propertyName, int targetIndex )
	{
		RestParamProperty property = getProperty( propertyName );
		int ix = properties.indexOf( property );

		if( ix == targetIndex )
			return;

		if( targetIndex < 0 )
			targetIndex = 0;

		String value = property.getValue();
		config.removeParameter( ix );

		RestParameterConfig propertyConfig = null;

		if( targetIndex < properties.size() )
		{
			properties.add( targetIndex, properties.remove( ix ) );
			propertyConfig = config.insertNewParameter( targetIndex );
		}
		else
		{
			properties.add( properties.remove( ix ) );
			propertyConfig = config.addNewParameter();
		}

		propertyConfig.setName( propertyName );
		propertyConfig.setValue( value );

		resetPropertiesConfig( config );

		if( targetIndex > properties.size() )
			targetIndex = properties.size();

		firePropertyMoved( propertyName, ix, targetIndex );
	}

	public void clear()
	{
		while( size() > 0 )
			removeProperty( getPropertyAt( 0 ).getName() );
	}

	public boolean containsKey( Object key )
	{
		return hasProperty( ( String )key );
	}

	public boolean containsValue( Object value )
	{
		return propertyMap.containsValue( value );
	}

	public Set<java.util.Map.Entry<String, TestProperty>> entrySet()
	{
		HashSet<java.util.Map.Entry<String, TestProperty>> result = new HashSet<Entry<String, TestProperty>>();

		for( TestProperty p : propertyMap.values() )
		{
			// This does not compile on JDK 1.5:
			// result.add( new java.util.HashMap.SimpleEntry<String,
			// TestProperty>(p.getName(), p));
			result.add( new HashMapEntry<String, TestProperty>( p.getName(), p ) );
		}

		return result;
	}

	private static class HashMapEntry<K, V> implements java.util.Map.Entry<K, V>
	{
		private K key;
		private V value;

		public HashMapEntry( K key, V value )
		{
			this.key = key;
			this.value = value;
		}

		public K getKey()
		{
			return key;
		}

		public V getValue()
		{
			return value;
		}

		public V setValue( V value )
		{
			throw new UnsupportedOperationException();
		}
	}

	public RestParamProperty get( Object key )
	{
		return getProperty( ( String )key );
	}

	public boolean isEmpty()
	{
		return propertyMap.isEmpty();
	}

	public Set<String> keySet()
	{
		return new HashSet<String>( Arrays.asList( getPropertyNames() ) );
	}

	public TestProperty put( String key, TestProperty value )
	{
		TestProperty result = addProperty( key );
		result.setValue( value.getValue() );
		return result;
	}

	public void putAll( Map<? extends String, ? extends TestProperty> m )
	{
		for( TestProperty p : m.values() )
		{
			addProperty( p.getName() ).setValue( p.getValue() );
		}
	}

	public TestProperty remove( Object key )
	{
		return removeProperty( ( String )key );
	}

	public int size()
	{
		return propertyMap.size();
	}

	public Collection<TestProperty> values()
	{
		ArrayList<TestProperty> result = new ArrayList<TestProperty>();
		result.addAll( propertyMap.values() );
		return result;
	}

	public String getPropertiesLabel()
	{
		return propertiesLabel;
	}

	public void setPropertiesLabel( String propertiesLabel )
	{
		this.propertiesLabel = propertiesLabel;
	}

	public XmlObject getConfig()
	{
		return config;
	}

	public void addParameters( XmlBeansRestParamsTestPropertyHolder params )
	{
		for( int c = 0; c < params.getPropertyCount(); c++ )
		{
			RestParamProperty property = params.getPropertyAt( c );
			if( !hasProperty( property.getName() ) )
			{
				RestParamProperty prop = addProperty( property.getName() );
				prop.setStyle( property.getStyle() );
				prop.setValue( property.getValue() );
				prop.setType( property.getType() );
				prop.setDefaultValue( property.getDefaultValue() );
				prop.setDescription( property.getDescription() );
				prop.setOptions( property.getOptions() );
				prop.setRequired( property.getRequired() );
			}
		}
	}

	public void release()
	{
	}

	public RestParamProperty addProperty( RestParamProperty prop )
	{
		RestParameterConfig propertyConfig = ( RestParameterConfig )config.addNewParameter().set( prop.getConfig() );
		return addProperty( propertyConfig, true );
	}
}
