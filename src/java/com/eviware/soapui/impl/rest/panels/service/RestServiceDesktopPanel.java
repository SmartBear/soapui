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

package com.eviware.soapui.impl.rest.panels.service;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class RestServiceDesktopPanel extends ModelItemDesktopPanel<RestService>
{
	public RestServiceDesktopPanel(RestService modelItem)
	{
		super(modelItem);
	}

	@Override
	public boolean dependsOn(ModelItem modelItem)
	{
		return false;
	}

	public boolean onClose(boolean canCancel)
	{
		return true;
	}
}
