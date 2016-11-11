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

package com.eviware.soapui.actions;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.components.DirectoryFormComponent;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

import java.util.ArrayList;

/**
 * Preferences class for ToolsSettings
 *
 * @author ole.matzura
 */

public class ToolsPrefs implements Prefs {
    public static final String AXIS_1_X = "Axis 1.X";
    public static final String WSCOMPILE = "JAX-RPC WSCompile";
    public static final String WSIMPORT = "JAX-WS WSImport";
    public static final String AXIS_2 = "Axis 2";
    public static final String WSTOOLS = "JBossWS wstools";
    public static final String JAVAC = "JDK 1.5 javac";
    public static final String DOTNET = ".NET 2.0 wsdl.exe";
    public static final String XFIRE = "XFire 1.X";
    public static final String CXF = "CXF 2.X";
    public static final String GSOAP = "GSoap";
    public static final String ANT = "ANT 1.6+";
    public static final String XMLBEANS = "XmlBeans 2.X";
    public static final String JAXB = "JAXB xjc";
    public static final String TCPMON = "Apache TcpMon";
    public static final String WSA = "Oracle wsa.jar";
    public static final String WADL = "WADL2Java";
    public static final String LIBRARIES = "Script libraries";
    public static final String HERMES_JMS = "Hermes JMS";

    private static final String[][] TOOLS = {{WSTOOLS, ToolsSettings.JBOSSWS_WSTOOLS_LOCATION},
            {AXIS_1_X, ToolsSettings.AXIS_1_X_LOCATION}, {AXIS_2, ToolsSettings.AXIS_2_LOCATION},
            {WSCOMPILE, ToolsSettings.JWSDP_WSCOMPILE_LOCATION}, {WSIMPORT, ToolsSettings.JWSDP_WSIMPORT_LOCATION},
            {JAVAC, ToolsSettings.JAVAC_LOCATION}, {DOTNET, ToolsSettings.DOTNET_WSDL_LOCATION},
            {CXF, ToolsSettings.CXF_LOCATION}, {XFIRE, ToolsSettings.XFIRE_LOCATION},
            {GSOAP, ToolsSettings.GSOAP_LOCATION}, {ANT, ToolsSettings.ANT_LOCATION},
            {XMLBEANS, ToolsSettings.XMLBEANS_LOCATION}, {JAXB, ToolsSettings.JAXB_LOCATION},
            {TCPMON, ToolsSettings.TCPMON_LOCATION}, {WSA, ToolsSettings.ORACLE_WSA_LOCATION},
            {WADL, ToolsSettings.WADL2JAVA_LOCATION}, {HERMES_JMS, ToolsSettings.HERMES_JMS},};

    private SimpleForm toolsForm;
    private final String title;

    public ToolsPrefs(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Get the tools to be displayed in the Eclipse plugin.
     *
     * @return
     */
    public String[][] getEclipseTools() {
        // Return all tools except .NET related and tools that are part of
        // Eclipse.
        ArrayList<String[]> list = new ArrayList<String[]>();
        for (String[] s : TOOLS) {
            String tool = s[0];

            // Filter out .NET related tools.
            if (tool != ToolsPrefs.DOTNET && tool != ToolsPrefs.GSOAP &&

                    // Filter out tools that are part of Eclipse.
                    tool != ToolsPrefs.JAVAC && tool != ToolsPrefs.ANT) {
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()][]);
    }

    public SimpleForm getForm() {
        if (toolsForm == null) {
            toolsForm = new SimpleForm();
            toolsForm.addSpace(5);
            toolsForm.append(ToolsPrefs.WSTOOLS, new DirectoryFormComponent("Location of JBossWS wstools"));
            toolsForm.append(ToolsPrefs.WSCOMPILE, new DirectoryFormComponent("Location of JWSDP wscompile"));
            toolsForm.append(ToolsPrefs.WSIMPORT, new DirectoryFormComponent("Location of JAX-WS wsimport"));
            toolsForm.append(ToolsPrefs.AXIS_1_X, new DirectoryFormComponent("Location of Axis 1.X"));
            toolsForm.append(ToolsPrefs.AXIS_2, new DirectoryFormComponent("Location of Axis 2"));
            toolsForm.append(ToolsPrefs.DOTNET, new DirectoryFormComponent("Location of .NET 2.0 wsdl.exe"));
            toolsForm.append(ToolsPrefs.XFIRE, new DirectoryFormComponent("Location of XFire 1.X"));
            toolsForm.append(ToolsPrefs.CXF, new DirectoryFormComponent("Location of Apache CXF 2.x"));
            toolsForm.append(ToolsPrefs.ANT, new DirectoryFormComponent("Location of Apache ANT 1.6.5 or later"));
            toolsForm.append(ToolsPrefs.GSOAP, new DirectoryFormComponent("Location of GSoap 2.X"));
            toolsForm.append(ToolsPrefs.JAXB, new DirectoryFormComponent("Location of JAXB xjc"));
            toolsForm.append(ToolsPrefs.XMLBEANS, new DirectoryFormComponent("Location of XMLBeans 2.X"));
            toolsForm.append(ToolsPrefs.JAVAC, new DirectoryFormComponent("Location of JDK 1.5 javac"));
            toolsForm.append(ToolsPrefs.TCPMON, new DirectoryFormComponent("Location of TcpMon directory"));
            toolsForm.append(ToolsPrefs.WSA, new DirectoryFormComponent("Location of Orace wsa.jar"));
            toolsForm.append(ToolsPrefs.WADL, new DirectoryFormComponent("Location of wadl2java script"));
            toolsForm.append(ToolsPrefs.HERMES_JMS, new DirectoryFormComponent("Location of HermesJMS"));
            toolsForm.addSpace(5);
        }

        return toolsForm;
    }

    public void getFormValues(Settings settings) {
        StringToStringMap values = new StringToStringMap();
        toolsForm.getValues(values);
        storeValues(values, settings);
    }

    public void storeValues(StringToStringMap values, Settings settings) {
        for (int i = 0; i < TOOLS.length; i++) {
            settings.setString(TOOLS[i][1], values.get(TOOLS[i][0]));
        }
    }

    public void setFormValues(Settings settings) {
        getForm().setValues(getValues(settings));
    }

    public StringToStringMap getValues(Settings settings) {
        StringToStringMap toolsValues = new StringToStringMap();
        for (int i = 0; i < TOOLS.length; i++) {
            toolsValues.put(TOOLS[i][0], settings.getString(TOOLS[i][1], ""));
        }
        return toolsValues;
    }
}
