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

package com.eviware.soapui.support.swing;

import javax.swing.JList;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;

/**
 * ListMouseListener for ModelItems
 * 
 * @author ole.matzura
 */

public class ModelItemListMouseListener extends AbstractListMouseListener
{
	public ModelItemListMouseListener()
	{
		this( true );
	}

	public ModelItemListMouseListener( boolean enablePopup )
	{
		super( enablePopup );
	}

	@Override
	protected ActionList getActionsForRow( JList list, int row )
	{
		ModelItem item = ( ModelItem )list.getModel().getElementAt( row );
		try
		{
			return item == null ? null : ActionListBuilder.buildActions( item );
		}
		catch( Exception e )
		{
			return null;
		}
	}
}