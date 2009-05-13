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

package com.eviware.soapui.support;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public abstract class ListDataChangeListener implements ListDataListener
{
	public void contentsChanged( ListDataEvent e )
	{
		dataChanged( ( ListModel )e.getSource() );
	}

	public void intervalAdded( ListDataEvent e )
	{
		dataChanged( ( ListModel )e.getSource() );
	}

	public void intervalRemoved( ListDataEvent e )
	{
		dataChanged( ( ListModel )e.getSource() );
	}

	public abstract void dataChanged( ListModel model );
}
