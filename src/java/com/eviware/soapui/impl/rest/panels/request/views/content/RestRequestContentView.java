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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;

import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.panels.resource.InstanceRestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.http.HttpRequestContentView;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpRequestMessageEditor;
import com.eviware.soapui.impl.wsdl.support.xsd.SampleXmlUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.TupleList;

public class RestRequestContentView extends HttpRequestContentView
{
	private RestRequestInterface restRequest;
	private JButton recreateButton;
	private RestParamsTable paramsTable;

	@SuppressWarnings("unchecked")
	public RestRequestContentView( HttpRequestMessageEditor restRequestMessageEditor, RestRequestInterface restRequest )
	{
		super( restRequestMessageEditor, restRequest );
		this.restRequest = restRequest;
	}

	protected RestParamsTable buildParamsTable()
	{
		paramsTable = new InstanceRestParamsTable( restRequest.getParams() )
		{
			protected void insertAdditionalButtons( JXToolBar toolbar )
			{
				toolbar.add( UISupport.createToolbarButton( new UpdateRestParamsAction() ) );
			}
		};
		return paramsTable;
	}

	public RestParamsTable getParamsTable()
	{
		return paramsTable;
	}

	protected Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		addMediaTypeCombo( toolbar );
		toolbar.addSeparator();

		recreateButton = UISupport.createActionButton( new CreateDefaultRepresentationAction(), true );
		recreateButton.setEnabled( canRecreate() );
		toolbar.addFixed( recreateButton );

		toolbar.addSeparator();

		addPostQueryCheckBox( toolbar );

		toolbar.setMinimumSize( new Dimension( 50, 20 ) );

		return toolbar;
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( "mediaType" ) && recreateButton != null )
		{
			recreateButton.setEnabled( canRecreate() );
		}
		else if( evt.getPropertyName().equals( "restMethod" ))
		{
			paramsTable.setParams( restRequest.getParams() );
		}
		
		super.propertyChange( evt );
	}

	protected Object[] getRequestMediaTypes()
	{
		StringList result = new StringList( super.getRequestMediaTypes() );

		for( RestRepresentation representation : restRequest.getRepresentations( RestRepresentation.Type.REQUEST, null ) )
		{
			if( !result.contains( representation.getMediaType() ) )
				result.add( representation.getMediaType() );
		}

		return result.toStringArray();
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

	private class CreateDefaultRepresentationAction extends AbstractAction
	{
		private CreateDefaultRepresentationAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/recreate_request.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Recreates a default representation from the schema" );
		}

		@SuppressWarnings("unchecked")
		public void actionPerformed( ActionEvent e )
		{
			TupleList<RestRepresentation, SchemaType> list = new TupleList<RestRepresentation, SchemaType>()
			{
				protected String toStringHandler( Tuple tuple )
				{
					return tuple.getValue2().getDocumentElementName().toString();
				}
			};

			for( RestRepresentation representation : ( ( RestRequestInterface )restRequest ).getRepresentations(
					RestRepresentation.Type.REQUEST, restRequest.getMediaType() ) )
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
}