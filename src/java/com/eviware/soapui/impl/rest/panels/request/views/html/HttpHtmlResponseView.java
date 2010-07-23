/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.request.views.html;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseMessageEditor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.BrowserComponent;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlEditor;

@SuppressWarnings( "unchecked" )
public class HttpHtmlResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements PropertyChangeListener
{
	private final HttpRequestInterface<?> httpRequest;

	public HttpRequestInterface<?> getHttpRequest()
	{
		return httpRequest;
	}

	private JPanel contentPanel;
	private JPanel panel;
	private JLabel statusLabel;
	private BrowserComponent browser;
	private JButton recordButton;
	private boolean recordHttpTrafic;

	public boolean isRecordHttpTrafic()
	{
		return recordHttpTrafic;
	}

	public void setRecordHttpTrafic( boolean recordHttpTrafic )
	{
		this.recordHttpTrafic = recordHttpTrafic;
	}

	public HttpHtmlResponseView( HttpResponseMessageEditor httpRequestMessageEditor, HttpRequestInterface<?> httpRequest )
	{
		super( "HTML", httpRequestMessageEditor, HttpHtmlResponseViewFactory.VIEW_ID );
		this.httpRequest = httpRequest;
		httpRequest.addPropertyChangeListener( this );
	}

	public HttpHtmlResponseView( XmlEditor xmlEditor, MessageExchangeModelItem messageExchangeModelItem )
	{
		super( "HTML", xmlEditor, HttpHtmlResponseViewFactory.VIEW_ID );
		this.httpRequest = ( HttpRequestInterface<?> )messageExchangeModelItem;
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

		httpRequest.removePropertyChangeListener( this );
	}

	private Component buildStatus()
	{
		statusLabel = new JLabel();
		statusLabel.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		return statusLabel;
	}

	private Component buildContent()
	{
		contentPanel = new JPanel( new BorderLayout() );

		browser = new BrowserComponent( false );
		Component component = browser.getComponent();
		component.setMinimumSize( new Dimension( 100, 100 ) );
		contentPanel.add( component );

		HttpResponse response = httpRequest.getResponse();
		if( response != null )
			setEditorContent( response );
		return contentPanel;
	}

	protected void setEditorContent( HttpResponse httpResponse )
	{
		if( httpResponse != null && httpResponse.getContentType() != null )
		{
			String contentType = httpResponse.getContentType();
			 if( contentType.contains( "html" ) || contentType.contains( "text" )
			 )
			 {
			 try
			 {
			
			 String content = httpResponse.getContentAsString();
			 content = new String( content.getBytes( "UTF-8" ), "iso-8859-1" );
			 browser.setContent( content, contentType,new URL( httpResponse.getURL().toURI().toString()).toString() );
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
				writeHttpBody( httpResponse.getRawResponseData(), fileOutputStream );
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
			browser.setContent( "<missing content>", "text/plain" );
		}
	}

	private void writeHttpBody( byte[] rawResponse, FileOutputStream out ) throws IOException
	{
		int index = 0;
		byte[] divider = "\r\n\r\n".getBytes();
		for( ; index < ( rawResponse.length - divider.length ); index++ )
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

	protected void addToggleButton( JXToolBar toggleToolbar )
	{
		recordButton = new JButton( new RecordHttpTraficAction() );

		toggleToolbar.addLabeledFixed( "Record HTTP trafic", recordButton );
		toggleToolbar.addSeparator();
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		recordButton = new JButton( new RecordHttpTraficAction() );

		toolbar.addLabeledFixed( "Record HTTP trafic", recordButton );
		return toolbar;
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( AbstractHttpRequestInterface.RESPONSE_PROPERTY ) )
		{
			if( browser != null )
				setEditorContent( ( ( HttpResponse )evt.getNewValue() ) );
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

	private class RecordHttpTraficAction extends AbstractAction
	{

		public RecordHttpTraficAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/record_http_false.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Record HTTP trafic" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{

			if( HttpHtmlResponseView.this.isRecordHttpTrafic() )
			{
				HttpHtmlResponseView.this.setRecordHttpTrafic( false );
				recordButton.setIcon( UISupport.createImageIcon( "/record_http_false.gif" ) );
			}
			else
			{
				HttpHtmlResponseView.this.setRecordHttpTrafic( true );
				recordButton.setIcon( UISupport.createImageIcon( "/record_http_true.gif" ) );
			}
			if( browser != null )
			{
				browser.setHttpHtmlResponseView( HttpHtmlResponseView.this );
			}
		}

	}

}