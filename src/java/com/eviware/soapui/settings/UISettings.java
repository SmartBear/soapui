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

package com.eviware.soapui.settings;

/**
 * UI-related settings constants
 * 
 * @author Ole.Matzura
 */

public interface UISettings
{
	public final static String EDITOR_FONT = UISettings.class.getSimpleName() + "@" + "editor-font";

	public final static String CLOSE_PROJECTS = UISettings.class.getSimpleName() + "@close-projects";
	public final static String ORDER_PROJECTS = UISettings.class.getSimpleName() + "@order-projects";
	public final static String ORDER_REQUESTS = UISettings.class.getSimpleName() + "@order-requests";
	public final static String ORDER_LOADTESTS = UISettings.class.getSimpleName() + "@order-loadtests";
	public final static String ORDER_TESTSUITES = UISettings.class.getSimpleName() + "@order-testsuites";
	public final static String ORDER_INTERFACES = UISettings.class.getSimpleName() + "@order-interfaces";
	public final static String NO_RESIZE_REQUEST_EDITOR = UISettings.class.getSimpleName() + "@no_resize_request_editor";
	public final static String START_WITH_REQUEST_TABS = UISettings.class.getSimpleName() + "@start_with_request_tabs";
	public final static String AUTO_VALIDATE_REQUEST = UISettings.class.getSimpleName() + "@auto_validate_request";
	public final static String ABORT_ON_INVALID_REQUEST = UISettings.class.getSimpleName() + "@abort_on_invalid_request";
	public final static String AUTO_VALIDATE_RESPONSE = UISettings.class.getSimpleName() + "@auto_validate_response";
	public final static String CREATE_BACKUP = UISettings.class.getSimpleName() + "@create_backup";
	public final static String BACKUP_FOLDER = UISettings.class.getSimpleName() + "@backup_folder";

	public static final String SHOW_XML_LINE_NUMBERS = UISettings.class.getSimpleName() + "@show_xml_line_numbers";
	public static final String SHOW_GROOVY_LINE_NUMBERS = UISettings.class.getSimpleName() + "@show_groovy_line_numbers";

	public static final String ORDER_MOCKOPERATION = UISettings.class.getSimpleName() + "@order-mockoperations";
	public static final String ORDER_MOCKCASES = UISettings.class.getSimpleName() + "@order-mockcases";

	public static final String DESKTOP_TYPE = UISettings.class.getSimpleName() + "@desktop-type";
	public static final String NATIVE_LAF = UISettings.class.getSimpleName() + "@native-laf";
	public static final String DONT_DISABLE_GROOVY_LOG = UISettings.class.getSimpleName() + "@dont-disable-groovy-log";
	public static final String SHOW_LOGS_AT_STARTUP = UISettings.class.getSimpleName() + "@show_logs_at_startup";

	public static final String SHOW_PROPERTIES_IN_TREE = UISettings.class.getSimpleName() + "@show_properties_in_tree";

	public static final String AUTO_SAVE_INTERVAL = UISettings.class.getSimpleName() + "@auto_save_interval";
	public static final String SHOW_STARTUP_PAGE = UISettings.class.getSimpleName() + "@show_startup_page";

	public static final String SHOW_DESCRIPTIONS = UISettings.class.getSimpleName() + "@show_descriptions";

	public static final String AUTO_SAVE_PROJECTS_ON_EXIT = UISettings.class.getSimpleName()
			+ "@auto_save_projects_on_exit";

}
