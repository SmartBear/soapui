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

package com.eviware.soapui.support.editor.inspectors.jms.property;

import com.eviware.soapui.impl.wsdl.panels.request.StringToStringMapTableModel;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.support.types.StringToStringMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class JMSHeaderAndPropertyInspector extends AbstractXmlInspector implements PropertyChangeListener
{
	private StringToStringMapTableModel headersTableModel;
	private final JMSHeaderAndPropertyInspectorModel model;
	private JTable headersTable;

	private JPanel panel;
	public boolean changing;

	protected JMSHeaderAndPropertyInspector( JMSHeaderAndPropertyInspectorModel model )
	{
		super( "JMS (" + ( model.getJMSHeadersAndProperties() == null ? "0" : model.getJMSHeadersAndProperties().size() )
				+ ")", "JMS Header and Property for this message", true, JMSHeaderAndPropertyInspectorFactory.INSPECTOR_ID );

		this.model = model;

		model.addPropertyChangeListener( this );
		model.setInspector( this );
	}

	public JComponent getComponent()
	{
		if( panel != null )
			return panel;

		headersTableModel = new StringToStringMapTableModel( model.getJMSHeadersAndProperties(), "Key", "Value",
				!model.isReadOnly() );
		headersTableModel.addTableModelListener( new TableModelListener()
		{
			public void tableChanged( TableModelEvent arg0 )
			{
				StringToStringMap map = model.getJMSHeadersAndProperties();
				setTitle( "JMS (" + ( map == null ? "0" : map.size() ) + ")" );
			}
		} );
		headersTable = JTableFactory.getInstance().makeJTable( headersTableModel );

		panel = new JPanel( new BorderLayout() );
		panel.add( new JScrollPane( headersTable ), BorderLayout.CENTER );

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
			headersTableModel.setData( model.getJMSHeadersAndProperties() );
	}

	public JMSHeaderAndPropertyInspectorModel getModel()
	{
		return model;
	}

	public StringToStringMapTableModel getHeadersTableModel()
	{
		return headersTableModel;
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}
}
