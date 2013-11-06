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

import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

public class XFormRadioGroupTopButtonPosition extends XFormRadioGroup
{

	public XFormRadioGroupTopButtonPosition( String[] values )
	{
		super( values );
	}

	public void addItem( Object value )
	{
		JRadioButton button = new JRadioButton( String.valueOf( value ) );
		button.setVerticalTextPosition( SwingConstants.TOP );

		button.setActionCommand( String.valueOf( value ) );
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

}
