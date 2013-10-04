/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui;

import java.io.File;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.security.registry.SecurityScanRegistry;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;

public interface SoapUICore
{
	public final static String DEFAULT_SETTINGS_FILE = "soapui-settings.xml";

	public Settings getSettings();

	public MockEngine getMockEngine();

	public SoapUIListenerRegistry getListenerRegistry();

	public SoapUIActionRegistry getActionRegistry();

	public SoapUIFactoryRegistry getFactoryRegistry();

	public String saveSettings() throws Exception;

	public Settings importSettings( File file ) throws Exception;

	public SoapUIExtensionClassLoader getExtensionClassLoader();

	public SecurityScanRegistry getSecurityScanRegistry();
}
