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

package com.eviware.x.impl.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.eviware.soapui.support.components.JFormComponent;

public class JComponentFormField extends AbstractSwingXFormField<JPanel>
{
	private JComponent component;

	public JComponentFormField( String label, String description )
	{
		super( new JPanel( new BorderLayout() ) );
		getComponent().setPreferredSize( new Dimension( 400, 200 ) );
	}

	public void setValue( String value )
	{
		if( component instanceof JFormComponent )
			( ( JFormComponent )component ).setValue( value );
	}

	public String getValue()
	{
		if( component instanceof JFormComponent )
			return ( ( JFormComponent )component ).getValue();
		else
			return null;
	}

	@Override
	public void setProperty( String name, Object value )
	{
		if( name.equals( "component" ) )
		{
			getComponent().removeAll();
			if( value != null )
			{
				getComponent().add( ( JComponent )value, BorderLayout.CENTER );
			}

			getComponent().revalidate();
			getComponent().getParent().repaint();
		}
		else if( name.equals( "preferredSize" ) )
		{
			getComponent().setPreferredSize( ( Dimension )value );
			getComponent().setMaximumSize( ( Dimension )value );
			getComponent().setMinimumSize( ( Dimension )value );
			getComponent().setSize( ( Dimension )value );
		}
		else
		{
			super.setProperty( name, value );
		}
	}
}
