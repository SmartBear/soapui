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
import com.eviware.soapui.Util.SoapUITools;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportFactory;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.workspace.WorkspaceListener;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.eviware.soapui.utils.CommonMatchers.aCollectionWithSize;
import static com.eviware.soapui.utils.CommonMatchers.anEmptyCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class PluginLoaderTest {

    private PluginLoader pluginLoader;
    private DefaultSoapUICore defaultSoapUICore;
    private String originalSoapUIHome;

    @Before
    public void setUp() throws Exception {
        originalSoapUIHome = SoapUITools.absolutePath(SoapUITools.soapuiHomeDir());
        File fakeSoapUIHome = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        System.setProperty("soapui.home", fakeSoapUIHome.getAbsolutePath());
        defaultSoapUICore = DefaultSoapUICore.createDefault();
        pluginLoader = new PluginLoader(defaultSoapUICore.getFactoryRegistry(), defaultSoapUICore.getActionRegistry(),
                defaultSoapUICore.getListenerRegistry());
    }

    @After
    public void tearDown() throws Exception {
        if (originalSoapUIHome != null) {
            System.setProperty("soapui.home", originalSoapUIHome);
        }
    }

    @Test
    public void installsPluginFromFile() throws IOException {
        File pluginFile = new File(PluginLoaderTest.class.getResource("plugin-example-1.0.jar").getPath());
        Plugin loadedPlugin = pluginLoader.loadPlugin(pluginFile, java.util.Collections.<JarClassLoader>emptySet()).plugin;

        assertThat(loadedPlugin.getInfo().getId().getName(), is("Example plugin"));
        assertThat(defaultSoapUICore.getActionRegistry().getAction("ExampleAction"), is(notNullValue()));
        assertThat(defaultSoapUICore.getFactoryRegistry().getFactories(RequestTransportFactory.class),
                is(aCollectionWithSize(1)));
        assertThat(defaultSoapUICore.getListenerRegistry().getListeners(WorkspaceListener.class),
                is(aCollectionWithSize(1)));
    }

    @Test
    public void installsBarePluginFromFile() throws IOException {
        File pluginFile = new File(PluginLoaderTest.class.getResource("bare-plugin.jar").getPath());
        Plugin loadedPlugin = pluginLoader.loadPlugin(pluginFile, java.util.Collections.<JarClassLoader>emptySet()).plugin;

        assertThat(loadedPlugin.getInfo().getId().getName(), is("NonAdapterPlugin"));
    }

    @Test
    public void loadsPluginInfoFromFile() throws IOException {
        File pluginFile = new File(PluginLoaderTest.class.getResource("plugin-example-1.0.jar").getPath());
        PluginInfo pluginInfo = pluginLoader.loadPluginInfoFrom(pluginFile, java.util.Collections.<JarClassLoader>emptySet());

        assertThat(pluginInfo.getId().getName(), is("Example plugin"));
        assertThat(pluginInfo.getId().getGroupId(), is("com.smartbear.soapui"));
        assertThat(pluginInfo.getVersion(), is(Version.fromString("0.1")));
    }

    @Test
    public void removesPluginComponentsOnUnload() throws Exception {
        File pluginFile = new File(PluginLoaderTest.class.getResource("plugin-example-1.0.jar").getPath());
        Plugin loadedPlugin = pluginLoader.loadPlugin(pluginFile, java.util.Collections.<JarClassLoader>emptySet()).plugin;

        pluginLoader.unloadPlugin(loadedPlugin);
        assertThat(defaultSoapUICore.getActionRegistry().getAction("ExampleAction"), is(nullValue()));
        assertThat(defaultSoapUICore.getFactoryRegistry().getFactories(RequestTransportFactory.class),
                is(anEmptyCollection()));
        assertThat(defaultSoapUICore.getListenerRegistry().getListeners(WorkspaceListener.class),
                is(anEmptyCollection()));
    }

    @Test(expected = InvalidPluginException.class)
    public void rejectsPluginThatRequiresHigherReadyApiVersion() throws Exception {
        pluginLoader.loadPlugin(ScienceFictionPlugin.class, null);

    }

    @Test
    public void acceptsPluginThatDoesNotRequireAHigherReadyApiVersion() throws Exception {
        pluginLoader.loadPlugin(VanillaPlugin.class, null);

    }

    @Test
    public void setsPluginInPluginAwareComponents() throws Exception {
        Plugin plugin = pluginLoader.loadPlugin(AwarenessPlugin.class, null);
        AwareAction action = (AwareAction)plugin.getActions().get(0);
        AwareFactory factory = (AwareFactory)plugin.getFactories().iterator().next();
        assertThat(action.plugin, is(plugin));
        assertThat(factory.plugin, is(plugin));
    }

    @PluginConfiguration(groupId = "com.smartbear.ready", name = "Time warp plugin", version = "0.1",
            minimumReadyApiVersion = "99.0.0", autoDetect = false)
    public static class ScienceFictionPlugin extends PluginAdapter {

    }

    @PluginConfiguration(groupId = "com.smartbear.ready", name = "Down to earth plugin", version = "0.1", autoDetect = false)
    public static class VanillaPlugin extends PluginAdapter {

    }


    @PluginConfiguration(groupId = "com.smartbear.ready", name = "Awareness plugin", version = "0.1", autoDetect = false)
    public static class AwarenessPlugin extends PluginAdapter {

        @Override
        public List<? extends SoapUIAction> getActions() {
            return Arrays.asList(new AwareAction());
        }

        @Override
        public Collection<? extends SoapUIFactory> getFactories() {
            return Arrays.asList(new AwareFactory());
        }


    }

    private static class AwareAction extends AbstractSoapUIAction implements PluginAware {
        private Plugin plugin;

        public AwareAction() {
            super("AwareAction", "An Aware Action");
        }

        @Override
        public void perform(ModelItem target, Object param) {
        }

        @Override
        public void setPlugin(Plugin plugin) {
            this.plugin = plugin;
        }

    }

    private static class AwareFactory implements SoapUIFactory, PluginAware {

        private Plugin plugin;

        @Override
        public void setPlugin(Plugin plugin) {
            this.plugin = plugin;
        }



        @Override
        public Class<?> getFactoryType() {
            return String.class;
        }
    }




}
