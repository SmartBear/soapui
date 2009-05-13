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

package com.eviware.soapui.impl.wsdl.actions.request;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a WsdlRequest
 * 
 * @author Ole.Matzura
 */

public class CloneRequestAction extends AbstractSoapUIAction<WsdlRequest>
{
	public static final String SOAPUI_ACTION_ID = "CloneRequestAction";

	public CloneRequestAction()
	{
		super( "Clone Request", "Creates a copy of this request" );
	}

	public void perform( WsdlRequest request, Object param )
	{
		String name = UISupport
				.prompt( "Specify name of cloned request", "Clone Request", "Copy of " + request.getName() );
		if( name == null )
			return;

		WsdlRequest newRequest = ( ( WsdlOperation )request.getOperation() ).addNewRequest( name );
		request.copyTo( newRequest, true, true );

		UISupport.selectAndShow( newRequest );
	}
}
