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

package com.eviware.soapui.settings;

import com.eviware.soapui.settings.Setting.SettingType;

/**
 * WS-I Testing-Tools related settings constants
 *
 * @author Ole.Matzura
 */

public interface WSISettings {
    @Setting(name = "Verbose", description = "sets verbose output of WSI tools", type = SettingType.BOOLEAN)
    public final static String VERBOSE = WSISettings.class.getSimpleName() + "@" + "verbose";

    @Setting(name = "Results Type", description = "specify which types of assertions to report", type = SettingType.ENUMERATION, values = {
            "all", "onlyFailed", "notPassed", "notInfo"})
    public final static String RESULTS_TYPE = WSISettings.class.getSimpleName() + "@" + "results_type";

    @Setting(name = "Message Entry", description = "if message entries should be included in the report", type = SettingType.BOOLEAN)
    public final static String MESSAGE_ENTRY = WSISettings.class.getSimpleName() + "@" + "messageEntry";

    @Setting(name = "Failure Message", description = "if failure message defined for each test assertion should be included in the report", type = SettingType.BOOLEAN)
    public final static String FAILURE_MESSAGE = WSISettings.class.getSimpleName() + "@" + "failureMessage";

    @Setting(name = "Assertion Description", description = "if description of each test assertion should be included in the report", type = SettingType.BOOLEAN)
    public final static String ASSERTION_DESCRIPTION = WSISettings.class.getSimpleName() + "@" + "assertionDescription";

    @Setting(name = "Tool Location", description = "specifies the root folder of the wsi-test-tools installation", type = SettingType.FOLDER)
    public final static String WSI_LOCATION = WSISettings.class.getSimpleName() + "@" + "location";

    @Setting(name = "Show Log", description = "show console-log for ws-i analyzer", type = SettingType.BOOLEAN)
    public final static String SHOW_LOG = WSISettings.class.getSimpleName() + "@" + "showLog";

    @Setting(name = "Output Folder", description = "specifies the output folder for reports generated from commandline", type = SettingType.FOLDER)
    public final static String OUTPUT_FOLDER = WSISettings.class.getSimpleName() + "@" + "outputFolder";
}
