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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep.PropertyTransferResult;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

/**
 * Shows a desktop-panel with the TestStepResult for a ValueTransferResult
 * 
 * @author Ole.Matzura
 */

public class ShowTransferValuesResultsAction extends AbstractAction
{
	private final PropertyTransferResult result;
	private DefaultDesktopPanel desktopPanel;

	public ShowTransferValuesResultsAction( WsdlTestStepResult result )
	{
		this.result = ( PropertyTransferResult )result;
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
			if( result.isDiscarded() )
				UISupport.showInfoMessage( "Request has been discarded.." );
			else
				showDesktopPanel();
		}
		catch( Exception ex )
		{
			SoapUI.logError( ex );
		}
	}

	public DesktopPanel showDesktopPanel()
	{
		return UISupport.showDesktopPanel( buildFrame() );
	}

	private DesktopPanel buildFrame()
	{
		if( desktopPanel == null )
		{
			desktopPanel = new DefaultDesktopPanel( "TestStep Result", "TestStep result for "
					+ result.getTestStep().getName(), buildContent() );
		}

		return desktopPanel;
	}

	private JComponent buildContent()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		JXTable table = new JXTable( new TransfersTableModel() );

		// table.setColumnControlVisible( true );
		table.setHorizontalScrollEnabled( true );
		table.packAll();

		Component descriptionPanel = UISupport.buildDescription( "PropertyTransfer Results",
				"See the result of each performed transfer below", null );
		panel.add( descriptionPanel, BorderLayout.NORTH );

		JScrollPane scrollPane = new JScrollPane( table );
		scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ),
				scrollPane.getBorder() ) );

		panel.add( scrollPane, BorderLayout.CENTER );
		panel.setPreferredSize( new Dimension( 550, 300 ) );

		return panel;
	}

	private class TransfersTableModel extends AbstractTableModel
	{
		public int getRowCount()
		{
			return result.getTransferCount();
		}

		public int getColumnCount()
		{
			return 2;
		}

		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "Transfer Name";
			case 1 :
				return "Transferred Values";
			}

			return null;
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			switch( columnIndex )
			{
			case 0 :
				return result.getTransferAt( rowIndex ).getName();
			case 1 :
				return Arrays.toString( result.getTransferredValuesAt( rowIndex ) );
			}

			return null;
		}

	}

}
