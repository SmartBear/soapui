/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
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

import org.junit.Test;

import java.net.URL;
import java.util.List;

import static com.eviware.soapui.utils.CommonMatchers.aCollectionWithSize;
import static com.eviware.soapui.utils.CommonMatchers.endsWith;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class PluginLoaderTest {


    @Test
    public void canLoadAvailablePluginsFromJson() throws Exception {
        PluginLoader pluginLoader = new PluginLoader(null, null, null, null);
        URL jsonUrl = PluginLoaderTest.class.getResource("availablePlugins.json");
        List<AvailablePlugin> availablePlugins = pluginLoader.loadAvailablePluginsFrom(jsonUrl);

        assertThat(availablePlugins, is(aCollectionWithSize(2)));
        verifyCorrectContent(availablePlugins.get(0), "1");
        verifyCorrectContent(availablePlugins.get(1), "2");
    }

    private void verifyCorrectContent(AvailablePlugin availablePlugin, String suffix) {
        PluginInfo pluginInfo = availablePlugin.getPluginInfo();
        assertThat(pluginInfo.getId().getGroupId(), endsWith(suffix));
        assertThat(pluginInfo.getId().getName(), endsWith(suffix));
        assertThat(pluginInfo.getVersion().toString(), endsWith(suffix));
        assertThat(pluginInfo.getDescription(), endsWith(suffix));
        assertThat(availablePlugin.getUrl().toString(), endsWith(suffix + ".jar"));
    }


}
