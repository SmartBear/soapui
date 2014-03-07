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

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.eviware.soapui.model.mock.MockOperation;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.support.PropertyChangeNotifier;

public abstract class AbstractMockOperationDispatcher implements PropertyChangeNotifier, MockOperationDispatcher
{
	private MockOperation mockOperation;
	private PropertyChangeSupport propertyChangeSupport;

	protected AbstractMockOperationDispatcher( MockOperation mockOperation )
	{
		this.mockOperation = mockOperation;
		propertyChangeSupport = new PropertyChangeSupport( this );
	}

	public JComponent getEditorComponent()
	{
		return new JPanel();
	}

	public void releaseEditorComponent()
	{
	}

	public void release()
	{
		mockOperation = null;
	}

	public MockOperation getMockOperation()
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
