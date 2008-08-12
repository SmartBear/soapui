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

package com.eviware.soapui.impl.wsdl.panels.request;

import javax.swing.JButton;
import javax.swing.JToolBar;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToTestCaseAction;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;

/**
 * DesktopPanel for standard WsdlRequests
 * 
 * @author ole.matzura
 */

public class WsdlRequestDesktopPanel extends AbstractWsdlRequestDesktopPanel<WsdlRequest,WsdlRequest>
{
	private JButton addToTestCaseButton;
	
	public WsdlRequestDesktopPanel(WsdlRequest request)
	{
		super(request, request);
	}
	
	@Override
	protected void init(WsdlRequest request)
	{
		addToTestCaseButton = createActionButton(
				SwingActionDelegate.createDelegate( AddRequestToTestCaseAction.SOAPUI_ACTION_ID, getRequest(), 
							null, "/addToTestCase.gif"), true );
		
		super.init(request);
	}

	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		addToTestCaseButton.setEnabled(enabled);
	}

	protected void insertButtons(JToolBar toolbar)
	{
		toolbar.add(addToTestCaseButton);
		super.insertButtons(toolbar);
	}
}
