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

package com.eviware.soapui.support.editor.inspectors.httpheaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.types.StringToStringMap;

public interface HttpHeadersInspectorModel
{
	public StringToStringMap getHeaders();

	public void addPropertyChangeListener( PropertyChangeListener listener );

	public void setHeaders( StringToStringMap headers );

	public void removePropertyChangeListener( PropertyChangeListener listener );

	public boolean isReadOnly();

	public void release();

	public static abstract class AbstractHeadersModel<T extends ModelItem> implements HttpHeadersInspectorModel,
			PropertyChangeListener
	{
		private boolean readOnly;
		private PropertyChangeSupport propertyChangeSupport;
		private final T modelItem;
		private final String propertyName;

		protected AbstractHeadersModel( boolean readOnly, T modelItem, String propertyName )
		{
			this.readOnly = readOnly;
			this.modelItem = modelItem;
			this.propertyName = propertyName;
			propertyChangeSupport = new PropertyChangeSupport( this );
			modelItem.addPropertyChangeListener( propertyName, this );
		}

		public void addPropertyChangeListener( PropertyChangeListener listener )
		{
			propertyChangeSupport.addPropertyChangeListener( listener );
		}

		public boolean isReadOnly()
		{
			return readOnly;
		}

		public void removePropertyChangeListener( PropertyChangeListener listener )
		{
			propertyChangeSupport.removePropertyChangeListener( listener );
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			propertyChangeSupport.firePropertyChange( evt );
		}

		public void release()
		{
			modelItem.removePropertyChangeListener( propertyName, this );
		}

		public T getModelItem()
		{
			return modelItem;
		}

		public void setHeaders( StringToStringMap headers )
		{
			if( !readOnly )
				throw new NotImplementedException();
		}
	}
}
