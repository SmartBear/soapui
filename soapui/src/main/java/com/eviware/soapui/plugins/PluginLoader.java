package com.eviware.soapui.plugins;

import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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

    public PluginLoader(SoapUIExtensionClassLoader extensionClassLoader, SoapUIFactoryRegistry factoryRegistry,
                        SoapUIActionRegistry actionRegistry, SoapUIListenerRegistry listenerRegistry) {
        this.extensionClassLoader = extensionClassLoader;
        this.factoryRegistry = factoryRegistry;
        this.actionRegistry = actionRegistry;
        this.listenerRegistry = listenerRegistry;
    }

    Plugin loadPluginFrom(File pluginFile) throws IOException {
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

    private Plugin loadPlugin(Class<?> pluginClass) {
        try {
            if (!Plugin.class.isAssignableFrom(pluginClass)) {
                throw new InvalidPluginException("Invalid plugin class: " + pluginClass + " does not implement Plugin");
            }
            Plugin plugin = (Plugin) pluginClass.newInstance();
            if (plugin.isActive()) {
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
            }
            UISupport.addResourceClassLoader(pluginClass.getClassLoader());
            return plugin;
        } catch (Exception e) {
            throw new InvalidPluginException("Error loading plugin " + pluginClass, e);
        }
    }

    void loadOldStylePluginFrom(File pluginFile) throws IOException {
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


    public List<AvailablePlugin> loadAvailablePluginsFrom(URL jsonUrl) throws IOException {
        List<AvailablePlugin> plugins = new ArrayList<AvailablePlugin>();
        JSON pluginsAsJson = new JsonSlurper().parse(jsonUrl);
        if (pluginsAsJson instanceof JSONArray) {
           JSONArray array = (JSONArray)pluginsAsJson;
            for (Object pluginElement : array) {
                if (pluginElement instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject)pluginElement;
                    PluginId id = new PluginId((String)jsonObject.get("groupId"), (String)jsonObject.get("name"));
                    Version version = Version.fromString((String)jsonObject.get("version"));
                    PluginInfo pluginInfo = new PluginInfo(id, version, jsonObject.getString("description"),
                            jsonObject.optString("infoUrl", ""));
                    URL pluginUrl = new URL((String)jsonObject.get("url"));
                    plugins.add(new AvailablePlugin(pluginInfo, pluginUrl));
                }
            }
            return plugins;
        }
        else {
            throw new InvalidPluginException("Invalid JSON found at URL " + jsonUrl);
        }
    }
}
