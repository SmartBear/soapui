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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.panels.request.StringToStringMapTableModel;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeRequestMessageEditor;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeResponseMessageEditor;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.RequestAssertedMessageExchange;
import com.eviware.soapui.model.testsuite.ResponseAssertedMessageExchange;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

/**
 * Shows a desktop-panel with the TestStepResult for a WsdlTestRequestStepResult
 * 
 * @author Ole.Matzura
 */

public class ShowMessageExchangeAction extends AbstractAction
{
	private DefaultDesktopPanel desktopPanel;
	private final MessageExchange messageExchange;
	private final String ownerName;
	private MessageExchangeResponseMessageEditor responseMessageEditor;
	private MessageExchangeRequestMessageEditor requestMessageEditor;

	public ShowMessageExchangeAction( MessageExchange messageExchange, String ownerName )
	{
		super( "Show Message Exchange" );
		this.ownerName = ownerName;
		this.messageExchange = messageExchange;
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
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
			desktopPanel = new MessageExchangeDesktopPanel( "Message Viewer", "Message for " + ownerName, buildContent() );
		}

		return desktopPanel;
	}

	private JComponent buildContent()
	{
		JTabbedPane messageTabs = new JTabbedPane();
		messageTabs.addTab( "Request Message", buildRequestTab() );
		messageTabs.addTab( "Response Message", buildResponseTab() );
		messageTabs.addTab( "Properties", buildPropertiesTab() );

		String[] messages = messageExchange.getMessages();
		if( messages != null && messages.length > 0 )
			messageTabs.addTab( "Messages", buildMessagesTab() );

		if( getAssertedXPaths().size() > 0 )
			messageTabs.addTab( "XPath Assertions", buildAssertionsTab() );

		messageTabs.setPreferredSize( new Dimension( 500, 400 ) );

		JPanel tabPanel = UISupport.createTabPanel( messageTabs, true );

		Component descriptionPanel = UISupport.buildDescription( "MessageExchange Results",
				"See the request/response message below", null );
		tabPanel.add( descriptionPanel, BorderLayout.NORTH );

		return tabPanel;
	}

	private Component buildAssertionsTab()
	{
		List<AssertedXPath> assertedXPaths = getAssertedXPaths();

		DefaultTableModel tm = new DefaultTableModel( assertedXPaths.size(), 2 );
		tm.setColumnIdentifiers( new String[] { "Label", "XPath" } );

		JXTable table = new JXTable( tm );
		table.setHorizontalScrollEnabled( true );
		table.getColumn( 0 ).setPreferredWidth( 100 );

		for( int c = 0; c < assertedXPaths.size(); c++ )
		{
			tm.setValueAt( assertedXPaths.get( c ).getLabel(), c, 0 );
			tm.setValueAt( assertedXPaths.get( c ).getPath(), c, 1 );
		}

		return new JScrollPane( table );
	}

	private List<AssertedXPath> getAssertedXPaths()
	{
		List<AssertedXPath> assertedXPaths = new ArrayList<AssertedXPath>();

		if( messageExchange instanceof RequestAssertedMessageExchange )
		{
			AssertedXPath[] xpaths = ( ( RequestAssertedMessageExchange )messageExchange ).getAssertedXPathsForRequest();
			if( xpaths != null && xpaths.length > 0 )
			{
				assertedXPaths.addAll( Arrays.asList( xpaths ) );
			}
		}

		if( messageExchange instanceof ResponseAssertedMessageExchange )
		{
			AssertedXPath[] xpaths = ( ( ResponseAssertedMessageExchange )messageExchange ).getAssertedXPathsForResponse();
			if( xpaths != null && xpaths.length > 0 )
			{
				assertedXPaths.addAll( Arrays.asList( xpaths ) );
			}
		}
		return assertedXPaths;
	}

	private Component buildPropertiesTab()
	{
		StringToStringMap properties = new StringToStringMap();
		if( messageExchange != null && messageExchange.getProperties() != null )
		{
			properties.putAll( messageExchange.getProperties() );

			// for( String name : messageExchange.getResponse().getPropertyNames())
			// {
			// properties.put( name, messageExchange.getResponse().getProperty(
			// name ) );
			// }

			properties.put( "Timestamp", new Date( messageExchange.getTimestamp() ).toString() );
			properties.put( "Time Taken", String.valueOf( messageExchange.getTimeTaken() ) );
		}
		JTable table = new JTable( new StringToStringMapTableModel( properties, "Name", "Value", false ) );
		return new JScrollPane( table );
	}

	private Component buildMessagesTab()
	{
		String[] messages = messageExchange.getMessages();
		return messages == null || messages.length == 0 ? new JLabel( "No messages to display" ) : new JScrollPane(
				new JList( messages ) );
	}

	private Component buildResponseTab()
	{
		responseMessageEditor = new MessageExchangeResponseMessageEditor( messageExchange );
		return responseMessageEditor;
	}

	private Component buildRequestTab()
	{
		requestMessageEditor = new MessageExchangeRequestMessageEditor( messageExchange );
		return requestMessageEditor;
	}

	private final class MessageExchangeDesktopPanel extends DefaultDesktopPanel
	{
		private MessageExchangeDesktopPanel( String title, String description, JComponent component )
		{
			super( title, description, component );
		}

		@Override
		public boolean onClose( boolean canCancel )
		{
			requestMessageEditor.release();
			responseMessageEditor.release();

			desktopPanel = null;

			return super.onClose( canCancel );
		}

		@Override
		public boolean dependsOn( ModelItem modelItem )
		{
			return ModelSupport.dependsOn( messageExchange.getModelItem(), modelItem );
		}
	}
}
