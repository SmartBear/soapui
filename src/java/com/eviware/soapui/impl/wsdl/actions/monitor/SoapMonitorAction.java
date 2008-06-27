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

package com.eviware.soapui.impl.wsdl.actions.monitor;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.panels.monitor.SoapMonitorDesktopPanel;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.types.StringList;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class SoapMonitorAction extends AbstractSoapUIAction<WsdlProject>
{
	private static final String CREATE_TCP_TUNNEL = "Create TCP Tunnel";
	private static final String CREATE_HTTP_PROXY = "Create HTTP Proxy";
	private XFormDialog dialog;

	public SoapMonitorAction()
	{
		super( "Launch SOAP Monitor", "Launches a SOAP traffic monitor for this project" );
	}

	public void perform( WsdlProject target, Object param )
	{
		if( target.getInterfaceCount() == 0 )
		{
			UISupport.showErrorMessage( "Missing interfaces to monitor" );
			return;
		}
		
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( LaunchForm.class );
			dialog.getFormField( LaunchForm.MODE ).addFormFieldListener( new XFormFieldListener() {

				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					dialog.getFormField( LaunchForm.TARGET_HOST ).setEnabled( !newValue.equals( CREATE_HTTP_PROXY ) );
					dialog.getFormField( LaunchForm.ADD_ENDPOINT ).setEnabled( !newValue.equals( CREATE_HTTP_PROXY ) );
				}});
			
			dialog.setBooleanValue( LaunchForm.ADD_ENDPOINT, true );
		}

		Settings settings = target.getSettings();
		
		StringList endpoints = new StringList();
		endpoints.add( null );
		
		for( Interface iface : target.getInterfaceList())
		{
			if( iface.getInterfaceType().equals(WsdlInterfaceFactory.WSDL_TYPE))
				endpoints.addAll( iface.getEndpoints() );
		}
		
		dialog.setOptions( LaunchForm.TARGET_HOST, endpoints.toStringArray() );
		
		dialog.setIntValue( LaunchForm.PORT, ( int ) settings.getLong( LaunchForm.PORT, 8081 ));
		dialog.setValue( LaunchForm.TARGET_HOST, settings.getString( LaunchForm.TARGET_HOST, "" ));
		String launchMode = settings.getString( LaunchForm.MODE, CREATE_TCP_TUNNEL );
		dialog.setValue( LaunchForm.MODE, launchMode);

		dialog.getFormField( LaunchForm.TARGET_HOST ).setEnabled( !launchMode.equals( CREATE_HTTP_PROXY ) );
		dialog.getFormField( LaunchForm.ADD_ENDPOINT ).setEnabled( !launchMode.equals( CREATE_HTTP_PROXY ) );

		dialog.setOptions( LaunchForm.REQUEST_WSS, 
					StringUtils.merge( target.getWssContainer().getIncomingWssNames(), "<none>" ) );
		dialog.setOptions( LaunchForm.RESPONSE_WSS, 
					StringUtils.merge( target.getWssContainer().getIncomingWssNames(), "<none>" ) );
		
		if( dialog.show())
		{
			int listenPort = dialog.getIntValue( LaunchForm.PORT, 8080 );
			settings.setLong( LaunchForm.PORT, listenPort );
			String targetHost = dialog.getValue( LaunchForm.TARGET_HOST );
			settings.setString( LaunchForm.TARGET_HOST, targetHost);
			settings.setString( LaunchForm.MODE, dialog.getValue( LaunchForm.MODE ));
			
			openSoapMonitor( target, listenPort, targetHost, dialog.getBooleanValue( LaunchForm.ADD_ENDPOINT ), 
						dialog.getValue( LaunchForm.MODE ).equals( CREATE_HTTP_PROXY ), 
						dialog.getValue( LaunchForm.REQUEST_WSS),
						dialog.getValue( LaunchForm.RESPONSE_WSS ));
		}
	}

	protected void openSoapMonitor( WsdlProject target, int listenPort, String targetHost, boolean addEndpoint, 
				boolean isProxy, String incomingRequestWss, String incomingResponseWss )
	{
		UISupport.showDesktopPanel( new SoapMonitorDesktopPanel( target, 
					targetHost, 
					listenPort, addEndpoint, isProxy, incomingRequestWss, incomingResponseWss	) );
	}
	
	@AForm(description = "Specify SOAP Monitor settings", name = "Launch SOAP Monitor" )
	private interface LaunchForm
	{
		@AField(description = "The local port to listen on", name = "Port", type=AFieldType.INT )
		public final static String PORT = "Port";
		
		@AField(description = "Specifies monitor mode", name = "Mode", type=AFieldType.RADIOGROUP,
					values= {CREATE_TCP_TUNNEL, CREATE_HTTP_PROXY})
		public final static String MODE = "Mode";
		
		@AField(description = "The target host to invoke", name = "Target Host", type=AFieldType.ENUMERATION )
		public final static String TARGET_HOST = "Target Host";
		
		@AField(description = "Adds an endpoint for the Tcp Tunnel", name = "Add Endpoint", type=AFieldType.BOOLEAN )
		public final static String ADD_ENDPOINT = "Add Endpoint";
		
		@AField(description = "The Incoming WSS configuration to use for processing requests", name = "Incoming Request WSS", type=AFieldType.ENUMERATION )
		public final static String REQUEST_WSS = "Incoming Request WSS";

		@AField(description = "The Outgoing WSS configuration to use for processing responses", name = "Incoming Response WSS", type=AFieldType.ENUMERATION )
		public final static String RESPONSE_WSS = "Incoming Response WSS";
	}
}
