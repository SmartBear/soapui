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

package com.eviware.soapui.impl.rest.panels.request.views.html;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestResponseDocument;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestResponseMessageEditor;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.BrowserComponent;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;

@SuppressWarnings( "unchecked" )
public class RestHtmlResponseView extends AbstractXmlEditorView<RestResponseDocument> implements PropertyChangeListener // ,
																																								// BrComponentListener
{
	private final RestRequest restRequest;
	private JPanel contentPanel;
	private JPanel panel;
	private JLabel statusLabel;
	private BrowserComponent browser;

	public RestHtmlResponseView( RestResponseMessageEditor restRequestMessageEditor, RestRequest restRequest )
	{
		super( "HTML", restRequestMessageEditor, RestHtmlResponseViewFactory.VIEW_ID );
		this.restRequest = restRequest;

		restRequest.addPropertyChangeListener( this );
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

		browser.release();
		restRequest.removePropertyChangeListener( this );
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

		browser = new BrowserComponent();

		HttpResponse response = restRequest.getResponse();
		if( response != null )
			setEditorContent( response );

		Component component = browser.getComponent();
		component.setMinimumSize( new Dimension( 100, 100 ) );
		contentPanel.add( component );

		return contentPanel;
	}

	protected void setEditorContent( HttpResponse httpResponse )
	{
		if( httpResponse != null && httpResponse.getContentType() != null
				&& httpResponse.getContentType().contains( "html" ) )
		{
			try
			{
				browser.setContent( httpResponse.getContentAsString(), httpResponse.getContentType(), httpResponse.getURL()
						.toURI().toString() );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			browser.setContent( "<missing content>", "text/text" );
		}
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		return toolbar;
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( AbstractHttpRequest.RESPONSE_PROPERTY ) )
		{
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

}