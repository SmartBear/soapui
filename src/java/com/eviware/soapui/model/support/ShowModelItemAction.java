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

package com.eviware.soapui.model.support;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;

public class ShowModelItemAction extends AbstractAction
{
	private final ModelItem modelItem;

	public ShowModelItemAction( String title, ModelItem modelItem )
	{
		super( title );
		this.modelItem = modelItem;
	}

	public void actionPerformed( ActionEvent e )
	{
		UISupport.selectAndShow( modelItem );
	}
}