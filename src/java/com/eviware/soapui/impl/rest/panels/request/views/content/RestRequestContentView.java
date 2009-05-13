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

package com.eviware.soapui.impl.rest.panels.request.views.content;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestDocument;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestMessageEditor;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.support.xsd.SampleXmlUtil;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.TupleList;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.support.xml.XmlUtils;

public class RestRequestContentView extends AbstractXmlEditorView<RestRequestDocument> implements
		PropertyChangeListener
{
	private final RestRequest restRequest;
	private JPanel contentPanel;
	private JXEditTextArea contentEditor;
	private boolean updatingRequest;
	private JComponent panel;
	private JComboBox mediaTypeCombo;
	private JSplitPane split;
	private JButton recreateButton;
	private RestParamsTable paramsTable;
	private JCheckBox postQueryCheckBox;

	public RestRequestContentView( RestRequestMessageEditor restRequestMessageEditor, RestRequest restRequest )
	{
		super( "Request", restRequestMessageEditor, RestRequestContentViewFactory.VIEW_ID );
		this.restRequest = restRequest;

		restRequest.addPropertyChangeListener( this );
	}

	public JComponent getComponent()
	{
		if( panel == null )
		{
			buildComponent();
		}

		return panel;
	}

	protected void buildComponent()
	{
		JPanel p = new JPanel( new BorderLayout() );

		p.add( buildToolbar(), BorderLayout.NORTH );
		p.add( buildContent(), BorderLayout.CENTER );

		paramsTable = new RestParamsTable( restRequest.getParams(), true )
		{
			protected void insertAdditionalButtons( JXToolBar toolbar )
			{
				toolbar.add( UISupport.createToolbarButton( new UpdateRestParamsAction() ) );
			}
		};

		split = UISupport.createVerticalSplit( paramsTable, p );

		panel = new JPanel( new BorderLayout() );
		panel.add( split );

		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				// wait for panel to get shown..
				if( panel.getHeight() == 0 )
				{
					SwingUtilities.invokeLater( this );
				}
				else
				{
					if( !restRequest.hasRequestBody() )
						split.setDividerLocation( 1.0F );
					else
						split.setDividerLocation( 0.5F );
				}
			}
		} );
	}

	@Override
	public void release()
	{
		super.release();
		restRequest.removePropertyChangeListener( this );
		paramsTable.release();
	}

	public RestRequest getRestRequest()
	{
		return restRequest;
	}

	private Component buildContent()
	{
		contentPanel = new JPanel( new BorderLayout() );

		contentEditor = JXEditTextArea.createXmlEditor( true );
		contentEditor.setText( XmlUtils.prettyPrintXml( restRequest.getRequestContent() ) );

		contentEditor.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{

			@Override
			public void update( Document document )
			{
				if( !updatingRequest )
				{
					updatingRequest = true;
					restRequest.setRequestContent( getText( document ) );
					updatingRequest = false;
				}
			}
		} );

		contentPanel.add( new JScrollPane( contentEditor ) );

		PropertyExpansionPopupListener.enable( contentEditor, restRequest );

		enableBodyComponents();

		return contentPanel;
	}

	private void enableBodyComponents()
	{
		mediaTypeCombo.setEnabled( restRequest.hasRequestBody() && !restRequest.isPostQueryString() );
		contentEditor.setEnabledAndEditable( restRequest.hasRequestBody() && !restRequest.isPostQueryString() );
		postQueryCheckBox.setEnabled( restRequest.hasRequestBody() );
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		mediaTypeCombo = new JComboBox( getRequestMediaTypes() );
		mediaTypeCombo.setPreferredSize( new Dimension( 120, 20 ) );
		mediaTypeCombo.setEnabled( restRequest.hasRequestBody() );
		mediaTypeCombo.setEditable( true );
		if( restRequest.getMediaType() != null )
			mediaTypeCombo.setSelectedItem( restRequest.getMediaType() );

		mediaTypeCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				restRequest.setMediaType( String.valueOf( mediaTypeCombo.getSelectedItem() ) );
			}
		} );

		toolbar.addLabeledFixed( "Media Type", mediaTypeCombo );
		toolbar.addSeparator();

		recreateButton = UISupport.createActionButton( new CreateDefaultRepresentationAction(), true );
		recreateButton.setEnabled( canRecreate() );
		toolbar.addFixed( recreateButton );

		toolbar.addSeparator();

		postQueryCheckBox = new JCheckBox( "Post QueryString", restRequest.isPostQueryString() );
		postQueryCheckBox.setToolTipText( "Controls if Query-parameters should be put in message body" );
		postQueryCheckBox.setOpaque( false );
		postQueryCheckBox.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				restRequest.setPostQueryString( postQueryCheckBox.isSelected() );
				enableBodyComponents();
			}
		} );

		postQueryCheckBox.setPreferredSize( new Dimension( 130, 20 ) );
		toolbar.addFixed( postQueryCheckBox );

		toolbar.setMinimumSize( new Dimension( 50, 20 ) );

		return toolbar;
	}

	private boolean canRecreate()
	{
		for( RestRepresentation representation : restRequest.getRepresentations( RestRepresentation.Type.REQUEST,
				restRequest.getMediaType() ) )
		{
			if( representation.getSchemaType() != null )
				return true;
		}

		return false;
	}

	private Object[] getRequestMediaTypes()
	{
		StringList result = new StringList();

		for( RestRepresentation representation : restRequest.getRepresentations( RestRepresentation.Type.REQUEST, null ) )
		{
			if( !result.contains( representation.getMediaType() ) )
				result.add( representation.getMediaType() );
		}

		if( !result.contains( "application/xml" ) )
			result.add( "application/xml" );

		if( !result.contains( "text/xml" ) )
			result.add( "text/xml" );

		if( !result.contains( "multipart/form-data" ) )
			result.add( "multipart/form-data" );

		return result.toStringArray();
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( "request" ) && !updatingRequest )
		{
			updatingRequest = true;
			contentEditor.setText( ( String )evt.getNewValue() );
			updatingRequest = false;
		}
		else if( evt.getPropertyName().equals( "method" ) )
		{
			enableBodyComponents();

			if( !restRequest.hasRequestBody() )
			{
				split.setDividerLocation( 1.0 );
			}
			else if( split.getDividerLocation() >= split.getHeight() - 20 )
			{
				split.setDividerLocation( 0.5 );
			}
		}
		else if( evt.getPropertyName().equals( "mediaType" ) )
		{
			mediaTypeCombo.setSelectedItem( evt.getNewValue() );
			recreateButton.setEnabled( canRecreate() );
		}
		else if( evt.getPropertyName().equals( AbstractHttpRequest.ATTACHMENTS_PROPERTY ) )
		{
			mediaTypeCombo.setModel( new DefaultComboBoxModel( getRequestMediaTypes() ) );
			mediaTypeCombo.setSelectedItem( restRequest.getMediaType() );
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
		contentEditor.setEnabledAndEditable( enabled ? restRequest.hasRequestBody() : false );
		mediaTypeCombo.setEnabled( enabled && !restRequest.isPostQueryString() );
		postQueryCheckBox.setEnabled( enabled );
	}

	public RestParamsTable getParamsTable()
	{
		return paramsTable;
	}

	private class CreateDefaultRepresentationAction extends AbstractAction
	{
		private CreateDefaultRepresentationAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/recreate_request.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Recreates a default representation from the schema" );
		}

		public void actionPerformed( ActionEvent e )
		{
			TupleList<RestRepresentation, SchemaType> list = new TupleList<RestRepresentation, SchemaType>()
			{
				protected String toStringHandler( Tuple tuple )
				{
					return tuple.getValue2().getDocumentElementName().toString();
				}
			};

			for( RestRepresentation representation : restRequest.getRepresentations( RestRepresentation.Type.REQUEST,
					restRequest.getMediaType() ) )
			{
				SchemaType schemaType = representation.getSchemaType();
				if( schemaType != null )
				{
					list.add( representation, schemaType );
				}
			}

			if( list.isEmpty() )
			{
				UISupport.showErrorMessage( "Missing recreatable representations for this method" );
				return;
			}

			TupleList<RestRepresentation, SchemaType>.Tuple result = ( TupleList.Tuple )UISupport.prompt(
					"Select element to create", "Create default content", list.toArray() );
			if( result == null )
			{
				return;
			}

			restRequest.setRequestContent( SampleXmlUtil.createSampleForType( result.getValue2() ) );
		}
	}

	private class UpdateRestParamsAction extends AbstractAction
	{
		private UpdateRestParamsAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Updates this Requests params from a specified URL" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String str = UISupport.prompt( "Enter new url below", "Extract Params", "" );
			if( str == null )
				return;

			try
			{
				restRequest.getParams().resetValues();
				RestUtils.extractParams( str, restRequest.getParams(), false );
				paramsTable.refresh();
			}
			catch( Exception e1 )
			{
				UISupport.showErrorMessage( e1 );
			}
		}
	}
}