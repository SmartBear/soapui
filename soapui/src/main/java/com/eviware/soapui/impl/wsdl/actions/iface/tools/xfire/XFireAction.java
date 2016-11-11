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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.xfire;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.settings.ToolsSupport;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormTextField;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Invokes XFire wsgen utility
 *
 * @author Ole.Matzura
 */

public class XFireAction extends AbstractToolsAction<Interface> {
    private static final String PACKAGE = "Package";
    private static final String OUTPUT = "Output Directory";
    private static final String BINDING = "Binding";
    private static final String EXTERNAL_BINDINGS = "External Bindings";
    private static final String BASE_URI = "Base URI";
    private static final String PROFILE = "Profile";
    private static final String CLASSPATH = "Classpath";
    private static final String OVERWRITE = "Overwrite previously generated files";
    private static final String EXPLICIT_ANNOTATION = "Explicit Annotations";
    private static final String SERVER_STUBS = "Generate Server Stubs";
    public static final String SOAPUI_ACTION_ID = "XFireAction";

    public XFireAction() {
        super("XFire 1.X Stubs", "Generates XFire 1.X stubs using the wsgen utility");
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("XFire 1.X Stubs");

        XForm mainForm = builder.createForm("Basic");
        addWSDLFields(mainForm, modelItem);

        mainForm.addTextField(OUTPUT, "Root directory for all emitted files.", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(PACKAGE, "Package for generated classes", XForm.FieldType.JAVA_PACKAGE).setRequired(
                true, "Package is required");
        XFormField bindingCombo = mainForm.addComboBox(BINDING, new String[]{"jaxb", "xmlbeans"},
                "Binding framework to use");

        XFormTextField cpField = mainForm.addTextField(CLASSPATH, "Classpath to generated xmlbeans for binding",
                XForm.FieldType.PROJECT_FILE);
        XFormTextField extBindingsField = mainForm.addTextField(EXTERNAL_BINDINGS, "External jaxb binding file(s)",
                XForm.FieldType.PROJECT_FILE);
        bindingCombo.addComponentEnabler(cpField, "xmlbeans");
        bindingCombo.addComponentEnabler(extBindingsField, "jaxb");

        mainForm.addTextField(PROFILE, "Profile to use for generating artifacts", XForm.FieldType.TEXT);
        mainForm.addTextField(BASE_URI, "Base URI to use", XForm.FieldType.URL);
        mainForm.addCheckBox(OVERWRITE, null);
        mainForm.addCheckBox(EXPLICIT_ANNOTATION, null);
        mainForm.addCheckBox(SERVER_STUBS, null);

        buildArgsForm(builder, true, "WsGen");

        return builder.buildDialog(buildDefaultActions(HelpUrls.XFIRE_HELP_URL, modelItem),
                "Specify arguments for XFire 1.X WsGen", UISupport.TOOL_ICON);
    }

    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);
        values.putIfMissing(BINDING, "jaxb");
        return values;
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String xfireDir = SoapUI.getSettings().getString(ToolsSettings.XFIRE_LOCATION, null);
        if (Tools.isEmpty(xfireDir)) {
            UISupport.showErrorMessage("XFire 1.X directory must be set in global preferences");
            return;
        }

        String antDir = ToolsSupport.getToolLocator().getAntDir(true);
        if (Tools.isEmpty(antDir)) {
            UISupport.showErrorMessage("ANT directory must be set in global preferences");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(values, buildClasspath(xfireDir, antDir, values.get(CLASSPATH)), modelItem);
        builder.command(args.getArgs());
        builder.directory(new File(xfireDir));

        toolHost.run(new ProcessToolRunner(builder, "XFire 1.X WsGen", modelItem));
    }

    private String buildClasspath(String xfireDir, String antDir, String additional) {
        String libDir = xfireDir + File.separatorChar + "lib";
        String[] xfireLibs = new File(libDir).list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (!name.endsWith(".jar")) {
                    return false;
                }

                if (name.startsWith("jaxb")) {
                    return name.indexOf("2.0") > 0;
                }

                return true;
            }
        });

        String modulesDir = xfireDir + File.separatorChar + "modules";
        String[] xfireJars = new File(modulesDir).list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        StringBuilder classpath = new StringBuilder();

        for (int c = 0; c < xfireLibs.length; c++) {
            if (c > 0) {
                classpath.append(File.pathSeparatorChar);
            }

            classpath.append(libDir + File.separatorChar + xfireLibs[c]);
        }

        for (int c = 0; c < xfireJars.length; c++) {
            classpath.append(File.pathSeparatorChar);
            classpath.append(modulesDir).append(File.separatorChar).append(xfireJars[c]);
        }

        classpath.append(File.pathSeparatorChar);
        classpath.append(antDir).append(File.separatorChar).append("lib").append(File.separatorChar)
                .append("ant.jar");

        if (additional != null && additional.trim().length() > 0) {
            classpath.append(File.pathSeparatorChar).append(additional.trim());
        }

        return classpath.toString();
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, String classpath, Interface modelItem)
            throws IOException {
        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT), ""));

        ArgumentBuilder builder = new ArgumentBuilder(values);

        builder.addArgs("java");
        addJavaArgs(values, builder);

        builder.addArgs("-cp", classpath, "org.codehaus.xfire.gen.WsGen");
        builder.addArgs("-wsdl", getWsdlUrl(values, modelItem));
        builder.addString(OUTPUT, "-o");
        builder.addString(PACKAGE, "-p");
        builder.addString(BINDING, "-b");
        builder.addString(EXTERNAL_BINDINGS, "-e");
        builder.addString(PROFILE, "-r");
        builder.addString(BASE_URI, "-u");
        builder.addString(OVERWRITE, "-overwrite");
        builder.addString(EXPLICIT_ANNOTATION, "-x");
        builder.addString(SERVER_STUBS, "-ss");
        addToolArgs(values, builder);
        return builder;
    }
}
