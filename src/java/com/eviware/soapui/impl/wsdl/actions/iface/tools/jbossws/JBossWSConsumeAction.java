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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.jbossws;

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
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * Invokes JBossWS-specific JAX-WS WSImport
 * 
 * @author Ole.Matzura
 */

public class JBossWSConsumeAction extends AbstractToolsAction<Interface>
{
	private static final String PACKAGE = "Package";
	private static final String OUTPUT = "Target Directory";
	private static final String SOURCE_OUTPUT = "Source Directory";
	private static final String WSDLLOCATION = "wsdlLocation";
	private static final String BINDING_FILES = "Binding files";
	private static final String KEEP = "Keep";
	private static final String CATALOG = "Catalog";
	private static final String STACKTRACE = "Stacktrace";
	public static final String SOAPUI_ACTION_ID = "JBossWSConsumeAction";

	public JBossWSConsumeAction()
	{
		super( "JBossWS JAX-WS Artifacts", "Generates JAX-WS artifacts using JBossWS wsconsume" );
	}

	protected XFormDialog buildDialog( Interface modelItem )
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "JBossWS JAX-WS Artifacts" );

		XForm mainForm = builder.createForm( "Basic" );
		addWSDLFields( mainForm, modelItem );

		mainForm.addTextField( OUTPUT, "target directory for generated files.", XForm.FieldType.PROJECT_FOLDER );
		mainForm.addTextField( PACKAGE, "target package nam", XForm.FieldType.JAVA_PACKAGE );
		mainForm.addTextField( SOURCE_OUTPUT, "target directory for generated source files",
				XForm.FieldType.PROJECT_FOLDER );
		mainForm.addTextField( CATALOG, "catalog file to resolve external entity references",
				XForm.FieldType.PROJECT_FILE );
		mainForm.addTextField( BINDING_FILES, "comma-separated list of external JAX-WS or JAXB binding files",
				XForm.FieldType.TEXT );
		mainForm.addTextField( WSDLLOCATION, "@WebService.wsdlLocation and @WebServiceClient.wsdlLocation value",
				XForm.FieldType.TEXT );

		mainForm.addCheckBox( KEEP, "(keep generated files)" );
		mainForm.addCheckBox( STACKTRACE, "(show stacktrace for errors)" );

		buildArgsForm( builder, false, "wsconsume" );

		return builder.buildDialog( buildDefaultActions( HelpUrls.JBOSSWS_WSCONSUME_HELP_URL, modelItem ),
				"Specify arguments for JBossWS wsconsume", UISupport.TOOL_ICON );
	}

	protected void generate( StringToStringMap values, ToolHost toolHost, Interface modelItem ) throws Exception
	{
		String wsimportDir = SoapUI.getSettings().getString( ToolsSettings.JBOSSWS_WSTOOLS_LOCATION, null );
		if( Tools.isEmpty( wsimportDir ) )
		{
			UISupport.showErrorMessage( "JBossWS wstools/wsconsume directory must be set in global preferences" );
			return;
		}

		String wsimportExtension = UISupport.isWindows() ? ".bat" : ".sh";

		File wscompileFile = new File( wsimportDir + File.separatorChar + "wsconsume" + wsimportExtension );
		if( !wscompileFile.exists() )
		{
			UISupport.showErrorMessage( "Could not find wsconsume script at [" + wscompileFile + "]" );
			return;
		}

		ProcessBuilder builder = new ProcessBuilder();
		ArgumentBuilder args = buildArgs( UISupport.isWindows(), modelItem );
		builder.command( args.getArgs() );
		builder.directory( new File( wsimportDir ) );

		toolHost.run( new ProcessToolRunner( builder, "JBossWS wsconsume", modelItem ) );
	}

	private ArgumentBuilder buildArgs( boolean isWindows, Interface modelItem ) throws IOException
	{
		StringToStringMap values = dialog.getValues();
		values.put( OUTPUT, Tools.ensureDir( values.get( OUTPUT ), "" ) );
		values.put( SOURCE_OUTPUT, Tools.ensureDir( values.get( SOURCE_OUTPUT ), values.get( OUTPUT ) ) );

		ArgumentBuilder builder = new ArgumentBuilder( values );
		builder.startScript( "wsconsume" );

		builder.addString( OUTPUT, "-o" );
		builder.addString( CATALOG, "-c" );
		builder.addString( PACKAGE, "-p" );
		builder.addString( SOURCE_OUTPUT, "-s" );
		builder.addString( WSDLLOCATION, "-w" );
		builder.addBoolean( KEEP, "-k" );
		builder.addBoolean( STACKTRACE, "-t" );

		String[] bindingFiles = values.get( BINDING_FILES ).split( "," );
		for( String file : bindingFiles )
		{
			if( file.trim().length() > 0 )
				builder.addArgs( "-b", file.trim() );
		}

		addToolArgs( values, builder );
		builder.addArgs( getWsdlUrl( values, modelItem ) );

		return builder;
	}
}
