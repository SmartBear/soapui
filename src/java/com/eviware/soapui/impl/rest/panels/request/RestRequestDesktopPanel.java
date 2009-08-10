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

package com.eviware.soapui.impl.rest.panels.request;

import javax.swing.JButton;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;

public class RestRequestDesktopPanel extends
		AbstractRestRequestDesktopPanel<RestRequestInterface, RestRequestInterface>
{
	private JButton addToTestCaseButton;

	public RestRequestDesktopPanel( RestRequestInterface modelItem )
	{
		super( modelItem, modelItem );
	}

	@Override
	protected void init( RestRequestInterface request )
	{
		addToTestCaseButton = createActionButton( SwingActionDelegate.createDelegate(
				AddRestRequestToTestCaseAction.SOAPUI_ACTION_ID, getRequest(), null, "/addToTestCase.gif" ), true );

		super.init( request );
	}

	protected String getHelpUrl()
	{
		return HelpUrls.RESTREQUESTEDITOR_HELP_URL;
	}

	public void setEnabled( boolean enabled )
	{
		super.setEnabled( enabled );
		addToTestCaseButton.setEnabled( enabled );
	}

	protected void insertButtons( JXToolBar toolbar )
	{
		toolbar.add( addToTestCaseButton );
	}
}
