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

package com.eviware.soapui.impl.wsdl.actions.support;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action for showing the desktop panel for the specified ModelItem
 * 
 * @author Ole.Matzura
 */

public class ShowDesktopPanelAction extends AbstractSoapUIAction<ModelItem>
{
	public static final String SOAPUI_ACTION_ID = "ShowDesktopPanelAction";

	public ShowDesktopPanelAction()
	{
		super( "Show Desktop Panel", "Show Desktop Panel for this item" );
	}

	public void perform( ModelItem target, Object param )
	{
		UISupport.setHourglassCursor();
		try
		{
			if( target instanceof WsdlInterface )
			{
				try
				{
					( ( WsdlInterface )target ).getWsdlContext().loadIfNecessary();
				}
				catch( Exception e )
				{
					UISupport.showErrorMessage( e );
					return;
				}
			}

			UISupport.selectAndShow( target );
		}
		finally
		{
			UISupport.resetCursor();
		}
	}
}