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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

public class OverlayRestParamsPropertyHolder implements RestParamsPropertyHolder
{
	private RestParamsPropertyHolder parent;
	private RestParamsPropertyHolder overlay;
	private Set<TestPropertyListener> listeners = new HashSet<TestPropertyListener>();

	public OverlayRestParamsPropertyHolder( RestParamsPropertyHolder parent, RestParamsPropertyHolder overlay )
	{
		this.parent = parent;
		this.overlay = overlay;
		parent.addTestPropertyListener( new ParentListener() );
		overlay.addTestPropertyListener( new OverlayListener() );
	}

	public void addParameter( RestParamProperty prop )
	{
		overlay.addParameter( prop );
	}

	public RestParamProperty addProperty( String name )
	{
		return overlay.addProperty( name );
	}

	public void clear()
	{
		overlay.clear();
	}

	public boolean containsKey( Object key )
	{
		return overlay.containsKey( key ) || parent.containsKey( key );
	}

	public boolean containsValue( Object value )
	{
		return overlay.containsValue( value ) || parent.containsValue( value );
	}

	public Set<java.util.Map.Entry<String, TestProperty>> entrySet()
	{
		return getProperties().entrySet();
	}

	public RestParamProperty get( Object key )
	{
		return overlay.containsKey( key ) ? overlay.get( key ) : parent.get( key );
	}

	public ModelItem getModelItem()
	{
		return overlay.getModelItem();
	}

	public Map<String, TestProperty> getProperties()
	{
		HashMap<String, TestProperty> result = new HashMap<String, TestProperty>();

		for( TestProperty p : values() )
		{
			result.put( p.getName(), p );
		}

		return result;
	}

	public String getPropertiesLabel()
	{
		return overlay.getPropertiesLabel();
	}

	public RestParamProperty getProperty( String name )
	{
		return get( name );
	}

	public RestParamProperty getPropertyAt( int index )
	{
		return values().toArray( new RestParamProperty[] {} )[index];
	}

	public int getPropertyCount()
	{
		return values().size();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		return overlay.getPropertyExpansions();
	}

	public int getPropertyIndex( String name )
	{
		int index = 0;
		for( TestProperty prop : values() )
		{
			if( prop.getName().equals( name ) )
				return index;
			index++ ;
		}
		return -1;
	}

	public String[] getPropertyNames()
	{
		return keySet().toArray( new String[] {} );
	}

	public String getPropertyValue( String name )
	{
		return overlay.hasProperty( name ) ? overlay.getPropertyValue( name ) : parent.getPropertyValue( name );
	}

	public boolean hasProperty( String name )
	{
		return containsKey( name );
	}

	public boolean isEmpty()
	{
		return overlay.isEmpty() && parent.isEmpty();
	}

	public Set<String> keySet()
	{
		Set<String> names = new HashSet<String>();
		for( TestProperty prop : values() )
		{
			names.add( prop.getName() );
		}
		return names;
	}

	public void moveProperty( String propertyName, int targetIndex )
	{
		overlay.moveProperty( propertyName, targetIndex );
	}

	public TestProperty put( String key, TestProperty value )
	{
		return overlay.put( key, value );
	}

	public void putAll( Map<? extends String, ? extends TestProperty> m )
	{
		overlay.putAll( m );
	}

	public TestProperty remove( Object key )
	{
		return overlay.remove( key );
	}

	public RestParamProperty removeProperty( String propertyName )
	{
		return overlay.removeProperty( propertyName );
	}

	public boolean renameProperty( String name, String newName )
	{
		return overlay.renameProperty( name, newName );
	}

	public void resetValues()
	{
		overlay.resetValues();
	}

	public void saveTo( Properties props )
	{
		for( TestProperty prop : values() )
		{
			props.setProperty( prop.getName(), prop.getValue() != null ? prop.getValue() : "" );
		}
	}

	public void setPropertiesLabel( String propertiesLabel )
	{
		overlay.setPropertiesLabel( propertiesLabel );
	}

	public void setPropertyValue( String name, String value )
	{
		overlay.setPropertyValue( name, value );
	}

	public int size()
	{
		return getPropertyCount();
	}

	public Collection<TestProperty> values()
	{
		// List<TestProperty> values = new
		// ArrayList<TestProperty>(overlay.values());
		List<TestProperty> values = new ArrayList<TestProperty>();
		for( TestProperty prop : parent.values() )
		{
			if( overlay.hasProperty( prop.getName() ) )
				values.add( overlay.getProperty( prop.getName() ) );
			else
				values.add( prop );
		}
		for( TestProperty prop : overlay.values() )
		{
			if( !parent.hasProperty( prop.getName() ) )
				values.add( prop );
		}
		return values;
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

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		listeners.add( listener );
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		listeners.remove( listener );
	}

	private class ParentListener implements TestPropertyListener
	{

		public void propertyAdded( String name )
		{
			if( !overlay.hasProperty( name ) )
				firePropertyAdded( name );
		}

		public void propertyMoved( String name, int oldIndex, int newIndex )
		{
		}

		public void propertyRemoved( String name )
		{
			if( !overlay.hasProperty( name ) )
				firePropertyRemoved( name );
		}

		public void propertyRenamed( String oldName, String newName )
		{
			if( overlay.hasProperty( oldName ) )
			{
				if( !overlay.hasProperty( newName ) )
					firePropertyAdded( newName );
			}
			else if( overlay.hasProperty( newName ) )
			{
				firePropertyRemoved( oldName );
			}
		}

		public void propertyValueChanged( String name, String oldValue, String newValue )
		{
			if( !overlay.hasProperty( name ) )
				firePropertyValueChanged( name, oldValue, newValue );
		}
	}

	private class OverlayListener implements TestPropertyListener
	{

		public void propertyAdded( String name )
		{
			if( parent.hasProperty( name ) )
			{
				if( !parent.getPropertyValue( name ).equals( overlay.getPropertyValue( name ) ) )
					firePropertyValueChanged( name, parent.getPropertyValue( name ), overlay.getPropertyValue( name ) );
			}
			else
				firePropertyAdded( name );
		}

		public void propertyMoved( String name, int oldIndex, int newIndex )
		{
		}

		public void propertyRemoved( String name )
		{
			if( parent.hasProperty( name ) )
				firePropertyValueChanged( name, null, parent.getPropertyValue( name ) );
			else
				firePropertyRemoved( name );
		}

		public void propertyRenamed( String oldName, String newName )
		{
			if( !parent.hasProperty( oldName ) && !parent.hasProperty( newName ) )
				firePropertyRenamed( oldName, newName );
			else if( parent.hasProperty( oldName ) && parent.hasProperty( newName ) )
			{
				firePropertyValueChanged( oldName, overlay.getPropertyValue( newName ), parent.getPropertyValue( oldName ) );
				firePropertyValueChanged( newName, parent.getPropertyValue( newName ), overlay.getPropertyValue( newName ) );
			}
			else if( parent.hasProperty( oldName ) )
				firePropertyAdded( newName );
			else
				firePropertyRemoved( oldName );
		}

		public void propertyValueChanged( String name, String oldValue, String newValue )
		{
			firePropertyValueChanged( name, oldValue, newValue );
		}
	}

	public List<TestProperty> getPropertyList()
	{
		return overlay.getPropertyList();
	}

}
