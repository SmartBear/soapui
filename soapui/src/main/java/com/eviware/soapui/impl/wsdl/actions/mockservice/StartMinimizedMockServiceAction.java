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

package com.eviware.soapui.impl.wsdl.actions.mockservice;

import javax.swing.SwingUtilities;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * Clones a WsdlMockService
 * 
 * @author Ole.Matzura
 */

public class StartMinimizedMockServiceAction<MockServiceType extends AbstractMockService>
		extends AbstractSoapUIAction<MockServiceType>
{
	public final static String SOAPUI_ACTION_ID = "StartMinimizedMockServiceAction";

	public StartMinimizedMockServiceAction()
	{
		super( "Start Minimized", "Starts this MockService and minimizes its desktop window" );
	}

	public void perform( MockServiceType mockService, Object param )
	{
		try
		{
			UISupport.setHourglassCursor();
			final DesktopPanel desktopPanel = UISupport.showDesktopPanel( mockService );
			if( mockService.getMockRunner() == null )
				mockService.start();

			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					SoapUI.getDesktop().minimize( desktopPanel );
				}
			} );
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
