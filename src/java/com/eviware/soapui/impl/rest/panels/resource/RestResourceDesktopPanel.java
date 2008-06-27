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

package com.eviware.soapui.impl.rest.panels.resource;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class RestResourceDesktopPanel extends ModelItemDesktopPanel<RestResource>
{
	public RestResourceDesktopPanel(RestResource modelItem)
	{
		super(modelItem);
		
		add( buildToolbar(), BorderLayout.NORTH );
		add( buildContent(), BorderLayout.CENTER );
	}

	private Component buildContent()
	{
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Request Parameters", new JWadlParamsTable( getModelItem().getParams() ));
		return UISupport.createTabPanel(tabs, false );
	}

	@Override
	public String getTitle()
	{
		return getName( getModelItem() );
	}

	private String getName(RestResource modelItem)
	{
		if( modelItem.getParentResource() != null )
			return getName( modelItem.getParentResource() ) + "/" + modelItem.getName();
		else 
			return modelItem.getName();
	}

	private Component buildToolbar()
	{
		return new JXToolBar();
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
