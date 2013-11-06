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

package com.eviware.soapui.model.support;

import org.apache.commons.beanutils.PropertyUtils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.TestProperty;

public class XPathReferenceImpl implements XPathReference
{
	private String label;
	private Object target;
	private String xpathPropertyName;
	private String xpath;
	private Operation operation;
	private boolean request;

	public XPathReferenceImpl( String label, Operation operation, boolean request, Object target,
			String xpathPropertyName )
	{
		this.label = label;
		this.operation = operation;
		this.request = request;
		this.target = target;
		this.xpathPropertyName = xpathPropertyName;

		try
		{
			this.xpath = ( String )PropertyUtils.getProperty( target, xpathPropertyName );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public XPathReferenceImpl( String label, TestProperty property, Object target, String xpathPropertyName )
	{
		this.label = label;
		this.target = target;
		this.xpathPropertyName = xpathPropertyName;

		ModelItem modelItem = property == null ? null : property.getModelItem();

		if( modelItem instanceof WsdlTestRequestStep )
		{
			operation = ( ( WsdlTestRequestStep )modelItem ).getTestRequest().getOperation();
			request = property.getName().equalsIgnoreCase( "Request" );
		}
		else if( modelItem instanceof WsdlMockResponseTestStep )
		{
			operation = ( ( WsdlMockResponseTestStep )modelItem ).getOperation();
			request = property.getName().equalsIgnoreCase( "Request" );
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			operation = ( ( WsdlMockResponse )modelItem ).getMockOperation().getOperation();
			request = property.getName().equalsIgnoreCase( "Request" );
		}
		else if( modelItem instanceof WsdlMockOperation )
		{
			operation = ( ( WsdlMockOperation )modelItem ).getOperation();
			request = property.getName().equalsIgnoreCase( "Request" );
		}
		try
		{
			this.xpath = ( String )PropertyUtils.getProperty( target, xpathPropertyName );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public String getLabel()
	{
		return label;
	}

	public String getXPath()
	{
		return xpath;
	}

	public void setXPath( String xpath )
	{
		this.xpath = xpath;
	}

	public void update()
	{
		try
		{
			PropertyUtils.setProperty( target, xpathPropertyName, xpath );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public Operation getOperation()
	{
		return operation;
	}

	public boolean isRequest()
	{
		return request;
	}

}
