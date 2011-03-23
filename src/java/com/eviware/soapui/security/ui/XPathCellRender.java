/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

@SuppressWarnings( "serial" )
public class XPathCellRender extends AbstractCellEditor implements TableCellEditor, MouseListener
{

	protected JDialog dialog;
	JTextArea textArea;

	protected static final String EDIT = "edit";

	private JTextField textField;

	public XPathCellRender(Frame frame)
	{

		dialog = new JDialog(frame, true);
		dialog.setLayout( new BorderLayout() );
		dialog.setUndecorated( true );
		textArea = new JTextArea( 4, 5 );
		textArea.setWrapStyleWord( true );

		textArea.addKeyListener( new KeyAdapter()
		{
			public void keyPressed( KeyEvent evt )
			{
				switch( evt.getKeyCode() )
				{
				case KeyEvent.VK_ENTER :
					textField.setText( textArea.getText() );
					dialog.setVisible( false );
					break;
				case KeyEvent.VK_ESCAPE :
					dialog.setVisible( false );
					break;
				}
			}
		} );

		dialog.add( new JScrollPane( textArea ), BorderLayout.CENTER );
		dialog.setPreferredSize( new Dimension( 200, 100 ) );
		dialog.setMinimumSize( new Dimension( 200, 100 ) );

		textField = new JTextField();
		textField.addMouseListener( this );

	}

	@Override
	public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
	{
		String val = ( String )table.getModel().getValueAt( row, column );
		textField.setText( val );
		return textField;
	}

	@Override
	public Object getCellEditorValue()
	{
		return textField.getText();
	}

	@Override
	public void mouseClicked( MouseEvent e )
	{
		textArea.setText( textField.getText() );
		Point position = textField.getLocationOnScreen();
		dialog.setBounds( position.x, position.y, 0, 0 );
		dialog.setVisible( true );
	}

	@Override
	public void mouseEntered( MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited( MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed( MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased( MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

}
