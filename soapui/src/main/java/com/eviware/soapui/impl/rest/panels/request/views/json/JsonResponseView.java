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

package com.eviware.soapui.impl.rest.panels.request.views.json;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.eviware.soapui.impl.rest.support.handlers.JsonMediaTypeHandler;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseMessageEditor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;

@SuppressWarnings( "unchecked" )
public class JsonResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements PropertyChangeListener
{
	private final HttpRequestInterface<?> httpRequest;
	private JPanel contentPanel;
	private RSyntaxTextArea contentEditor;
	private boolean updatingRequest;
	private JPanel panel;

	public JsonResponseView( HttpResponseMessageEditor httpRequestMessageEditor, HttpRequestInterface<?> httpRequest )
	{
		super( "JSON", httpRequestMessageEditor, JsonResponseViewFactory.VIEW_ID );
		this.httpRequest = httpRequest;

		httpRequest.addPropertyChangeListener( this );
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

		httpRequest.removePropertyChangeListener( this );
	}

	private Component buildStatus()
	{
		return new JPanel();
	}

	private Component buildContent()
	{
		contentPanel = new JPanel( new BorderLayout() );

		contentEditor = SyntaxEditorUtil.createDefaultJavaScriptSyntaxTextArea();
		HttpResponse response = httpRequest.getResponse();
		if( response != null )
			setEditorContent( response );

		RTextScrollPane scrollPane = new RTextScrollPane( contentEditor );
		scrollPane.setFoldIndicatorEnabled( true );
		scrollPane.setLineNumbersEnabled( true );
		contentPanel.add( scrollPane );
		contentEditor.setEditable( false );

		return contentPanel;
	}

	protected void setEditorContent( HttpResponse httpResponse )
	{
		if( httpResponse == null )
		{
			contentEditor.setText( "" );
		}
		else
		{
			String content = "<Not JSON content>";

			if( JsonMediaTypeHandler.couldBeJsonContent( httpResponse.getContentType() ) )
			{
				try
				{
					JSON json = JSONSerializer.toJSON( httpResponse.getContentAsString() );
					if( json.isEmpty() )
						content = "<Empty JSON content>";
					else
						content = json.toString( 3 );
				}
				catch( Throwable e )
				{
					if( !"Invalid JSON String".equals( e.getMessage() ) )
						e.printStackTrace();
					else
						content = httpResponse.getContentAsString();
				}

				contentEditor.setText( content );
			}
			else
			{
				contentEditor.setText( "<Not JSON content>" );
			}
		}
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		return toolbar;
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( AbstractHttpRequestInterface.RESPONSE_PROPERTY ) && !updatingRequest )
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
