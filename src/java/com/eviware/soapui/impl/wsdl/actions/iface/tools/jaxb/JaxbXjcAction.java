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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.jaxb;

import java.io.File;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * Generates JAXB classes for given interface
 * 
 * @author Ole.Matzura
 */

public class JaxbXjcAction extends AbstractToolsAction<Interface>
{
	private final static String PACKAGE = "package";
	private final static String OUTPUT = "output";
	private final static String NOVALIDATION = "no validation";
	private final static String BINDINGS = "binding files";
	private final static String CLASSPATH = "classpath";
	private final static String CATALOG = "catalog";
	private final static String HTTPPROXY = "http proxy";
	private final static String READONLY = "read-only";
	private final static String NPA = "npa";
	private final static String VERBOSE = "verbose";
	public static final String SOAPUI_ACTION_ID = "JaxbXjcAction";

	// Configure the behavior of this action:
	private String output = null;

	public JaxbXjcAction()
	{
		super( "JAXB 2.0 Artifacts", "Generates JAXB artifacts" );
	}

	@Override
	public boolean applies( Interface target )
	{
		Interface iface = ( Interface )target;
		return !iface.getProject().hasNature( Project.JBOSSWS_NATURE_ID );
	}

	/**
	 * Set this to predefine the output directory instead of letting the user
	 * select.
	 */
	public void setOutput( String output )
	{
		this.output = output;
	}

	protected StringToStringMap initValues( Interface modelItem, Object param )
	{
		StringToStringMap values = super.initValues( modelItem, param );

		if( output != null )
			values.put( OUTPUT, output );

		return values;
	}

	protected XFormDialog buildDialog( Interface modelItem )
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "JAXB Artifacts" );

		XForm mainForm = builder.createForm( "Basic" );
		addWSDLFields( mainForm, modelItem );

		mainForm.addTextField( OUTPUT, "generated files will go into this directory", XForm.FieldType.PROJECT_FOLDER );
		mainForm.addTextField( PACKAGE, "the target package", XForm.FieldType.JAVA_PACKAGE );

		mainForm.addTextField( BINDINGS, "external bindings file(s), comma-separated", XForm.FieldType.PROJECT_FILE );
		mainForm.addTextField( CATALOG, "catalog files to resolve external entity references",
				XForm.FieldType.PROJECT_FILE );
		mainForm.addTextField( CLASSPATH, "where to find user class files", XForm.FieldType.PROJECT_FOLDER );

		mainForm.addTextField( HTTPPROXY, "set HTTP/HTTPS proxy. Format is [user[:password]@]proxyHost[:proxyPort]",
				XForm.FieldType.TEXT );
		mainForm.addCheckBox( READONLY, "(generated files will be in read-only mode)" );
		mainForm.addCheckBox( NOVALIDATION, "(do not resolve strict validation of the input schema(s))" );
		mainForm.addCheckBox( NPA, "(suppress generation of package level annotations (**/package-info.java))" );

		mainForm.addCheckBox( VERBOSE, "(be extra verbose)" );

		buildArgsForm( builder, false, "xjc" );

		return builder.buildDialog( buildDefaultActions( HelpUrls.JABXJC_HELP_URL, modelItem ),
				"Specify arguments for the JAXB 2 xjc compiler", UISupport.TOOL_ICON );
	}

	protected void generate( StringToStringMap values, ToolHost toolHost, Interface modelItem ) throws Exception
	{
		String jaxbDir = SoapUI.getSettings().getString( ToolsSettings.JAXB_LOCATION, null );
		if( Tools.isEmpty( jaxbDir ) )
		{
			UISupport.showErrorMessage( "JAXB location must be set in global preferences" );
			return;
		}

		ProcessBuilder builder = new ProcessBuilder();
		ArgumentBuilder argumentBuilder = buildArgs( modelItem );
		builder.command( argumentBuilder.getArgs() );
		builder.directory( new File( jaxbDir + File.separatorChar + "bin" ) );

		toolHost.run( new ProcessToolRunner( builder, "JAXB xjc", modelItem, argumentBuilder ) );
	}

	private ArgumentBuilder buildArgs( Interface modelItem )
	{
		StringToStringMap values = dialog.getValues();
		ArgumentBuilder builder = new ArgumentBuilder( values );

		builder.startScript( "xjc", ".bat", ".sh" );

		String outputValue = ( output != null ? output : values.get( OUTPUT ) );
		values.put( OUTPUT, Tools.ensureDir( outputValue, "" ) );

		builder.addString( OUTPUT, "-d" );
		builder.addString( PACKAGE, "-p" );
		builder.addString( CLASSPATH, "-classpath" );
		builder.addString( CATALOG, "-catalog" );
		builder.addString( HTTPPROXY, "-httpproxy " );

		builder.addBoolean( NOVALIDATION, "-nv" );
		builder.addBoolean( NPA, "-npa" );
		builder.addBoolean( READONLY, "-readOnly" );
		builder.addBoolean( VERBOSE, "-verbose" );

		addToolArgs( values, builder );

		builder.addArgs( "-wsdl", getWsdlUrl( values, modelItem ) );

		String[] bindings = values.get( BINDINGS ).split( "," );
		for( String binding : bindings )
		{
			if( binding.trim().length() > 0 )
				builder.addArgs( "-b", binding.trim() );
		}

		return builder;
	}
}
