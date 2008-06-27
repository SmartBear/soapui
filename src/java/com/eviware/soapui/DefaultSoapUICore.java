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

package com.eviware.soapui;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.eviware.soapui.config.SoapuiSettingsDocumentConfig;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.ClasspathHacker;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;
import com.eviware.soapui.support.types.StringList;

/**
 * Initializes core objects. Transform to a Spring "ApplicationContext"?
 * 
 * @author ole.matzura
 */

public class DefaultSoapUICore implements SoapUICore
{
	public static Logger log; 
	
	private boolean logIsInitialized;
	private String root;
	private SoapuiSettingsDocumentConfig settingsDocument;
	private MockEngine mockEngine;
	private XmlBeansSettingsImpl settings;
	private SoapUIListenerRegistry listenerRegistry;
	private SoapUIActionRegistry actionRegistry;

	private String settingsFile;

	public static DefaultSoapUICore createDefault()
	{
		return new DefaultSoapUICore( null, DEFAULT_SETTINGS_FILE );
	}
	
	public DefaultSoapUICore()
	{
	}

	public DefaultSoapUICore( String root )
	{
		this.root = root;
	}
	
	public DefaultSoapUICore( String root, String settingsFile )
	{
		this( root );
		init( settingsFile );
	}

	public void init( String settingsFile )
	{
		initLog();
		
		SoapUI.setSoapUICore( this );
		
		loadExternalLibraries();
		initSettings( settingsFile == null ? DEFAULT_SETTINGS_FILE : settingsFile );
		initCoreComponents();
		initExtensions( getExtensionClassLoader() );
		
		SoapVersion.Soap11.equals( SoapVersion.Soap12 );
	}

	protected void initExtensions( ClassLoader extensionClassLoader )
	{
		String extDir = System.getProperty("soapui.ext.listeners");
		addExternalListeners( extDir != null ? extDir : 
			root == null ? "listeners" : root + File.separatorChar + "listeners",	extensionClassLoader );
	}

	protected ClassLoader getExtensionClassLoader()
	{
		return SoapUI.class.getClassLoader();
	}

	protected void initCoreComponents()
	{
	}
	
	public String getRoot()
	{
		return root;
	}

	protected Settings initSettings( String fileName )
	{
		try
		{
			File settingsFile = root == null ? new File( fileName ) : new File( new File( root ), fileName );
			if( !settingsFile.exists() )
			{
				if( settingsDocument == null )
				{
					log.info( "Creating new settings at [" + settingsFile.getAbsolutePath() + "]" );
					settingsDocument = SoapuiSettingsDocumentConfig.Factory.newInstance();
				}
			}
			else
			{
				settingsDocument = SoapuiSettingsDocumentConfig.Factory.parse( settingsFile );
				log.info( "initialized soapui-settings from [" + settingsFile.getAbsolutePath() + "]" );
			}
		}
		catch( Exception e )
		{
			log.warn( "Failed to load settings from [" + e.getMessage() + "], creating new" );
			settingsDocument = SoapuiSettingsDocumentConfig.Factory.newInstance();
		}
		
		if( settingsDocument.getSoapuiSettings() == null )
		{
			settingsDocument.addNewSoapuiSettings();
			settings = new XmlBeansSettingsImpl( null, null, settingsDocument.getSoapuiSettings() );

			initDefaultSettings( settings );
		}
		else
		{
			settings = new XmlBeansSettingsImpl( null, null, settingsDocument.getSoapuiSettings() );
		}
		
		this.settingsFile = fileName;
		
		if( !settings.isSet( WsdlSettings.EXCLUDED_TYPES ))
		{
			StringList list = new StringList();
			list.add( "schema@http://www.w3.org/2001/XMLSchema");
			settings.setString( WsdlSettings.EXCLUDED_TYPES, list.toXml() );
		}

		if( !settings.isSet( WsdlSettings.NAME_WITH_BINDING ))
		{
			settings.setBoolean( WsdlSettings.NAME_WITH_BINDING, true );
		}
		
		if( !settings.isSet(  HttpSettings.MAX_CONNECTIONS_PER_HOST ))
		{
			settings.setLong( HttpSettings.MAX_CONNECTIONS_PER_HOST, 500 );
		}

		if( !settings.isSet(  HttpSettings.MAX_TOTAL_CONNECTIONS ))
		{
			settings.setLong( HttpSettings.MAX_TOTAL_CONNECTIONS, 2000 );
		}

		return settings;
	}
	
	/* (non-Javadoc)
	 * @see com.eviware.soapui.SoapUICore#importSettings(java.io.File)
	 */
	public void importSettings( File file ) throws Exception
	{
		if( file != null )
		{
			log.info( "Importing preferences from [" + file.getAbsolutePath() + "]" );
			SoapuiSettingsDocumentConfig doc = SoapuiSettingsDocumentConfig.Factory.parse( file );
			settings.setConfig( doc.getSoapuiSettings() );
		}
	}
	
	/* (non-Javadoc)
	 * @see com.eviware.soapui.SoapUICore#getSettings()
	 */
	public Settings getSettings()
	{
		if( settings == null )
		{
			initSettings( DEFAULT_SETTINGS_FILE );
		}
		
		return settings;
	}

	protected void initDefaultSettings(Settings settings2)
	{
		settings.setBoolean( WsdlSettings.CACHE_WSDLS, true );
		settings.setBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, true );
		
		settings.setBoolean( HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN, true );
		settings.setBoolean( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN, true );
		
		settings.setString( UISettings.AUTO_SAVE_INTERVAL, "0" );
	}
	
	/* (non-Javadoc)
	 * @see com.eviware.soapui.SoapUICore#saveSettings()
	 */
	public String saveSettings() throws Exception
	{
		if( settingsFile == null )
			settingsFile = DEFAULT_SETTINGS_FILE;
		
		File file = root == null ? new File( settingsFile ) : new File( new File( root ), settingsFile );
		settingsDocument.save( file );
		log.info( "Settings saved to [" + file.getAbsolutePath() + "]" );
		return file.getAbsolutePath();
	}
	
	public String getSettingsFile()
	{
		return settingsFile;
	}

	public void setSettingsFile( String settingsFile )
	{
		this.settingsFile = settingsFile;
	}

	protected void initLog()
	{
		if( !logIsInitialized )
		{
			File log4jconfig = root == null ? new File( "soapui-log4j.xml" ) : new File( new File( root ), "soapui-log4j.xml" );
			if( log4jconfig.exists() )
			{
				System.out.println( "Configuring log4j from [" + log4jconfig.getAbsolutePath() + "]" );
				DOMConfigurator.configureAndWatch( log4jconfig.getAbsolutePath(), 5000 );
			}
			else
			{
				URL url = getClass().getResource( "/com/eviware/soapui/resources/conf/soapui-log4j.xml" );
				if( url != null )
				{
					DOMConfigurator.configure( url );
				}
				else
					System.err.println( "Missing soapui-log4j.xml configuration" );
			}
			
			logIsInitialized = true;
			
			log = Logger.getLogger( DefaultSoapUICore.class );
		}
	}

	protected void loadExternalLibraries()
	{
		try
		{
			String extDir = System.getProperty("soapui.ext.libraries");
			
			File dir = extDir != null ? new File( extDir ) : 
				StringUtils.isNullOrEmpty( root ) ? new File( "ext" ) : new File( new File( root ), "ext" );
			
			if( dir.exists() && dir.isDirectory() )
			{
				File[] files = dir.listFiles();
				for( File file : files )
				{
					if( file.getName().toLowerCase().endsWith( ".jar" ))
					{
						ClasspathHacker.addFile( file );
					}
				}
			}
			else
			{
				log.warn( "Missing folder [" + dir.getAbsolutePath() + "] for external libraries" );
			}
		}
		catch( Exception e )
		{
			log.error( e.toString() );
		}
	}
	
	/* (non-Javadoc)
	 * @see com.eviware.soapui.SoapUICore#getMockEngine()
	 */
	public MockEngine getMockEngine()
	{
		if( mockEngine == null )
			mockEngine = new MockEngine();
			
		return mockEngine;
	}
	
	/* (non-Javadoc)
	 * @see com.eviware.soapui.SoapUICore#getListenerRegistry()
	 */
	public SoapUIListenerRegistry getListenerRegistry()
	{
		if( listenerRegistry == null )
			initListenerRegistry();
		
		return listenerRegistry;
	}

	protected void initListenerRegistry()
	{
		listenerRegistry = new SoapUIListenerRegistry( null );
	}
	
	/* (non-Javadoc)
	 * @see com.eviware.soapui.SoapUICore#getActionRegistry()
	 */
	public SoapUIActionRegistry getActionRegistry()
	{
		if( actionRegistry == null )
			actionRegistry = initActionRegistry();
		
		return actionRegistry;
	}

	protected SoapUIActionRegistry initActionRegistry()
	{
		return new SoapUIActionRegistry( 
					DefaultSoapUICore.class.getResourceAsStream( "/com/eviware/soapui/resources/conf/soapui-actions.xml" ));
	}
	
	protected void addExternalListeners( String folder, ClassLoader classLoader )
	{
		File[] actionFiles = new File( folder ).listFiles();
      if( actionFiles != null )
      {
	      for( File actionFile : actionFiles )
	      {
	      	if( actionFile.isDirectory() )
	      	{
	      		addExternalListeners( actionFile.getAbsolutePath(), classLoader );
	      		continue;
	      	}
	      	
	      	if( !actionFile.getName().toLowerCase().endsWith( "-listeners.xml" ))
	      		continue;

	      	try
				{
	      		log.info( "Adding listeners from [" + actionFile.getAbsolutePath() + "]" );
					
					SoapUI.getListenerRegistry().addConfig( new FileInputStream( actionFile ), 
								classLoader);
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
	      }
      }
	}
}
