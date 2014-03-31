package com.eviware.soapui.plugins;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.editor.registry.InspectorFactory;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Responsible for loading plugins into SoapUI.
 */
public class PluginLoader
{

	public static Logger log = Logger.getLogger( PluginLoader.class );

	private SoapUIExtensionClassLoader extensionClassLoader;
	private SoapUIFactoryRegistry factoryRegistry;
	private SoapUIActionRegistry actionRegistry;
	private SoapUIListenerRegistry listenerRegistry;

	public PluginLoader( SoapUIExtensionClassLoader extensionClassLoader, SoapUIFactoryRegistry factoryRegistry,
								SoapUIActionRegistry actionRegistry, SoapUIListenerRegistry listenerRegistry )
	{
		this.extensionClassLoader = extensionClassLoader;
		this.factoryRegistry = factoryRegistry;
		this.actionRegistry = actionRegistry;
		this.listenerRegistry = listenerRegistry;
	}

	public void loadPlugins()
	{
		File[] pluginFiles = new File( "plugins" ).listFiles();
		if( pluginFiles != null )
		{
			for( File pluginFile : pluginFiles )
			{
				try
				{
					log.info( "Adding plugin from [" + pluginFile.getAbsolutePath() + "]" );

					// add jar to our extension classLoader
					extensionClassLoader.addFile( pluginFile );
					JarFile jarFile = new JarFile( pluginFile );

					// look for factories
					JarEntry entry = jarFile.getJarEntry( "META-INF/factories.xml" );
					if( entry != null )
						factoryRegistry.addConfig( jarFile.getInputStream( entry ), extensionClassLoader );

					// look for listeners
					entry = jarFile.getJarEntry( "META-INF/listeners.xml" );
					if( entry != null )
						listenerRegistry.addConfig( jarFile.getInputStream( entry ), extensionClassLoader );

					// look for actions
					entry = jarFile.getJarEntry( "META-INF/actions.xml" );
					if( entry != null )
						actionRegistry.addConfig( jarFile.getInputStream( entry ), extensionClassLoader );

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

	public static void main( String[] args ) throws MalformedURLException
	{
		URL url = new File( "/Users/manne/projekt/soapui_new/soapui/target/soapui-5.0.0-SNAPSHOT.jar" ).toURI().toURL();
		Reflections reflections = new Reflections( new ConfigurationBuilder().setUrls( url ) );
		for( Class<? extends AbstractHttpRequest> aClass : reflections.getSubTypesOf( AbstractHttpRequest.class ) )
		{
			System.out.println(aClass);
		}
	}
}
