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

package com.eviware.soapui.support.swing;

import javax.swing.JTable;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;

/**
 * ListMouseListener for ModelItems
 * 
 * @author ole.matzura
 */

public abstract class AbstractModelItemTableMouseListener extends AbstractTableMouseListener
{
	public AbstractModelItemTableMouseListener()
	{
		this( true );
	}

	public AbstractModelItemTableMouseListener( boolean enablePopup )
	{
		super( enablePopup );
	}

	@Override
	protected ActionList getActionsForRow( JTable table, int row )
	{
		ModelItem item = ( ModelItem )getModelItemAt( row );
		try
		{
			return item == null ? null : ActionListBuilder.buildActions( item );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	abstract protected ModelItem getModelItemAt( int row );
}
