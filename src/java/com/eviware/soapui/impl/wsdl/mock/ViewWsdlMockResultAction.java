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

package com.eviware.soapui.impl.wsdl.mock;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.panels.request.StringToStringMapTableModel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Shows a desktop-panel with the MessageExchange for a WsdlMockResult
 * 
 * @author Ole.Matzura
 */

public class ViewWsdlMockResultAction extends AbstractAction
{
	private final WsdlMockResult result;
	private DefaultDesktopPanel desktopPanel;

	public ViewWsdlMockResultAction( WsdlMockResult result )
	{
		super( "Show Results" );

		this.result = result;
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
			if( result.isDiscarded() )
				UISupport.showInfoMessage( "Request has been discarded.." );
			else
				UISupport.showDesktopPanel( buildFrame() );
		}
		catch( Exception ex )
		{
			SoapUI.logError( ex );
		}
	}

	private DesktopPanel buildFrame()
	{
		if( desktopPanel == null )
		{
			String title = "Mock Result for [" + result.getMockResponse().getName() + "]";
			desktopPanel = new DefaultDesktopPanel( title, title, buildContent() );
		}

		return desktopPanel;
	}

	private JComponent buildContent()
	{
		JTabbedPane messageTabs = new JTabbedPane();
		messageTabs.addTab( "Request", buildRequestTab() );
		messageTabs.addTab( "Response", buildResponseTab() );
		messageTabs.setPreferredSize( new Dimension( 500, 400 ) );

		JPanel panel = new JPanel( new BorderLayout() );
		panel.add( UISupport.createTabPanel( messageTabs, true ), BorderLayout.CENTER );

		ButtonBarBuilder builder = new ButtonBarBuilder();
		builder.addFixed( new JLabel( "Mock Request handled at " + new Date( result.getTimestamp() ) + ", time taken: "
				+ result.getTimeTaken() + "ms" ) );
		builder.addGlue();
		builder.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		panel.add( builder.getPanel(), BorderLayout.PAGE_START );

		return panel;
	}

	private Component buildResponseTab()
	{
		JXEditTextArea responseArea = JXEditTextArea.createXmlEditor( false );
		responseArea.setText( XmlUtils.prettyPrintXml( result.getResponseContent() ) );
		responseArea.setEditable( false );
		responseArea.setToolTipText( "Response Content" );
		JScrollPane scrollPane = new JScrollPane( responseArea );

		JSplitPane split = UISupport.createVerticalSplit( new JScrollPane( new JTable( new StringToStringMapTableModel(
				result.getResponseHeaders(), "Header", "Value", false ) ) ), scrollPane );
		split.setDividerLocation( 150 );
		return split;
	}

	private Component buildRequestTab()
	{
		JXEditTextArea resultArea = JXEditTextArea.createXmlEditor( false );
		resultArea.setText( XmlUtils.prettyPrintXml( result.getMockRequest().getRequestContent() ) );
		resultArea.setEditable( false );
		resultArea.setToolTipText( "Request Content" );
		JScrollPane scrollPane = new JScrollPane( resultArea );

		JSplitPane split = UISupport.createVerticalSplit( new JScrollPane( new JTable( new StringToStringMapTableModel(
				result.getMockRequest().getRequestHeaders(), "Header", "Value", false ) ) ), scrollPane );
		split.setDividerLocation( 150 );
		return split;
	}
}
