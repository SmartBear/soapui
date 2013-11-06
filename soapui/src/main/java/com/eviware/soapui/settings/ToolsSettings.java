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

/**
 * Tools/Integration-related settings constants
 * 
 * @author Ole.Matzura
 */

public interface ToolsSettings
{
	public final static String JWSDP_WSCOMPILE_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "jwsdp_wscompile";
	public final static String JWSDP_WSIMPORT_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "jwsdp_wsimport";
	public final static String JBOSSWS_WSTOOLS_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "jbossws_wstools";
	public final static String JAVAC_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "javac";
	public final static String AXIS_1_X_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "axis_1_X";
	public final static String AXIS_2_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "axis_2";
	public final static String DOTNET_WSDL_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "dotnet_wsdl";
	public final static String XFIRE_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "xfire";
	public final static String CXF_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "cxf";
	public final static String ANT_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "ant";
	public final static String GSOAP_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "gsoap";
	public final static String XMLBEANS_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "xmlbeans";
	public final static String JAXB_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "jaxb";
	public final static String TCPMON_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "tcpmon";
	public static final String ORACLE_WSA_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "wsa";
	public static final String WADL2JAVA_LOCATION = ToolsSettings.class.getSimpleName() + "@" + "wadl2java";
	public static final String HERMES_JMS = ToolsSettings.class.getSimpleName() + "@" + "hermesjms";
	// public static final String SCRIPT_LIBRARIES =
	// ToolsSettings.class.getSimpleName() + "@" + "libraries";
}
