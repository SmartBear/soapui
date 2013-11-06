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

package com.eviware.soapui.impl.rest.actions.request;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a WsdlRequest from its WsdlOperation
 * 
 * @author Ole.Matzura
 */

public class DeleteRestRequestAction extends AbstractSoapUIAction<RestRequest>
{
	public DeleteRestRequestAction()
	{
		super( "Delete", "Deletes this Request" );
	}

	public void perform( RestRequest request, Object param )
	{
		if( UISupport.confirm( "Delete Request [" + request.getName() + "] from Resource ["
				+ request.getOperation().getName() + "]", "Delete Request" ) )
		{
			RestMethod method = request.getRestMethod();
			method.removeRequest( request );
		}
	}
}
