/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.actions.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.definition.export.WadlDefinitionExporter;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

public class CreateWadlDocumentationAction extends AbstractSoapUIAction<RestService>
{
	public static final String SOAPUI_ACTION_ID = "CreateWadlDocumentationAction";

	private static final String REPORT_DIRECTORY_SETTING = CreateWadlDocumentationAction.class.getSimpleName()
			+ "@report-directory";
	private XFormDialog dialog;
	private static Map<String, Transformer> transformers;

	public CreateWadlDocumentationAction()
	{
		super( "CreateWadlDocumentationAction", "Create Documentation",
				"Generate simple HTML Documentation for this WADL" );
	}

	public void perform( RestService target, Object param )
	{
		try
		{
			if( dialog == null )
			{
				dialog = ADialogBuilder.buildDialog( Form.class );
			}

			Settings settings = target.getSettings();
			dialog.setValue( Form.OUTPUT_FOLDER, settings.getString( REPORT_DIRECTORY_SETTING, "" ) );

			if( !dialog.show() )
			{
				return;
			}

			settings.setString( REPORT_DIRECTORY_SETTING, dialog.getValue( Form.OUTPUT_FOLDER ) );

			final File reportDirectory = new File( settings.getString( REPORT_DIRECTORY_SETTING, "" ) );
			String reportDirAbsolutePath = reportDirectory.getAbsolutePath();
			String filename = reportDirAbsolutePath + File.separatorChar + "report.xml";
			String reportUrl = transform( target, reportDirAbsolutePath, filename );
			Tools.openURL( reportUrl );
		}
		catch( Exception e )
		{
			UISupport.showErrorMessage( e );
		}
	}

	private static String transform( RestService target, final String reportDirAbsolutePath, String filename )
			throws Exception
	{
		if( transformers == null )
		{
			initTransformers();
		}

		Transformer transformer = transformers.get( "WADL" );
		if( transformer == null )
		{
			throw new Exception( "Missing transformer for format [" + target + "]" );
		}

		transformer.setParameter( "output.dir", reportDirAbsolutePath );

		String reportFile = reportDirAbsolutePath + File.separatorChar + "wadl-report.html";
		StreamResult result = new StreamResult( new FileWriter( reportFile ) );

		WadlDefinitionExporter exporter = new WadlDefinitionExporter( target );
		String infile = exporter.export( reportDirAbsolutePath );

		transformer.setURIResolver( new FileUriResolver( reportDirAbsolutePath ) );
		transformer.transform( new StreamSource( new FileReader( infile ) ), result );

		String reportUrl = new File( reportFile ).toURI().toURL().toString();
		return reportUrl;
	}

	protected static void initTransformers() throws Exception
	{
		transformers = new HashMap<String, Transformer>();
		TransformerFactory xformFactory = new org.apache.xalan.processor.TransformerFactoryImpl();

		transformers.put(
				"WADL",
				xformFactory.newTemplates(
						new StreamSource( SoapUI.class
								.getResourceAsStream( "/com/eviware/soapui/resources/doc/wadl_documentation.xsl" ) ) )
						.newTransformer() );
	}

	@AForm( description = "Creates an HTML-Report for the current WADL", name = "Create Report", helpUrl = HelpUrls.CREATEWADLDOC_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( name = "Output Folder", description = "The folder where to create the report", type = AFieldType.FOLDER )
		public final static String OUTPUT_FOLDER = "Output Folder";
	}

	public static class FileUriResolver implements URIResolver
	{
		private final String basePath;

		public FileUriResolver( String basePath )
		{
			this.basePath = basePath;
		}

		public Source resolve( String href, String base ) throws TransformerException
		{
			try
			{
				if( PathUtils.isHttpPath( href ) )
					return new StreamSource( new URL( href ).openStream() );

				File file = PathUtils.isAbsolutePath( href ) ? new File( href ) : new File( basePath, href );
				FileReader reader = new FileReader( file );
				return new StreamSource( reader );
			}
			catch( Exception e )
			{
				return null;
			}
		}
	}
}
