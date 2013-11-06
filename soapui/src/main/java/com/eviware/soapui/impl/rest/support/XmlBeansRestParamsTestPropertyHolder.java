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

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestParameterConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;

public class XmlBeansRestParamsTestPropertyHolder implements RestParamsPropertyHolder
{
	public static final String PROPERTY_STYLE = "style";
	public static final String PARAM_LOCATION = "paramLocation";
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

	protected XmlBeansRestParamProperty addProperty( RestParameterConfig propertyConfig, boolean notify )
	{
		XmlBeansRestParamProperty propertiesStepProperty = new XmlBeansRestParamProperty( propertyConfig,
				getParamLocation());
		properties.add( propertiesStepProperty );
		propertyMap.put( propertiesStepProperty.getName().toUpperCase(), propertiesStepProperty );

		if( notify )
		{
			firePropertyAdded( propertiesStepProperty.getName() );
		}

		return propertiesStepProperty;
	}

	private ParamLocation getParamLocation()
	{

		//TODO: uncomment when we suppor request level parameters
		/*if(getModelItem() instanceof RestRequest)
		{
			return ParamLocation.REQUEST;
		} else*/
		if (getModelItem()==null || getModelItem() instanceof RestResource)
		{
			return ParamLocation.RESOURCE;
		} else if (getModelItem() instanceof RestMethod)
		{
			return ParamLocation.METHOD;
		}
		return null;
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
		if( hasProperty( name ) )
			return getProperty( name );
		RestParameterConfig propertyConfig = config.addNewParameter();
		propertyConfig.setName( name );
		return addProperty( propertyConfig, true );
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		listeners.add( listener );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder#getProperty
	 * (java.lang.String)
	 */
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

			firePropertyRemoved( propertyName );

			config.removeParameter( ix );
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder#resetValues
	 * ()
	 */
	public void resetValues()
	{
		for( RestParamProperty property : properties )
		{
			( ( XmlBeansRestParamProperty )property ).reset();
		}
	}

	public void resetPropertiesConfig( RestParametersConfig config )
	{
		this.config = config;

		for( int c = 0; c < config.sizeOfParameterArray(); c++ )
		{
			( ( XmlBeansRestParamProperty )properties.get( c ) ).setConfig( config.getParameterArray( c ) );
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

		firePropertyRenamed( name, newName );
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder#getPropertyIndex
	 * (java.lang.String)
	 */
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

	public class XmlBeansRestParamProperty implements RestParamProperty
	{
		private RestParameterConfig propertyConfig;
		private PropertyChangeSupport propertySupport;
		private ParamLocation paramLocation;

		public XmlBeansRestParamProperty( RestParameterConfig propertyConfig, ParamLocation location )
		{
			this.propertyConfig = propertyConfig;
			this.paramLocation = location;
			propertySupport = new PropertyChangeSupport( this );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seecom.eviware.soapui.impl.rest.support.RestParamProperty#
		 * addPropertyChangeListener(java.beans.PropertyChangeListener)
		 */
		public void addPropertyChangeListener( PropertyChangeListener listener )
		{
			propertySupport.addPropertyChangeListener( listener );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seecom.eviware.soapui.impl.rest.support.RestParamProperty#
		 * addPropertyChangeListener(java.lang.String,
		 * java.beans.PropertyChangeListener)
		 */
		public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
		{
			propertySupport.addPropertyChangeListener( propertyName, listener );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seecom.eviware.soapui.impl.rest.support.RestParamProperty#
		 * removePropertyChangeListener(java.beans.PropertyChangeListener)
		 */
		public void removePropertyChangeListener( PropertyChangeListener listener )
		{
			propertySupport.removePropertyChangeListener( listener );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seecom.eviware.soapui.impl.rest.support.RestParamProperty#
		 * removePropertyChangeListener(java.lang.String,
		 * java.beans.PropertyChangeListener)
		 */
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

		public ParamLocation getParamLocation()
		{
			return this.paramLocation;
		}

		public void setParamLocation( ParamLocation paramLocation )
		{
			if(this.paramLocation==paramLocation)
			{
				return;
			}
			ParamLocation old = this.paramLocation;
			this.paramLocation = paramLocation;
			propertySupport.firePropertyChange( PARAM_LOCATION, old, this.paramLocation );
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
			propertySupport.firePropertyChange( PROPERTY_STYLE, old, style );
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.eviware.soapui.impl.rest.support.RestParamProperty#isDisableUrlEncoding
		 * ()
		 */
		public boolean isDisableUrlEncoding()
		{
			return propertyConfig.getDisableUrlEncoding();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seecom.eviware.soapui.impl.rest.support.RestParamProperty#
		 * setDisableUrlEncoding(boolean)
		 */
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
			QName old = getType();
			if(old.equals( arg0 ))
			{
				return;
			}
			propertyConfig.setType( arg0 );
			propertySupport.firePropertyChange( "type", old, arg0 );
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
			if( obj instanceof XmlBeansRestParamProperty )
			{
				return propertyConfig.toString().equals( ( ( XmlBeansRestParamProperty )obj ).propertyConfig.toString() );
			}

			return super.equals( obj );
		}

		public void reset()
		{
			setValue( getDefaultValue() );
		}

		@Override
		public String getPath()
		{
			return propertyConfig.getPath();

		}

		@Override
		public void setPath( String path )
		{
			String old = getPath();
			propertyConfig.setPath( path );
			propertySupport.firePropertyChange( "path", old, path );

		}

		@Override
		public boolean isRequestPart()
		{
			return false;
		}

		@Override
		public SchemaType getSchemaType()
		{
			return XmlBeans.getBuiltinTypeSystem().findType( getType() );
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder#saveTo(java
	 * .util.Properties)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder#getPropertyAt
	 * (int)
	 */
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
		for( RestParamProperty property : propertyMap.values() )
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

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.eviware.soapui.impl.rest.support.RestParamsPropertyHolder#
	 * getPropertyExpansions()
	 */
	public PropertyExpansion[] getPropertyExpansions()
	{
		List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

		for( RestParamProperty prop : properties )
			result.addAll( PropertyExpansionUtils.extractPropertyExpansions( getModelItem(), prop, "value" ) );

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
		String defaultValue = property.getDefaultValue();
		String style = property.getStyle().name();
		String[] options = property.getOptions();
		boolean required = property.getRequired();
		QName type = property.getType();
		String description = property.getDescription();
		boolean disableUrlEncoding = property.isDisableUrlEncoding();

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
		propertyConfig.setDefault( defaultValue );
		propertyConfig.setStyle( RestParameterConfig.Style.Enum.forString( style ) );
		propertyConfig.setOptionArray( options );
		propertyConfig.setRequired( required );
		propertyConfig.setType( type );
		propertyConfig.setDescription( description );
		propertyConfig.setDisableUrlEncoding( disableUrlEncoding );

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

	public void addParameters( RestParamsPropertyHolder params )
	{
		for( int c = 0; c < params.getPropertyCount(); c++ )
		{
			RestParamProperty property = params.getPropertyAt( c );
			if( !hasProperty( property.getName() ) )
			{
				addParameter( property );
			}
		}
	}

	public void addParameter( RestParamProperty property )
	{
		RestParamProperty prop = addProperty( property.getName() );
		prop.setStyle( property.getStyle() );
		prop.setValue( property.getValue() );
		prop.setType( property.getType() );
		prop.setDefaultValue( property.getDefaultValue() );
		prop.setDescription( property.getDescription() );
		prop.setOptions( property.getOptions() );
		prop.setPath( property.getPath() );
		prop.setRequired( property.getRequired() );
	}

	public void release()
	{
	}

	public RestParamProperty addProperty( XmlBeansRestParamProperty prop )
	{
		RestParameterConfig propertyConfig = ( RestParameterConfig )config.addNewParameter().set( prop.getConfig() );
		return addProperty( propertyConfig, true );
	}
}
