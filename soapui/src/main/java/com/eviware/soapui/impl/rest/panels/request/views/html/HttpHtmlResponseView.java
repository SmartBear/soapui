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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseMessageEditor;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.BrowserComponent;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlEditor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings( "unchecked" )
public class HttpHtmlResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements PropertyChangeListener
{
	private HttpRequestInterface<?> httpRequest;
	private JPanel panel;
	private BrowserComponent browser;
	private JToggleButton recordButton;
	private boolean recordHttpTrafic;
	private MessageExchangeModelItem messageExchangeModelItem;
	private boolean hasResponseForRecording;

	public boolean isRecordHttpTrafic()
	{
		return recordHttpTrafic;
	}

	public void setRecordHttpTrafic( boolean recordHttpTrafic )
	{
		// no change?
		if( SoapUI.isJXBrowserDisabled() || recordHttpTrafic == this.recordHttpTrafic )
			return;

		if( recordHttpTrafic )
		{
			recordButton.setIcon( UISupport.createImageIcon( "/record_http_true.gif" ) );
			recordButton.setToolTipText( "Stop recording" );
			recordButton.setSelected( true );
			browser.setRecordingHttpHtmlResponseView( HttpHtmlResponseView.this );
		}
		else
		{
			browser.setRecordingHttpHtmlResponseView( null );
			recordButton.setIcon( UISupport.createImageIcon( "/record_http_false.gif" ) );
			recordButton.setToolTipText( "Start recording" );
			recordButton.setSelected( false );
		}
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
		this.messageExchangeModelItem = messageExchangeModelItem;
		this.httpRequest = ( HttpRequestInterface<?> )messageExchangeModelItem;
		messageExchangeModelItem.addPropertyChangeListener( this );
	}

	public JComponent getComponent()
	{
		if( panel == null )
		{
			panel = new JPanel( new BorderLayout() );

			if( SoapUI.isJXBrowserDisabled() )
			{
				panel.add( new JLabel( "Browser Component is disabled" ) );
			}
			else
			{
				browser = new BrowserComponent( false, true );
				Component component = browser.getComponent();
				component.setMinimumSize( new Dimension( 100, 100 ) );
				panel.add( buildToolbar(), BorderLayout.NORTH );
				panel.add( component, BorderLayout.CENTER );

				HttpResponse response = httpRequest.getResponse();
				if( response != null )
					setEditorContent( response );
			}
		}

		return panel;
	}

	@Override
	public void release()
	{
		super.release();

		if( browser != null )
			browser.release();

		if( messageExchangeModelItem != null )
			messageExchangeModelItem.removePropertyChangeListener( this );
		else
			httpRequest.removePropertyChangeListener( this );

		httpRequest = null;
		messageExchangeModelItem = null;
	}

	protected void setEditorContent( HttpResponse httpResponse )
	{
		if( httpResponse != null && httpResponse.getContentAsString() != null )
		{
			String contentType = httpResponse.getContentType();

			if( contentType != null )
			{
				if( isSupportedContentType( contentType ) )
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
						hasResponseForRecording = true;
					}
					catch( Throwable e )
					{
						e.printStackTrace();
					}
				}
				else
				{
					try
					{
						browser.setContent( new String( httpResponse.getContentAsString().getBytes( "utf-8" ) ), httpResponse
								.getURL().toURI().toString() );
						hasResponseForRecording = true;
					}
					catch( Throwable e )
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				browser.setContent( "unsupported content-type [" + contentType + "]" );
				hasResponseForRecording = false;
			}
		}
		else
		{
			browser.setContent( "<missing content>" );
			hasResponseForRecording = false;
		}
	}

	private boolean isSupportedContentType( String contentType )
	{
		return contentType != null && contentType.trim().toLowerCase().startsWith( "image" );
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

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		recordButton = new JToggleButton( new RecordHttpTraficAction() );

		toolbar.addLabeledFixed( "Record HTTP traffic", recordButton );
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
			putValue( Action.SHORT_DESCRIPTION, "Start recording" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			if( browser == null )
				return;

			if( isRecordHttpTrafic() )
			{
				setRecordHttpTrafic( false );
			}
			else
			{
				if( !hasResponseForRecording )
				{
					// resubmit so we have "live" content
					try
					{
						getHttpRequest().submit( new WsdlSubmitContext( getHttpRequest() ), false ).waitUntilFinished();
					}
					catch( SubmitException e )
					{
						SoapUI.logError( e );
					}
				}

				setRecordHttpTrafic( true );
			}
		}
	}

	public HttpRequestInterface<?> getHttpRequest()
	{
		return httpRequest;
	}
}
