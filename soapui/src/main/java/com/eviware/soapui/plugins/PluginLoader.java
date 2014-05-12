package com.eviware.soapui.plugins;

import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    private Map<File, Plugin> installedPlugins = new HashMap<File, Plugin>();
    private File pluginDirectory;

    public PluginLoader(SoapUIExtensionClassLoader extensionClassLoader, SoapUIFactoryRegistry factoryRegistry,
                        SoapUIActionRegistry actionRegistry, SoapUIListenerRegistry listenerRegistry) {
        this.extensionClassLoader = extensionClassLoader;
        this.factoryRegistry = factoryRegistry;
        this.actionRegistry = actionRegistry;
        this.listenerRegistry = listenerRegistry;
        pluginDirectory = new File(System.getProperty("soapui.home"), "plugins");
    }

    public boolean installPlugin(File pluginFile) throws IOException {
        Plugin plugin = loadPluginFrom(pluginFile);
        if (plugin != null) {
            deleteOldVersionOf(plugin);
            File destinationFile = new File(pluginDirectory, pluginFile.getName());
            FileUtils.copyFile(pluginFile, destinationFile);
            installedPlugins.put(destinationFile, plugin);
            return true;
        } else {
            return false;
        }
    }

    private void deleteOldVersionOf(Plugin plugin) {
        for (File installedPluginFile : installedPlugins.keySet()) {
            if (installedPlugins.get(installedPluginFile).getId().equals(plugin.getId())) {
                if (!installedPluginFile.delete()) {
                    throw new RuntimeException("Couldn't delete old plugin file " + installedPluginFile);
                }
                installedPlugins.remove(installedPluginFile);
                break;
            }
        }
    }

    public void loadPlugins() {
        File[] pluginFiles = pluginDirectory.listFiles();
        if (pluginFiles != null) {
            for (File pluginFile : pluginFiles) {
                log.info("Adding plugin from [" + pluginFile.getAbsolutePath() + "]");
                try {
                    Plugin plugin = loadPluginFrom(pluginFile);
                    if (plugin == null) {
                        loadOldStylePluginFrom(pluginFile);
                    } else {
                        //TODO: probably check if there is a duplicate in the list, here or elsewhere
                        installedPlugins.put(pluginFile, plugin);
                    }
                } catch (IOException e) {
                    log.warn("Could not load plugin from file [" + pluginFile + "]");
                }
            }
        }
    }

    private Plugin loadPluginFrom(File pluginFile) throws IOException {
        JarClassLoader jarClassLoader = new JarClassLoader(pluginFile, ClassLoader.getSystemClassLoader());
        Reflections jarFileScanner = new Reflections(new ConfigurationBuilder().setUrls(jarClassLoader.getURLs()).addClassLoader(jarClassLoader));
        Set<Class<?>> pluginClasses = jarFileScanner.getTypesAnnotatedWith(PluginConfiguration.class);
        if (pluginClasses.isEmpty()) {
            log.warn("No plugin classes found in JAR file " + pluginFile);
            return null;
        } else if (pluginClasses.size() == 1) {
            return loadPlugin(pluginClasses.iterator().next());
        } else {
            throw new InvalidPluginException("Multiple plugin classes found in " + pluginFile + ": " + pluginClasses);
        }
    }

    public Collection<Plugin> getInstalledPlugins() {
        return Collections.unmodifiableCollection(installedPlugins.values());
    }

    private Plugin loadPlugin(Class<?> pluginClass) {
        try {
            if (!Plugin.class.isAssignableFrom(pluginClass)) {
                throw new InvalidPluginException("Invalid plugin class: " + pluginClass + " does not implement Plugin");
            }
            Plugin plugin = (Plugin) pluginClass.newInstance();
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
