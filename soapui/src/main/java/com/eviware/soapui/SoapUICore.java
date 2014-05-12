/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.plugins.PluginLoader;
import com.eviware.soapui.security.registry.SecurityScanRegistry;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;

import java.io.File;

public interface SoapUICore {
    public final static String DEFAULT_SETTINGS_FILE = "soapui-settings.xml";

    public Settings getSettings();

    public MockEngine getMockEngine();

    public SoapUIListenerRegistry getListenerRegistry();

    public SoapUIActionRegistry getActionRegistry();

    public SoapUIFactoryRegistry getFactoryRegistry();

    public String saveSettings() throws Exception;

    public Settings importSettings(File file) throws Exception;

    public SoapUIExtensionClassLoader getExtensionClassLoader();

    public SecurityScanRegistry getSecurityScanRegistry();

    public PluginLoader getPluginLoader();
}
