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

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.ListenerRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class PluginManager {

    FileOperations fileOperations = new DefaultFileOperations();
    PluginLoader pluginLoader;

    private static Logger log = LogManager.getLogger(PluginManager.class);
    private Map<File, InstalledPluginRecord> installedPlugins = new HashMap<File, InstalledPluginRecord>();
    private File pluginDirectory;
    private List<PluginListener> listeners = new ArrayList<PluginListener>();
    private final File pluginDeleteListFile;
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
                         SoapUIActionRegistry actionRegistry, ListenerRegistry listenerRegistry) {
        pluginLoader = new PluginLoader(factoryRegistry, actionRegistry, listenerRegistry);
        File soapUiDirectory = new File(System.getProperty("user.home"), ".soapuios");
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
            List<File> pluginFileList = new ArrayList<>();
            for (File file : pluginFiles) {
                pluginFileList.add(file);
            }

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
            log.info(pluginFileList.size() + " plugins loaded in " + timeTaken + " ms");
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

    public void addPluginListener(PluginListener listener) {
        listeners.add(listener);
    }

    public void removePluginListener(PluginListener listener) {
        listeners.add(listener);
    }

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
