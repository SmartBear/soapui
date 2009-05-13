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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.Document;

import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.xml.JXEditTextArea;

/**
 * Panel for displaying line-numbers next to a JXEditTextArea
 * 
 * @author ole.matzura
 */

public class LineNumbersPanel extends JPanel
{
	private JXEditTextArea editArea;
	private JTable lineNumberTable;

	public LineNumbersPanel( JXEditTextArea editArea )
	{
		super( new BorderLayout() );
		this.editArea = editArea;

		lineNumberTable = new JTable( new LineNumberTableModel() );
		lineNumberTable.setBackground( StandaloneSoapUICore.SoapUITheme.BACKGROUND_COLOR );
		lineNumberTable.setRowHeight( editArea.getLineHeight() );
		lineNumberTable.getColumnModel().getColumn( 0 ).setWidth( 30 );
		lineNumberTable.setPreferredSize( new Dimension( 30, 0 ) );
		lineNumberTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 0, 0, 0, 1, Color.LIGHT_GRAY ),
				BorderFactory.createEmptyBorder( 2, 2, 0, 1 ) ) );
		add( lineNumberTable, BorderLayout.CENTER );

		lineNumberTable.getColumnModel().getColumn( 0 ).setCellRenderer( new LineNumberCellRenderer() );
		lineNumberTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				int row = lineNumberTable.getSelectedRow();
				if( row != -1 )
				{
					LineNumbersPanel.this.editArea
							.setCaretPosition( LineNumbersPanel.this.editArea.getLineStartOffset( row ) );
					LineNumbersPanel.this.editArea.requestFocusInWindow();
				}
			}
		} );
	}

	private class LineNumberCellRenderer extends DefaultTableCellRenderer
	{
		public LineNumberCellRenderer()
		{
			super();

			setForeground( Color.DARK_GRAY );
			setHorizontalAlignment( SwingConstants.RIGHT );
		}

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column )
		{
			setValue( value );
			return this;
		}
	}

	private class LineNumberTableModel extends AbstractTableModel
	{
		private int lastLineCount;

		public LineNumberTableModel()
		{
			editArea.getDocument().addDocumentListener( new DocumentListenerAdapter()
			{

				@Override
				public void update( Document document )
				{
					if( lastLineCount != editArea.getLineCount() )
						fireTableDataChanged();
				}
			} );
		}

		public int getColumnCount()
		{
			return 1;
		}

		public int getRowCount()
		{
			lastLineCount = editArea.getLineCount();
			return lastLineCount;
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			return String.valueOf( rowIndex + 1 );
		}
	}
}
