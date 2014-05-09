package com.eviware.soapui.plugins;

import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Responsible for loading plugins into SoapUI.
 */
public class PluginLoader {

    public static Logger log = Logger.getLogger(PluginLoader.class);

    private SoapUIExtensionClassLoader extensionClassLoader;
    private SoapUIFactoryRegistry factoryRegistry;
    private SoapUIActionRegistry actionRegistry;
    private SoapUIListenerRegistry listenerRegistry;
    private List<Plugin> installedPlugins = new ArrayList<Plugin>();

    public PluginLoader(SoapUIExtensionClassLoader extensionClassLoader, SoapUIFactoryRegistry factoryRegistry,
                        SoapUIActionRegistry actionRegistry, SoapUIListenerRegistry listenerRegistry) {
        this.extensionClassLoader = extensionClassLoader;
        this.factoryRegistry = factoryRegistry;
        this.actionRegistry = actionRegistry;
        this.listenerRegistry = listenerRegistry;
    }

    public void loadPlugins() {
        File[] pluginFiles = new File("plugins").listFiles();
        if (pluginFiles != null) {
            for (File pluginFile : pluginFiles) {
                log.info("Adding plugin from [" + pluginFile.getAbsolutePath() + "]");
                try {
                    if (!loadPluginFrom(pluginFile)) {
                        loadOldStylePluginFrom(pluginFile);
                    }
                } catch (IOException e) {
                    log.warn("Could not load plugin from file [" + pluginFile + "]");
                }
            }
        }
    }

    public boolean loadPluginFrom(File pluginFile) throws IOException {
        JarClassLoader jarClassLoader = new JarClassLoader(pluginFile, extensionClassLoader);
        Reflections jarFileScanner = new Reflections(new ConfigurationBuilder().setUrls(jarClassLoader.getURLs()));
        Set<Class<? extends Plugin>> pluginClasses = jarFileScanner.getSubTypesOf(Plugin.class);
        if (pluginClasses.isEmpty()) {
            log.warn("No plugin classes found in JAR file " + pluginFile);
        }
        if (pluginClasses.size() == 1) {
            installedPlugins.add(loadPlugin(pluginClasses.iterator().next()));
            return true;
        } else {
            throw new InvalidPluginException("Multiple plugin classes found in " + pluginFile + ": " + pluginClasses);
        }
    }

    public List<Plugin> getInstalledPlugins() {
        return Collections.unmodifiableList(installedPlugins);
    }

    private Plugin loadPlugin(Class<? extends Plugin> pluginClass) {
        try {
            Plugin plugin = pluginClass.newInstance();
            plugin.initialize();
            for (SoapUIFactory factory : plugin.getFactories()) {
                factoryRegistry.addFactory(factory.getFactoryType(), factory);
            }
            for (SoapUIAction action : plugin.getActions()) {
                actionRegistry.addAction(action.getId(), action);
            }
            for (Class<? extends SoapUIListener> listenerClass : plugin.getListeners()) {
                for (Class<?> implementedInterface : listenerClass.getInterfaces()) {
                    if (implementedInterface.isAssignableFrom(SoapUIListener.class)) {
                        listenerRegistry.addListener(implementedInterface, listenerClass, null);
                    }
                }
            }
            return plugin;
        } catch (Exception e) {
            throw new InvalidPluginException("Error loading plugin " + pluginClass, e);
        }
    }

    private void loadOldStylePluginFrom(File pluginFile) throws IOException {
        JarFile jarFile = new JarFile(pluginFile);
        // add jar to our extension classLoader
        extensionClassLoader.addFile(pluginFile);

        // look for factories
        JarEntry entry = jarFile.getJarEntry("META-INF/factories.xml");
        if (entry != null) {
            factoryRegistry.addConfig(jarFile.getInputStream(entry), extensionClassLoader);
        }

        // look for listeners
        entry = jarFile.getJarEntry("META-INF/listeners.xml");
        if (entry != null) {
            listenerRegistry.addConfig(jarFile.getInputStream(entry), extensionClassLoader);
        }

        // look for actions
        entry = jarFile.getJarEntry("META-INF/actions.xml");
        if (entry != null) {
            actionRegistry.addConfig(jarFile.getInputStream(entry), extensionClassLoader);
        }

        // add jar to resource classloader so embedded images can be found with UISupport.loadImageIcon(..)
        UISupport.addResourceClassLoader(new URLClassLoader(new URL[]{pluginFile.toURI().toURL()}));

    }


}
