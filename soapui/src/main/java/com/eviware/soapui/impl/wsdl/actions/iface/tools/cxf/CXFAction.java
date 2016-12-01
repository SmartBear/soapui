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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.cxf;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

import java.io.File;
import java.io.IOException;

/**
 * Invokes Apache CXF wsdl2java utility
 *
 * @author Ole.Matzura
 */

public class CXFAction extends AbstractToolsAction<Interface> {
    private static final String PACKAGE = "Package";
    private static final String OUTPUT = "Output Directory";
    private static final String NAMESPACE_MAPPING = "Output Packages";
    private static final String CATALOG_FILE = "Catalog";
    private static final String SERVER_STUBS = "Server";
    private static final String CLIENT_STUBS = "Client";
    private static final String IMPL_STUBS = "Implementation";
    private static final String ANT_FILE = "build.xml";
    private static final String GENERATE_ALL = "All Code";
    private static final String COMPILE = "Compile";
    private static final String CLASSDIR = "Class Folder";
    private static final String VALIDATE = "Validate WSDL";
    private static final String EXCLUDE_NAMESPACES = "Exclude namespaces";
    private static final String EXSH = "EXSH";
    private static final String DNS = "DNS";
    private static final String DEX = "DEX";

    public static final String SOAPUI_ACTION_ID = "CXFAction";
    private static final String BINDING_FILES = "Bindings";

    public CXFAction() {
        super("Apache CXF", "Generates Apache CXF code using the wsdl2java utility");
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Apache CXF Stubs");

        XForm mainForm = builder.createForm("Basic");
        addWSDLFields(mainForm, modelItem);

        mainForm.addTextField(OUTPUT, "Root directory for all emitted files.", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(PACKAGE, "Default Package for generated classes", XForm.FieldType.JAVA_PACKAGE);
        mainForm.addNameSpaceTable(NAMESPACE_MAPPING, modelItem);

        mainForm.addCheckBox(CLIENT_STUBS, "Generates starting point code for a client mainline");
        mainForm.addCheckBox(SERVER_STUBS, "Generates starting point code for a server mainline");
        mainForm.addCheckBox(IMPL_STUBS, "Generates starting point code for an implementation object");
        mainForm.addCheckBox(ANT_FILE, "Generates the Ant build.xml file");
        mainForm.addCheckBox(GENERATE_ALL,
                "<html>Generates all starting point code: types, <br>service proxy, service interface, server mainline, "
                        + "<br>client mainline, implementation object, and an Ant build.xml file</html>");

        XForm advForm = builder.createForm("Advanced");

        advForm.addTextField(BINDING_FILES, "Space-separated list of JAXWS or JAXB binding files", XForm.FieldType.TEXT);
        advForm.addCheckBox(COMPILE, "Compiles generated Java files");
        advForm.addTextField(CLASSDIR, "The directory into which the compiled class files are written",
                XForm.FieldType.FOLDER);

        advForm.addTextField(CATALOG_FILE, "The catalog file to map the imported wsdl/schema", XForm.FieldType.FILE);

        advForm.addNameSpaceTable(EXCLUDE_NAMESPACES, modelItem);
        advForm.addCheckBox(EXSH, "Enables processing of extended soap header message binding");
        advForm.addCheckBox(DNS, "Enables loading of the default namespace package name mapping");
        advForm.addCheckBox(DEX, "Enables loading of the default excludes namespace mapping");
        advForm.addCheckBox(VALIDATE, "Enables validating the WSDL before generating the code");

        buildArgsForm(builder, true, "wsdl2java");

        return builder.buildDialog(buildDefaultActions(HelpUrls.CXFWSDL2JAVA_HELP_URL, modelItem),
                "Specify arguments for Apache CXF wsdl2java", UISupport.TOOL_ICON);
    }

    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);
        values.putIfMissing(DNS, "true");
        values.putIfMissing(DEX, "true");
        return values;
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String xfireDir = SoapUI.getSettings().getString(ToolsSettings.CXF_LOCATION, null);
        if (Tools.isEmpty(xfireDir)) {
            UISupport.showErrorMessage("CXF directory must be set in global preferences");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(values, modelItem);
        builder.command(args.getArgs());
        builder.directory(new File(xfireDir));

        toolHost.run(new ProcessToolRunner(builder, "Apache CXF wsdl2java", modelItem));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, Interface modelItem) throws IOException {
        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT), ""));

        ArgumentBuilder builder = new ArgumentBuilder(values);

        builder.startScript("wsdl2java");
        StringToStringMap nsMappings = StringToStringMap.fromXml(values.get(NAMESPACE_MAPPING));
        String packages = values.get(PACKAGE).trim();
        for (String key : nsMappings.keySet()) {
            packages += " " + key + "=" + nsMappings.get(key);
        }

        if (packages.length() > 0) {
            builder.addBoolean(NAMESPACE_MAPPING, "-p", null, packages);
        }

        builder.addString(BINDING_FILES, "-b");
        builder.addString(CATALOG_FILE, "-catalog");
        builder.addString(OUTPUT, "-d");

        builder.addBoolean(COMPILE, "-compile");
        builder.addString(CLASSDIR, "-classdir");

        builder.addBoolean(CLIENT_STUBS, "-client");
        builder.addBoolean(SERVER_STUBS, "-server");
        builder.addBoolean(IMPL_STUBS, "-impl");
        builder.addBoolean(GENERATE_ALL, "-all");
        builder.addBoolean(ANT_FILE, "-ant");

        StringToStringMap excludes = StringToStringMap.fromXml(values.get(EXCLUDE_NAMESPACES));
        for (String key : excludes.keySet()) {
            String value = excludes.get(key);
            if (value.equals("-")) {
                builder.addArgs("-b", key);
            } else {
                builder.addArgs("-b", key + "=" + value);
            }
        }

        builder.addBoolean(EXSH, "-exsh", "true", "false");
        builder.addBoolean(DNS, "-dns", "true", "false");
        builder.addBoolean(DEX, "-dex", "true", "false");
        builder.addBoolean(VALIDATE, "-validate");

        builder.addArgs("-verbose");
        addToolArgs(values, builder);
        builder.addArgs(getWsdlUrl(values, modelItem));
        return builder;
    }
}
