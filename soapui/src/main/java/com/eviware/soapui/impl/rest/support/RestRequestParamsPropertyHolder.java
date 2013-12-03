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

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.StringListConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.types.StringToStringMap;
import org.apache.xmlbeans.SchemaType;

import javax.xml.namespace.QName;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class RestRequestParamsPropertyHolder implements RestParamsPropertyHolder, TestPropertyListener
{
	private StringToStringMap values;
	private RestParamsPropertyHolder methodParams;
	private List<String> sortedPropertyNames;
	private RestRequest restRequest;
	private Set<TestPropertyListener> listeners = new HashSet<TestPropertyListener>();
	private Map<RestParamProperty, InternalRestParamProperty> wrappers = new HashMap<RestParamProperty, InternalRestParamProperty>();
	private String parameterBeingMoved;

	public RestRequestParamsPropertyHolder( RestParamsPropertyHolder methodParams, RestRequest restRequest,
														 StringToStringMap values )
	{
		this.methodParams = methodParams;
		this.restRequest = restRequest;
		buildPropertyNameList();
		this.values = values;
		methodParams.addTestPropertyListener( this );
	}

	private void buildPropertyNameList()
	{
		RestRequestConfig requestConfig = restRequest.getConfig();
		List<String> propertyNames;
		List<String> methodParamNames = new ArrayList<String>( Arrays.asList( methodParams.getPropertyNames() ) );
		if( requestConfig.isSetParameterOrder() )
		{
			propertyNames = new ArrayList<String>( requestConfig.getParameterOrder().getEntryList() );
			propertyNames.retainAll( methodParamNames );
			methodParamNames.removeAll( propertyNames );
			propertyNames.addAll( methodParamNames );
		}
		else
		{
			propertyNames = new ArrayList<String>( methodParamNames );
		}
		sortedPropertyNames = propertyNames;
	}

	public void reset( RestParamsPropertyHolder methodParams, StringToStringMap values )
	{
		this.methodParams = methodParams;
		this.values = values;

		clearWrappers();
	}

	private void clearWrappers()
	{
		for( InternalRestParamProperty property : wrappers.values() )
		{
			property.release();
		}

		wrappers.clear();
	}

	public RestParamProperty addProperty( String name )
	{

		RestParamProperty property = methodParams.addProperty( name );
		setParameterLocation( property, NewRestResourceActionBase.ParamLocation.RESOURCE );
		//setting the param location changes the parent of the property, hence need to get it again
		return getWrapper(methodParams.getProperty( name ));
	}

	public void addParameter( RestParamProperty prop )
	{
		methodParams.addParameter( prop );
	}

	@Override
	public void setParameterLocation( RestParamProperty parameter, NewRestResourceActionBase.ParamLocation newLocation )
	{
		if (newLocation == parameter.getParamLocation())
		{
			return;
		}
		parameterBeingMoved = parameter.getName();
		try
		{
			ParameterStyle parameterStyle = parameter.getStyle();
			String parameterValue = parameter.getValue();
			QName type = parameter.getType();
			String[] options = parameter.getOptions();
			boolean required = parameter.getRequired();
			String description = parameter.getDescription();
			boolean disableURLEncoding = parameter.isDisableUrlEncoding();
			RestParamProperty newParameter;
			List<String> copyOfSortedPropertyNames = new ArrayList<String>(sortedPropertyNames);
			if (newLocation == NewRestResourceActionBase.ParamLocation.METHOD)
			{
				restRequest.getResource().removeProperty( parameterBeingMoved );
				newParameter = restRequest.getRestMethod().addProperty( parameterBeingMoved );
			}
			else
			{
				restRequest.getRestMethod().removeProperty( parameterBeingMoved );
				newParameter = restRequest.getResource().addProperty( parameterBeingMoved );
			}
			newParameter.setType(type);
			newParameter.setStyle( parameterStyle );
			newParameter.setValue( parameterValue );
			newParameter.setDefaultValue( parameterValue );
			newParameter.setOptions( options );
			newParameter.setRequired( required );
			newParameter.setDescription( description );
			newParameter.setDisableUrlEncoding( disableURLEncoding );
			restRequest.getProperty( parameterBeingMoved ).setValue( parameterValue );
			sortedPropertyNames = copyOfSortedPropertyNames;
			firePropertyRemoved( parameterBeingMoved );
			firePropertyAdded( parameterBeingMoved );
		} finally
		{
   		parameterBeingMoved = null;
		}
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		listeners.add( listener );
	}

	public void clear()
	{
		for( String key : getPropertyNames() )
		{
			String oldValue = getPropertyValue( key );
			values.put( key, "" );
			firePropertyValueChanged( key, oldValue, "" );
		}
	}

	public boolean containsKey( Object key )
	{
		return methodParams.containsKey( key );
	}

	public boolean containsValue( Object value )
	{
		return values.containsValue( value ) || methodParams.containsValue( value );
	}

	public Set<Entry<String, TestProperty>> entrySet()
	{
		Set<Entry<String, TestProperty>> entrySet = methodParams.entrySet();
		for( Entry<String, TestProperty> entry : entrySet )
		{
			entry.setValue( getWrapper( ( RestParamProperty )entry.getValue() ) );
		}
		return entrySet;
	}

	public RestParamProperty get( Object key )
	{
		if( !methodParams.containsKey( key ) )
			return null;
		return getWrapper( methodParams.get( key ) );
	}

	public ModelItem getModelItem()
	{
		return this.restRequest;
	}

	public Map<String, TestProperty> getProperties()
	{
		Map<String, TestProperty> map = methodParams.getProperties();
		for( String key : map.keySet() )
		{
			map.put( key, getWrapper( ( RestParamProperty )map.get( key ) ) );
		}
		return map;
	}

	public String getPropertiesLabel()
	{
		return methodParams.getPropertiesLabel();
	}

	public RestParamProperty getProperty( String name )
	{
		if( !methodParams.hasProperty( name ) )
			return null;
		return getWrapper( methodParams.getProperty( name ) );
	}

	public RestParamProperty getPropertyAt( int index )
	{
		if( methodParams.getPropertyCount() <= index )
		{
			return null;
		}
		buildPropertyNameList();
		String propertyName = sortedPropertyNames.get( index );
		RestParamProperty propertyToWrap = methodParams.getProperty( propertyName );
		return getWrapper( propertyToWrap );
	}

	public int getPropertyCount()
	{
		return methodParams.getPropertyCount();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		return methodParams.getPropertyExpansions();
	}

	public int getPropertyIndex( String name )
	{
		return sortedPropertyNames.indexOf( name );
	}

	public String[] getPropertyNames()
	{
		return sortedPropertyNames.toArray( new String[sortedPropertyNames.size()] );
	}

	public String getPropertyValue( String name )
	{
		return values.containsKey( name ) ? values.get( name ) : methodParams.getPropertyValue( name );
	}

	public boolean hasProperty( String name )
	{
		return methodParams.hasProperty( name );
	}

	public boolean isEmpty()
	{
		return methodParams.isEmpty();
	}

	public Set<String> keySet()
	{
		return new LinkedHashSet<String>( sortedPropertyNames );
	}

	public void moveProperty( String propertyName, int targetIndex )
	{
		if( sortedPropertyNames.contains( propertyName ) )
		{
			int oldIndex = sortedPropertyNames.indexOf( propertyName );
			String valueAtNewindex = sortedPropertyNames.get( targetIndex );
			sortedPropertyNames.set( targetIndex, propertyName );
			sortedPropertyNames.set( oldIndex, valueAtNewindex );
			firePropertyMoved( propertyName, oldIndex, targetIndex );
		}
	}

	public TestProperty put( String key, TestProperty value )
	{
		if( value.getValue() != null )
			values.put( key, value.getValue() );
		else
			values.remove( key );
		return get( key );
	}

	public void putAll( Map<? extends String, ? extends TestProperty> m )
	{
		for( Entry<? extends String, ? extends TestProperty> e : m.entrySet() )
			put( e.getKey(), e.getValue() );
	}

	public TestProperty remove( Object key )
	{
		return removeProperty( ( String )key );
	}

	public RestParamProperty removeProperty( String propertyName )
	{
		values.remove( propertyName );

		RestParamProperty property = methodParams.removeProperty( propertyName );
		sortedPropertyNames.remove( propertyName );
		firePropertyRemoved( propertyName );
		return property;
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		listeners.remove( listener );
	}

	public boolean renameProperty( String name, String newName )
	{
		if( name.equals( newName ) )
		{
			return false;
		}
		RestParamProperty parameter = methodParams.getProperty( name );
		boolean renamePerformed;
		if( parameter.getParamLocation() == NewRestResourceActionBase.ParamLocation.METHOD )
		{
			renamePerformed = methodParams.renameProperty( name, newName );
		}
		else
		{
			renamePerformed = restRequest.getResource().renameProperty( name, newName );
		}
		if( renamePerformed )
		{
			buildPropertyNameList();
		}
		return renamePerformed;
	}

	private void renameLocalProperty( String name, String newName )
	{
		String value = values.get( name ) == null ? getPropertyValue( name ) : values.get( name );

		if( this.containsKey( name ) )
		{
			RestParamProperty restParamProperty = this.get( name );
			restParamProperty.setName( newName );
			this.put( newName, restParamProperty );
			this.remove( name );
		}

		values.put( newName, value );
		values.remove( name );
		firePropertyRenamed( name, newName );
	}

	public void resetValues()
	{
		values.clear();
	}

	public void release()
	{
		methodParams.removeTestPropertyListener( this );
		clearWrappers();
	}

	public void saveTo( Properties props )
	{
		int count = getPropertyCount();
		for( int i = 0; i < count; i++ )
		{
			RestParamProperty p = getPropertyAt( i );
			String name = p.getName();
			String value = values.containsKey( name ) ? values.get( name ) : p.getValue();
			if( value == null )
				value = "";

			props.setProperty( name, value );
		}
	}

	public void setPropertiesLabel( String propertiesLabel )
	{
		// methodParams.setPropertiesLabel(propertiesLabel);
	}

	public void setPropertyValue( String name, String value )
	{
		if( value == null )
			values.remove( name );
		else
			values.put( name, value );
	}

	public int size()
	{
		return methodParams.size();
	}

	public Collection<TestProperty> values()
	{
		List<TestProperty> ret = new ArrayList<TestProperty>();
		for( TestProperty p : methodParams.values() )
		{
			ret.add( getWrapper( ( RestParamProperty )p ) );
		}
		return ret;
	}

	private void firePropertyAdded( String name )
	{
		saveParameterOrder();
		TestPropertyListener[] listenersArray = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : listenersArray )
		{
			listener.propertyAdded( name );
		}
	}

	private void firePropertyRemoved( String name )
	{
		saveParameterOrder();
		TestPropertyListener[] listenersArray = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : listenersArray )
		{
			listener.propertyRemoved( name );
		}
	}

	private void firePropertyMoved( String name, int oldIndex, int newIndex )
	{
		saveParameterOrder();
		TestPropertyListener[] listenersArray = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : listenersArray )
		{
			listener.propertyMoved( name, oldIndex, newIndex );
		}
	}

	private void firePropertyRenamed( String oldName, String newName )
	{
		saveParameterOrder();
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

	private void saveParameterOrder()
	{
		StringListConfig mapConfig = StringListConfig.Factory.newInstance();
		mapConfig.setEntryArray( keySet().toArray( new String[keySet().size()] ) );
		restRequest.getConfig().setParameterOrder( mapConfig );
	}

	private RestParamProperty getWrapper( RestParamProperty key )
	{
		if( !wrappers.containsKey( key ) )
		{
			wrappers.put( key, new InternalRestParamProperty( key ) );
		}
		return wrappers.get( key );
	}

	public void propertyAdded( String name )
	{
		if ( isChangingLocationOfParameter( name ))
		{
			return;
		}
		if( !sortedPropertyNames.contains( name ) )
		{
			sortedPropertyNames.add( name );
		}
		firePropertyAdded( name );
	}

	public void propertyMoved( String name, int oldIndex, int newIndex )
	{
		firePropertyMoved( name, oldIndex, newIndex );
	}

	public void propertyRemoved( String name )
	{
		if ( isChangingLocationOfParameter( name ))
		{
			return;
		}
		sortedPropertyNames.remove( name );
		values.remove( name );
		firePropertyRemoved( name );
	}

	public void propertyRenamed( String oldName, String newName )
	{
		if( values.containsKey( oldName ) )
		{
			values.put( newName, values.get( oldName ) );
			values.remove( oldName );
		}
		if( sortedPropertyNames.contains( oldName ) )
		{
			sortedPropertyNames.set( sortedPropertyNames.indexOf( oldName ), newName );
			firePropertyRenamed( oldName, newName );
		}
	}

	public void propertyValueChanged( String name, String oldValue, String newValue )
	{
		if( !values.containsKey( name ) )
			firePropertyValueChanged( name, oldValue, newValue );
	}

	private boolean isChangingLocationOfParameter( String name )
	{
		return parameterBeingMoved != null && parameterBeingMoved.equals(name);
	}

	public class InternalRestParamProperty implements RestParamProperty, PropertyChangeListener
	{
		private RestParamProperty overriddenProp;
		private PropertyChangeSupport propertySupport;

		public InternalRestParamProperty( RestParamProperty override )
		{
			overriddenProp = override;
			overriddenProp.addPropertyChangeListener( this );
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

		public boolean isDisableUrlEncoding()
		{
			return overriddenProp.isDisableUrlEncoding();
		}

		public void removePropertyChangeListener( PropertyChangeListener listener )
		{
			propertySupport.removePropertyChangeListener( listener );
		}

		public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
		{
			propertySupport.removePropertyChangeListener( propertyName, listener );
		}

		public void setDisableUrlEncoding( boolean encode )
		{
			overriddenProp.setDisableUrlEncoding( encode );
		}

		public void setName( String name )
		{
			overriddenProp.setName( name );
		}

		public String getDefaultValue()
		{
			return overriddenProp.getDefaultValue();
		}

		public String getDescription()
		{
			return overriddenProp.getDescription();
		}

		public ModelItem getModelItem()
		{
			return restRequest;
		}

		public String getName()
		{
			return overriddenProp.getName();
		}

		public QName getType()
		{
			return overriddenProp.getType();
		}

		public String getValue()
		{
			if( values.containsKey( getName() ) && values.get( getName() ) != null )
				return values.get( getName() );
			return getDefaultValue();
		}

		public boolean isReadOnly()
		{
			return overriddenProp.isReadOnly();
		}

		public void setValue( String value )
		{
			String oldValue = getValue();
			if( getDefaultValue() != null && getDefaultValue().equals( value ) )
				value = null;

			if( value == null )
				values.remove( getName() );
			else
				values.put( getName(), value );
			firePropertyValueChanged( getName(), oldValue, getValue() );
		}

		public String[] getOptions()
		{
			return overriddenProp.getOptions();
		}

		public boolean getRequired()
		{
			return overriddenProp.getRequired();
		}

		public ParameterStyle getStyle()
		{
			return overriddenProp.getStyle();
		}

		public void setDefaultValue( String default1 )
		{
			//overriddenProp.setDefaultValue(default1);
		}

		public void setDescription( String description )
		{
			overriddenProp.setDescription( description );
		}

		public void setOptions( String[] arg0 )
		{
			overriddenProp.setOptions( arg0 );
		}

		public void setRequired( boolean arg0 )
		{
			overriddenProp.setRequired( arg0 );
		}

		public void setStyle( ParameterStyle style )
		{
			overriddenProp.setStyle( style );
		}

		@Override
		public NewRestResourceActionBase.ParamLocation getParamLocation()
		{
			return overriddenProp.getParamLocation();
		}

		@Override
		public void setParamLocation( NewRestResourceActionBase.ParamLocation paramLocation )
		{
			overriddenProp.setParamLocation( paramLocation );
		}

		public void setType( QName arg0 )
		{
			overriddenProp.setType( arg0 );
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			propertySupport.firePropertyChange( evt );
		}

		public void release()
		{
			overriddenProp.removePropertyChangeListener( this );
			overriddenProp = null;
			propertySupport = null;
		}

		@Override
		public String getPath()
		{
			return overriddenProp.getPath();
		}

		@Override
		public void setPath( String path )
		{
			overriddenProp.setPath( path );
		}

		@Override
		public boolean isRequestPart()
		{
			return false;
		}

		@Override
		public SchemaType getSchemaType()
		{
			return overriddenProp.getSchemaType();
		}

	}

	public List<TestProperty> getPropertyList()
	{
		List<TestProperty> propertyList = new ArrayList<TestProperty>();
		for( InternalRestParamProperty internalRestParamProperty : wrappers.values() )
		{
			propertyList.add( internalRestParamProperty );
		}
		return propertyList;
	}

}
