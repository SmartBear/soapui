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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.tcpmon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * Invokes Apache TCPmon tool
 * 
 * @author Ole.Matzura
 */

public class TcpMonAction extends AbstractToolsAction<WsdlInterface>
{
	private static final String ENDPOINT = "Endpoint";
	private static final String PORT = "Local Port";
	private static final String ADD_ENDPOINT = "Add local endpoint";
	private XForm mainForm;
	public static final String SOAPUI_ACTION_ID = "TcpMonAction";

	public TcpMonAction()
	{
		super( "Launch TcpMon", "Launch Tcp Mon for monitoring SOAP traffic" );
	}

	protected XFormDialog buildDialog( WsdlInterface modelItem )
	{
		if( modelItem == null )
			return null;

		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Launch TcpMon" );

		mainForm = builder.createForm( "Basic" );
		mainForm.addComboBox( ENDPOINT, new String[] { "" }, "endpoint to forward to" );
		mainForm.addTextField( PORT, "Local port to listen on.", XForm.FieldType.TEXT );
		mainForm.addCheckBox( ADD_ENDPOINT, "adds an endpoint to the interface pointing to the started monitor" );

		return builder.buildDialog( buildDefaultActions( HelpUrls.TCPMON_HELP_URL, modelItem ),
				"Specify arguments for launching TcpMon", UISupport.TOOL_ICON );
	}

	protected Action createRunOption( WsdlInterface modelItem )
	{
		Action action = super.createRunOption( modelItem );
		action.putValue( Action.NAME, "Launch" );
		return action;
	}

	protected StringToStringMap initValues( WsdlInterface modelItem, Object param )
	{
		if( modelItem != null )
		{
			List<String> endpoints = new ArrayList<String>( Arrays.asList( modelItem.getEndpoints() ) );
			endpoints.add( 0, null );
			mainForm.setOptions( ENDPOINT, endpoints.toArray() );
		}
		else if( mainForm != null )
		{
			mainForm.setOptions( ENDPOINT, new String[] { null } );
		}

		StringToStringMap values = super.initValues( modelItem, param );
		if( !values.isEmpty() )
			return values;

		values.put( ENDPOINT, getDefinition( modelItem ) );
		values.put( PORT, "8080" );

		return values;
	}

	protected void generate( StringToStringMap values, ToolHost toolHost, WsdlInterface modelItem ) throws Exception
	{
		String tcpMonDir = SoapUI.getSettings().getString( ToolsSettings.TCPMON_LOCATION, null );
		if( Tools.isEmpty( tcpMonDir ) )
		{
			UISupport.showErrorMessage( "TcpMon directory must be set in global preferences" );
			return;
		}

		ProcessBuilder builder = new ProcessBuilder();
		ArgumentBuilder args = buildArgs( modelItem );
		builder.command( args.getArgs() );
		builder.directory( new File( tcpMonDir + File.separatorChar + "build" ) );

		SoapUI.log( "Launching tcpmon in directory [" + builder.directory() + "] with arguments [" + args.toString()
				+ "]" );

		builder.start();
		closeDialog( modelItem );
	}

	private ArgumentBuilder buildArgs( WsdlInterface modelItem ) throws IOException
	{
		if( dialog == null )
		{
			ArgumentBuilder builder = new ArgumentBuilder( new StringToStringMap() );
			builder.startScript( "tcpmon", ".bat", ".sh" );
			return builder;
		}

		StringToStringMap values = dialog.getValues();

		ArgumentBuilder builder = new ArgumentBuilder( values );
		builder.startScript( "tcpmon", ".bat", ".sh" );

		builder.addArgs( values.get( PORT ) );
		String endpoint = values.get( ENDPOINT );
		if( endpoint != null && !endpoint.equals( "- none available -" ) )
		{
			URL url = new URL( endpoint );
			builder.addArgs( url.getHost() );
			builder.addArgs( ( url.getPort() == -1 ) ? "80" : "" + url.getPort() );

			if( values.getBoolean( ADD_ENDPOINT ) )
			{
				modelItem.addEndpoint( "http://localhost:" + values.get( PORT ) + url.getPath() );
			}
		}

		addToolArgs( values, builder );
		return builder;
	}
}
