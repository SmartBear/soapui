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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import com.eviware.x.form.XFormTextField;

public class JTableFormField extends AbstractSwingXFormField<JComponent> implements XFormTextField
{
	private JScrollPane scrollPane;

	public JTableFormField( String description )
	{
		super( new JTable() );

		scrollPane = new JScrollPane( getTable() );
		scrollPane.setPreferredSize( new Dimension( 350, 200 ) );
		getTable().setToolTipText( description );
		getTable().setHorizontalScrollEnabled( true );
	}

	public void setRequired( boolean required, String message )
	{
		super.setRequired( required, message );

		if( required )
			getComponent().setBorder(
					BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( Color.RED ), BorderFactory
							.createEmptyBorder( 2, 2, 2, 2 ) ) );
		else
			getComponent().setBorder(
					BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( Color.GRAY ), BorderFactory
							.createEmptyBorder( 2, 2, 2, 2 ) ) );
	}

	public JXTable getTable()
	{
		return ( JXTable )super.getComponent();
	}

	public JComponent getComponent()
	{
		return scrollPane;
	}

	@Override
	public void setProperty( String name, Object value )
	{
		if( "tableModel".equals( name ) )
		{
			getTable().setModel( ( TableModel )value );
		}
		else
			super.setProperty( name, value );
	}

	@Override
	public Object getProperty( String name )
	{
		if( "tableModel".equals( name ) )
		{
			return getTable().getModel();
		}
		else
			return super.getProperty( name );
	}

	public void setValue( String value )
	{

	}

	public String getValue()
	{
		return null;
	}

	public void setWidth( int columns )
	{
	}

	@Override
	public boolean isMultiRow()
	{
		return true;
	}
}
