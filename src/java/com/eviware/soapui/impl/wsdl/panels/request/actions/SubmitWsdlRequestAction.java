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

package com.eviware.soapui.impl.wsdl.panels.request.actions;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class SubmitWsdlRequestAction extends AbstractSoapUIAction<WsdlRequest>
{
	public SubmitWsdlRequestAction()
	{
		super( "Submit", "Submits Request to configured endpoint" );
	}

	public void perform( WsdlRequest target, Object param )
	{

	}
}
