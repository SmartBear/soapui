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

package com.eviware.soapui;

import com.eviware.soapui.config.SoapuiSettingsDocumentConfig;
import com.eviware.soapui.impl.rest.panels.request.inspectors.representations.RestRepresentationsInspectorFactory;
import com.eviware.soapui.impl.rest.panels.request.inspectors.schema.InferredSchemaInspectorFactory;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SwingToolHost;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.inspectors.amfheader.AMFHeadersInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.attachments.AttachmentsInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.aut.AuthInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.httpheaders.HttpHeadersInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.jms.header.JMSHeaderInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.jms.property.JMSHeaderAndPropertyInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.jms.property.JMSPropertyInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.script.ScriptInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.ssl.SSLInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.wsa.WsaInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.wsrm.WsrmInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.wss.WssInspectorFactory;
import com.eviware.soapui.support.editor.registry.EditorViewFactory;
import com.eviware.soapui.support.editor.registry.EditorViewFactoryRegistry;
import com.eviware.soapui.support.editor.registry.InspectorFactory;
import com.eviware.soapui.support.editor.registry.InspectorRegistry;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.SwingFormFactory;

import java.io.File;
import java.io.FileInputStream;

public class SwingSoapUICore extends DefaultSoapUICore
{
	public SwingSoapUICore()
	{
		super();
	}

	public SwingSoapUICore( String root, String settingsFile )
	{
		super( root, settingsFile );
	}

	public SwingSoapUICore( boolean settingPassword, String soapUISettingsPassword )
	{
		super( settingPassword, soapUISettingsPassword );
	}

	public void prepareUI()
	{
		UISupport.setToolHost( new SwingToolHost() );
		XFormFactory.Factory.instance = new SwingFormFactory();
	}

	public void afterStartup( Workspace workspace )
	{
		for( EditorViewFactory factory : SoapUI.getFactoryRegistry().getFactories( EditorViewFactory.class ) )
		{
			EditorViewFactoryRegistry.getInstance().addFactory( factory );
		}

		InspectorRegistry inspectorRegistry = InspectorRegistry.getInstance();
		inspectorRegistry.addFactory( new ScriptInspectorFactory() );
		inspectorRegistry.addFactory( new AuthInspectorFactory() );
		inspectorRegistry.addFactory( new HttpHeadersInspectorFactory() );
		inspectorRegistry.addFactory( new AttachmentsInspectorFactory() );
		inspectorRegistry.addFactory( new SSLInspectorFactory() );
		inspectorRegistry.addFactory( new WssInspectorFactory() );
		inspectorRegistry.addFactory( new WsaInspectorFactory() );
		inspectorRegistry.addFactory( new WsrmInspectorFactory() );
		// inspectorRegistry.addFactory( new WsrmPiggybackInspectorFactory());
		inspectorRegistry.addFactory( new RestRepresentationsInspectorFactory() );
		inspectorRegistry.addFactory( new InferredSchemaInspectorFactory() );
		inspectorRegistry.addFactory( new JMSHeaderInspectorFactory() );
		inspectorRegistry.addFactory( new JMSPropertyInspectorFactory() );
		inspectorRegistry.addFactory( new JMSHeaderAndPropertyInspectorFactory() );
		inspectorRegistry.addFactory( new AMFHeadersInspectorFactory() );

		for( InspectorFactory factory : SoapUI.getFactoryRegistry().getFactories( InspectorFactory.class ) )
		{
			inspectorRegistry.addFactory( factory );
		}

		String actionsDir = System.getProperty( "soapui.ext.actions" );
		addExternalActions( actionsDir == null ? getRoot() == null ? "actions" : getRoot() + File.separatorChar
				+ "actions" : actionsDir, getExtensionClassLoader() );
	}

	@Override
	protected Settings initSettings( String fileName )
	{
		String fn = fileName;

		if( !new File( fileName ).exists() )
		{
			try
			{
				fileName = importSettingsOnStartup( fileName );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		Settings result = super.initSettings( fileName );

		if( !fileName.equals( fn ) )
			setSettingsFile( fn );

		return result;
	}

	protected String importSettingsOnStartup( String fileName ) throws Exception
	{
		if( UISupport.getDialogs().confirm( "Missing SoapUI Settings, import from existing installation?",
				"Import Preferences" ) )
		{
			while( true )
			{
				File settingsFile = UISupport.getFileDialogs().open( null, "Import Preferences", ".xml",
						"SoapUI settings XML", fileName );
				if( settingsFile != null )
				{
					try
					{
						SoapuiSettingsDocumentConfig.Factory.parse( settingsFile );
						log.info( "imported soapui-settings from [" + settingsFile.getAbsolutePath() + "]" );
						return settingsFile.getAbsolutePath();
					}
					catch( Exception e )
					{
						if( !UISupport.getDialogs().confirm(
								"Error loading settings from [" + settingsFile.getAbsolutePath() + "]\r\nspecify another?",
								"Error Importing" ) )
						{
							break;
						}
					}
				}
			}
		}

		return fileName;
	}

	protected void addExternalActions( String folder, ClassLoader classLoader )
	{
		File[] actionFiles = new File( folder ).listFiles();
		if( actionFiles != null )
		{
			for( File actionFile : actionFiles )
			{
				if( actionFile.isDirectory() )
				{
					addExternalActions( actionFile.getAbsolutePath(), classLoader );
					continue;
				}

				if( !actionFile.getName().toLowerCase().endsWith( "-actions.xml" ) )
					continue;

				try
				{
					log.info( "Adding actions from [" + actionFile.getAbsolutePath() + "]" );

					SoapUI.getActionRegistry().addConfig( new FileInputStream( actionFile ), classLoader );
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}
	}
}
