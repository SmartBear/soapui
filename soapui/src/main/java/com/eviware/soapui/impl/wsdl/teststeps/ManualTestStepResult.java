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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.support.types.StringList;

public class ManualTestStepResult extends WsdlTestStepResult
{
	private StringList urls = new StringList();
	private String result;

	public ManualTestStepResult( ManualTestStep testStep )
	{
		super( testStep );
	}

	protected StringList getUrls()
	{
		return urls;
	}

	protected void setUrls( Object[] urls )
	{
		this.urls.clear();
		for( Object o : urls )
		{
			this.urls.add( String.valueOf( o ) );
			addMessage( "URL: " + String.valueOf( o ) );
		}
	}

	protected String getResult()
	{
		return result;
	}

	protected void setResult( String result )
	{
		this.result = result;
		super.addMessage( result );
	}
}
