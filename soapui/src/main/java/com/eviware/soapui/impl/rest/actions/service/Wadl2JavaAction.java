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

package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
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

/**
 * Invokes Apache CXF wsdl2java utility
 *
 * @author Ole.Matzura
 */

public class Wadl2JavaAction extends AbstractToolsAction<Interface> {
    private static final String PACKAGE = "Package";
    private static final String OUTPUT = "Output Directory";
    private static final String AUTOMATIC_PACKAGE_NAMES = "Automatic Package Names";
    private static final String JAXB_CUSTOMIZATION = "JAXB Customization File(s)";

    public static final String SOAPUI_ACTION_ID = "Wadl2JavaAction";

    public Wadl2JavaAction() {
        super("WADL2Java", "Generates java code from WADL");
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("WADL2Java");

        XForm mainForm = builder.createForm("Basic");

        mainForm.addTextField(OUTPUT, "Root directory for all emitted files.", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(PACKAGE, "Default Package for generated classes", XForm.FieldType.JAVA_PACKAGE);

        mainForm.addCheckBox(AUTOMATIC_PACKAGE_NAMES, "Generates starting point code for a client mainline");
        mainForm.addTextField(JAXB_CUSTOMIZATION, "Space-separated list of JAXWS or JAXB binding files",
                XForm.FieldType.TEXT);

        buildArgsForm(builder, true, "wadl2java");

        return builder.buildDialog(buildDefaultActions(HelpUrls.WADL2JAVA_HELP_URL, modelItem),
                "Specify arguments for reference wadl2java", UISupport.TOOL_ICON);
    }

    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);
        return values;
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String xfireDir = SoapUI.getSettings().getString(ToolsSettings.WADL2JAVA_LOCATION, null);
        if (Tools.isEmpty(xfireDir)) {
            UISupport.showErrorMessage("WADL2Java directory must be set in global preferences");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(values, modelItem);
        builder.command(args.getArgs());
        builder.directory(new File(xfireDir));

        ((RestService) modelItem).getWadlContext().regenerateWadl();

        toolHost.run(new ProcessToolRunner(builder, "WADL2Java", modelItem, args));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, Interface modelItem) throws Exception {
        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT), ""));

        ArgumentBuilder builder = new ArgumentBuilder(values);

        builder.startScript("wadl2java");

        builder.addString(OUTPUT, "-o");
        builder.addString(PACKAGE, "-p");

        builder.addBoolean(AUTOMATIC_PACKAGE_NAMES, "-a");
        builder.addString(JAXB_CUSTOMIZATION, "-c");

        addToolArgs(values, builder);
        String wsdlUrl = getWadlUrl(values, (RestService) modelItem);
        if (PathUtils.isFilePath(wsdlUrl)) {
            wsdlUrl = new File(wsdlUrl).toURI().toURL().toString();
        }
        builder.addArgs(wsdlUrl);
        return builder;
    }

    protected String getWadlUrl(StringToStringMap values, RestService modelItem) throws Exception {
        String expandPath = PathUtils.expandPath(modelItem.getDefinition(), modelItem);
        if (PathUtils.isHttpPath(expandPath) && !modelItem.isGenerated()) {
            return expandPath;
        } else {
            File tempFile = File.createTempFile("tempdir", null);
            String path = tempFile.getAbsolutePath();
            tempFile.delete();
            return modelItem.getDefinitionContext().export(path);
        }
    }
}
