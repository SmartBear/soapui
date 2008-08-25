/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.request.views.content;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.Document;

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestDocument;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestMessageEditor;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.support.xml.XmlUtils;

public class RestRequestContentView extends AbstractXmlEditorView<RestRequestDocument> implements PropertyChangeListener
{
	private final RestRequest restRequest;
	private RestRepresentation requestRepresentation;
	private JPanel contentPanel;
	private JXEditTextArea contentEditor;
	private boolean updatingRequest;
	private JComponent panel;
	private JComboBox mediaTypeCombo;
	
	public RestRequestContentView(RestRequestMessageEditor restRequestMessageEditor, RestRequest restRequest)
	{
		super( "Body", restRequestMessageEditor, RestRequestContentViewFactory.VIEW_ID );
		this.restRequest = restRequest;
		
		RestRepresentation[] representations = restRequest.getRepresentations(RestRepresentation.Type.REQUEST);
		if( representations.length == 0 )
		{
			requestRepresentation = restRequest.addNewRepresentation( RestRepresentation.Type.REQUEST );
		}
		else
		{
			requestRepresentation = representations[0];
		}

		if( !StringUtils.hasContent(requestRepresentation.getMediaType()))
			requestRepresentation.setMediaType("application/xml");
		
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
		restRequest.removePropertyChangeListener( this );
	}

	private Component buildStatus()
	{
		return new JPanel();
	}

	private Component buildContent()
	{
		contentPanel = new JPanel( new BorderLayout() );
		
		contentEditor = JXEditTextArea.createXmlEditor(true);
		contentEditor.setText( XmlUtils.prettyPrintXml( restRequest.getRequestContent() ) );
		
		contentEditor.getDocument().addDocumentListener(new DocumentListenerAdapter() {

			@Override
			public void update(Document document)
			{
				updatingRequest = true;
				restRequest.setRequestContent( contentEditor.getText() );
				updatingRequest = false;
			}} );
		
		contentPanel.add( new JScrollPane( contentEditor ));
		contentEditor.setEnabledAndEditable( restRequest.hasRequestBody() );
		
		return contentPanel;
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		
		mediaTypeCombo = new JComboBox(new Object[] { requestRepresentation.getMediaType() });
		mediaTypeCombo.setPreferredSize(new Dimension( 200, 20 ));
		mediaTypeCombo.setEditable(true);
		mediaTypeCombo.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				updatingRequest = true;
				requestRepresentation.setMediaType((String) mediaTypeCombo.getSelectedItem());
				updatingRequest = false;
			}
		});

		toolbar.addLabeledFixed("Media Type", mediaTypeCombo);
		toolbar.addSeparator();
		
		return toolbar;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if( evt.getPropertyName().equals( "request" ) && !updatingRequest )
		{
			contentEditor.setText( (String)evt.getNewValue() );
		}
		else if( evt.getPropertyName().equals( "method"))
		{
			contentEditor.setEnabledAndEditable( restRequest.hasRequestBody() );
		}
	}

	@Override
	public void setXml(String xml)
	{
	}

	public boolean saveDocument(boolean validate)
	{
		return false;
	}

	public void setEditable(boolean enabled)
	{
		contentEditor.setEnabledAndEditable(enabled ? restRequest.hasRequestBody() : false );
	}
}