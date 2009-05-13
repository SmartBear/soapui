/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.support.PropertyChangeNotifier;

public abstract class AbstractMockOperationDispatcher implements PropertyChangeNotifier, MockOperationDispatcher
{
	private WsdlMockOperation mockOperation;
	private PropertyChangeSupport propertyChangeSupport;

	protected AbstractMockOperationDispatcher( WsdlMockOperation mockOperation )
	{
		this.mockOperation = mockOperation;
		propertyChangeSupport = new PropertyChangeSupport( this );
	}

	public JComponent buildEditorComponent()
	{
		return new JPanel();
	}

	public void release()
	{
		mockOperation = null;
	}

	public XmlObject getConfig()
	{
		return mockOperation.getConfig().getDispatchConfig();
	}

	protected void saveConfig( XmlObject xmlObject )
	{
		mockOperation.getConfig().getDispatchConfig().set( xmlObject );
	}

	public WsdlMockOperation getMockOperation()
	{
		return mockOperation;
	}

	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
	}

	protected PropertyChangeSupport getPropertyChangeSupport()
	{
		return propertyChangeSupport;
	}
}
