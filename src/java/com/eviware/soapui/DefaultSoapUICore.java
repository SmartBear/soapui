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

package com.eviware.soapui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.apache.commons.ssl.OpenSSL;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.eviware.soapui.config.SoapuiSettingsDocumentConfig;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.SecuritySettings;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.settings.WsaSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.ClasspathHacker;
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
	protected SoapuiSettingsDocumentConfig settingsDocument;
	private MockEngine mockEngine;
	private XmlBeansSettingsImpl settings;
	private SoapUIListenerRegistry listenerRegistry;
	private SoapUIActionRegistry actionRegistry;

	private String settingsFile;

	private String password;

	protected boolean initialImport;

	public boolean getInitialImport()
	{
		return initialImport;
	}

	public void setInitialImport( boolean initialImport )
	{
		this.initialImport = initialImport;
	}

	public static DefaultSoapUICore createDefault()
	{
		return new DefaultSoapUICore( null, DEFAULT_SETTINGS_FILE );
	}

	public DefaultSoapUICore()
	{
	}

	/*
	 * this method is added for enabling settings password (like in core) all the
	 * way down in hierarchy boolean setingPassword is a dummy parameter, because
	 * the constructor with only one string parameter already existed
	 */
	public DefaultSoapUICore( boolean settingPassword, String soapUISettingsPassword )
	{
		this.password = soapUISettingsPassword;
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

	public DefaultSoapUICore( String root, String settingsFile, String password )
	{
		this( root );
		this.password = password;
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
		String extDir = System.getProperty( "soapui.ext.listeners" );
		addExternalListeners( extDir != null ? extDir : root == null ? "listeners" : root + File.separatorChar
				+ "listeners", extensionClassLoader );
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
		if( root == null || root.length() == 0 )
			root = System.getProperty( "soapui.home", new File( "." ).getAbsolutePath() );
		return root;
	}

	protected Settings initSettings( String fileName )
	{
		// TODO Why try to load settings from current directory before using root?
		// This caused a bug in Eclipse:
		// https://sourceforge.net/tracker/?func=detail&atid=737763&aid=2620284&group_id=136013
		File settingsFile = new File( fileName ).exists() ? new File( fileName ) : null;

		try
		{
			if( settingsFile == null )
			{
				settingsFile = new File( new File( getRoot() ), DEFAULT_SETTINGS_FILE );
				if( !settingsFile.exists() )
				{
					settingsFile = new File( new File( System.getProperty( "user.home", "." ) ), DEFAULT_SETTINGS_FILE );
				}
			}
			else
			{
				settingsFile = new File( fileName );
			}
			if( !settingsFile.exists() )
			{
				if( settingsDocument == null )
				{
					log.info( "Creating new settings at [" + settingsFile.getAbsolutePath() + "]" );
					settingsDocument = SoapuiSettingsDocumentConfig.Factory.newInstance();
					setInitialImport( true );
				}
			}
			else
			{
				settingsDocument = SoapuiSettingsDocumentConfig.Factory.parse( settingsFile );

				byte[] encryptedContent = settingsDocument.getSoapuiSettings().getEncryptedContent();
				if( encryptedContent != null )
				{
					char[] password = null;
					if( this.password == null )
					{
						// swing element -!! uh!
						JPasswordField passwordField = new JPasswordField();
						JLabel qLabel = new JLabel( "Password" );
						JOptionPane.showConfirmDialog( null, new Object[] { qLabel, passwordField }, "Global Settings",
								JOptionPane.OK_CANCEL_OPTION );
						password = passwordField.getPassword();
					}
					else
					{
						password = this.password.toCharArray();
					}

					byte[] data = OpenSSL.decrypt( "des3", password, encryptedContent );
					try
					{
						settingsDocument = SoapuiSettingsDocumentConfig.Factory.parse( new String( data, "UTF-8" ) );
					}
					catch( Exception e )
					{
						log.warn( "Wrong password." );
						JOptionPane.showMessageDialog( null, "Wrong password, creating backup settings file [ "
								+ settingsFile.getAbsolutePath() + ".bak.xml. ]\nSwitch to default settings.",
								"Error - Wrong Password", JOptionPane.ERROR_MESSAGE );
						settingsDocument.save( new File( settingsFile.getAbsolutePath() + ".bak.xml" ) );
						throw e;
					}
				}

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

		this.settingsFile = settingsFile.getAbsolutePath();

		if( !settings.isSet( WsdlSettings.EXCLUDED_TYPES ) )
		{
			StringList list = new StringList();
			list.add( "schema@http://www.w3.org/2001/XMLSchema" );
			settings.setString( WsdlSettings.EXCLUDED_TYPES, list.toXml() );
		}

		if( !settings.isSet( WsdlSettings.NAME_WITH_BINDING ) )
		{
			settings.setBoolean( WsdlSettings.NAME_WITH_BINDING, true );
		}

		if( !settings.isSet( HttpSettings.MAX_CONNECTIONS_PER_HOST ) )
		{
			settings.setLong( HttpSettings.MAX_CONNECTIONS_PER_HOST, 500 );
		}

		if( !settings.isSet( HttpSettings.HTTP_VERSION ) )
		{
			settings.setString( HttpSettings.HTTP_VERSION, HttpSettings.HTTP_VERSION_1_1 );
		}

		if( !settings.isSet( HttpSettings.MAX_TOTAL_CONNECTIONS ) )
		{
			settings.setLong( HttpSettings.MAX_TOTAL_CONNECTIONS, 2000 );
		}

		if( !settings.isSet( HttpSettings.RESPONSE_COMPRESSION ) )
		{
			settings.setBoolean( HttpSettings.RESPONSE_COMPRESSION, true );
		}

		if( !settings.isSet( HttpSettings.LEAVE_MOCKENGINE ) )
		{
			settings.setBoolean( HttpSettings.LEAVE_MOCKENGINE, true );
		}

		if( !settings.isSet( UISettings.AUTO_SAVE_PROJECTS_ON_EXIT ) )
		{
			settings.setBoolean( UISettings.AUTO_SAVE_PROJECTS_ON_EXIT, true );
		}

		if( !settings.isSet( UISettings.SHOW_DESCRIPTIONS ) )
		{
			settings.setBoolean( UISettings.SHOW_DESCRIPTIONS, true );
		}

		if( !settings.isSet( WsaSettings.USE_DEFAULT_RELATES_TO ) )
		{
			settings.setBoolean( WsaSettings.USE_DEFAULT_RELATES_TO, true );
		}

		if( !settings.isSet( WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE ) )
		{
			settings.setBoolean( WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE, true );
		}

		if( !settings.isSet( UISettings.SHOW_STARTUP_PAGE ) )
		{
			settings.setBoolean( UISettings.SHOW_STARTUP_PAGE, true );
		}

		if( settings.getString( HttpSettings.HTTP_VERSION, HttpSettings.HTTP_VERSION_1_1 ).equals(
				HttpSettings.HTTP_VERSION_0_9 ) )
		{
			settings.setString( HttpSettings.HTTP_VERSION, HttpSettings.HTTP_VERSION_1_1 );
		}

		return settings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.SoapUICore#importSettings(java.io.File)
	 */
	public void importSettings( File file ) throws Exception
	{
		if( file != null )
		{
			log.info( "Importing preferences from [" + file.getAbsolutePath() + "]" );
			initSettings( file.getAbsolutePath() );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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

	protected void initDefaultSettings( Settings settings2 )
	{
		settings.setBoolean( WsdlSettings.CACHE_WSDLS, true );
		settings.setBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, true );

		settings.setString( HttpSettings.HTTP_VERSION, HttpSettings.HTTP_VERSION_1_1 );
		settings.setBoolean( HttpSettings.RESPONSE_COMPRESSION, true );
		settings.setBoolean( HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN, true );
		settings.setBoolean( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN, true );
		settings.setBoolean( HttpSettings.LEAVE_MOCKENGINE, true );

		settings.setString( UISettings.AUTO_SAVE_INTERVAL, "0" );
		settings.setBoolean( UISettings.SHOW_STARTUP_PAGE, true );

		settings.setBoolean( WsaSettings.SOAP_ACTION_OVERRIDES_WSA_ACTION, false );
		settings.setBoolean( WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE, true );
		settings.setBoolean( WsaSettings.USE_DEFAULT_RELATES_TO, true );
		settings.setBoolean( WsaSettings.OVERRIDE_EXISTING_HEADERS, false );
		settings.setBoolean( WsaSettings.ENABLE_FOR_OPTIONAL, false );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.SoapUICore#saveSettings()
	 */
	public String saveSettings() throws Exception
	{
		if( settingsFile == null )
			settingsFile = DEFAULT_SETTINGS_FILE;

		// Save settings to root or user.home
		File file = new File( new File( getRoot() ), DEFAULT_SETTINGS_FILE );
		if( !file.canWrite() )
		{
			file = new File( new File( System.getProperty( "user.home", "." ) ), DEFAULT_SETTINGS_FILE );
		}

		SoapuiSettingsDocumentConfig settingsDocument = ( SoapuiSettingsDocumentConfig )this.settingsDocument.copy();
		String password = settings.getString( SecuritySettings.SHADOW_PASSWORD, null );

		if( password != null && password.length() > 0 )
		{
			try
			{
				byte[] data = settingsDocument.xmlText().getBytes();
				byte[] encryptedData = OpenSSL.encrypt( "des3", password.toCharArray(), data );
				settingsDocument.setSoapuiSettings( null );
				settingsDocument.getSoapuiSettings().setEncryptedContent( encryptedData );
			}
			catch( UnsupportedEncodingException e )
			{
				log.error( "Encryption error", e );
			}
			catch( IOException e )
			{
				log.error( "Encryption error", e );
			}
			catch( GeneralSecurityException e )
			{
				log.error( "Encryption error", e );
			}
		}

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
			File log4jconfig = root == null ? new File( "soapui-log4j.xml" ) : new File( new File( getRoot() ),
					"soapui-log4j.xml" );
			if( log4jconfig.exists() )
			{
				System.out.println( "Configuring log4j from [" + log4jconfig.getAbsolutePath() + "]" );
				DOMConfigurator.configureAndWatch( log4jconfig.getAbsolutePath(), 5000 );
			}
			else
			{
				URL url = SoapUI.class.getResource( "/com/eviware/soapui/resources/conf/soapui-log4j.xml" );
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
			String extDir = System.getProperty( "soapui.ext.libraries" );

			File dir = extDir != null ? new File( extDir ) : new File( new File( getRoot() ), "ext" );

			if( dir.exists() && dir.isDirectory() )
			{
				File[] files = dir.listFiles();
				for( File file : files )
				{
					if( file.getName().toLowerCase().endsWith( ".jar" ) )
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.SoapUICore#getMockEngine()
	 */
	public MockEngine getMockEngine()
	{
		if( mockEngine == null )
			mockEngine = new MockEngine();

		return mockEngine;
	}

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
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
		return new SoapUIActionRegistry( DefaultSoapUICore.class
				.getResourceAsStream( "/com/eviware/soapui/resources/conf/soapui-actions.xml" ) );
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

				if( !actionFile.getName().toLowerCase().endsWith( "-listeners.xml" ) )
					continue;

				try
				{
					log.info( "Adding listeners from [" + actionFile.getAbsolutePath() + "]" );

					SoapUI.getListenerRegistry().addConfig( new FileInputStream( actionFile ), classLoader );
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}
	}
}
