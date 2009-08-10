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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequestInterface;
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
	private RestRequestInterface request;

	protected RestResponseRepresentationsInspector( RestRequestInterface request )
	{
		super( request.getRestMethod(), "Representations", "Response Representations", new RestRepresentation.Type[] {
				RestRepresentation.Type.RESPONSE, RestRepresentation.Type.FAULT } );

		request.addSubmitListener( this );
		this.request = request;
	}

	protected void addToToolbar( JXToolBar toolbar )
	{
		enableRecordingCheckBox = new JCheckBox( "Auto-Create" );
		enableRecordingCheckBox.setToolTipText( "Automatically create Representations from received Responses" );
		enableRecordingCheckBox.setOpaque( false );
		UISupport.setFixedSize( enableRecordingCheckBox, 150, 20 );
		toolbar.addFixed( enableRecordingCheckBox );
		XmlBeansSettingsImpl settings = getMethod().getSettings();
		if( settings.isSet( RECORD_RESPONSE_REPRESENTATIONS ) )
		{
			enableRecordingCheckBox.setSelected( settings.getBoolean( RECORD_RESPONSE_REPRESENTATIONS ) );
		}
		else
		{
			enableRecordingCheckBox.setSelected( getMethod().getResource() == null
					|| getMethod().getResource().getService().isGenerated() );
		}

		enableRecordingCheckBox.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				getMethod().getSettings()
						.setBoolean( RECORD_RESPONSE_REPRESENTATIONS, enableRecordingCheckBox.isSelected() );
			}
		} );
	}

	public boolean beforeSubmit( Submit submit, SubmitContext context )
	{
		return true;
	}

	public void afterSubmit( Submit submit, SubmitContext context )
	{
		HttpResponse response = request.getResponse();
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
		RestRepresentation[] representations = getMethod().getRepresentations( type, null );
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
			RestRepresentation representation = getMethod().addNewRepresentation( type );
			representation.setMediaType( response.getContentType() );
			representation.setStatus( Arrays.asList( response.getStatusCode() ) );

			String xmlContent = response.getContentAsXml();

			if( !xmlContent.equals( "<xml/>" ) )
			{
				// if(response.getContentType().equals("text/xml") ||
				// response.getContentType().equals("application/xml")) {
				try
				{
					XmlCursor cursor = XmlObject.Factory.parse( xmlContent ).newCursor();
					cursor.toFirstChild();
					representation.setElement( cursor.getName() );
				}
				catch( Exception e )
				{

				}
			}
		}
	}

	public void release()
	{
		super.release();
		request.removeSubmitListener( this );
	}
}
