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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.jaxb;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

import java.io.File;

/**
 * Generates JAXB classes for given interface
 *
 * @author Ole.Matzura
 */

public class JaxbXjcAction extends AbstractToolsAction<Interface> {
    private final static String PACKAGE = "package";
    private final static String OUTPUT = "output";
    private final static String NOVALIDATION = "no validation";
    private final static String BINDINGS = "binding files";
    private final static String CLASSPATH = "classpath";
    private final static String CATALOG = "catalog";
    private final static String HTTPPROXY = "http proxy";
    private final static String READONLY = "read-only";
    private final static String NPA = "npa";
    private final static String VERBOSE = "verbose";
    public static final String SOAPUI_ACTION_ID = "JaxbXjcAction";

    // Configure the behavior of this action:
    private String output = null;

    public JaxbXjcAction() {
        super("JAXB 2.0 Artifacts", "Generates JAXB artifacts");
    }

    @Override
    public boolean applies(Interface target) {
        Interface iface = (Interface) target;
        return !iface.getProject().hasNature(Project.JBOSSWS_NATURE_ID);
    }

    /**
     * Set this to predefine the output directory instead of letting the user
     * select.
     */
    public void setOutput(String output) {
        this.output = output;
    }

    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);

        if (output != null) {
            values.put(OUTPUT, output);
        }

        return values;
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("JAXB Artifacts");

        XForm mainForm = builder.createForm("Basic");
        addWSDLFields(mainForm, modelItem);

        mainForm.addTextField(OUTPUT, "generated files will go into this directory", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(PACKAGE, "the target package", XForm.FieldType.JAVA_PACKAGE);

        mainForm.addTextField(BINDINGS, "external bindings file(s), comma-separated", XForm.FieldType.PROJECT_FILE);
        mainForm.addTextField(CATALOG, "catalog files to resolve external entity references",
                XForm.FieldType.PROJECT_FILE);
        mainForm.addTextField(CLASSPATH, "where to find user class files", XForm.FieldType.PROJECT_FOLDER);

        mainForm.addTextField(HTTPPROXY, "set HTTP/HTTPS proxy. Format is [user[:password]@]proxyHost[:proxyPort]",
                XForm.FieldType.TEXT);
        mainForm.addCheckBox(READONLY, "(generated files will be in read-only mode)");
        mainForm.addCheckBox(NOVALIDATION, "(do not resolve strict validation of the input schema(s))");
        mainForm.addCheckBox(NPA, "(suppress generation of package level annotations (**/package-info.java))");

        mainForm.addCheckBox(VERBOSE, "(be extra verbose)");

        buildArgsForm(builder, false, "xjc");

        return builder.buildDialog(buildDefaultActions(HelpUrls.JABXJC_HELP_URL, modelItem),
                "Specify arguments for the JAXB 2 xjc compiler", UISupport.TOOL_ICON);
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String jaxbDir = SoapUI.getSettings().getString(ToolsSettings.JAXB_LOCATION, null);
        if (Tools.isEmpty(jaxbDir)) {
            UISupport.showErrorMessage("JAXB location must be set in global preferences");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder argumentBuilder = buildArgs(values, modelItem);
        builder.command(argumentBuilder.getArgs());
        builder.directory(new File(jaxbDir + File.separatorChar + "bin"));

        toolHost.run(new ProcessToolRunner(builder, "JAXB xjc", modelItem, argumentBuilder));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, Interface modelItem) {
        ArgumentBuilder builder = new ArgumentBuilder(values);

        builder.startScript("xjc", ".bat", ".sh");

        String outputValue = (output != null ? output : values.get(OUTPUT));
        values.put(OUTPUT, Tools.ensureDir(outputValue, ""));

        builder.addString(OUTPUT, "-d");
        builder.addString(PACKAGE, "-p");
        builder.addString(CLASSPATH, "-classpath");
        builder.addString(CATALOG, "-catalog");
        builder.addString(HTTPPROXY, "-httpproxy ");

        builder.addBoolean(NOVALIDATION, "-nv");
        builder.addBoolean(NPA, "-npa");
        builder.addBoolean(READONLY, "-readOnly");
        builder.addBoolean(VERBOSE, "-verbose");

        addToolArgs(values, builder);

        builder.addArgs("-wsdl", getWsdlUrl(values, modelItem));

        String[] bindings = values.get(BINDINGS).split(",");
        for (String binding : bindings) {
            if (binding.trim().length() > 0) {
                builder.addArgs("-b", binding.trim());
            }
        }

        return builder;
    }
}
