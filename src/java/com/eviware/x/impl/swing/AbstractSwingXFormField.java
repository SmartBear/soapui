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

package com.eviware.x.impl.swing;

import java.awt.Dimension;

import javax.swing.JComponent;

import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.AbstractXFormField;

public abstract class AbstractSwingXFormField<T extends JComponent> extends AbstractXFormField<T>
{
	private T component;

	public AbstractSwingXFormField( T component )
	{
		this.component = component;
	}

	public T getComponent()
	{
		return component;
	}

	public void setToolTip( String tooltip )
	{
		component.setToolTipText( tooltip );
		component.getAccessibleContext().setAccessibleDescription( tooltip );
	}

	public boolean isEnabled()
	{
		return component.isEnabled();
	}

	public void setEnabled( boolean enabled )
	{
		component.setEnabled( enabled );
	}

	public void setProperty( String name, Object value )
	{
		if( name.equals( "dimension" ) )
		{
			UISupport.setFixedSize( getComponent(), ( Dimension )value );
		}
	}
}
