/*
 *  soapUI, copyright (C) 2004-2011 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.ui.support;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.eviware.soapui.model.ModelItem;

@SuppressWarnings( "serial" )
public abstract class KeySensitiveModelItemDesktopPanel<T extends ModelItem> extends ModelItemDesktopPanel<T> implements
		KeyListener
{
	public KeySensitiveModelItemDesktopPanel( T modelItem )
	{
		super( modelItem );

		this.addKeyListener( this );
	}

	@Override
	protected boolean release()
	{
		removeKeyListener( this );
		return super.release();
	}

	@Override
	public void keyPressed( KeyEvent e )
	{
		switch( e.getKeyCode() )
		{
		case KeyEvent.VK_F2 :
			renameModelItem();
			break;
		case KeyEvent.VK_F9 :
			cloneModelItem();
			break;
		}
		e.consume();
	}

	@Override
	public void keyReleased( KeyEvent e )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped( KeyEvent e )
	{
		// TODO Auto-generated method stub

	}

	protected abstract void renameModelItem();
	
	protected abstract void cloneModelItem();
	
	
}
