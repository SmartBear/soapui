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

package com.eviware.soapui.settings;

import com.eviware.soapui.settings.Setting.SettingType;

/**
 * WS-I Testing-Tools related settings constants
 * 
 * @author Ole.Matzura
 */

public interface WSISettings
{
	@Setting( name = "Verbose", description = "sets verbose output of WSI tools", type = SettingType.BOOLEAN )
	public final static String VERBOSE = WSISettings.class.getSimpleName() + "@" + "verbose";

	@Setting( name = "Results Type", description = "specify which types of assertions to report", type = SettingType.ENUMERATION, values = {
			"all", "onlyFailed", "notPassed", "notInfo" } )
	public final static String RESULTS_TYPE = WSISettings.class.getSimpleName() + "@" + "results_type";

	@Setting( name = "Message Entry", description = "if message entries should be included in the report", type = SettingType.BOOLEAN )
	public final static String MESSAGE_ENTRY = WSISettings.class.getSimpleName() + "@" + "messageEntry";

	@Setting( name = "Failure Message", description = "if failure message defined for each test assertion should be included in the report", type = SettingType.BOOLEAN )
	public final static String FAILURE_MESSAGE = WSISettings.class.getSimpleName() + "@" + "failureMessage";

	@Setting( name = "Assertion Description", description = "if description of each test assertion should be included in the report", type = SettingType.BOOLEAN )
	public final static String ASSERTION_DESCRIPTION = WSISettings.class.getSimpleName() + "@" + "assertionDescription";

	@Setting( name = "Tool Location", description = "specifies the root folder of the wsi-test-tools installation", type = SettingType.FOLDER )
	public final static String WSI_LOCATION = WSISettings.class.getSimpleName() + "@" + "location";

	@Setting( name = "Show Log", description = "show console-log for ws-i analyzer", type = SettingType.BOOLEAN )
	public final static String SHOW_LOG = WSISettings.class.getSimpleName() + "@" + "showLog";

	@Setting( name = "Output Folder", description = "specifies the output folder for reports generated from commandline", type = SettingType.FOLDER )
	public final static String OUTPUT_FOLDER = WSISettings.class.getSimpleName() + "@" + "outputFolder";
}
