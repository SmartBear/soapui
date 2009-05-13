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

package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a WsdlRequest from its WsdlOperation
 * 
 * @author Ole.Matzura
 */

public class DeleteRestServiceAction extends AbstractSoapUIAction<RestService>
{
	public DeleteRestServiceAction()
	{
		super( "Delete", "Deletes this Service" );
	}

	public void perform( RestService service, Object param )
	{
		if( UISupport.confirm( "Delete Service [" + service.getName() + "]", "Delete Service" ) )
		{
			service.getProject().removeInterface( service );
		}
	}
}
