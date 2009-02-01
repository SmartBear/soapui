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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JList;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;

public abstract class ModelItemListKeyListener extends KeyAdapter
{
	public void keyPressed(KeyEvent e)
	{
		int ix = ((JList)e.getSource()).getSelectedIndex();
		if (ix == -1)
			return;
		
		ModelItem modelItem = getModelItemAt( ix );
		ActionList actions = ActionListBuilder.buildActions( modelItem );
		if( actions != null )
			actions.dispatchKeyEvent( e );
	}
	
	public abstract ModelItem getModelItemAt( int ix );
}