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

package com.eviware.soapui.impl.wsdl.actions.mockservice;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a WsdlMockService
 * 
 * @author Ole.Matzura
 */

public class RestartMockServiceAction extends AbstractSoapUIAction<WsdlMockService>
{
	public final static String SOAPUI_ACTION_ID = "RestartMockServiceAction";

	public RestartMockServiceAction()
	{
		super( "Restart", "(Re)starts this MockService and opens its desktop window if required" );
	}

	public void perform( WsdlMockService mockService, Object param )
	{
		try
		{
			UISupport.setHourglassCursor();
			if( !SoapUI.getDesktop().hasDesktopPanel( mockService ))
				UISupport.showDesktopPanel( mockService );
			
			if( mockService.getMockRunner() != null )
				mockService.getMockRunner().stop();
			
			mockService.start();
		}
		catch( Exception e )
		{
			UISupport.showErrorMessage( e );
		}
		finally
		{
			UISupport.resetCursor();
		}
	}
}
