/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui;

import java.io.File;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;

public interface SoapUICore
{
	public final static String DEFAULT_SETTINGS_FILE = "soapui-settings.xml";

	public Settings getSettings();

	public MockEngine getMockEngine();

	public SoapUIListenerRegistry getListenerRegistry();

	public SoapUIActionRegistry getActionRegistry();

	public String saveSettings() throws Exception;

	public void importSettings( File file ) throws Exception;
}