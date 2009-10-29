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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.lang.ref.SoftReference;

import com.eviware.soapui.impl.wsdl.panels.teststeps.JdbcResponse;

public class JdbcTestStepResult extends WsdlTestStepResult
{
	private JdbcResponse response;
	private SoftReference<JdbcResponse> softResponse;
	private String requestContent;

	public JdbcTestStepResult( WsdlTestStep testStep )
	{
		super( testStep );
	}

	public void setResponse( JdbcResponse response, boolean useSoftReference )
	{
		if( useSoftReference )
			this.softResponse = new SoftReference<JdbcResponse>( response );
		else
			this.response = response;
	}

	public void setRequestContent( String requestContent )
	{
		this.requestContent = requestContent;
	}
}
