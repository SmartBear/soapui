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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;

/**
 * DesktopPanel for WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class WsdlMockResponseDesktopPanel extends AbstractWsdlMockResponseDesktopPanel<WsdlMockResponse>
{
	public WsdlMockResponseDesktopPanel( WsdlMockResponse mockResponse )
	{
		super( mockResponse );

		init( mockResponse );
	}
}
