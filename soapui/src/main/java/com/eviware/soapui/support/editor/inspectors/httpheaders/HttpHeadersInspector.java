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

package com.eviware.soapui.support.editor.inspectors.httpheaders;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.request.StringToStringsMapTableModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.swing.JTableFactory;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class HttpHeadersInspector extends AbstractXmlInspector implements PropertyChangeListener
{
	private StringToStringsMapTableModel headersTableModel;
	private final HttpHeadersInspectorModel model;
	private JTable headersTable;
	private JPanel panel;
	private JButton removeButton;
	public boolean changing;

	protected HttpHeadersInspector( HttpHeadersInspectorModel model )
	{
		super( "Headers (" + ( model.getHeaders() == null ? "0" : model.getHeaders().valueCount() ) + ")",
				"Additional HTTP Headers for this message", true, HttpHeadersInspectorFactory.INSPECTOR_ID );
		this.model = model;
		model.setInspector( this );

		model.addPropertyChangeListener( this );
	}

	public JComponent getComponent()
	{
		if( panel != null )
			return panel;

		headersTableModel = new StringToStringsMapTableModel( model.getHeaders(), "Header", "Value", !model.isReadOnly() );
		headersTableModel.addTableModelListener( new TableModelListener()
		{
			public void tableChanged( TableModelEvent arg0 )
			{
				model.setHeaders( headersTableModel.getData() );
				setTitle( "Headers (" + ( model.getHeaders() == null ? "0" : model.getHeaders().valueCount() ) + ")" );
			}
		} );

		headersTable = JTableFactory.getInstance().makeJTable( headersTableModel );

		panel = new JPanel( new BorderLayout() );
		panel.add( new JScrollPane( headersTable ), BorderLayout.CENTER );

		if( !model.isReadOnly() )
		{
			headersTable.setSurrendersFocusOnKeystroke( true );
			headersTable.putClientProperty( "terminateEditOnFocusLost", Boolean.TRUE );

			JXToolBar builder = UISupport.createSmallToolbar();
			builder.addFixed( UISupport.createToolbarButton( new AddAction() ) );
			removeButton = UISupport.createToolbarButton( new RemoveAction() );
			builder.addFixed( removeButton );
			builder.addGlue();
			builder.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.HEADERS_HELP_URL ) ) );

			panel.add( builder, BorderLayout.NORTH );

			headersTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
			{

				public void valueChanged( ListSelectionEvent e )
				{
					removeButton.setEnabled( headersTable.getSelectedRow() != -1 );
				}
			} );

			if( headersTable.getRowCount() > 0 )
				headersTable.setRowSelectionInterval( 0, 0 );
			else
				removeButton.setEnabled( false );
		}

		return panel;
	}

	public JTable getHeadersTable()
	{
		return headersTable;
	}

	@Override
	public void release()
	{
		super.release();
		model.release();
		model.removePropertyChangeListener( this );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( !changing )
			headersTableModel.setData( model.getHeaders() );
	}

	private final class RemoveAction extends AbstractAction
	{
		private RemoveAction()
		{
			super();
			putValue( AbstractAction.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( AbstractAction.SHORT_DESCRIPTION, "Removes the selected custom HTTP Header from this message" );
		}

		public void actionPerformed( ActionEvent arg0 )
		{
			int row = headersTable.getSelectedRow();
			if( row != -1 && UISupport.confirm( "Delete selected header?", "Remove Header" ) )
			{
				changing = true;
				headersTableModel.remove( row );
				changing = false;
			}
		}
	}

	private final class AddAction extends AbstractAction
	{
		private AddAction()
		{
			super();
			putValue( AbstractAction.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( AbstractAction.SHORT_DESCRIPTION, "Adds a custom HTTP Header to this message" );
		}

		public void actionPerformed( ActionEvent arg0 )
		{
			Object header = UISupport.prompt( "Specify name of header to add", "Add HTTP Header", "" );
			if( header != null )
			{
				changing = true;
				headersTableModel.add( header.toString(), "" );
				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						int row = headersTable.getRowCount() - 1;
						headersTable.scrollRectToVisible( headersTable.getCellRect( row, 1, true ) );
						headersTable.setRowSelectionInterval( row, row );

						SwingUtilities.invokeLater( new Runnable()
						{
							public void run()
							{
								headersTable.editCellAt( headersTable.getRowCount() - 1, 1 );
								headersTable.getEditorComponent().requestFocusInWindow();
							}
						} );
					}
				} );

				changing = false;
			}
		}
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}
}
