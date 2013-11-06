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

package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.actions.iface.RemoveInterfaceAction;
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
		if( RemoveInterfaceAction.hasRunningDependingTests( service ) )
		{
			UISupport.showErrorMessage( "Cannot remove Service due to running depending tests" );
			return;
		}

		if( UISupport.confirm( "Delete Service [" + service.getName() + "] from Project?", "Delete Service" ) )
		{
			if( RemoveInterfaceAction.hasDependingTests( service ) )
			{
				if( !UISupport.confirm( "Service has depending TestSteps which will also be removed. Remove anyway?",
						"Remove Service" ) )
					return;
			}

			service.getProject().removeInterface( service );
		}
	}
}
