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

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestDocument;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestMessageEditor;
import com.eviware.soapui.impl.rest.panels.resource.JWadlParamsTable;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.support.xml.XmlUtils;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RestRequestContentView extends AbstractXmlEditorView<RestRequestDocument> implements PropertyChangeListener
{
	private final RestRequest restRequest;
	private JPanel contentPanel;
	private JXEditTextArea contentEditor;
	private boolean updatingRequest;
	private JComponent panel;
	private JComboBox mediaTypeCombo;
	private JSplitPane split;
	
	public RestRequestContentView(RestRequestMessageEditor restRequestMessageEditor, RestRequest restRequest)
	{
		super( "Request", restRequestMessageEditor, RestRequestContentViewFactory.VIEW_ID );
		this.restRequest = restRequest;
		
		restRequest.addPropertyChangeListener( this );
	}

	public JComponent getComponent()
	{
		if( panel == null )
		{
			JPanel p = new JPanel( new BorderLayout() );
			
			p.add( buildToolbar(), BorderLayout.NORTH );
			p.add( buildContent(), BorderLayout.CENTER );
			
			split = UISupport.createVerticalSplit( new JWadlParamsTable( restRequest.getParams(), true ), p );
			
			panel = new JPanel( new BorderLayout() );
			panel.add( split);
			
			SwingUtilities.invokeLater(new Runnable() {

				public void run()
				{
					split.setDividerLocation( restRequest.hasRequestBody() ? 0.5 : 1.0);
				}} );
		}
		
		return panel;
	}

	@Override
	public void release()
	{
		super.release();
		restRequest.removePropertyChangeListener( this );
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
		
		mediaTypeCombo = new JComboBox( getRequestMediaTypes() );
		mediaTypeCombo.setPreferredSize(new Dimension( 200, 20 ));
		mediaTypeCombo.setEnabled( restRequest.hasRequestBody());
      mediaTypeCombo.setEditable( true );
      mediaTypeCombo.setSelectedItem( restRequest.getMediaType() );
      mediaTypeCombo.addItemListener( new ItemListener()
		{
         public void itemStateChanged( ItemEvent e )
         {
            restRequest.setMediaType( mediaTypeCombo.getSelectedItem().toString() );
         }
      });

		toolbar.addLabeledFixed("Media Type", mediaTypeCombo );
		toolbar.addSeparator();
		
		return toolbar;
	}

   private Object[] getRequestMediaTypes()
   {
      StringList result = new StringList();

      for( RestRepresentation representation : restRequest.getRepresentations( RestRepresentation.Type.REQUEST ))
      {
         if( !result.contains( representation.getMediaType() ))
            result.add( representation.getMediaType());
      }

      if( !result.contains( "application/xml"))
         result.add( "application/xml");

      if( !result.contains( "text/xml"))
         result.add( "text/xml");

      return result.toStringArray();
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
			mediaTypeCombo.setEnabled( restRequest.hasRequestBody() );
			
			if( !restRequest.hasRequestBody())
			{
				split.setDividerLocation(1.0);
			}
			else if( split.getDividerLocation() >= split.getHeight()-20 )
			{
				split.setDividerLocation( 0.5);
			}
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