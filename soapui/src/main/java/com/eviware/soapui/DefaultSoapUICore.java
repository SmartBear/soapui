/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.GeneralSecurityException;
import java.util.TimerTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.apache.commons.ssl.OpenSSL;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.eviware.soapui.config.SoapuiSettingsDocumentConfig;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.monitor.JettyMockEngine;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.security.registry.SecurityScanRegistry;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.settings.SecuritySettings;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.settings.VersionUpdateSettings;
import com.eviware.soapui.settings.WSISettings;
import com.eviware.soapui.settings.WebRecordingSettings;
import com.eviware.soapui.settings.WsaSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.SecurityScanUtil;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
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
	private volatile MockEngine mockEngine;
	private XmlBeansSettingsImpl settings;
	private SoapUIListenerRegistry listenerRegistry;
	private SoapUIActionRegistry actionRegistry;
	private SoapUIFactoryRegistry factoryRegistry;
	private long lastSettingsLoad = 0;

	private String settingsFile;
	private String password;
	protected boolean initialImport;
	private TimerTask settingsWatcher;
	private SoapUIExtensionClassLoader extClassLoader;

	public boolean isSavingSettings;

	protected SecurityScanRegistry securityScanRegistry;

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

		initExtensions( getExtensionClassLoader() );
		initPlugins();
		initCoreComponents();

		// this is to provoke initialization
		SoapVersion.Soap11.equals( SoapVersion.Soap12 );

	}

	protected void initPlugins()
	{
		File[] pluginFiles = new File( "plugins" ).listFiles();
		if( pluginFiles != null )
		{
			for( File pluginFile : pluginFiles )
			{
				if( !pluginFile.getName().toLowerCase().endsWith( "-plugin.jar" ) )
					continue;

				try
				{
					log.info( "Adding plugin from [" + pluginFile.getAbsolutePath() + "]" );

					// add jar to our extension classLoader
					getExtensionClassLoader().addFile( pluginFile );
					JarFile jarFile = new JarFile( pluginFile );

					// look for factories
					JarEntry entry = jarFile.getJarEntry( "META-INF/factories.xml" );
					if( entry != null )
						getFactoryRegistry().addConfig( jarFile.getInputStream( entry ), extClassLoader );

					// look for listeners
					entry = jarFile.getJarEntry( "META-INF/listeners.xml" );
					if( entry != null )
						getListenerRegistry().addConfig( jarFile.getInputStream( entry ), extClassLoader );

					// look for actions
					entry = jarFile.getJarEntry( "META-INF/actions.xml" );
					if( entry != null )
						getActionRegistry().addConfig( jarFile.getInputStream( entry ), extClassLoader );

					// add jar to resource classloader so embedded images can be found with UISupport.loadImageIcon(..)
					UISupport.addResourceClassLoader( new URLClassLoader( new URL[] { pluginFile.toURI().toURL() } ) );
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}
	}

	protected void initExtensions( ClassLoader extensionClassLoader )
	{
		String extDir = System.getProperty( "soapui.ext.listeners" );
		addExternalListeners( extDir != null ? extDir : root == null ? "listeners" : root + File.separatorChar
				+ "listeners", extensionClassLoader );

		String factoriesDir = System.getProperty( "soapui.ext.factories" );
		addExternalFactories( factoriesDir != null ? factoriesDir : root == null ? "factories" : root
				+ File.separatorChar + "factories", extensionClassLoader );
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
					lastSettingsLoad = 0;
				}
			}
			else
			{
				settingsFile = new File( fileName );
				if( !settingsFile.getAbsolutePath().equals( this.settingsFile ) )
					lastSettingsLoad = 0;
			}

			if( !settingsFile.exists() )
			{
				if( settingsDocument == null )
				{
					log.info( "Creating new settings at [" + settingsFile.getAbsolutePath() + "]" );
					settingsDocument = SoapuiSettingsDocumentConfig.Factory.newInstance();
					setInitialImport( true );
				}

				lastSettingsLoad = System.currentTimeMillis();
			}
			else if( settingsFile.lastModified() > lastSettingsLoad )
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
				lastSettingsLoad = settingsFile.lastModified();

				if( settingsWatcher == null )
				{
					settingsWatcher = new SettingsWatcher();
					SoapUI.getSoapUITimer().scheduleAtFixedRate( settingsWatcher, 10000, 10000 );
				}
			}
		}
		catch( Exception e )
		{
			log.warn( "Failed to load settings from [" + e.getMessage() + "], creating new" );
			settingsDocument = SoapuiSettingsDocumentConfig.Factory.newInstance();
			lastSettingsLoad = 0;
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

		if( !settings.isSet( WebRecordingSettings.EXCLUDED_HEADERS ) )
		{
			StringList list = new StringList();
			list.add( "Cookie" );
			list.add( "Set-Cookie" );
			list.add( "Referer" );
			list.add( "Keep-Alive" );
			list.add( "Connection" );
			list.add( "Proxy-Connection" );
			list.add( "Pragma" );
			list.add( "Cache-Control" );
			list.add( "Transfer-Encoding" );
			list.add( "Date" );
			settings.setString( WebRecordingSettings.EXCLUDED_HEADERS, list.toXml() );
		}

		if( settings.getString( HttpSettings.HTTP_VERSION, HttpSettings.HTTP_VERSION_1_1 ).equals(
				HttpSettings.HTTP_VERSION_0_9 ) )
		{
			settings.setString( HttpSettings.HTTP_VERSION, HttpSettings.HTTP_VERSION_1_1 );
		}

		setIfNotSet( WsdlSettings.NAME_WITH_BINDING, true );
		setIfNotSet( WsdlSettings.NAME_WITH_BINDING, 500 );
		setIfNotSet( HttpSettings.HTTP_VERSION, HttpSettings.HTTP_VERSION_1_1 );
		setIfNotSet( HttpSettings.MAX_TOTAL_CONNECTIONS, 2000 );
		setIfNotSet( HttpSettings.RESPONSE_COMPRESSION, true );
		setIfNotSet( HttpSettings.LEAVE_MOCKENGINE, true );
		setIfNotSet( UISettings.AUTO_SAVE_PROJECTS_ON_EXIT, true );
		setIfNotSet( UISettings.SHOW_DESCRIPTIONS, true );
		setIfNotSet( WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS, true );
		setIfNotSet( WsaSettings.USE_DEFAULT_RELATES_TO, true );
		setIfNotSet( WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE, true );
		setIfNotSet( UISettings.SHOW_STARTUP_PAGE, true );
		setIfNotSet( UISettings.GC_INTERVAL, "60" );
		setIfNotSet( WsdlSettings.CACHE_WSDLS, true );
		setIfNotSet( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, true );
		setIfNotSet( HttpSettings.RESPONSE_COMPRESSION, true );
		setIfNotSet( HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN, true );
		setIfNotSet( HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN, true );
		setIfNotSet( HttpSettings.LEAVE_MOCKENGINE, true );
		setIfNotSet( UISettings.AUTO_SAVE_INTERVAL, "0" );
		setIfNotSet( UISettings.GC_INTERVAL, "60" );
		setIfNotSet( UISettings.SHOW_STARTUP_PAGE, true );
		setIfNotSet( WsaSettings.SOAP_ACTION_OVERRIDES_WSA_ACTION, false );
		setIfNotSet( WsaSettings.USE_DEFAULT_RELATIONSHIP_TYPE, true );
		setIfNotSet( WsaSettings.USE_DEFAULT_RELATES_TO, true );
		setIfNotSet( WsaSettings.OVERRIDE_EXISTING_HEADERS, false );
		setIfNotSet( WsaSettings.ENABLE_FOR_OPTIONAL, false );
		setIfNotSet( VersionUpdateSettings.AUTO_CHECK_VERSION_UPDATE, true );

		boolean setWsiDir = false;
		String wsiLocationString = settings.getString( WSISettings.WSI_LOCATION, null );
		if( StringUtils.isNullOrEmpty( wsiLocationString ) )
		{
			setWsiDir = true;
		}
		else
		{
			File wsiFile = new File( wsiLocationString );
			if( !wsiFile.exists() )
			{
				setWsiDir = true;
			}
		}
		if( setWsiDir )
		{
			String wsiDir = System.getProperty( "wsi.dir", new File( "." ).getAbsolutePath() );
			settings.setString( WSISettings.WSI_LOCATION, wsiDir );
		}
		HttpClientSupport.addSSLListener( settings );

		return settings;
	}

	private void setIfNotSet( String id, boolean value )
	{
		if( !settings.isSet( id ) )
			settings.setBoolean( id, true );
	}

	private void setIfNotSet( String id, String value )
	{
		if( !settings.isSet( id ) )
			settings.setString( id, value );
	}

	private void setIfNotSet( String id, long value )
	{
		if( !settings.isSet( id ) )
			settings.setLong( id, value );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.SoapUICore#importSettings(java.io.File)
	 */
	public Settings importSettings( File file ) throws Exception
	{
		if( file != null )
		{
			log.info( "Importing preferences from [" + file.getAbsolutePath() + "]" );
			return initSettings( file.getAbsolutePath() );
		}
		return null;
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

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.SoapUICore#saveSettings()
	 */
	public String saveSettings() throws Exception
	{
		PropertyExpansionUtils.saveGlobalProperties();
		SecurityScanUtil.saveGlobalSecuritySettings();
		isSavingSettings = true;
		try
		{
			if( settingsFile == null )
				settingsFile = getRoot() + File.separatorChar + DEFAULT_SETTINGS_FILE;

			// Save settings to root or user.home
			File file = new File( settingsFile );
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

			FileOutputStream out = new FileOutputStream( file );
			settingsDocument.save( out );
			out.flush();
			out.close();
			log.info( "Settings saved to [" + file.getAbsolutePath() + "]" );
			lastSettingsLoad = file.lastModified();
			return file.getAbsolutePath();
		}
		finally
		{
			isSavingSettings = false;
		}
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
			String logFileName = System.getProperty( SoapUISystemProperties.SOAPUI_LOG4j_CONFIG_FILE, "soapui-log4j.xml" );
			File log4jconfig = root == null ? new File( logFileName ) : new File( new File( getRoot() ), logFileName );
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

	public synchronized void loadExternalLibraries()
	{
		if( extClassLoader == null )
		{
			try
			{
				extClassLoader = SoapUIExtensionClassLoader.create( getRoot(), getExtensionClassLoaderParent() );
			}
			catch( MalformedURLException e )
			{
				SoapUI.logError( e );
			}
		}
	}

	protected ClassLoader getExtensionClassLoaderParent()
	{
		return SoapUI.class.getClassLoader();
	}

	public SoapUIExtensionClassLoader getExtensionClassLoader()
	{
		if( extClassLoader == null )
			loadExternalLibraries();

		return extClassLoader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.SoapUICore#getMockEngine()
	 */
	public MockEngine getMockEngine()
	{
		if( mockEngine == null )
		{
			synchronized( DefaultSoapUICore.class )
			{
				if( mockEngine == null )
				{
					mockEngine = buildMockEngine();
				}
			}
		}

		return mockEngine;
	}

	protected MockEngine buildMockEngine()
	{
		return new JettyMockEngine();
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
		return new SoapUIActionRegistry(
				DefaultSoapUICore.class.getResourceAsStream( "/com/eviware/soapui/resources/conf/soapui-actions.xml" ) );
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

	protected void addExternalFactories( String folder, ClassLoader classLoader )
	{
		File[] factoryFiles = new File( folder ).listFiles();
		if( factoryFiles != null )
		{
			for( File factoryFile : factoryFiles )
			{
				if( factoryFile.isDirectory() )
				{
					addExternalListeners( factoryFile.getAbsolutePath(), classLoader );
					continue;
				}

				if( !factoryFile.getName().toLowerCase().endsWith( "-factories.xml" ) )
					continue;

				try
				{
					log.info( "Adding factories from [" + factoryFile.getAbsolutePath() + "]" );

					getFactoryRegistry().addConfig( new FileInputStream( factoryFile ), classLoader );
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}
	}

	private class SettingsWatcher extends TimerTask
	{
		@Override
		public void run()
		{
			if( settingsFile != null && !isSavingSettings )
			{
				File file = new File( settingsFile );
				if( file.exists() && file.lastModified() > lastSettingsLoad )
				{
					log.info( "Reloading updated settings file" );
					initSettings( settingsFile );
					SoapUI.setProxyEnabled( getSettings().getBoolean( ProxySettings.ENABLE_PROXY ) );
				}
			}
		}
	}

	@Override
	public SoapUIFactoryRegistry getFactoryRegistry()
	{
		if( factoryRegistry == null )
			initFactoryRegistry();

		return factoryRegistry;
	}

	protected void initFactoryRegistry()
	{
		factoryRegistry = new SoapUIFactoryRegistry( null );
	}

	protected void initSecurityScanRegistry()
	{
		securityScanRegistry = SecurityScanRegistry.getInstance();
	}

	@Override
	public SecurityScanRegistry getSecurityScanRegistry()
	{
		if( securityScanRegistry == null )
			initSecurityScanRegistry();
		return securityScanRegistry;
	}
}
