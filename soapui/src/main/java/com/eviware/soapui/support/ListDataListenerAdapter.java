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

package com.eviware.soapui.support;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public abstract class ListDataListenerAdapter implements ListDataListener
{
	public void contentsChanged( ListDataEvent e )
	{
		update( e );
	}

	public void intervalAdded( ListDataEvent e )
	{
		update( e );
	}

	public void intervalRemoved( ListDataEvent e )
	{
		update( e );
	}

	protected void update( ListDataEvent e )
	{
	}
}
