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

package com.eviware.soapui.impl.rest.panels.request.views.html;

import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlEditor;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class HttpHtmlMessageExchangeResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements
		PropertyChangeListener
{
	private final MessageExchangeModelItem messageExchangeModelItem;
	private JPanel panel;
	private WebViewBasedBrowserComponent browser;
	private JPanel contentPanel;

	public HttpHtmlMessageExchangeResponseView( XmlEditor editor, MessageExchangeModelItem messageExchangeModelItem )
	{
		super( "HTML", editor, HttpHtmlResponseViewFactory.VIEW_ID );
		this.messageExchangeModelItem = messageExchangeModelItem;

		messageExchangeModelItem.addPropertyChangeListener( this );
	}

	public JComponent getComponent()
	{
		if( panel == null )
		{
			panel = new JPanel( new BorderLayout() );

			panel.add( buildToolbar(), BorderLayout.NORTH );
			panel.add( buildContent(), BorderLayout.CENTER );
			panel.add( buildStatus(), BorderLayout.SOUTH );
		}

		return panel;
	}

	@Override
	public void release()
	{
		super.release();

		if( browser != null )
			browser.release();

		messageExchangeModelItem.removePropertyChangeListener( this );
	}

	private Component buildStatus()
	{
		JLabel statusLabel = new JLabel();
		statusLabel.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		return statusLabel;
	}

	private Component buildContent()
	{
		contentPanel = new JPanel( new BorderLayout() );

		return contentPanel;
	}

	@Override
	public boolean activate( EditorLocation<HttpResponseDocument> location )
	{
		boolean activated = super.activate( location );
		if(activated){
			if(browser == null){
				browser = new WebViewBasedBrowserComponent( false );
				Component component = browser.getComponent();
				component.setMinimumSize( new Dimension( 100, 100 ) );
				contentPanel.add( new JScrollPane( component ) );
			}
			setEditorContent( messageExchangeModelItem );
		}
		return activated;
	}

	@Override
	public boolean deactivate()
	{
		boolean deactivated = super.deactivate();
		if(deactivated){
			browser.setContent( "" );
		}
		return deactivated;
	}

	protected void setEditorContent( JProxyServletWsdlMonitorMessageExchange jproxyServletWsdlMonitorMessageExchange )
	{

		if( jproxyServletWsdlMonitorMessageExchange != null )
		{
			String contentType = jproxyServletWsdlMonitorMessageExchange.getResponseContentType();
			if( contentType.contains( "html" ) || contentType.contains( "text" ) )
			{
				try
				{

					String content = jproxyServletWsdlMonitorMessageExchange.getResponseContent();
					browser.setContent( content );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
			else if( isSupportedContentType( contentType ) )
			{
				try
				{
					String ext = ContentTypeHandler.getExtensionForContentType( contentType );
					File temp = File.createTempFile( "response", "." + ext );
					FileOutputStream fileOutputStream = new FileOutputStream( temp );
					writeHttpBody( jproxyServletWsdlMonitorMessageExchange.getRawResponseData(), fileOutputStream );
					fileOutputStream.close();
					browser.navigate( temp.toURI().toURL().toString(), null );
					temp.deleteOnExit();
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
			else
			{
				browser.setContent( "unsupported content-type [" + contentType + "]" );
			}
		}
		else
		{
			browser.setContent( "-missing content-" );
		}
	}

	private boolean isSupportedContentType( String contentType )
	{
		return contentType.toLowerCase().contains( "image" );
	}

	protected void setEditorContent( MessageExchangeModelItem messageExchangeModelItem2 )
	{

		if( messageExchangeModelItem2 != null && messageExchangeModelItem2.getMessageExchange() != null )
		{
			String contentType = messageExchangeModelItem2.getMessageExchange().getResponseHeaders()
					.get( "Content-Type", "" );
			if( contentType.contains( "html" ) || contentType.contains( "text" ) )
			{
				try
				{

					final String content = messageExchangeModelItem2.getMessageExchange().getResponseContent();
					browser.setContent( content, contentType );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
			else if( !contentType.contains( "xml" ) )
			{
				try
				{
					String ext = ContentTypeHandler.getExtensionForContentType( contentType );
					File temp = File.createTempFile( "response", "." + ext );
					FileOutputStream fileOutputStream = new FileOutputStream( temp );
					writeHttpBody( messageExchangeModelItem2.getMessageExchange().getRawResponseData(), fileOutputStream );
					fileOutputStream.close();
					browser.navigate( temp.toURI().toURL().toString(), null );
					temp.deleteOnExit();
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			browser.setContent( "<missing content>" );
		}
	}


	private void writeHttpBody( byte[] rawResponse, FileOutputStream out ) throws IOException
	{
		int index = 0;
		byte[] divider = "\r\n\r\n".getBytes();
		for(; index < ( rawResponse.length - divider.length ); index++ )
		{
			int i;
			for( i = 0; i < divider.length; i++ )
			{
				if( rawResponse[index + i] != divider[i] )
					break;
			}

			if( i == divider.length )
			{
				out.write( rawResponse, index + divider.length, rawResponse.length - ( index + divider.length ) );
				return;
			}
		}

		out.write( rawResponse );
	}

	private Component buildToolbar()
	{
		return UISupport.createToolbar();
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( "messageExchange" ) )
		{
			if( browser != null && evt.getNewValue() != null )
				setEditorContent( ( ( JProxyServletWsdlMonitorMessageExchange )evt.getNewValue() ) );
		}
	}

	@Override
	public void setXml( String xml )
	{
	}

	public boolean saveDocument( boolean validate )
	{
		return false;
	}

	public void setEditable( boolean enabled )
	{
	}

}
