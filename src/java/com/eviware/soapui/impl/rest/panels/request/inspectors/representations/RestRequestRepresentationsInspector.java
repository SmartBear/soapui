/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

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

import javax.swing.JCheckBox;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

public class RestRequestRepresentationsInspector extends AbstractRestRepresentationsInspector implements SubmitListener
{
	private JCheckBox enableRecordingCheckBox;
	public static final String RECORD_REQUEST_REPRESENTATIONS = "RecordRequestRepresentations";
	private RestRequest request;

	protected RestRequestRepresentationsInspector( RestRequest request )
	{
		super( request.getRestMethod(), "Representations", "Request Representations",
				new RestRepresentation.Type[] { RestRepresentation.Type.REQUEST } );

		request.addSubmitListener( this );
		this.request = request;
	}

	protected void addToToolbar( JXToolBar toolbar )
	{
		enableRecordingCheckBox = new JCheckBox( "Auto-Create" );
		enableRecordingCheckBox.setToolTipText( "Automatically create Representations from sent Requests" );
		enableRecordingCheckBox.setOpaque( false );
		UISupport.setFixedSize( enableRecordingCheckBox, 100, 20 );
		toolbar.addFixed( enableRecordingCheckBox );
		XmlBeansSettingsImpl settings = request.getSettings();
		if( settings.isSet( RECORD_REQUEST_REPRESENTATIONS ) )
		{
			enableRecordingCheckBox.setSelected( settings.getBoolean( RECORD_REQUEST_REPRESENTATIONS ) );
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
				request.getSettings().setBoolean( RECORD_REQUEST_REPRESENTATIONS, enableRecordingCheckBox.isSelected() );
			}
		} );
	}

	public boolean beforeSubmit( Submit submit, SubmitContext context )
	{
		return true;
	}

	public void afterSubmit( Submit submit, SubmitContext context )
	{
		HttpResponse response = ( HttpResponse )submit.getResponse();
		if( response != null && enableRecordingCheckBox.isSelected() )
		{
			extractRepresentation( response );
		}
	}

	@SuppressWarnings( "unchecked" )
	protected void extractRepresentation( HttpResponse response )
	{
		String responseContentType = response.getRequestHeaders().get( "Content-Type" );
		if( StringUtils.isNullOrEmpty( responseContentType ) )
			return;
		responseContentType = responseContentType.split( ";" )[0].trim();

		RestRepresentation[] representations = getMethod().getRepresentations( RestRepresentation.Type.REQUEST, null );
		int c = 0;

		for( ; c < representations.length; c++ )
		{
			String repMediaType = representations[c].getMediaType();

			if( responseContentType.equals( repMediaType ) )
			{
				break;
			}
		}

		if( c == representations.length )
		{
			RestRepresentation representation = getMethod().addNewRepresentation( RestRepresentation.Type.REQUEST );
			representation.setMediaType( responseContentType );

			String xmlContent = response.getContentAsXml();

			if( !xmlContent.equals( "<xml/>" ) )
			{
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