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

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import javax.swing.JComponent;

import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.w3c.dom.Document;

import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.binding.PresentationModel;

public class AddTimestampEntry extends WssEntryBase
{
	public static final String TYPE = "Timestamp";

	private int timeToLive;
	private boolean strictTimestamp;

	public void init( WSSEntryConfig config, OutgoingWss container )
	{
		super.init( config, container, TYPE );
	}

	@Override
	protected JComponent buildUI()
	{
		SimpleBindingForm form = new SimpleBindingForm( new PresentationModel<AddTimestampEntry>( this ) );
		form.addSpace( 5 );
		form.appendTextField( "timeToLive", "Time To Live", "Sets the TimeToLive value for the Timestamp Token" );
		form.appendCheckBox( "strictTimestamp", "Millisecond Precision", "Sets precision of timestamp to milliseconds" );

		return form.getPanel();
	}

	@Override
	protected void load( XmlObjectConfigurationReader reader )
	{
		timeToLive = reader.readInt( "timeToLive", 0 );
		strictTimestamp = reader.readBoolean( "strictTimestamp", true );
	}

	@Override
	protected void save( XmlObjectConfigurationBuilder builder )
	{
		builder.add( "timeToLive", timeToLive );
		builder.add( "strictTimestamp", strictTimestamp );
	}

	public void process( WSSecHeader secHeader, Document doc, PropertyExpansionContext context )
	{
		if( timeToLive <= 0 )
			return;

		WSSecTimestamp timestamp = new WSSecTimestamp();
		timestamp.setTimeToLive( timeToLive );

		if( !strictTimestamp )
		{
			WSSConfig wsc = WSSConfig.getNewInstance();
			wsc.setPrecisionInMilliSeconds( false );
			wsc.setTimeStampStrict( false );
			timestamp.setWsConfig( wsc );
		}

		timestamp.build( doc, secHeader );
	}

	public String getTimeToLive()
	{
		return String.valueOf( timeToLive );
	}

	public boolean isStrictTimestamp()
	{
		return strictTimestamp;
	}

	public void setStrictTimestamp( boolean strictTimestamp )
	{
		this.strictTimestamp = strictTimestamp;
		saveConfig();
	}

	public void setTimeToLive( String timeToLive )
	{
		try
		{
			this.timeToLive = Integer.valueOf( timeToLive );
			saveConfig();
		}
		catch( Exception e )
		{
		}
	}
}
