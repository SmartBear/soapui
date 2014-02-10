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

package com.eviware.x.form.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.eviware.soapui.support.types.StringList;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.impl.swing.AbstractSwingXFormField;

/**
 * Swing-specific RadioGroup
 * 
 * @author ole.matzura
 */

public class XFormRadioGroup extends AbstractSwingXFormField<JPanel> implements XFormOptionsField
{
	protected ButtonGroup buttonGroup;
	protected Map<String, ButtonModel> models = new HashMap<String, ButtonModel>();
	protected StringList items = new StringList();

	public XFormRadioGroup( String[] values )
	{
		super( new JPanel() );

		buttonGroup = new ButtonGroup();
		getComponent().setLayout( new BoxLayout( getComponent(), BoxLayout.Y_AXIS ) );

		for( String value : values )
		{
			addItem( value );
		}
	}

	public String getValue()
	{
		ButtonModel selection = buttonGroup.getSelection();
		return selection == null ? null : selection.getActionCommand();
	}

	public void setValue( String value )
	{
		buttonGroup.setSelected( models.get( value ), true );
	}

	public void addItem( Object value )
	{
		JRadioButton button = new JRadioButton( String.valueOf( value ) );

		button.setActionCommand( String.valueOf( value ) );
		button.setName( String.valueOf( value ) );
		button.setFocusPainted( false );
		button.addActionListener( new ActionListener()
		{

			public void actionPerformed( ActionEvent e )
			{
				fireValueChanged( e.getActionCommand(), null );
			}
		} );

		getComponent().add( button );
		buttonGroup.add( button );
		models.put( String.valueOf( value ), button.getModel() );
		items.add( String.valueOf( value ) );
	}

	public Object[] getOptions()
	{
		return items.toStringArray();
	}

	public Object[] getSelectedOptions()
	{
		return new String[] { getValue() };
	}

	public void setOptions( Object[] values )
	{
		while( buttonGroup.getButtonCount() > 0 )
			buttonGroup.remove( buttonGroup.getElements().nextElement() );

		models.clear();
		items.clear();
		getComponent().removeAll();

		for( Object value : values )
		{
			addItem( value.toString() );
		}
	}

	public void setSelectedOptions( Object[] options )
	{

	}

	public int[] getSelectedIndexes()
	{
		return new int[] { items.indexOf( getValue() ) };
	}

	public void setDisabled()
	{
		for( Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements(); )
		{
			buttons.nextElement().setEnabled( false );
		}
	}
}
