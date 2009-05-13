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

package com.eviware.soapui.impl.rest.panels.request.inspectors.representations;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

public class RestResponseRepresentationsInspector extends AbstractRestRepresentationsInspector implements
		SubmitListener
{
	private JCheckBox enableRecordingCheckBox;
	public static final String RECORD_RESPONSE_REPRESENTATIONS = "RecordResponseRepresentations";

	protected RestResponseRepresentationsInspector( RestRequest request )
	{
		super( request, "Representations", "Response Representations", new RestRepresentation.Type[] {
				RestRepresentation.Type.RESPONSE, RestRepresentation.Type.FAULT } );

		request.addSubmitListener( this );
	}

	protected JXToolBar buildToolbar()
	{
		JXToolBar toolbar = super.buildToolbar();

		toolbar.addSeparator();

		enableRecordingCheckBox = new JCheckBox( "Auto-Create" );
		enableRecordingCheckBox.setToolTipText( "Automatically create Representations from received Responses" );
		enableRecordingCheckBox.setOpaque( false );
		UISupport.setFixedSize( enableRecordingCheckBox, 150, 20 );
		toolbar.addFixed( enableRecordingCheckBox );
		XmlBeansSettingsImpl settings = getRequest().getSettings();
		if( settings.isSet( RECORD_RESPONSE_REPRESENTATIONS ) )
		{
			enableRecordingCheckBox.setSelected( settings.getBoolean( RECORD_RESPONSE_REPRESENTATIONS ) );
		}
		else
		{
			enableRecordingCheckBox.setSelected( getRequest().getResource() == null
					|| getRequest().getResource().getService().isGenerated() );
		}

		enableRecordingCheckBox.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				getRequest().getSettings().setBoolean( RECORD_RESPONSE_REPRESENTATIONS,
						enableRecordingCheckBox.isSelected() );
			}
		} );

		return toolbar;
	}

	public boolean beforeSubmit( Submit submit, SubmitContext context )
	{
		return true;
	}

	public void afterSubmit( Submit submit, SubmitContext context )
	{
		HttpResponse response = getRequest().getResponse();
		if( response != null && enableRecordingCheckBox.isSelected() )
		{
			if( HttpUtils.isErrorStatus( response.getStatusCode() ) )
			{
				extractRepresentation( response, RestRepresentation.Type.FAULT );
			}
			else
			{
				extractRepresentation( response, RestRepresentation.Type.RESPONSE );
			}
		}
	}

	@SuppressWarnings( "unchecked" )
	protected void extractRepresentation( HttpResponse response, RestRepresentation.Type type )
	{
		RestRepresentation[] representations = getRequest().getRepresentations( type, null );
		int c = 0;
		for( ; c < representations.length; c++ )
		{
			if( representations[c].getMediaType() != null
					&& representations[c].getMediaType().equals( response.getContentType() ) )
			{
				List status = representations[c].getStatus();
				if( status == null || !status.contains( response.getStatusCode() ) )
				{
					status = status == null ? new ArrayList<Integer>() : new ArrayList<Integer>( status );
					status.add( response.getStatusCode() );
					representations[c].setStatus( status );
				}
				break;
			}
		}

		if( c == representations.length )
		{
			RestRepresentation representation = getRequest().addNewRepresentation( type );
			representation.setMediaType( response.getContentType() );
			representation.setStatus( Arrays.asList( response.getStatusCode() ) );
		}
	}

	public void release()
	{
		super.release();
		getRequest().removeSubmitListener( this );
	}
}
