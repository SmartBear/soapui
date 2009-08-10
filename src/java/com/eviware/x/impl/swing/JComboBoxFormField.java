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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import com.eviware.x.form.XFormOptionsField;

public class JComboBoxFormField extends AbstractSwingXFormField<JComboBox> implements ItemListener, XFormOptionsField
{
	public JComboBoxFormField( Object[] values )
	{
		super( new JComboBox() );

		setOptions( values );

		getComponent().addItemListener( this );
	}

	public void setValue( String value )
	{
		getComponent().setSelectedItem( value );
	}

	public String getValue()
	{
		Object selectedItem = getComponent().getSelectedItem();
		return selectedItem == null ? null : selectedItem.toString();
	}

	public void itemStateChanged( ItemEvent e )
	{
		Object selectedItem = getComponent().getSelectedItem();
		fireValueChanged( selectedItem == null ? null : selectedItem.toString(), null );
	}

	public void addItem( Object value )
	{
		getComponent().addItem( value );
	}

	public void setOptions( Object[] values )
	{
		String selectedItem = getValue();
		DefaultComboBoxModel model = new DefaultComboBoxModel( values );

		if( values.length > 0 && values[0] == null )
		{
			model.removeElementAt( 0 );
			getComponent().setEditable( true );
		}
		else
		{
			getComponent().setEditable( false );
		}

		getComponent().setModel( model );

		if( selectedItem != null )
			getComponent().setSelectedItem( selectedItem );
		else if( getComponent().isEditable() )
			getComponent().setSelectedItem( "" );
	}

	public Object[] getOptions()
	{
		ComboBoxModel model = getComponent().getModel();

		Object[] result = new Object[model.getSize()];
		for( int c = 0; c < result.length; c++ )
			result[c] = model.getElementAt( c );

		return result;
	}

	public Object[] getSelectedOptions()
	{
		return new Object[] {getComponent().getSelectedItem()};
	}

	public void setSelectedOptions( Object[] options )
	{
		getComponent().setSelectedItem( options.length > 0 ? options[0] : null );
	}

	public int[] getSelectedIndexes()
	{
		return new int[] { getComponent().getSelectedIndex() };
	}
}
