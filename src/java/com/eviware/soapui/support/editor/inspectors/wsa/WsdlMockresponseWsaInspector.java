/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.editor.inspectors.wsa;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.support.editor.xml.XmlInspector;

public class WsdlMockresponseWsaInspector extends AbstractWsaInspector implements XmlInspector, PropertyChangeListener
{
	private final WsdlMockResponse response;

	public WsdlMockresponseWsaInspector( WsdlMockResponse response )
	{
		super( response );
		this.response = response;
	}

	public void propertyChange(PropertyChangeEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

}