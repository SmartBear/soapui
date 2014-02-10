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

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;

public class AuthInspectorFactory implements RequestInspectorFactory
{
	public static final String INSPECTOR_ID = "Auth";

	public String getInspectorId()
	{
		return INSPECTOR_ID;
	}

	public EditorInspector<?> createRequestInspector( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof RestRequest )
		{
			return new OAuth2AuthenticationInspector( ( RestRequest )modelItem );
		}
		else if( modelItem instanceof WsdlRequest )
		{
			return new WSSAuthenticationInspector( ( WsdlRequest )modelItem );
		}
		else if( modelItem instanceof AbstractHttpRequestInterface<?> )
		{
			return new BasicAuthenticationInspector( ( ( AbstractHttpRequest<?> )modelItem ) );
		}
		return null;
	}
}
