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

package com.eviware.soapui.settings;

import com.eviware.soapui.settings.Setting.SettingType;

/**
 * WS-I Testing-Tools related settings constants
 *
 * @author Ole.Matzura
 */

public interface WSISettings {
    public static String BASIC_PROFILE_10_TAD = "BasicSecurityProfile-1.0-TAD.xml";
    public static String BASICf_PROFILE_11_TAD = "BasicSecurityProfile-1.1-TAD.xml";

    public static final String ENDPOINT_LOG_FILE_CORRELATION_TYPE = "endpoint";
    public static final String NAMESPACE_LOG_FILE_CORRELATION_TYPE = "namespace";
    public static final String OPERATION_LOG_FILE_CORRELATION_TYPE = "operation";

    @Setting(name = "Verbose", description = "Verbose output of WSI tools", type = SettingType.BOOLEAN)
    public final static String VERBOSE = WSISettings.class.getSimpleName() + "@" + "verbose";

    /*
    @Setting(name = "Results type", description = "Specify which types of assertions to report", type = SettingType.ENUMERATION, values = {
            "all", "onlyFailed", "notPassed", "notInfo"})
    public final static String RESULTS_TYPE = WSISettings.class.getSimpleName() + "@" + "results_type";
    */

    @Setting(name = "Profile", description = "Specify the type of profile", type = SettingType.ENUMERATION, values = {
            BASIC_PROFILE_10_TAD, BASICf_PROFILE_11_TAD})
    public final static String PROFILE_TYPE = WSISettings.class.getSimpleName() + "@" + "profile_type";

    @Setting(name = "Correlation", description = "Specify the log file correlation type", type = SettingType.ENUMERATION, values = {
            ENDPOINT_LOG_FILE_CORRELATION_TYPE, NAMESPACE_LOG_FILE_CORRELATION_TYPE, OPERATION_LOG_FILE_CORRELATION_TYPE})
    public final static String CORRELATION_TYPE = WSISettings.class.getSimpleName() + "@" + "correlation_type";

    @Setting(name = "Message entry", description = "Message entries should be included in the report", type = SettingType.BOOLEAN)
    public final static String MESSAGE_ENTRY = WSISettings.class.getSimpleName() + "@" + "messageEntry";

    @Setting(name = "Failure message", description = "Failure message defined for each test assertion should be included in the report", type = SettingType.BOOLEAN)
    public final static String FAILURE_MESSAGE = WSISettings.class.getSimpleName() + "@" + "failureMessage";

    @Setting(name = "Assertion description", description = "Description of each test assertion should be included in the report", type = SettingType.BOOLEAN)
    public final static String ASSERTION_DESCRIPTION = WSISettings.class.getSimpleName() + "@" + "assertionDescription";

    @Setting(name = "Tool location", description = "Specifies the root folder of the wsi-test-tools installation", type = SettingType.FOLDER)
    public final static String WSI_LOCATION = WSISettings.class.getSimpleName() + "@" + "location";

    @Setting(name = "Show log", description = "Show console-log for ws-i analyzer", type = SettingType.BOOLEAN)
    public final static String SHOW_LOG = WSISettings.class.getSimpleName() + "@" + "showLog";

    @Setting(name = "Output folder", description = "Specifies the output folder for reports generated from commandline", type = SettingType.FOLDER)
    public final static String OUTPUT_FOLDER = WSISettings.class.getSimpleName() + "@" + "outputFolder";
}
