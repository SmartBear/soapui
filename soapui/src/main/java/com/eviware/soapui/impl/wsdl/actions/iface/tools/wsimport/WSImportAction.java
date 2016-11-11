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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.wsimport;

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
import java.util.List;

/**
 * Invokes JAX-WS WSImport
 *
 * @author Ole.Matzura
 */

public class WSImportAction extends AbstractToolsAction<Interface> {
    private static final String PACKAGE = "Package";
    private static final String OUTPUT = "Target Directory";
    private static final String SOURCE_OUTPUT = "Source Directory";
    private static final String WSDLLOCATION = "wsdlLocation";
    private static final String BINDING_FILES = "Binding files";
    private static final String HTTPPROXY = "HTTP Proxy";
    private static final String KEEP = "Keep";
    private static final String CATALOG = "Catalog";
    public static final String SOAPUI_ACTION_ID = "WSImportAction";

    public WSImportAction() {
        super("JAX-WS Artifacts", "Generates JAX-WS artifacts using wsimport");
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("JAX-WS Artifacts");

        XForm mainForm = builder.createForm("Basic");
        addWSDLFields(mainForm, modelItem);

        mainForm.addTextField(OUTPUT, "target directory for generated files.", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(PACKAGE, "target package nam", XForm.FieldType.JAVA_PACKAGE);
        mainForm.addTextField(SOURCE_OUTPUT, "target directory for generated source files",
                XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(HTTPPROXY, "HTTP Proxy-server", XForm.FieldType.TEXT);
        mainForm.addTextField(CATALOG, "catalog file to resolve external entity references",
                XForm.FieldType.PROJECT_FILE);
        mainForm.addTextField(BINDING_FILES, "comma-separated list of external JAX-WS or JAXB binding files",
                XForm.FieldType.TEXT);
        mainForm.addTextField(WSDLLOCATION, "@WebService.wsdlLocation and @WebServiceClient.wsdlLocation value",
                XForm.FieldType.TEXT);

        mainForm.addCheckBox(KEEP, "(keep generated files)");

        buildArgsForm(builder, false, "WSImport");

        return builder.buildDialog(buildDefaultActions(HelpUrls.WSIMPORT_HELP_URL, modelItem),
                "Specify arguments for JWSDP/JAX-WS wsimport", UISupport.TOOL_ICON);
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String wsimportDir = SoapUI.getSettings().getString(ToolsSettings.JWSDP_WSIMPORT_LOCATION, null);
        if (Tools.isEmpty(wsimportDir)) {
            UISupport.showErrorMessage("wsimport directory must be set in global preferences");
            return;
        }

        String wsimportExtension = UISupport.isWindows() ? ".bat" : ".sh";

        File wscompileFile = new File(wsimportDir + File.separatorChar + "wsimport" + wsimportExtension);
        if (!wscompileFile.exists() && wsimportExtension.equals(".bat")) {
            wsimportExtension = ".exe";
            wscompileFile = new File(wsimportDir + File.separatorChar + "wsimport" + wsimportExtension);
        }
        if (!wscompileFile.exists()) {
            UISupport.showErrorMessage("Could not find wsimport script at [" + wscompileFile + "]");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(values, UISupport.isWindows(), modelItem);
        List<String> argList = args.getArgs();
        if (wsimportExtension.equals(".exe")) {
            int i = argList.indexOf("wsimport.bat");
            argList.remove(i);
            argList.add(i, "wsimport.exe");
        }
        builder.command(argList);
        builder.directory(new File(wsimportDir));

        toolHost.run(new ProcessToolRunner(builder, "JAX-WS wsimport", modelItem));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, boolean isWindows, Interface modelItem)
            throws IOException {
        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT), ""));
        values.put(SOURCE_OUTPUT, Tools.ensureDir(values.get(SOURCE_OUTPUT), values.get(OUTPUT)));

        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.startScript("wsimport");

        builder.addString(OUTPUT, "-d");
        builder.addString(CATALOG, "-catalog");
        builder.addString(HTTPPROXY, "-httpproxy", ":");
        builder.addString(PACKAGE, "-p");
        builder.addString(SOURCE_OUTPUT, "-s");
        builder.addString(WSDLLOCATION, "-wsdllocation");
        builder.addBoolean(KEEP, "-keep");

        String[] bindingFiles = values.get(BINDING_FILES).split(",");
        for (String file : bindingFiles) {
            if (file.trim().length() > 0) {
                builder.addArgs("-b", file.trim());
            }
        }

        builder.addArgs("-verbose");
        addToolArgs(values, builder);
        builder.addArgs(getWsdlUrl(values, modelItem));

        return builder;
    }
}
