/*
 *  SoapUI, copyright (C) 2004-2015 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.plugins;

import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.support.SoapUITools;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.ListenerRegistry;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

//import com.smartbear.ready.ui.toolbar.ReadyApiToolbarComponentRegistry;

public class PluginManager {

    public static final String PLUGINS_URL = "http://productextensions.s3.amazonaws.com/ReadyAPI-Plugins/availablePlugins.json";

    FileOperations fileOperations = new DefaultFileOperations();
    PluginLoader pluginLoader;
    AvailablePluginsLoader availablePluginsLoader;

    private static Logger log = Logger.getLogger(PluginManager.class);
    private Map<File, InstalledPluginRecord> installedPlugins = new HashMap<File, InstalledPluginRecord>();
    private File pluginDirectory;
    private List<PluginListener> listeners = new ArrayList<PluginListener>();
    private final File pluginDeleteListFile;
    //private Injector readyApiInjector;
    private PluginDependencyResolver resolver;

    private static ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory, new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            System.err.println("Problem running task in the forkJoinPool");
            e.printStackTrace();
        }
    }, false);

    public PluginManager(SoapUIFactoryRegistry factoryRegistry,
                         SoapUIActionRegistry actionRegistry, ListenerRegistry listenerRegistry/*,
                         ReadyApiToolbarComponentRegistry toolbarComponentRegistry*/) {
        pluginLoader = new PluginLoader(factoryRegistry, actionRegistry, listenerRegistry/*, toolbarComponentRegistry,
                SimpleVcsIntegrationRegistry.instance()*/);
        availablePluginsLoader = new AvailablePluginsLoader();
        File soapUiDirectory = new File(System.getProperty("user.home"), ".soapui");
        pluginDirectory = new File(soapUiDirectory, "plugins");
        if (!pluginDirectory.exists() && !pluginDirectory.mkdirs()) {
            log.error("Couldn't create plugin directory in location " + pluginDirectory.getAbsolutePath());
        }
        pluginDeleteListFile = new File(pluginDirectory, "delete_files.txt");
        if (pluginDeleteListFile.exists()) {
            deleteOldPluginFiles();
        }
    }

    public PluginLoader getPluginLoader() {
        return pluginLoader;
    }

    public static ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }

    public void loadPlugins() {
        File[] pluginFiles = pluginDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && (pathname.getName().toLowerCase().endsWith(".jar") ||
                        pathname.getName().toLowerCase().endsWith(".zip"));
            }
        });
        if (pluginFiles != null) {
            List<File> pluginFileList = Arrays.asList(pluginFiles);
            resolver = null;
            try {
                resolver = new PluginDependencyResolver(pluginLoader, pluginFileList);
                pluginFileList = resolver.determineLoadOrder();
            } catch (Exception e) {
                log.error("Couldn't resolve plugin dependency order. This may impair plugin functionality.", e);
            }
            long startTime = System.currentTimeMillis();

            getForkJoinPool().invoke(new LoadPluginsTask(pluginFileList));
            long timeTaken = System.currentTimeMillis() - startTime;
            log.info(pluginFiles.length + " plugins loaded in " + timeTaken + " ms");
        }
    }

    private Collection<JarClassLoader> findDependentClassLoaders(File pluginFile) throws IOException {
        if (resolver == null) {
            return Collections.emptySet();
        }
        Collection<PluginInfo> allDependencies = resolver.findAllDependencies(pluginFile);
        Set<JarClassLoader> classLoaders = new HashSet<JarClassLoader>();
        for (PluginInfo dependency : allDependencies) {
            for (InstalledPluginRecord installedPluginRecord : installedPlugins.values()) {
                if (installedPluginRecord.plugin.getInfo().isCompatibleWith(dependency)) {
                    classLoaders.add(installedPluginRecord.pluginClassLoader);
                    break;
                }
            }
        }
        return classLoaders;
    }

    private Plugin doInstallPlugin(File pluginFile, Collection<JarClassLoader> classLoaders) throws IOException {
        // add jar to resource classloader so embedded images can be found with UISupport.loadImageIcon(..)
        UISupport.addResourceClassLoader(new URLClassLoader(new URL[]{pluginFile.toURI().toURL()}));

        InstalledPluginRecord context = pluginLoader.loadPlugin(pluginFile, classLoaders);
        /*
        if (readyApiInjector != null) {
            injectMembersIntoPlugin(context.plugin);
        }
        */
        installedPlugins.put(pluginFile, context);
        for (PluginListener listener : listeners) {
            listener.pluginLoaded(context.plugin);
        }

        return context.plugin;
    }

    public Plugin installPlugin(File pluginFile) throws IOException {
        PluginInfo pluginInfo = pluginLoader.loadPluginInfoFrom(pluginFile, Collections.<JarClassLoader>emptySet());
        if (findInstalledVersionOf(pluginInfo) != null && !overwriteConfirmed(pluginInfo)) {
            return null;
        }
        File destinationFile = new File(pluginDirectory, pluginFile.getName());
        if (destinationFile.exists()) {
            destinationFile = createNonExistingFileName(destinationFile);
        }
        fileOperations.copyFile(pluginFile, destinationFile);
        resolver.addPlugin(pluginInfo, destinationFile);
        if (uninstallPlugin(pluginInfo, true)) {
            return doInstallPlugin(destinationFile, findDependentClassLoaders(destinationFile));
        } else {
            return null;
        }
    }

    private File createNonExistingFileName(File fileToWrite) {
        String originalFileName = fileToWrite.getName();
        String newFileName = null;
        if (originalFileName.matches(".+\\-\\d+\\.jar")) {
            while (newFileName == null || new File(pluginDirectory, newFileName).exists()) {
                int lastDashIndex = originalFileName.lastIndexOf('-');
                int fileCountIndex = Integer.parseInt(originalFileName.substring(lastDashIndex + 1, originalFileName.length() - 4));
                newFileName = originalFileName.substring(0, lastDashIndex + 1) + (fileCountIndex + 1) + ".jar";
            }
        } else {
            newFileName = originalFileName.substring(0, originalFileName.length() - 4) + "-2.jar";
        }
        return new File(pluginDirectory, newFileName);
    }

    private boolean overwriteConfirmed(PluginInfo pluginInfo) {
        PluginInfo installedPluginInfo = findInstalledVersionOf(pluginInfo).getInfo();
        return UISupport.confirm("You currently have version " + installedPluginInfo.getVersion() + " of the plugin " +
                pluginInfo.getId().getName() + " installed.\nDo you want to overwrite it with version " +
                pluginInfo.getVersion() + " of the same plugin?", "Overwrite plugin");
    }

    private Plugin findInstalledVersionOf(PluginInfo pluginInfo) {
        for (InstalledPluginRecord installedPlugin : installedPlugins.values()) {
            if (installedPlugin.plugin.getInfo().getId().equals(pluginInfo.getId())) {
                return installedPlugin.plugin;
            }
        }
        return null;
    }

    public boolean uninstallPlugin(Plugin plugin) throws IOException {
        return uninstallPlugin(plugin.getInfo(), false);
    }

    public boolean uninstallPlugin(PluginInfo pluginInfo, boolean silent) throws IOException {
        for (File installedPluginFile : installedPlugins.keySet()) {
            Plugin installedPlugin = installedPlugins.get(installedPluginFile).plugin;
            if (installedPlugin.getInfo().getId().equals(pluginInfo.getId())) {
                if (!fileOperations.deleteFile(installedPluginFile)) {
                    log.warn("Couldn't delete old plugin file " + installedPluginFile + " - aborting uninstall");
                    return false;
                }
                String uninstallMessage = "Plugin uninstalled - you should restart SoapUI to ensure that the changes to take effect";
                if (installedPlugin instanceof UninstallablePlugin) {
                    try {
                        boolean uninstalled = ((UninstallablePlugin) installedPlugin).uninstall();

                        if (uninstalled) {
                            uninstallMessage = "Plugin uninstalled successfully";
                        }
                    } catch (Exception e) {
                        if (silent) {
                            log.error("Error while uninstalling plugin", e);
                        } else {
                            UISupport.showErrorMessage("The plugin file has been deleted but could not be uninstalled - " +
                                    "restart SoapUI for the changes to take effect");
                        }
                        return false;
                    }
                }

                try {
                    pluginLoader.unloadPlugin(installedPlugin);
                    for (PluginListener listener : listeners) {
                        listener.pluginUnloaded(installedPlugin);
                    }
                } catch (Exception e) {
                    uninstallMessage = "Plugin unloaded unsuccessfully - please restart";
                    log.error("Couldn't unload plugin", e);
                }

                installedPlugins.remove(installedPluginFile);
                resolver.removePlugin(pluginInfo);
                if (!silent) {
                    UISupport.showInfoMessage(uninstallMessage);
                }
                break;
            }
        }

        return true;
    }

    public Collection<Plugin> getInstalledPlugins() {
        Set<Plugin> plugins = new HashSet<Plugin>();
        for (InstalledPluginRecord installedPluginRecord : installedPlugins.values()) {
            plugins.add(installedPluginRecord.plugin);
        }
        return Collections.unmodifiableCollection(plugins);
    }

    public List<AvailablePlugin> getAvailablePlugins() {
        return availablePluginsLoader.readAvailablePlugins();
    }

    public void addPluginListener(PluginListener listener) {
        listeners.add(listener);
    }

    public void removePluginListener(PluginListener listener) {
        listeners.add(listener);
    }

    /*
    public void setGuiceInjectorInstance(Injector readyApiInjector) {
        if (this.readyApiInjector != null) {
            log.warn("Ignoring attempt to set Injector instance because it has already been set");
            return;
        }
        this.readyApiInjector = readyApiInjector;
        for (Plugin plugin : getInstalledPlugins()) {
            injectMembersIntoPlugin(plugin);
        }
    }
    */

    /* Helper methods */

    private void deleteOldPluginFiles() {
        try {
            List<String> filesToDelete = FileUtils.readLines(pluginDeleteListFile);
            for (String fileName : filesToDelete) {
                File oldPluginFile = new File(pluginDirectory, fileName.trim());
                if (oldPluginFile.exists()) {
                    if (!oldPluginFile.delete()) {
                        log.warn("Couldn't delete old plugin file " + fileName + " on startup");
                    }
                } else {
                    log.info("Old plugin file not found: " + fileName);
                }
            }
        } catch (IOException e) {
            log.error("Couldn't read list of old plugin files to delete from file " +
                    pluginDeleteListFile.getAbsolutePath());
        } finally {
            if (!pluginDeleteListFile.delete()) {
                log.warn("Couldn't remove file with list of old plugin files to delete");
            }
        }
    }

    /*
    private void injectMembersIntoPlugin(Plugin plugin) {
        injectMembers(plugin, plugin);
        for (SoapUIAction action : plugin.getActions()) {
            injectMembers(action, plugin);
        }
        for (SoapUIFactory factory : plugin.getFactories()) {
            injectMembers(factory, plugin);
        }
    }

    private void injectMembers(Object target, Plugin plugin) {
        try {
            readyApiInjector.injectMembers(target);
        } catch (Throwable e) { // catching Throwable, because this could be plugin code
            log.error("Guice couldn't inject members into object " + target + " - the plugin [" + plugin +
                    "] may not be fully functional");
        }
    }
    */

    public DependencyStatus checkDependencyStatus(File pluginFile) throws IOException {
        PluginInfo pluginInfo = pluginLoader.loadPluginInfoFrom(pluginFile, Collections.<JarClassLoader>emptySet());
        return checkDependencyStatus(pluginInfo);
    }

    public DependencyStatus checkDependencyStatus(PluginInfo pluginInfo) {
        List<PluginInfo> unsatisfiedDependencies = findUnsatisfiedDependencies(pluginInfo);
        if (unsatisfiedDependencies.isEmpty()) {
            return new DependencyStatus(true, Arrays.<PluginInfo>asList());
        } else {
            List<AvailablePlugin> availablePlugins = availablePluginsLoader.readAvailablePlugins();
            if (availablePlugins.isEmpty()) {
                return new DependencyStatus(false, Arrays.<PluginInfo>asList());
            }
            for (PluginInfo dependency : unsatisfiedDependencies) {
                if (!pluginIsAvailableForDownload(availablePlugins, dependency)) {
                    new DependencyStatus(false, Arrays.<PluginInfo>asList());
                }
            }
            return new DependencyStatus(true, unsatisfiedDependencies);
        }
    }

    private boolean pluginIsAvailableForDownload(List<AvailablePlugin> availablePlugins, PluginInfo requiredPlugin) {
        for (AvailablePlugin availablePlugin : availablePlugins) {
            PluginInfo pluginInfo = availablePlugin.getPluginInfo();
            if (pluginInfo.getId().equals(requiredPlugin.getId()) && pluginInfo.getVersion().compareTo(requiredPlugin.getVersion()) != -1) {
                return true;
            }
        }
        return false;
    }

    private List<PluginInfo> findUnsatisfiedDependencies(PluginInfo pluginInfo) {
        List<PluginInfo> unsatisfiedDependencies = new ArrayList<PluginInfo>();
        for (PluginInfo dependency : pluginInfo.getDependencies()) {
            if (!dependencyInstalled(dependency)) {
                unsatisfiedDependencies.add(dependency);
            }
        }
        return unsatisfiedDependencies;
    }

    private boolean dependencyInstalled(PluginInfo dependency) {
        for (Plugin plugin : getInstalledPlugins()) {
            PluginId dependencyId = dependency.getId();
            Version minimumVersion = dependency.getVersion();
            if (plugin.getInfo().getId().equals(dependencyId) && plugin.getInfo().getVersion().compareTo(minimumVersion) != -1) {
                return true;
            }
        }
        return false;
    }

    public void installPlugins(List<File> pluginFilesToInstall) throws IOException {
        PluginDependencyResolver downloadsResolver = new PluginDependencyResolver(pluginLoader, pluginFilesToInstall);
        for (File file : downloadsResolver.determineLoadOrder()) {
            installPlugin(file);
        }
    }

    public Collection<Plugin> getDependentPlugins(Plugin selectedPlugin) {
        Set<Plugin> dependentPlugins = new HashSet<Plugin>();
        for (InstalledPluginRecord installedPluginRecord : installedPlugins.values()) {
            Collection<PluginInfo> allDependencies = resolver.findAllDependencies(installedPluginRecord.plugin.getInfo());
            for (PluginInfo dependency : allDependencies) {
                if (dependency.isCompatibleWith(selectedPlugin.getInfo())) {
                    dependentPlugins.add(installedPluginRecord.plugin);
                    break;
                }
            }
        }
        return dependentPlugins;
    }

    private class DefaultFileOperations implements FileOperations {

        @Override
        public void copyFile(File sourceFile, File destinationFile) throws IOException {
            FileUtils.copyFile(sourceFile, destinationFile);
        }

        @Override
        public boolean deleteFile(File fileToDelete) throws IOException {
            if (!fileToDelete.delete()) {
                try {
                    FileUtils.write(pluginDeleteListFile, fileToDelete.getName() + "\r\n", true);
                } catch (IOException e) {
                    log.error("Couldn't schedule plugin file " + fileToDelete.getName() + " for deletion", e);
                    return false;
                }
            }
            return true;
        }

    }

    static interface FileOperations {

        void copyFile(File sourceFile, File destinationFile) throws IOException;

        boolean deleteFile(File fileToDelete) throws IOException;
    }

    class AvailablePluginsLoader {

        public List<AvailablePlugin> readAvailablePlugins() {
            String availablePluginsUrl = System.getProperty("soapui.plugins.url", PLUGINS_URL);
            try {
                if (StringUtils.hasContent(availablePluginsUrl)) {
                    return loadAvailablePluginsFrom(new URL(availablePluginsUrl));
                }
            } catch (IOException e) {
                log.warn("Could not load plugins from [" + availablePluginsUrl + "]");
            }
            return Collections.emptyList();
        }

        List<AvailablePlugin> loadAvailablePluginsFrom(URL jsonUrl) throws IOException {
            List<AvailablePlugin> plugins = new ArrayList<AvailablePlugin>();

            String urlString = jsonUrl.toString();
            String json;

            if (urlString.startsWith("file:")) {
                json = SoapUITools.readAll(jsonUrl.openStream(), 0).toString();
            } else {
                HttpGet get = new HttpGet(urlString);
                org.apache.http.HttpResponse response = HttpClientSupport.getHttpClient().execute(get);
                json = SoapUITools.readAll(response.getEntity().getContent(), 0).toString();
            }

            JSON pluginsAsJson = new JsonSlurper().parseText(json);
            if (pluginsAsJson instanceof JSONArray) {
                JSONArray array = (JSONArray) pluginsAsJson;
                for (Object pluginElement : array) {
                    if (pluginElement instanceof JSONObject) {
                        AvailablePlugin availablePlugin = makeAvailablePluginEntry((JSONObject) pluginElement);
                        if (availablePlugin == null) {
                            continue;
                        }
                        plugins.add(availablePlugin);
                    }
                }
                return plugins;
            } else {
                throw new InvalidPluginException("Invalid JSON found at URL " + jsonUrl);
            }
        }

        private AvailablePlugin makeAvailablePluginEntry(JSONObject pluginElement) {
            PluginInfo pluginInfo = makePluginInfo(pluginElement);
            URL pluginUrl;
            String urlString = pluginElement.optString("url");
            try {
                pluginUrl = new URL(urlString);
            } catch (MalformedURLException e) {
                log.warn("Skipping plugin [" + pluginInfo.getId() + " due to malformed URL: " + urlString);
                return null;
            }
            AvailablePlugin availablePlugin = new AvailablePlugin(pluginInfo, pluginUrl, PluginManager.this, pluginElement.optString("category"));
            JSONArray dependencies = pluginElement.optJSONArray("dependencies");
            if (dependencies != null) {
                for (Object dependency : dependencies) {
                    if (dependency instanceof JSONObject) {
                        JSONObject dependencyObject = (JSONObject) dependency;
                        try {
                            availablePlugin.getPluginInfo().addDependency(makePluginInfo(dependencyObject));
                        } catch (Exception ignore) {

                        }
                    }
                }
            }
            return availablePlugin;
        }

        private PluginInfo makePluginInfo(JSONObject pluginElement) {
            PluginId id = new PluginId((String) pluginElement.get("groupId"), (String) pluginElement.get("name"));
            Version version = Version.fromString((String) pluginElement.get("version"));
            return new PluginInfo(id, version, pluginElement.optString("description"),
                    pluginElement.optString("infoUrl", ""));
        }
    }

    private class LoadPluginsTask extends RecursiveTask<List<Plugin>> {

        private List<File> files;

        private LoadPluginsTask(Collection<File> files) {
            this.files = new ArrayList<File>(files);
        }

        @Override
        protected List<Plugin> compute() {
            int splitPoint = findSplitPoint(files.size() / 2);
            if (splitPoint == 0 || splitPoint == files.size() - 1) {
                return computeSequentially();
            } else {
                LoadPluginsTask leftTask = new LoadPluginsTask(files.subList(0, splitPoint));
                leftTask.fork();
                LoadPluginsTask rightTask = new LoadPluginsTask(files.subList(splitPoint, files.size()));
                List<Plugin> rightTaskResult = rightTask.compute();
                List<Plugin> leftTaskResult = leftTask.join();
                List<Plugin> result = new ArrayList<Plugin>();
                result.addAll(leftTaskResult);
                result.addAll(rightTaskResult);
                return result;
            }
        }

        private int findSplitPoint(int tentativeSplitPoint) {
            if (tentativeSplitPoint <= 0) {
                return 0;
            } else if (tentativeSplitPoint >= files.size() - 1) {
                return files.size() - 1;
            }
            List<PluginInfo> pluginInfoList = resolver.getPluginInfoListFromFiles(files);
            if (pluginInfoList.get(tentativeSplitPoint + 1).getDependencies().isEmpty()) {
                return tentativeSplitPoint;
            } else {
                int leftSplitPoint = findSplitPoint(tentativeSplitPoint - 1);
                int rightSplitPoint = findSplitPoint(tentativeSplitPoint + 1);
                if (leftSplitPoint > 0 && (tentativeSplitPoint - leftSplitPoint <= rightSplitPoint - tentativeSplitPoint)) {
                    return leftSplitPoint;
                } else if (rightSplitPoint < files.size() - 1) {
                    return rightSplitPoint;
                } else {
                    return 0;
                }
            }
        }


        private List<Plugin> computeSequentially() {
            List<Plugin> result = new ArrayList<Plugin>();
            for (File pluginFile : files) {
                try {
                    log.info("Adding plugin from [" + pluginFile.getAbsolutePath() + "]");
                    try {
                        Plugin plugin = doInstallPlugin(pluginFile, findDependentClassLoaders(pluginFile));
                        result.add(plugin);
                    } catch (MissingPluginClassException e) {
                        log.error("No plugin found in [" + pluginFile + "]");
                    } catch (Exception e) {
                        log.warn("Could not load plugin from file [" + pluginFile + "]", e);
                    }
                } catch (Throwable e) {
                    log.error("Failed to load module [" + pluginFile.getName() + "]", e);
                }
            }
            return result;
        }


    }
}
