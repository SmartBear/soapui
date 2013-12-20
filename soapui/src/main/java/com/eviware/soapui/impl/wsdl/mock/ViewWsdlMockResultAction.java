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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.panels.request.StringToStringsMapTableModel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;

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
		RSyntaxTextArea responseArea = SyntaxEditorUtil.createDefaultXmlSyntaxTextArea();
		responseArea.setText( XmlUtils.prettyPrintXml( result.getResponseContent() ) );
		responseArea.setEditable( false );
		responseArea.setToolTipText( "Response Content" );
		responseArea.setFont( UISupport.getEditorFont() );
		RTextScrollPane scrollPane = new RTextScrollPane( responseArea );
		scrollPane.setFoldIndicatorEnabled( true );
		scrollPane.setLineNumbersEnabled( true );

		JSplitPane split = UISupport.createVerticalSplit( new JScrollPane( JTableFactory.getInstance().makeJTable( new StringToStringsMapTableModel(
				result.getResponseHeaders(), "Header", "Value", false ) ) ), scrollPane );
		split.setDividerLocation( 150 );
		return split;
	}

	private Component buildRequestTab()
	{
		RSyntaxTextArea resultArea = SyntaxEditorUtil.createDefaultXmlSyntaxTextArea();
		resultArea.setFont( UISupport.getEditorFont() );
		resultArea.setText( XmlUtils.prettyPrintXml( result.getMockRequest().getRequestContent() ) );
		resultArea.setEditable( false );
		resultArea.setToolTipText( "Request Content" );

		RTextScrollPane scrollPane = new RTextScrollPane( resultArea );
		scrollPane.setFoldIndicatorEnabled( true );
		scrollPane.setLineNumbersEnabled( true );
		JSplitPane split = UISupport.createVerticalSplit( new JScrollPane( JTableFactory.getInstance().makeJTable( new StringToStringsMapTableModel(
				result.getMockRequest().getRequestHeaders(), "Header", "Value", false ) ) ), scrollPane );
		split.setDividerLocation( 150 );
		return split;
	}
}
