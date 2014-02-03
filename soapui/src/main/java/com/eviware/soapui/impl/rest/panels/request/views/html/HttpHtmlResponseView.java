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
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings( "unchecked" )
public class HttpHtmlResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements PropertyChangeListener
{
	private HttpRequestInterface<?> httpRequest;
	private JPanel panel;
	private WebViewBasedBrowserComponent browser;
	private MessageExchangeModelItem messageExchangeModelItem;

	public HttpHtmlResponseView( HttpResponseMessageEditor httpRequestMessageEditor, HttpRequestInterface<?> httpRequest )
	{
		super( "HTML", httpRequestMessageEditor, HttpHtmlResponseViewFactory.VIEW_ID );
		this.httpRequest = httpRequest;
		httpRequest.addPropertyChangeListener( this );
	}

	public JComponent getComponent()
	{
		if( panel == null )
		{
			panel = new JPanel( new BorderLayout() );

		}

		return panel;
	}

	@Override
	public boolean activate( EditorLocation<HttpResponseDocument> location )
	{
		boolean activated = super.activate( location );
		if (activated)
		{
			if( browser == null )
			{
				browser = new WebViewBasedBrowserComponent( false );
				Component component = browser.getComponent();
				component.setMinimumSize( new Dimension( 100, 100 ) );
				panel.add( component, BorderLayout.CENTER );
			}

			HttpResponse response = httpRequest.getResponse();
			if( response != null )
			{
				setEditorContent( response );
			}
		}
		return activated;
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
		if( httpResponse == null )
		{
			return;
		}
		String content = httpResponse.getContentAsString();
		if( content != null )
		{
			String contentType = httpResponse.getContentType();

			if( contentType != null && isSupportedContentType( contentType ) )
			{
				try
				{
					browser.setContent( content, contentType);
				}
				catch( Exception e )
				{
					SoapUI.logError( e, "Could not display response from " + httpResponse.getURL() + " as HTML" );
				}
			}
			else
			{
				browser.setContent( "unsupported content-type [" + contentType + "]" );
			}
		}
		else
		{
			browser.setContent( "<missing content>" );
		}
	}



	private boolean isSupportedContentType( String contentType )
	{
		return contentType != null && ( contentType.trim().toLowerCase().startsWith( "text" ) ||
				contentType.trim().toLowerCase().startsWith( "image" ) );
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

	public HttpRequestInterface<?> getHttpRequest()
	{
		return httpRequest;
	}
}
