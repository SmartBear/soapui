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

package com.eviware.soapui.impl.wsdl.actions.monitor;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.panels.monitor.SoapMonitorDesktopPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.ModelSupport;
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
	private static final String HTTPS_PROTOCOL = "https://";
	private static final String HTTP_TUNNEL = "HTTP Tunnel";
	private static final String HTTP_PROXY = "HTTP Proxy";
	private XFormDialog dialog;

	public SoapMonitorAction()
	{
		super( "Launch SOAP Monitor", "Launches a SOAP traffic monitor for this project" );
	}

	public void perform( WsdlProject project, Object param )
	{
		if( project.getInterfaceCount() == 0 )
		{
			UISupport.showErrorMessage( "Missing interfaces to monitor" );
			return;
		}

		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( LaunchForm.class );
		}

		Settings settings = project.getSettings();

		StringList endpoints = new StringList();
		endpoints.add( null );

		for( Interface iface : ModelSupport.getChildren( project, WsdlInterface.class ) )
		{
			endpoints.addAll( iface.getEndpoints() );
		}

		dialog.setIntValue( LaunchForm.PORT, ( int )settings.getLong( LaunchForm.PORT, 8081 ) );
		dialog.setOptions( LaunchForm.REQUEST_WSS, StringUtils.merge( project.getWssContainer().getIncomingWssNames(),
				"<none>" ) );
		dialog.setOptions( LaunchForm.RESPONSE_WSS, StringUtils.merge( project.getWssContainer().getIncomingWssNames(),
				"<none>" ) );
		dialog.setValue( LaunchForm.SETSSLMON, settings.getString( LaunchForm.SETSSLMON, "" ).length() > 0 ? settings
				.getString( LaunchForm.SETSSLMON, "" ) : HTTPS_PROTOCOL );
		dialog.setOptions( LaunchForm.SSLORHTTP, new String[] { HTTP_TUNNEL, HTTP_PROXY } );

		dialog.setValue( LaunchForm.SSLTUNNEL_KEYSTORE, settings.getString( LaunchForm.SSLTUNNEL_KEYSTORE, "" ) );
		dialog.setValue( LaunchForm.SSLTUNNEL_PASSWORD, settings.getString( LaunchForm.SSLTUNNEL_PASSWORD, "" ) );
		dialog.setValue( LaunchForm.SSLTUNNEL_KEYPASSWORD, settings.getString( LaunchForm.SSLTUNNEL_KEYPASSWORD, "" ) );
		dialog.setValue( LaunchForm.SSLTUNNEL_TRUSTSTORE, settings.getString( LaunchForm.SSLTUNNEL_TRUSTSTORE, "" ) );
		dialog.setValue( LaunchForm.SSLTUNNEL_TRUSTSTORE_PASSWORD, settings.getString(
				LaunchForm.SSLTUNNEL_TRUSTSTORE_PASSWORD, "" ) );
		dialog.setBooleanValue( LaunchForm.SSLTUNNEL_REUSESTATE, settings.getBoolean( LaunchForm.SSLTUNNEL_REUSESTATE ) );
		dialog.setValue( LaunchForm.SSLTUNNEL_KEYSTOREPATH, settings.getString( LaunchForm.SSLTUNNEL_KEYSTOREPATH, "" ) );
		dialog.setValue( LaunchForm.SSLTUNNEL_KEYSTOREPASSWORD, settings.getString(
				LaunchForm.SSLTUNNEL_KEYSTOREPASSWORD, "" ) );

		XFormField sslOrHttp = dialog.getFormField( LaunchForm.SSLORHTTP );
		sslOrHttp.setValue( HTTP_PROXY );
		setDialogState( HTTP_PROXY );
		sslOrHttp.addFormFieldListener( new XFormFieldListener()
		{

			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				setDialogState( newValue );
			}

		} );

		if( dialog.show() )
		{
			try
			{
				UISupport.setHourglassCursor();

				int listenPort = dialog.getIntValue( LaunchForm.PORT, 8080 );
				settings.setLong( LaunchForm.PORT, listenPort );

				settings.setString( LaunchForm.SETSSLMON, dialog.getValue( LaunchForm.SETSSLMON ) );

				settings.setString( LaunchForm.SSLTUNNEL_KEYSTORE, dialog.getValue( LaunchForm.SSLTUNNEL_KEYSTORE ) );
				settings.setString( LaunchForm.SSLTUNNEL_PASSWORD, dialog.getValue( LaunchForm.SSLTUNNEL_PASSWORD ) );
				settings.setString( LaunchForm.SSLTUNNEL_KEYPASSWORD, dialog.getValue( LaunchForm.SSLTUNNEL_KEYPASSWORD ) );
				settings.setString( LaunchForm.SSLTUNNEL_TRUSTSTORE, dialog.getValue( LaunchForm.SSLTUNNEL_TRUSTSTORE ) );
				settings.setString( LaunchForm.SSLTUNNEL_TRUSTSTORE_PASSWORD, dialog
						.getValue( LaunchForm.SSLTUNNEL_TRUSTSTORE_PASSWORD ) );
				settings.setString( LaunchForm.SSLTUNNEL_REUSESTATE, dialog.getValue( LaunchForm.SSLTUNNEL_REUSESTATE ) );
				settings
						.setString( LaunchForm.SSLTUNNEL_KEYSTOREPATH, dialog.getValue( LaunchForm.SSLTUNNEL_KEYSTOREPATH ) );
				settings.setString( LaunchForm.SSLTUNNEL_KEYSTOREPASSWORD, dialog
						.getValue( LaunchForm.SSLTUNNEL_KEYSTOREPASSWORD ) );

				// load all interfaces in project
				for( Interface iface : project.getInterfaceList() )
				{
					iface.getDefinitionContext().loadIfNecessary();
				}

				if( HTTP_PROXY.equals( dialog.getValue( LaunchForm.SSLORHTTP ) ) )
				{
					openSoapMonitor( project, listenPort, dialog.getValue( LaunchForm.REQUEST_WSS ), dialog
							.getValue( LaunchForm.RESPONSE_WSS ), dialog.getBooleanValue( LaunchForm.SETASPROXY ), null );
				}
				else
				{
					openSoapMonitor( project, listenPort, dialog.getValue( LaunchForm.REQUEST_WSS ), dialog
							.getValue( LaunchForm.RESPONSE_WSS ), dialog.getBooleanValue( LaunchForm.SETASPROXY ), dialog
							.getValue( LaunchForm.SETSSLMON ) );
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
			finally
			{
				UISupport.resetCursor();
			}
		}
	}

	protected void openSoapMonitor( WsdlProject target, int listenPort, String incomingRequestWss,
			String incomingResponseWss, boolean setAsProxy, String sslEndpoint )
	{
		if( sslEndpoint == null )
		{
			UISupport.showDesktopPanel( new SoapMonitorDesktopPanel( target, listenPort, incomingRequestWss,
					incomingResponseWss, setAsProxy, null ) );
		}
		else
		{
			String ssl = validate( sslEndpoint );
			if( ssl == null )
			{
				UISupport.showErrorMessage( "SSL Monitor needs endpoint." );
			}
			else
			{
				UISupport.showDesktopPanel( new SoapMonitorDesktopPanel( target, listenPort, incomingRequestWss,
						incomingResponseWss, setAsProxy, ssl ) );
			}
		}
	}

	protected String validate( String sslEndpoint )
	{
		String res = sslEndpoint;
		if( res.trim().length() > 0 )
		{
			return res.trim();
		}
		return null;
	}

	private void setDialogState( String newValue )
	{
		if( HTTP_PROXY.equals( newValue ) )
		{
			dialog.getFormField( LaunchForm.SETSSLMON ).setEnabled( false );
			dialog.getFormField( LaunchForm.SSLTUNNEL_KEYSTORE ).setEnabled( false );
			dialog.getFormField( LaunchForm.SSLTUNNEL_PASSWORD ).setEnabled( false );
			dialog.getFormField( LaunchForm.SSLTUNNEL_KEYPASSWORD ).setEnabled( false );
			dialog.getFormField( LaunchForm.SSLTUNNEL_TRUSTSTORE ).setEnabled( false );
			dialog.getFormField( LaunchForm.SSLTUNNEL_TRUSTSTORE_PASSWORD ).setEnabled( false );
			dialog.getFormField( LaunchForm.SSLTUNNEL_REUSESTATE ).setEnabled( false );
			dialog.getFormField( LaunchForm.SSLTUNNEL_KEYSTOREPATH ).setEnabled( false );
			dialog.getFormField( LaunchForm.SSLTUNNEL_KEYSTOREPASSWORD ).setEnabled( false );

			dialog.getFormField( LaunchForm.SETASPROXY ).setEnabled( true );
			dialog.getFormField( LaunchForm.REQUEST_WSS ).setEnabled( true );
			dialog.getFormField( LaunchForm.RESPONSE_WSS ).setEnabled( true );
		}
		else
		{
			dialog.getFormField( LaunchForm.SETSSLMON ).setEnabled( true );
			dialog.getFormField( LaunchForm.SSLTUNNEL_KEYSTORE ).setEnabled( true );
			dialog.getFormField( LaunchForm.SSLTUNNEL_PASSWORD ).setEnabled( true );
			dialog.getFormField( LaunchForm.SSLTUNNEL_KEYPASSWORD ).setEnabled( true );
			dialog.getFormField( LaunchForm.SSLTUNNEL_TRUSTSTORE ).setEnabled( true );
			dialog.getFormField( LaunchForm.SSLTUNNEL_TRUSTSTORE_PASSWORD ).setEnabled( true );
			dialog.getFormField( LaunchForm.SSLTUNNEL_REUSESTATE ).setEnabled( true );
			dialog.getFormField( LaunchForm.SSLTUNNEL_KEYSTOREPATH ).setEnabled( true );
			dialog.getFormField( LaunchForm.SSLTUNNEL_KEYSTOREPASSWORD ).setEnabled( true );

			dialog.getFormField( LaunchForm.SETASPROXY ).setEnabled( false );
			dialog.getFormField( LaunchForm.REQUEST_WSS ).setEnabled( false );
			dialog.getFormField( LaunchForm.RESPONSE_WSS ).setEnabled( false );
		}
	}

	@AForm( description = "Specify SOAP Monitor settings", name = "Launch SOAP Monitor", helpUrl = HelpUrls.SOAPMONITOR_HELP_URL )
	public interface LaunchForm
	{
		@AField( description = "SSL tunnel or HTTP proxy", name = "Choose one:", type = AFieldType.RADIOGROUP )
		public final static String SSLORHTTP = "Choose one:";

		@AField( description = "The local port to listen on", name = "Port", type = AFieldType.INT )
		public final static String PORT = "Port";

		@AField( description = "The Incoming WSS configuration to use for processing requests", name = "Incoming Request WSS", type = AFieldType.ENUMERATION )
		public final static String REQUEST_WSS = "Incoming Request WSS";

		@AField( description = "The Outgoing WSS configuration to use for processing responses", name = "Incoming Response WSS", type = AFieldType.ENUMERATION )
		public final static String RESPONSE_WSS = "Incoming Response WSS";

		@AField( description = "Set as Global Proxy", name = "Set as Proxy", type = AFieldType.BOOLEAN )
		public final static String SETASPROXY = "Set as Proxy";

		@AField( description = "Set endpoint", name = "Set endpoint for HTTP Tunnel:", type = AFieldType.STRING )
		public final static String SETSSLMON = "Set endpoint for HTTP Tunnel:";

		@AField( description = "Set SSL Tunnel KeyStore", name = "HTTP tunnel - KeyStore", type = AFieldType.STRING )
		public final static String SSLTUNNEL_KEYSTORE = "HTTP tunnel - KeyStore";

		@AField( description = "Set SSL Tunnel Password", name = "HTTP tunnel - Password", type = AFieldType.PASSWORD )
		public final static String SSLTUNNEL_PASSWORD = "HTTP tunnel - Password";

		@AField( description = "Set SSL Tunnel KeyPassword", name = "HTTP tunnel - KeyPassword", type = AFieldType.PASSWORD )
		public final static String SSLTUNNEL_KEYPASSWORD = "HTTP tunnel - KeyPassword";

		@AField( description = "Set SSL Tunnel TrustStore", name = "HTTP tunnel - TrustStore", type = AFieldType.STRING )
		public final static String SSLTUNNEL_TRUSTSTORE = "HTTP tunnel - TrustStore";

		@AField( description = "Set SSL Tunnel TrustStore Password", name = "HTTP tunnel - TrustStore Password", type = AFieldType.PASSWORD )
		public final static String SSLTUNNEL_TRUSTSTORE_PASSWORD = "HTTP tunnel - TrustStore Password";

		@AField( description = "Keep request state", name = "Reuse request state", type = AFieldType.BOOLEAN )
		public final static String SSLTUNNEL_REUSESTATE = "Reuse request state";

		@AField( description = "Set SSL Client Key Store", name = "HTTP tunnel - Set SSL Client Key Store path", type = AFieldType.STRING )
		public final static String SSLTUNNEL_KEYSTOREPATH = "HTTP tunnel - Set SSL Client Key Store path";

		@AField( description = "Set SSL Client Key Store Password", name = "HTTP tunnel - Set SSL Client Key Store Password", type = AFieldType.PASSWORD )
		public final static String SSLTUNNEL_KEYSTOREPASSWORD = "HTTP tunnel - Set SSL Client Key Store Password";
	}
}
