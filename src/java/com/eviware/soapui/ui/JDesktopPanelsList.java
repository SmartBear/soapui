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

package com.eviware.soapui.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.eviware.soapui.ui.desktop.DesktopListener;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;

/**
 * List for displaying current DesktopPanels
 * 
 * @author Ole.Matzura
 */

public class JDesktopPanelsList extends JPanel
{
	private DefaultListModel desktopPanels;
	private JList desktopPanelsList;
	private SoapUIDesktop desktop;
	private DesktopPanelPropertyChangeListener desktopPanelPropertyListener = new DesktopPanelPropertyChangeListener();
	private InternalDesktopListener desktopListener = new InternalDesktopListener();

	public JDesktopPanelsList( SoapUIDesktop desktop )
	{
		super( new BorderLayout() );
		setDesktop( desktop );

		desktopPanels = new DefaultListModel();
		desktopPanelsList = new JList( desktopPanels );
		desktopPanelsList.setCellRenderer( new DesktopItemsCellRenderer() );
		desktopPanelsList.setToolTipText( "Open windows" );
		desktopPanelsList.addMouseListener( new MouseAdapter()
		{
			public void mouseClicked( MouseEvent e )
			{
				if( e.getClickCount() < 2 )
					return;

				JDesktopPanelsList.this.desktop.showDesktopPanel( ( DesktopPanel )desktopPanelsList.getSelectedValue() );
			}
		} );

		add( new JScrollPane( desktopPanelsList ), BorderLayout.CENTER );
	}

	private class DesktopPanelPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			DesktopPanel desktopPanel = ( DesktopPanel )evt.getSource();
			int ix = desktopPanels.indexOf( desktopPanel );
			if( ix >= 0 )
				desktopPanels.set( ix, desktopPanel );
		}
	}

	private static class DesktopItemsCellRenderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

			DesktopPanel desktopPanel = ( DesktopPanel )value;
			String title = desktopPanel.getTitle();
			setText( title );
			setToolTipText( desktopPanel.getDescription() );
			setIcon( desktopPanel.getIcon() );

			setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ), getBorder() ) );

			return this;
		}
	}

	public List<DesktopPanel> getDesktopPanels()
	{
		List<DesktopPanel> result = new ArrayList<DesktopPanel>();

		for( int c = 0; c < desktopPanels.getSize(); c++ )
			result.add( ( DesktopPanel )desktopPanels.get( c ) );

		return result;
	}

	private class InternalDesktopListener implements DesktopListener
	{
		public void desktopPanelSelected( DesktopPanel desktopPanel )
		{
			desktopPanelsList.setSelectedValue( desktopPanel, false );
		}

		public void desktopPanelCreated( DesktopPanel desktopPanel )
		{
			desktopPanels.addElement( desktopPanel );
			desktopPanelsList.setSelectedValue( desktopPanel, false );

			desktopPanel.addPropertyChangeListener( desktopPanelPropertyListener );
		}

		public void desktopPanelClosed( DesktopPanel desktopPanel )
		{
			desktopPanels.removeElement( desktopPanel );
			desktopPanel.removePropertyChangeListener( desktopPanelPropertyListener );
		}
	}

	public void setDesktop( SoapUIDesktop newDesktop )
	{
		if( desktop != null )
		{
			desktop.removeDesktopListener( desktopListener );

			while( desktopPanels.size() > 0 )
			{
				DesktopPanel desktopPanel = ( DesktopPanel )desktopPanels.getElementAt( 0 );
				desktopPanel.removePropertyChangeListener( desktopPanelPropertyListener );
				desktopPanels.remove( 0 );
			}

		}

		desktop = newDesktop;

		desktop.addDesktopListener( desktopListener );

		for( DesktopPanel desktopPanel : desktop.getDesktopPanels() )
		{
			desktopPanel.addPropertyChangeListener( desktopPanelPropertyListener );
			desktopPanels.addElement( desktopPanel );
		}
	}

	public JList getDesktopPanelsList()
	{
		return desktopPanelsList;
	}

	public int getItemsCount()
	{
		return desktopPanels.size();
	}
}
