/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.settings.WsaSettings;
import com.eviware.soapui.support.UISupport;

/**
 * Adds WS-A headers to the specified WsdlRequests requestContent
 * 
 * @author dragica.soldo
 */

public class AddWsaHeadersToRequestAction extends AbstractAction
{
	private final WsdlRequest request;

	public AddWsaHeadersToRequestAction(WsdlRequest request)
	{
		super("Add WS-A headers");
		this.request = request;
	}

	public void actionPerformed(ActionEvent e)
	{
		try
		{
			SoapVersion soapVersion = request.getOperation().getInterface().getSoapVersion();
			WsaUtils wsaUtils = new WsaUtils(soapVersion, request.getOperation());
			String content = request.getRequestContent();
			if (!wsaUtils.hasWsAddressing(content))
			{
				content = wsaUtils.addWSAddressingRequest(content, request);
			}
			else if (SoapUI.getSettings().getBoolean(WsaSettings.OVERRIDE_EXISTING_HEADERS))
			{
				content = wsaUtils.overrideExistingRequestHeaders(content, request);
			}
			request.setRequestContent(content);
		}
		catch (Exception e1)
		{
			UISupport.showErrorMessage(e1);
		}
		finally
		{
			UISupport.resetCursor();
		}
	}
}
