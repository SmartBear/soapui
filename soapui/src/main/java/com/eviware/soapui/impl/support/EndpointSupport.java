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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.model.iface.Request;

public class EndpointSupport
{
	public String getEndpoint( AbstractHttpRequest<AbstractRequestConfig> request )
	{
		return request.getConfig().getEndpoint();
	}

	public void setEndpoint( AbstractHttpRequest<AbstractRequestConfig> request, String endpoint )
	{
		String old = request.getEndpoint();
		if( old != null && old.equals( endpoint ) )
			return;

		request.getConfig().setEndpoint( endpoint );
		request.notifyPropertyChanged( Request.ENDPOINT_PROPERTY, old, endpoint );
	}

}
