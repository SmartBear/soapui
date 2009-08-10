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

package com.eviware.x.form.support;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.impl.swing.AbstractSwingXFormField;

/**
 * Swing-Specific multi-select list
 * 
 * @author ole.matzura
 */

public class XFormMultiSelectList extends AbstractSwingXFormField<JPanel> implements XFormOptionsField
{
	private JList list;
	private DefaultListModel listModel;
	private List<Boolean> selected = new ArrayList<Boolean>();

	public XFormMultiSelectList( String[] values )
	{
		super( new JPanel( new BorderLayout() ) );

		listModel = new DefaultListModel();
		if (values != null)
		{
			for (String value : values)
			{
				selected.add(false);
				listModel.addElement(value);
			}
		}
		list = new JList( listModel );
		list.setCellRenderer( new CheckListCellRenderer() );
		list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		list.addMouseListener( new MouseAdapter()
		{
			public void mousePressed( MouseEvent e )
			{
				int index = list.locationToIndex( e.getPoint() );

				if( index != -1 )
				{
					selected.set( index, !selected.get( index ) );
					list.repaint();
				}
			}
		} );

		getComponent().add( new JScrollPane( list ), BorderLayout.CENTER );
		getComponent().add( buildToolbar(), BorderLayout.SOUTH );
		getComponent().setSize( new Dimension( 400, 120 ) );
		getComponent().setMaximumSize( new Dimension( 400, 120 ) );
		getComponent().setPreferredSize( new Dimension( 400, 120 ) );
		getComponent().setMinimumSize( new Dimension( 400, 120 ) );
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		toolbar.addFixed( new JButton( new SelectAllAction() ) );
		toolbar.addRelatedGap();
		toolbar.addFixed( new JButton( new UnselectAllAction() ) );

		return toolbar;
	}

	public String getValue()
	{
		return String.valueOf( list.getSelectedValue());
	}

	public void setValue( String value )
	{
		int index = listModel.indexOf( value );
		selected.set( index, true );
		list.setSelectedIndex( index );
	}

	public void addItem( Object value )
	{
		listModel.addElement( value );
		selected.add( false );
	}

	public Object[] getOptions()
	{
		Object[] options = new Object[listModel.size()];
		for( int c = 0; c < options.length; c++ )
			options[c] = listModel.get( c );
		return options;
	}

	public Object[] getSelectedOptions()
	{
		List<Object> result = new ArrayList<Object>();

		for( int c = 0; c < selected.size(); c++ )
		{
			if( selected.get( c ) )
				result.add( listModel.get( c ) );
		}

		return result.toArray();
	}

	public void setOptions( Object[] values )
	{
		listModel.clear();
		selected.clear();
		for( Object value : values )
		{
			selected.add( false );
			listModel.addElement( value );
		}
	}

	public class CheckListCellRenderer extends JCheckBox implements ListCellRenderer
	{
		public CheckListCellRenderer()
		{
			setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		}

		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			setText( value.toString() );
			setSelected( selected.get( index ) );

			if( isSelected )
			{
				setBackground( list.getSelectionBackground() );
				setForeground( list.getSelectionForeground() );
			}
			else
			{
				setBackground( list.getBackground() );
				setForeground( list.getForeground() );
			}

			return this;
		}
	}

	public void setSelectedOptions( Object[] options )
	{
		List<Object> asList = Arrays.asList( options );

		for( int c = 0; c < selected.size(); c++ )
		{
			selected.set( c, asList.contains( listModel.get( c ) ) );
		}

		list.repaint();
	}

	private class SelectAllAction extends AbstractAction
	{
		public SelectAllAction()
		{
			super( "Select all" );
			putValue( SHORT_DESCRIPTION, "Selects all items in the list" );
		}

		public void actionPerformed( ActionEvent e )
		{
			setSelectedOptions( getOptions() );
		}
	}

	private class UnselectAllAction extends AbstractAction
	{
		public UnselectAllAction()
		{
			super( "Unselect all" );
			putValue( SHORT_DESCRIPTION, "Unselects all items in the list" );
		}

		public void actionPerformed( ActionEvent e )
		{
			setSelectedOptions( new String[0] );
		}
	}

	public int[] getSelectedIndexes()
	{
		int cnt = 0;

		for( int c = 0; c < selected.size(); c++ )
		{
			if( selected.get( c ) )
				cnt++ ;
		}

		int[] result = new int[cnt];
		cnt = 0;

		for( int c = 0; c < selected.size(); c++ )
		{
			if( selected.get( c ) )
				result[cnt++ ] = c;
		}

		return result;
	}
}
