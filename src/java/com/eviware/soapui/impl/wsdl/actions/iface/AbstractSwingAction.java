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

package com.eviware.soapui.impl.wsdl.actions.iface;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.support.UISupport;

/**
 * Utility class for creating Swing Actions for ModelItems
 * 
 * @author ole.matzura
 */

public abstract class AbstractSwingAction<T extends Object> extends AbstractAction
{
	private T modelItem;
	private final String name;

	public AbstractSwingAction( String name, String description )
	{
		super( name );
		this.name = name;
		this.modelItem = null;

		putValue( Action.SHORT_DESCRIPTION, description );
	}

	public AbstractSwingAction( String name, String description, T modelItem )
	{
		super( name );
		this.name = name;
		this.modelItem = modelItem;

		putValue( Action.SHORT_DESCRIPTION, description );
	}

	public AbstractSwingAction( String name, String description, String iconUrl )
	{
		super( name );
		this.name = name;
		this.modelItem = null;

		putValue( Action.SHORT_DESCRIPTION, description );
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( iconUrl ) );
	}

	public AbstractSwingAction( String name, String description, String iconUrl, T modelItem )
	{
		super( name );
		this.name = name;
		this.modelItem = modelItem;

		putValue( Action.SHORT_DESCRIPTION, description );
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( iconUrl ) );
	}

	public void actionPerformed( ActionEvent arg0 )
	{
		actionPerformed( arg0, modelItem );
	}

	public String getName()
	{
		return name;
	}

	public abstract void actionPerformed( ActionEvent arg0, T modelItem2 );

	public T getModelItem()
	{
		return modelItem;
	}
}
