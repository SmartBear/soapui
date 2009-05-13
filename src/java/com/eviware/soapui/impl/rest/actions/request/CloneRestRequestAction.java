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

package com.eviware.soapui.impl.rest.actions.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a WsdlRequest
 * 
 * @author Ole.Matzura
 */

public class CloneRestRequestAction extends AbstractSoapUIAction<RestRequest>
{
	public static final String SOAPUI_ACTION_ID = "CloneRestRequestAction";

	public CloneRestRequestAction()
	{
		super( "Clone Request", "Creates a copy of this Request" );
	}

	public void perform( RestRequest request, Object param )
	{
		String name = UISupport
				.prompt( "Specify name of cloned Request", "Clone Request", "Copy of " + request.getName() );
		if( name == null )
			return;

		RestRequest newRequest = request.getResource().cloneRequest( request, name );

		UISupport.selectAndShow( newRequest );
	}
}
