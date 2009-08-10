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

package com.eviware.soapui.impl.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.support.UISupport;

/**
 * ComboBox model for a request endpoint
 * 
 * @author Ole.Matzura
 */

public class EndpointsComboBoxModel implements ComboBoxModel, PropertyChangeListener
{
	private static final String ADD_NEW_ENDPOINT = "[add new endpoint..]";
	private static final String EDIT_ENDPOINT = "[edit current..]";
	private static final String DELETE_ENDPOINT = "[delete current]";

	private Set<ListDataListener> listeners = new HashSet<ListDataListener>();
	private String[] endpoints;
	private AbstractHttpRequestInterface<?> request;

	public EndpointsComboBoxModel( AbstractHttpRequestInterface<?> request )
	{
		this.request = request;
		initEndpoints();
		request.addPropertyChangeListener( this );
		if( request.getOperation() != null )
			request.getOperation().getInterface().addPropertyChangeListener( this );
	}

	public void setSelectedItem( Object anItem )
	{
		final String endpoint = request.getEndpoint();
		if( anItem != null && anItem.equals( ADD_NEW_ENDPOINT ) )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					String value = UISupport.prompt( "Add new endpoint for interface ["
							+ request.getOperation().getInterface().getName() + "]", "Add new endpoint", endpoint );

					if( value != null )
					{
						if( request.getOperation() != null )
							request.getOperation().getInterface().addEndpoint( value );
						request.setEndpoint( value );
					}

				}
			} );

		}
		else if( anItem != null && anItem.equals( EDIT_ENDPOINT ) )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					String value = UISupport.prompt( "Edit endpoint for interface ["
							+ request.getOperation().getInterface().getName() + "]", "Edit endpoint", endpoint );

					if( value != null )
					{
						if( request.getOperation() != null )
							request.getOperation().getInterface().changeEndpoint( endpoint, value );
						request.setEndpoint( value );
					}
				}
			} );
		}
		else if( anItem != null && anItem.equals( DELETE_ENDPOINT ) )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					if( UISupport.confirm( "Delete endpoint [" + endpoint + "]", "Delete endpoint" ) )
					{
						if( request.getOperation() != null )
							request.getOperation().getInterface().removeEndpoint( endpoint );
						request.setEndpoint( null );
					}
				}
			} );
		}
		else
		{
			request.setEndpoint( ( String )anItem );
		}

		notifyContentsChanged();
	}

	public void refresh()
	{
		initEndpoints();
		notifyContentsChanged();
	}

	private void initEndpoints()
	{
		if( request.getOperation() != null )
			endpoints = request.getOperation().getInterface().getEndpoints();
		else
			endpoints = new String[0];
	}

	private void notifyContentsChanged()
	{
		Iterator<ListDataListener> iterator = listeners.iterator();
		ListDataEvent e = new ListDataEvent( this, ListDataEvent.CONTENTS_CHANGED, 0, getSize() );
		while( iterator.hasNext() )
		{
			iterator.next().contentsChanged( e );
		}
	}

	public Object getSelectedItem()
	{
		String endpoint = request.getEndpoint();
		return endpoint == null ? "- no endpoint set -" : endpoint;
	}

	public int getSize()
	{
		return endpoints.length + 3;
	}

	public Object getElementAt( int index )
	{
		if( index == endpoints.length )
			return EndpointsComboBoxModel.EDIT_ENDPOINT;
		else if( index == endpoints.length + 1 )
			return EndpointsComboBoxModel.ADD_NEW_ENDPOINT;
		else if( index == endpoints.length + 2 )
			return EndpointsComboBoxModel.DELETE_ENDPOINT;
		else
			return endpoints[index];
	}

	public void addListDataListener( ListDataListener l )
	{
		listeners.add( l );
	}

	public void removeListDataListener( ListDataListener l )
	{
		listeners.remove( l );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		String propertyName = evt.getPropertyName();

		if( propertyName.equals( AbstractHttpRequest.ENDPOINT_PROPERTY ) )
		{
			notifyContentsChanged();
		}
		else if( propertyName.equals( WsdlInterface.ENDPOINT_PROPERTY ) )
		{
			refresh();
		}
	}

	public void release()
	{
		request.removePropertyChangeListener( this );
		if( request.getOperation() != null )
			request.getOperation().getInterface().removePropertyChangeListener( this );
	}
}