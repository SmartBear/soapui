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

package com.eviware.soapui.impl.wsdl.panels.request;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestAsMockResponseStepAction;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToTestCaseAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;

/**
 * DesktopPanel for standard WsdlRequests
 * 
 * @author ole.matzura
 */

public class WsdlRequestDesktopPanel extends AbstractWsdlRequestDesktopPanel<WsdlRequest, WsdlRequest>
{
	private JButton addToTestCaseButton;
	private JButton addAsMockResponseStepToTestCaseButton;

	public WsdlRequestDesktopPanel( WsdlRequest request )
	{
		super( request, request );
	}

	@Override
	protected void init( WsdlRequest request )
	{
		addToTestCaseButton = createActionButton( SwingActionDelegate.createDelegate(
				AddRequestToTestCaseAction.SOAPUI_ACTION_ID, getRequest(), null, "/addToTestCase.gif" ), true );

		super.init( request );
	}

	protected String getHelpUrl()
	{
		return HelpUrls.REQUESTEDITOR_HELP_URL;
	}

	public void setEnabled( boolean enabled )
	{
		super.setEnabled( enabled );
		addToTestCaseButton.setEnabled( enabled );
		addAsMockResponseStepToTestCaseButton.setEnabled( enabled );
	}

	protected void insertButtons( JXToolBar toolbar )
	{
		toolbar.add( addToTestCaseButton );

		super.insertButtons( toolbar );

		AbstractAction delegate = SwingActionDelegate.createDelegate(
				AddRequestAsMockResponseStepAction.SOAPUI_ACTION_ID, getRequest(), null, "/addAsMockResponseStep.gif" );
		addAsMockResponseStepToTestCaseButton = createActionButton( delegate, true );

		toolbar.add( addAsMockResponseStepToTestCaseButton );
	}
}
