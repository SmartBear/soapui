/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.plugins;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.ListenerRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Responsible for loading plugins into SoapUI.
 */
public class PluginLoader extends LoaderBase {

    public static Logger log = LogManager.getLogger(PluginLoader.class);

    public PluginLoader(SoapUIFactoryRegistry factoryRegistry,
                        SoapUIActionRegistry actionRegistry, ListenerRegistry listenerRegistry) {
        super(listenerRegistry, actionRegistry, factoryRegistry);
    }

    InstalledPluginRecord loadPlugin(File pluginFile, Collection<JarClassLoader> dependencyClassLoaders) throws IOException {
        ReflectionsAndClassLoader tuple = makeJarFileScanner(pluginFile, dependencyClassLoaders);
        Class<?> pluginClass = readPluginConfigurationClasses(pluginFile, tuple.reflections);
        Plugin plugin = loadPlugin(pluginClass, tuple.reflections);
        return new InstalledPluginRecord(plugin, tuple.jarClassLoader);
    }

    private ReflectionsAndClassLoader makeJarFileScanner(File pluginFile, Collection<JarClassLoader> dependencyClassLoaders) throws IOException {
        File tempFile = File.createTempFile("soapuios", ".jar");
        tempFile.deleteOnExit();
        FileUtils.copyFile(pluginFile, tempFile);
        JarClassLoader jarClassLoader = new JarClassLoader(tempFile, PluginLoader.class.getClassLoader(), dependencyClassLoaders);
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder().setUrls(jarClassLoader.getURLs()).addClassLoader(jarClassLoader);

        if (jarClassLoader.hasScripts()) {
            configurationBuilder.addClassLoader(jarClassLoader.getScriptClassLoader());
            configurationBuilder.addScanners(new TypeAnnotationsScanner());
            configurationBuilder.setMetadataAdapter(new GroovyAndJavaReflectionAdapter(jarClassLoader));
        }

        return new ReflectionsAndClassLoader(new Reflections(configurationBuilder), jarClassLoader);
    }

    private Class<?> readPluginConfigurationClasses(File pluginFile, Reflections jarFileScanner) {
        Set<Class<?>> pluginClasses = jarFileScanner.getTypesAnnotatedWith(PluginConfiguration.class);
        if (pluginClasses.isEmpty()) {
            log.warn("No plugin classes found in JAR file " + pluginFile);
            throw new MissingPluginClassException("No plugin class found in " + pluginFile);
        } else if (pluginClasses.size() > 1) {
            throw new InvalidPluginException("Multiple plugin classes found in " + pluginFile + ": " + pluginClasses);
        }
        return pluginClasses.iterator().next();
    }

    Plugin loadPlugin(Class<?> pluginClass, Reflections jarFileScanner) {
        try {
            PluginConfiguration configurationAnnotation = pluginClass.getAnnotation(PluginConfiguration.class);
            Version minimumReadyApiVersion = Version.fromString(configurationAnnotation.minimumReadyApiVersion());
            Version installedReadyApiVersion = Version.fromString(SoapUI.SOAPUI_VERSION);
            if (minimumReadyApiVersion.compareTo(installedReadyApiVersion) > 0) {
                throw new InvalidPluginException("Plugin " + configurationAnnotation.name() + " requires version " +
                        minimumReadyApiVersion + " of ReadyAPI. Current application version: " + installedReadyApiVersion);
            }
            Plugin plugin;
            if (Plugin.class.isAssignableFrom(pluginClass)) {
                plugin = (Plugin) pluginClass.newInstance();
            } else {
                plugin = new EmptyPlugin(configurationAnnotation);
            }

            boolean autoDetect = configurationAnnotation.autoDetect();
            if (plugin.isActive()) {
                plugin.initialize();
                Collection<SoapUIFactory> factories = loadPluginFactories(plugin, autoDetect, jarFileScanner);
                List<SoapUIAction> actions = loadPluginActions(plugin, autoDetect, jarFileScanner);
                List<Class<? extends SoapUIListener>> listeners = loadPluginListeners(plugin, autoDetect, jarFileScanner);
                return createLoadedPluginInstance(plugin, factories, actions, listeners);
            }

            return plugin;
        } catch (InvalidPluginException e) {
            throw e;
        } catch (Throwable e) {
            throw new InvalidPluginException("Error loading plugin " + pluginClass, e);
        }
    }

    private LoadedPlugin createLoadedPluginInstance(Plugin plugin, Collection<SoapUIFactory> factories, List<SoapUIAction> actions,
                                                    List<Class<? extends SoapUIListener>> listeners) {
        LoadedPlugin loadedPlugin = new LoadedPlugin(plugin, factories, actions, listeners);
        for (SoapUIFactory factory : factories) {
            if (factory instanceof PluginAware) {
                ((PluginAware)factory).setPlugin(loadedPlugin);
            }
        }
        for (SoapUIAction action : actions) {
            if (action instanceof PluginAware) {
                ((PluginAware)action).setPlugin(loadedPlugin);
            }
        }
        return loadedPlugin;
    }

    private Collection<SoapUIFactory> loadPluginFactories(Plugin plugin, boolean autoDetect, Reflections jarFileScanner)
            throws IllegalAccessException, InstantiationException {
        Collection<SoapUIFactory> factories = new HashSet<SoapUIFactory>(plugin.getFactories());
        if (!factories.isEmpty())
            registerFactories(factories);

        if (autoDetect) {
            factories.addAll(loadFactories(jarFileScanner));
        }

        return factories;
    }


    private List<Class<? extends SoapUIListener>> loadPluginListeners(Plugin plugin, boolean autoDetect, Reflections jarFileScanner) throws IllegalAccessException, InstantiationException {
        List<Class<? extends SoapUIListener>> listeners = new ArrayList<Class<? extends SoapUIListener>>(plugin.getListeners());
        if (!listeners.isEmpty())
            registerListeners(listeners);

        if (autoDetect) {
            listeners.addAll(loadListeners(jarFileScanner));
        }

        return listeners;
    }

    private List<SoapUIAction> loadPluginActions(Plugin plugin, boolean autoDetect, Reflections jarFileScanner)
            throws InstantiationException, IllegalAccessException {
        List<SoapUIAction> actions = new ArrayList<SoapUIAction>(plugin.getActions());
        if (!actions.isEmpty())
            registerActions(actions);

        if (autoDetect) {
            actions.addAll(loadActions(jarFileScanner));
        }
        return actions;
    }

    public void unloadPlugin(Plugin plugin) {
        unregisterActions(plugin.getActions());
        unregisterListeners(plugin.getListeners());
        unregisterFactories(plugin.getFactories());
    }

    public PluginInfo loadPluginInfoFrom(File pluginFile, Collection<JarClassLoader> dependencyClassLoaders) throws IOException {
        ReflectionsAndClassLoader tuple = makeJarFileScanner(pluginFile, dependencyClassLoaders);
        Class<?> pluginClass = readPluginConfigurationClasses(pluginFile, tuple.reflections);
        return readPluginInfoFrom(pluginClass);
    }

    static PluginInfo readPluginInfoFrom(Class<?> pluginClass) {
        PluginInfo pluginInfo = readPluginInfoFromAnnotation(pluginClass.getAnnotation(PluginConfiguration.class));
        addDependency(pluginInfo, pluginClass.getAnnotation(PluginDependency.class));
        PluginDependencies pluginDependenciesAnnotation = pluginClass.getAnnotation(PluginDependencies.class);
        if (pluginDependenciesAnnotation != null) {
            for (PluginDependency pluginDependency : pluginDependenciesAnnotation.value()) {
                addDependency(pluginInfo, pluginDependency);
            }
        }
        return pluginInfo;
    }

    private static void addDependency(PluginInfo pluginInfo, PluginDependency dependencyAnnotation) {
        if (dependencyAnnotation != null) {
            PluginId id = new PluginId(dependencyAnnotation.groupId(), dependencyAnnotation.name());
            pluginInfo.addDependency(new PluginInfo(id, Version.fromString(dependencyAnnotation.minimumVersion()), "", ""));
        }
    }

    static PluginInfo readPluginInfoFromAnnotation(PluginConfiguration annotation) {
        PluginId id = new PluginId(annotation.groupId(), annotation.name());
        Version version = Version.fromString(annotation.version());
        String infoUrl = annotation.infoUrl();
        return new PluginInfo(id, version, annotation.description(), infoUrl);
    }


    // due to Reflections internals (or my misunderstanding of them) this class has to be
    // named as its superclass
    private static class TypeAnnotationsScanner extends org.reflections.scanners.TypeAnnotationsScanner {
        @Override
        public boolean acceptsInput(String file) {
            if (file.endsWith(".groovy")) {
                return true;
            } else {
                return super.acceptsInput(file);
            }
        }
    }

    private class LoadedPlugin implements Plugin{
        private final Plugin plugin;
        private final Collection<SoapUIFactory> factories;
        private final List<SoapUIAction> actions;
        private final List<Class<? extends SoapUIListener>> listeners;

        public LoadedPlugin(Plugin plugin, Collection<SoapUIFactory> factories, List<SoapUIAction> actions,
                            List<Class<? extends SoapUIListener>> listeners) {
            this.plugin = plugin;
            this.factories = factories;
            this.actions = actions;
            this.listeners = listeners;
        }

        @Override
        public PluginInfo getInfo() {
            return plugin.getInfo();
        }

        @Override
        public boolean isActive() {
            return plugin.isActive();
        }

        @Override
        public void initialize() {
            throw new IllegalStateException("Plugin has already been initialized");
        }

        @Override
        public List<Class<? extends SoapUIListener>> getListeners() {
            return listeners;
        }

        @Override
        public List<? extends SoapUIAction> getActions() {
            return actions;
        }

        @Override
        public Collection<? extends ApiImporter> getApiImporters() {
            return Collections.emptySet();
        }

        @Override
        public Collection<? extends SoapUIFactory> getFactories() {
            return factories;
        }

        @Override
        public boolean hasSameIdAs(Plugin otherPlugin) {
            return plugin.hasSameIdAs(otherPlugin);
        }

        @Override
        public String toString() {
            return plugin.toString();
        }
    }

    private class EmptyPlugin implements Plugin {

        private PluginInfo pluginInfo;

        private EmptyPlugin(PluginConfiguration annotation) {
            pluginInfo = readPluginInfoFromAnnotation(annotation);
        }

        @Override
        public PluginInfo getInfo() {
            return pluginInfo;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void initialize() {

        }

        @Override
        public List<Class<? extends SoapUIListener>> getListeners() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends SoapUIAction> getActions() {
            return Collections.emptyList();
        }

        @Override
        public Collection<? extends ApiImporter> getApiImporters() {
            return Collections.emptySet();
        }

        @Override
        public Collection<? extends SoapUIFactory> getFactories() {
            return Collections.emptySet();
        }

        @Override
        public boolean hasSameIdAs(Plugin otherPlugin) {
            return pluginInfo.getId().equals(otherPlugin.getInfo().getId());
        }
    }

    private class ReflectionsAndClassLoader {
        Reflections reflections;
        JarClassLoader jarClassLoader;

        private ReflectionsAndClassLoader(Reflections reflections, JarClassLoader jarClassLoader) {
            this.reflections = reflections;
            this.jarClassLoader = jarClassLoader;
        }
    }
}
