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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.oracle;

import java.io.File;
import java.io.IOException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * Invokes oracle genproxy
 * 
 * @author Ole.Matzura
 */

public class OracleWsaGenProxyAction extends AbstractToolsAction<Interface>
{
	private static final String OUTPUT = "Output Directory";
	private static final String PACKAGE = "Destination Package";
	public static final String SOAPUI_ACTION_ID = "OracleWsaGenProxyAction";

	public OracleWsaGenProxyAction()
	{
		super( "Oracle Proxy Artifacts", "Generates Oracle Proxy artifacts using the wsa.jar utility" );
	}

	protected XFormDialog buildDialog( Interface modelItem )
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Oracle Artifacts" );

		XForm mainForm = builder.createForm( "Basic" );
		addWSDLFields( mainForm, modelItem );

		mainForm.addTextField( OUTPUT, "The root directory for all emitted files.", XForm.FieldType.PROJECT_FOLDER );
		mainForm.addTextField( PACKAGE, "The target package for generated classes", XForm.FieldType.JAVA_PACKAGE );

		buildArgsForm( builder, true, "wsa" );

		ActionList actions = buildDefaultActions( HelpUrls.ORACLEWSA_HELP_URL, modelItem );
		return builder.buildDialog( actions, "Specify arguments for Oracle wsa.jar genProxy functionality",
				UISupport.TOOL_ICON );
	}

	protected void generate( StringToStringMap values, ToolHost toolHost, Interface modelItem ) throws Exception
	{
		String wsaDir = SoapUI.getSettings().getString( ToolsSettings.ORACLE_WSA_LOCATION, null );
		if( Tools.isEmpty( wsaDir ) )
		{
			UISupport.showErrorMessage( "wsa.jar directory must be set in global preferences" );
			return;
		}

		File wsaFile = new File( wsaDir + File.separatorChar + "wsa.jar" );
		if( !wsaFile.exists() )
		{
			UISupport.showErrorMessage( "Could not find wsa.jar at [" + wsaFile + "]" );
			return;
		}

		ProcessBuilder builder = new ProcessBuilder();
		ArgumentBuilder args = buildArgs( modelItem );
		builder.command( args.getArgs() );
		builder.directory( new File( wsaDir ) );

		toolHost.run( new ProcessToolRunner( builder, "Oracle wsa.jar", modelItem ) );
	}

	private ArgumentBuilder buildArgs( Interface modelItem ) throws IOException
	{
		StringToStringMap values = dialog.getValues();

		values.put( OUTPUT, Tools.ensureDir( values.get( OUTPUT ), "" ) );

		ArgumentBuilder builder = new ArgumentBuilder( values );
		builder.addArgs( "java", "-jar", "wsa.jar", "-genProxy" );
		addJavaArgs( values, builder );

		builder.addArgs( "-wsdl", getWsdlUrl( values, modelItem ) );
		builder.addString( OUTPUT, "-output" );
		builder.addString( PACKAGE, "-packageName" );

		addToolArgs( values, builder );
		return builder;
	}
}
