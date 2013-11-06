package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequestInterface;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import static com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod;

/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 *
 */
public class RestRequestMethodModel extends DefaultComboBoxModel<RequestMethod> implements PropertyChangeListener
{
	private RestRequestInterface request;

	public RestRequestMethodModel( RestRequestInterface request )
	{
		super( RequestMethod.values() );
		this.request = request;
		request.addPropertyChangeListener( this );
	}

	@Override
	public void setSelectedItem( Object anItem )
	{
		super.setSelectedItem( anItem );
		request.setMethod( ( RequestMethod )anItem );
	}

	@Override
	public Object getSelectedItem()
	{
		return request.getMethod();
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{
		fireContentsChanged( this, -1, -1 );
	}
}
