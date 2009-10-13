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
package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;

/**
 * 
 * @author ole.matzura
 */
public class JdbcMessageExchange extends AbstractNonHttpMessageExchange<JdbcRequestTestStep>
{

	private JdbcRequestTestStep modelItem;
	public JdbcMessageExchange( JdbcRequestTestStep modelItem )
	{
		super( modelItem );
		this.modelItem = modelItem;
	}

	public String getRequestContent()
	{
		return null;
	}

	public String getResponseContent()
	{
		return modelItem.getXmlStringResult();
	}

	public long getTimeTaken()
	{
		return 0;
	}

	public long getTimestamp()
	{
		return 0;
	}

	public boolean hasRequest( boolean ignoreEmpty )
	{
		return false;
	}

	public boolean hasResponse()
	{
		return getResponseContent()!= null ;
	}

	public boolean isDiscarded()
	{
		return false;
	}
}
