/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.Util.SoapUITools;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.utils.StubbedDialogsTestBase;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.eviware.soapui.utils.CommonMatchers.aCollectionWithSize;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PluginManagerTest extends StubbedDialogsTestBase {

    @Mock
    private PluginLoader pluginLoader;
    @Mock
    private PluginManager.FileOperations fileOperations;
    private Plugin plugin;
    private PluginManager pluginManager;
    private Plugin upgradedAndUninstallablePlugin;
    private File pluginFile;
    private File secondPluginFile;
    private String originalUserHome;
    private File pluginsDirectory;

    @Before
    public void setUp() throws Exception {
        originalUserHome = System.getProperty("user.home");
        File fakeHomeDirectory = SoapUITools.createTemporaryDirectory();
        System.setProperty("user.home", fakeHomeDirectory.getAbsolutePath());
        pluginsDirectory = new File(fakeHomeDirectory, ".soapuios/plugins");
        if (!pluginsDirectory.mkdirs()) {
            throw new Error("Couldn't create directory " + pluginsDirectory);
        }
        pluginManager = makePluginManager();
        MockitoAnnotations.initMocks(this);
        pluginManager.fileOperations = fileOperations;
        pluginManager.pluginLoader = pluginLoader;
        pluginManager.loadPlugins();
        setUpPluginsAndFiles();
    }

    @After
    public void tearDown() throws Exception {
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
        try {
            FileUtils.deleteDirectory(pluginsDirectory);
        } catch (Exception e) {
            System.err.println("Error when deleting fake plugins directory: " + e);
        }
    }

    @Test
    public void installsAndReturnsPlugin() throws Exception {
        assertThat(pluginManager.installPlugin(pluginFile), is(plugin));
        verify(pluginLoader).loadPlugin(pluginFile, java.util.Collections.<JarClassLoader>emptySet());
        verify(fileOperations).copyFile(eq(pluginFile), any(File.class));
    }

    @Test
    public void upgradesPluginWhenUserConfirms() throws Exception {
        pluginManager.installPlugin(pluginFile);
        when(fileOperations.deleteFile(any(File.class))).thenReturn(true);
        stubbedDialogs.mockConfirmWithReturnValue(true);

        pluginManager.installPlugin(secondPluginFile);
        assertThat(stubbedDialogs.getConfirmations(), is(aCollectionWithSize(1)));
        verify(fileOperations).copyFile(eq(pluginFile), any(File.class));
        assertThat(pluginManager.getInstalledPlugins(), is(aCollectionWithSize(1)));
        Plugin installedPlugin = pluginManager.getInstalledPlugins().iterator().next();
        assertThat(installedPlugin.getInfo().getVersion(), is(Version.fromString("0.2")));
    }

    @Test
    public void keepsOldPluginVersionWhenUserDeclinesOverwrite() throws Exception {
        pluginManager.installPlugin(pluginFile);
        stubbedDialogs.mockConfirmWithReturnValue(false);

        pluginManager.installPlugin(secondPluginFile);
        Plugin installedPlugin = pluginManager.getInstalledPlugins().iterator().next();
        assertThat(installedPlugin.getInfo().getVersion(), is(Version.fromString("0.1")));
    }

    @Test
    public void loadsNothingFromPluginWhenUserDeclinesOverwrite() throws Exception {
        pluginManager.installPlugin(pluginFile);
        when(pluginLoader.loadPluginInfoFrom(secondPluginFile, java.util.Collections.<JarClassLoader>emptySet())).thenReturn(upgradedAndUninstallablePlugin.getInfo());
        stubbedDialogs.mockConfirmWithReturnValue(false);

        pluginManager.installPlugin(secondPluginFile);
        verify(pluginLoader, never()).loadPlugin(secondPluginFile, java.util.Collections.<JarClassLoader>emptySet());
    }

    @Test
    public void displaysVersionNumbersWhenPromptingForOverwriteConfirmation() throws Exception {
        pluginManager.installPlugin(pluginFile);
        stubbedDialogs.mockConfirmWithReturnValue(false);

        pluginManager.installPlugin(secondPluginFile);
        String confirmationQuestion = stubbedDialogs.getConfirmations().get(0).question;
        assertThat(confirmationQuestion, containsString("0.1"));
        assertThat(confirmationQuestion, containsString("0.2"));
    }

    @Test
    public void unloadsPluginWhenUninstallingIt() throws Exception {
        pluginManager.installPlugin(pluginFile);
        when(fileOperations.deleteFile(any(File.class))).thenReturn(true);

        pluginManager.uninstallPlugin(plugin);
        verify(pluginLoader).unloadPlugin(plugin);
    }

    @Test
    public void promptsForRestartWhenPluginIsNotUninstallable() throws Exception {
        pluginManager.installPlugin(pluginFile);
        when(fileOperations.deleteFile(any(File.class))).thenReturn(true);

        pluginManager.uninstallPlugin(plugin);
        List<String> infoMessages = stubbedDialogs.getInfoMessages();
        assertThat(infoMessages, is(aCollectionWithSize(1)));
        assertThat(infoMessages.get(0), containsString("restart"));
    }

    @Test
    public void doesNotPromptForRestartWhenUninstallingUninstallablePlugin() throws Exception {
        pluginManager.installPlugin(secondPluginFile);
        when(fileOperations.deleteFile(any(File.class))).thenReturn(true);

        pluginManager.uninstallPlugin(upgradedAndUninstallablePlugin);
        List<String> infoMessages = stubbedDialogs.getInfoMessages();
        assertThat(infoMessages, is(aCollectionWithSize(1)));
        assertThat(infoMessages.get(0), not(containsString("restart")));
    }

    private void verifyDependency(PluginInfo pluginInfo, String expectedGroupId, String expectedName, String expectedVersion) {
        PluginId id = pluginInfo.getId();
        assertThat(id.getGroupId(), is(expectedGroupId));
        assertThat(id.getName(), is(expectedName));
        assertThat(pluginInfo.getVersion(), is(Version.fromString(expectedVersion)));
    }

    @Test
    public void findsDependentPlugins() throws Exception {
        pluginManager.installPlugin(pluginFile);
        DependentPlugin dependentPlugin = new DependentPlugin();
        when(pluginLoader.loadPluginInfoFrom(secondPluginFile, java.util.Collections.<JarClassLoader>emptySet())).thenReturn(
                PluginLoader.readPluginInfoFrom(DependentPlugin.class));

        when(pluginLoader.loadPlugin(eq(secondPluginFile), isA(Collection.class))).thenReturn(
                recordWith(dependentPlugin));
        pluginManager.installPlugin(secondPluginFile);

        Collection<Plugin> dependentPlugins = pluginManager.getDependentPlugins(plugin);
        assertThat(dependentPlugins, is(aCollectionWithSize(1)));
        assertThat(dependentPlugins, hasItem(dependentPlugin));
    }

    @Test
    public void loadsClassesFromDependenciesWhenLoadingPlugins() throws Exception {
         /* These three JARs contain the plugins RootPlugin, SingleDependencyPlugin and MultipleDependencyPlugin,
            but moved to another package to ensure that they can only be loaded from the JAR files themselves, i.e. not
            using the standard classpath. The file root-plugin.jar also contains the class RootPluginHelper, which
             is loaded in the class initializer of MultipleDependencyPlugin.
             Thus this test demonstrates that classes can be loaded from dependencies.
         */
        /*
        copyResourceToDirectory("/root-plugin.jar", pluginsDirectory);
        copyResourceToDirectory("/single-dependency-plugin.jar", pluginsDirectory);
        copyResourceToDirectory("/multiple-dependency-plugin.jar", pluginsDirectory);
        PluginManager nonMockingPluginManager = makePluginManager();
        nonMockingPluginManager.loadPlugins();

        assertThat(nonMockingPluginManager.getInstalledPlugins(), is(aCollectionWithSize(3)));
        */
    }

    /*
    Helpers
     */

    private PluginManager makePluginManager() {
        SoapUICore soapUICore = DefaultSoapUICore.createDefault();
        return new PluginManager(soapUICore.getFactoryRegistry(), soapUICore.getActionRegistry(), soapUICore.getListenerRegistry());
    }

    private void copyResourceToDirectory(String resource, File fakeHomeDirectory) throws IOException {
        FileUtils.copyFileToDirectory(new File(PluginManagerTest.class.getResource(resource).getFile()), fakeHomeDirectory);
    }

    private void setUpPluginsAndFiles() throws IOException {
        plugin = new OldPlugin();
        upgradedAndUninstallablePlugin = new UpgradedPlugin();
        pluginFile = new File(pluginsDirectory, "plugin-file.jar");
        when(pluginLoader.loadPlugin(pluginFile, java.util.Collections.<JarClassLoader>emptySet())).thenReturn(recordWith(plugin));
        when(pluginLoader.loadPluginInfoFrom(pluginFile, java.util.Collections.<JarClassLoader>emptySet())).thenReturn(plugin.getInfo());
        secondPluginFile = new File(pluginsDirectory, "plugin-file-0.2.jar");
        when(pluginLoader.loadPlugin(secondPluginFile, java.util.Collections.<JarClassLoader>emptySet())).thenReturn(
                recordWith(upgradedAndUninstallablePlugin));
        when(pluginLoader.loadPluginInfoFrom(secondPluginFile, java.util.Collections.<JarClassLoader>emptySet())).thenReturn(upgradedAndUninstallablePlugin.getInfo());
    }

    private InstalledPluginRecord recordWith(Plugin rootPlugin) {
        return new InstalledPluginRecord(rootPlugin, null);
    }


    /* Fake plugins */

    @PluginConfiguration(groupId = "com.smartbear.test", name = "TestPlugin", version = "0.1")
    private class OldPlugin extends PluginAdapter {

        private FakeAction fakeAction = new FakeAction();
        private FakeFactory fakeFactory = new FakeFactory();

        @Override
        public List<? extends SoapUIAction> getActions() {
            return Arrays.asList(fakeAction);
        }

        @Override
        public Collection<? extends SoapUIFactory> getFactories() {
            return Arrays.asList(fakeFactory);
        }
    }

    @PluginConfiguration(groupId = "com.smartbear.test", name = "TestPlugin", version = "0.2")
    private class UpgradedPlugin extends PluginAdapter implements UninstallablePlugin {

        @Override
        public boolean uninstall() throws Exception {
            return true;
        }
    }

    @PluginConfiguration(groupId = "com.smartbear.test", name = "DependentPlugin", version = "0.2")
    @PluginDependency(groupId = "com.smartbear.test", name = "TestPlugin")
    private class DependentPlugin extends PluginAdapter implements UninstallablePlugin {

        @Override
        public boolean uninstall() throws Exception {
            return true;
        }
    }


    private class FakeAction extends AbstractSoapUIAction<Workspace> {

        private FakeAction() {
            super("Fake", "Fake");
        }

        @Override
        public void perform(Workspace target, Object param) {
        }
    }

    private class FakeFactory implements SoapUIFactory {
        @Override
        public Class<?> getFactoryType() {
            return WsdlRequest.class;
        }
    }
}
