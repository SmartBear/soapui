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

package com.eviware.soapui.support.editor.inspectors.wss;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlLocation;

public abstract class AbstractWssInspector extends AbstractXmlInspector
{
	private JPanel mainPanel;
	private JList resultList;

	protected AbstractWssInspector()
	{
		super( "WSS", "Displays WS-Security information for this response", true, WssInspectorFactory.INSPECTOR_ID );
	}

	@Override
	public void release()
	{
		super.release();
	}

	public void locationChanged( XmlLocation location )
	{
	}

	public JComponent getComponent()
	{
		if( mainPanel == null )
		{
			mainPanel = new JPanel( new BorderLayout() );
			mainPanel.add( buildContent(), BorderLayout.CENTER );

			UISupport.addTitledBorder( mainPanel, "WS-Security processing results" );

			update();
		}

		return mainPanel;
	}

	private Component buildContent()
	{
		resultList = new JList( new ResultVectorListModel( getWssResults() ) );
		return new JScrollPane( resultList );
	}

	public abstract Vector<?> getWssResults();

	public void update()
	{
		resultList.setModel( new ResultVectorListModel( getWssResults() ) );
		int size = resultList.getModel().getSize();
		setTitle( "WSS (" + size + ")" );
		setEnabled( size > 0 );
	}

	private static class ResultVectorListModel extends AbstractListModel
	{
		private final Vector<?> result;

		public ResultVectorListModel( Vector<?> result )
		{
			this.result = result;
		}

		public Object getElementAt( int index )
		{
			return result == null ? null : result.get( index );
		}

		public int getSize()
		{
			return result == null ? 0 : result.size();
		}
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return true;
	}
}
