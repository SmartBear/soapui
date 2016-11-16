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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.xmlbeans;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.settings.ToolsSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

import java.io.File;

/**
 * Generates XMLBeans for given interface
 *
 * @author Ole.Matzura
 */

public class XmlBeans2Action extends AbstractToolsAction<Interface> {
    private final static String XSBTARGET = "class/xsb target";
    private final static String SRCTARGET = "src target";
    private final static String SRCONLY = "src only";
    private final static String JARFILE = "out jar";
    private final static String DOWNLOADS = "downloads";
    private final static String NOUPA = "noupa";
    private final static String NOPVR = "nopvr";
    private final static String NOANN = "noann";
    private final static String NOVDOC = "novdoc";
    private final static String VERBOSE = "verbose";
    private final static String JAVASOURCE = "javasource";
    private final static String DEBUG = "debug";
    private final static String ALLOWMDEF = "allowmdef";
    private final static String CATALOG = "catalog file";
    private final static String XSDCONFIG = "xsdconfig";
    public static final String SOAPUI_ACTION_ID = "XmlBeans2Action";
    private String output;

    public XmlBeans2Action() {
        super("XmlBeans Classes", "Generates XmlBeans classes");
    }

    @Override
    public boolean applies(Interface target) {
        Interface iface = (Interface) target;
        return !iface.getProject().hasNature(Project.JBOSSWS_NATURE_ID);
    }

    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);
        if (output != null) {
            values.put(SRCTARGET, output);
        }

        return values;
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("XmlBeans Classes");

        XForm mainForm = builder.createForm("Basic");
        addWSDLFields(mainForm, modelItem);

        mainForm.addTextField(XSBTARGET, "Target directory for CLASS and XSB files", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(SRCTARGET, "Target directory for generated JAVA files", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(JARFILE, "The name of the output JAR that will contain the result of compilation",
                XForm.FieldType.PROJECT_FILE);

        mainForm.addCheckBox(SRCONLY, "(Do not compile JAVA files or jar the output)");
        mainForm.addCheckBox(DOWNLOADS, "(Permit network downloads for imports and includes)");
        mainForm.addCheckBox(NOUPA, "(Do not enforce the unique particle attribution rule)");
        mainForm.addCheckBox(NOPVR, "(Do not enforce the particle valid (restriction) rule)");
        mainForm.addCheckBox(NOANN, "(Ignore annotations)");
        mainForm.addCheckBox(NOVDOC, "(Do not validate contents of <documentation> elements)");
        mainForm.addCheckBox(DEBUG, "(Compile with debug symbols)");

        mainForm.addComboBox(JAVASOURCE, new String[]{"1.5", "1.4"},
                "Generate Java source compatible for the specified Java version");

        mainForm.addTextField(ALLOWMDEF,
                "Ignore multiple defs in given namespaces. Use  ##local  to specify the no-namespace in that list",
                XForm.FieldType.TEXT);
        mainForm.addTextField(CATALOG, "Catalog file to use for resolving external entities",
                XForm.FieldType.PROJECT_FILE);
        mainForm.addTextField(XSDCONFIG, "Path to .xsdconfig file containing type-mapping information",
                XForm.FieldType.PROJECT_FILE);

        mainForm.addCheckBox(VERBOSE, "(Print more informational messages)");

        buildArgsForm(builder, false, "scomp");

        return builder.buildDialog(buildDefaultActions(HelpUrls.XMLBEANS_HELP_URL, modelItem),
                "Specify arguments for XmlBeans 2.X scomp", UISupport.TOOL_ICON);
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String xbDir = SoapUI.getSettings().getString(ToolsSettings.XMLBEANS_LOCATION, null);
        if (Tools.isEmpty(xbDir)) {
            UISupport.showErrorMessage("XmlBeans location must be set in global preferences");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder argumentBuilder = buildArgs(values, modelItem);
        builder.command(argumentBuilder.getArgs());
        builder.directory(new File(xbDir + File.separatorChar + "bin"));

        toolHost.run(new ProcessToolRunner(builder, "XmlBeans", modelItem));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, Interface modelItem) {
        ArgumentBuilder builder = new ArgumentBuilder(values);

        values.put(XSBTARGET, Tools.ensureDir(values.get(XSBTARGET), ""));
        values.put(SRCTARGET, Tools.ensureDir(values.get(SRCTARGET), ""));

        builder.startScript("scomp", ".cmd", "");

        builder.addString(XSBTARGET, "-d");
        builder.addString(SRCTARGET, "-src");
        builder.addString(JARFILE, "-out");

        builder.addBoolean(SRCONLY, "-srconly");
        builder.addBoolean(DOWNLOADS, "-dl");
        builder.addBoolean(NOUPA, "-noupa");
        builder.addBoolean(NOPVR, "-nopvr");
        builder.addBoolean(NOANN, "-noann");
        builder.addBoolean(NOVDOC, "-novdoc");
        builder.addBoolean(DEBUG, "-debug");

        builder.addString(JAVASOURCE, "-javasource");
        builder.addString(ALLOWMDEF, "-allowmdef");
        builder.addString(CATALOG, "-catalog");
        builder.addBoolean(VERBOSE, "-verbose");

        String javac = ToolsSupport.getToolLocator().getJavacLocation(false);

        if (StringUtils.hasContent(javac)) {
            builder.addArgs("-compiler", javac + File.separatorChar + "javac");
        }
        addToolArgs(values, builder);

        builder.addString(XSDCONFIG, null);
        builder.addArgs(getWsdlUrl(values, modelItem));

        return builder;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
