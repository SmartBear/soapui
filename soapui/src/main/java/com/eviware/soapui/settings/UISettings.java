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
 * UI-related settings constants
 *
 * @author Ole.Matzura
 */

public interface UISettings {
    public final static String EDITOR_FONT = UISettings.class.getSimpleName() + "@" + "editor-font";

    @Setting(name = "Close Projects", description = "(close all projects on startup)", type = SettingType.BOOLEAN)
    public final static String CLOSE_PROJECTS = UISettings.class.getSimpleName() + "@close-projects";

    @Setting(name = "Order Projects", description = "(orders projects alphabetically in tree)", type = SettingType.BOOLEAN)
    public final static String ORDER_PROJECTS = UISettings.class.getSimpleName() + "@order-projects";

    @Setting(name = "Order Services", description = "(orders services alphabetically in tree)", type = SettingType.BOOLEAN)
    public final static String ORDER_SERVICES = UISettings.class.getSimpleName() + "@order-services";

    @Setting(name = "Order Requests", description = "(orders Requests alphabetically in tree)", type = SettingType.BOOLEAN)
    public final static String ORDER_REQUESTS = UISettings.class.getSimpleName() + "@order-requests";

    // public final static String ORDER_LOADTESTS =
    // UISettings.class.getSimpleName() + "@order-loadtests";
    //
    // @Setting( name = "Order TestSuites", description =
    // "(orders TestSuites alphabetically in tree)", type = SettingType.BOOLEAN )
    // public final static String ORDER_TESTSUITES =
    // UISettings.class.getSimpleName() + "@order-testsuites";

    @Setting(name = "Show Descriptions", description = "(show description content when available)", type = SettingType.BOOLEAN)
    public static final String SHOW_DESCRIPTIONS = UISettings.class.getSimpleName() + "@show_descriptions";

    @Setting(name = "Save projects on exit", description = "(automatically save all projects on exit)", type = SettingType.BOOLEAN)
    public static final String AUTO_SAVE_PROJECTS_ON_EXIT = UISettings.class.getSimpleName()
            + "@auto_save_projects_on_exit";

    @Setting(name = "Create Backup", description = "(backup project files before they are saved)", type = SettingType.BOOLEAN)
    public final static String CREATE_BACKUP = UISettings.class.getSimpleName() + "@create_backup";

    @Setting(name = "Backup Folder", description = "(folder to backup to, can be both relative or absolute)", type = SettingType.FOLDER)
    public final static String BACKUP_FOLDER = UISettings.class.getSimpleName() + "@backup_folder";

    @Setting(name = "AutoSave Interval", description = "Sets the autosave interval in minutes, 0 = off", type = SettingType.INT)
    public static final String AUTO_SAVE_INTERVAL = UISettings.class.getSimpleName() + "@auto_save_interval";

    public static final String DESKTOP_TYPE = UISettings.class.getSimpleName() + "@desktop-type";

    @Setting(name = "MRU Panel Selection", description = "Activate the most recently used desktop panel when closing a panel",
            type = SettingType.BOOLEAN, defaultValue = "true")
    public static final String MRU_PANEL_SELECTOR = UISettings.class.getSimpleName() + "@mru_panel_selector";

    @Setting(name = "Native LF", description = "(use native Look & Feel - requires restart)", type = SettingType.BOOLEAN)
    public static final String NATIVE_LAF = UISettings.class.getSimpleName() + "@native-laf";

    public final static String NO_RESIZE_REQUEST_EDITOR = UISettings.class.getSimpleName() + "@no_resize_request_editor";
    public final static String START_WITH_REQUEST_TABS = UISettings.class.getSimpleName() + "@start_with_request_tabs";
    public final static String AUTO_VALIDATE_REQUEST = UISettings.class.getSimpleName() + "@auto_validate_request";
    public final static String ABORT_ON_INVALID_REQUEST = UISettings.class.getSimpleName() + "@abort_on_invalid_request";
    public final static String AUTO_VALIDATE_RESPONSE = UISettings.class.getSimpleName() + "@auto_validate_response";

    public static final String SHOW_XML_LINE_NUMBERS = UISettings.class.getSimpleName() + "@show_xml_line_numbers";
    public static final String SHOW_GROOVY_LINE_NUMBERS = UISettings.class.getSimpleName() + "@show_groovy_line_numbers";

    public static final String ORDER_MOCKOPERATION = UISettings.class.getSimpleName() + "@order-mockoperations";
    public static final String ORDER_MOCKCASES = UISettings.class.getSimpleName() + "@order-mockcases";

    public static final String DONT_DISABLE_GROOVY_LOG = UISettings.class.getSimpleName() + "@dont-disable-groovy-log";
    public static final String SHOW_LOGS_AT_STARTUP = UISettings.class.getSimpleName() + "@show_logs_at_startup";

    public static final String SHOW_PROPERTIES_IN_TREE = UISettings.class.getSimpleName() + "@show_properties_in_tree";

    public static final String SHOW_STARTUP_PAGE = UISettings.class.getSimpleName() + "@show_startup_page";
    public static final String DISABLE_TOOLTIPS = UISettings.class.getSimpleName() + "@disable_tooltips";
    public static final String SHOULD_DISPLAY_ANALYTICS_DIALOG = UISettings.class.getSimpleName() + "@display_analytics_opt_dialog";

    @Setting(name = "Disable usage statistics", description = "Stop sending usage statistics", type = SettingType.BOOLEAN)
    public static final String DISABLE_ANALYTICS = UISettings.class.getSimpleName() + "@disable_analytics";
    public static final String ANALYTICS_OPT_OUT_VERSION = UISettings.class.getSimpleName() + "@analytics_opt_out_version";

    @Setting(name = "Normalize Line-Breaks", description = "Normalize line-breaks when saving project", type = SettingType.BOOLEAN)
    public static final String LINEBREAK = UISettings.class.getSimpleName() + "@" + "normalize_line-breaks";

    @Setting(name = "GC Interval", description = "Sets the Garbage Collection interval in seconds, 0 = off", type = SettingType.INT)
    public static final String GC_INTERVAL = UISettings.class.getSimpleName() + "@gc_interval";

    @Setting(name = "Raw Request Message Size", description = "Sets size of raw request that will be shown", type = SettingType.INT)
    public static final String RAW_REQUEST_MESSAGE_SIZE = UISettings.class.getSimpleName()
            + "@raw_request_message_size_show";

    @Setting(name = "Raw Response Message Size", description = "Sets size of raw response that will be shown", type = SettingType.INT)
    public static final String RAW_RESPONSE_MESSAGE_SIZE = UISettings.class.getSimpleName()
            + "@raw_response_message_size_show";

    @Setting(name = "Wrap Raw Messages", description = "Wraps content in Raw message viewers", type = SettingType.BOOLEAN)
    public static final String WRAP_RAW_MESSAGES = UISettings.class.getSimpleName() + "@wrap_raw_messages";

}
