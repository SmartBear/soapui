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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.axis1;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

import java.io.File;
import java.util.Map;

/**
 * Invokes axis 1.X WSDL2Java
 *
 * @author Ole.Matzura
 */

public class Axis1XWSDL2JavaAction extends AbstractToolsAction<Interface> {
    private static final String NAMESPACE_MAPPING = "namespace mapping";
    private static final String FACTORY = "factory";
    private static final String OUTPUT = "output directory";
    private static final String PACKAGE = "target package";
    private static final String TYPE_MAPPING_VERSION = "typeMappingVersion";
    private static final String DEPLOY_SCOPE = "deployScope";
    private static final String SKELETON_DEPLOY = "skeletonDeploy";
    private static final String WRAP_ARRAYS = "wrapArrays";
    private static final String HELPER_GEN = "helperGen";
    private static final String ALL = "all";
    private static final String TEST_CASE = "testCase";
    private static final String SERVER_SIDE = "server-side";
    private static final String NO_WRAPPED = "noWrapped";
    private static final String NO_IMPORTS = "noImports";

    private static final String IMPLCLASS = "implementationClassName";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    public static final String SOAPUI_ACTION_ID = "Axis1XWSDL2JavaAction";
    public static final MessageSupport messages = MessageSupport.getMessages(Axis1XWSDL2JavaAction.class);

    public Axis1XWSDL2JavaAction() {
        super(messages.get("Title"), messages.get("Description"));
    }

    protected StringToStringMap initValues(Interface iface, Object param) {
        StringToStringMap values = super.initValues(iface, param);

        values.putIfMissing(SKELETON_DEPLOY, "none");
        values.putIfMissing(DEPLOY_SCOPE, "none");
        values.putIfMissing(TYPE_MAPPING_VERSION, "1.2");

        return values;
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(messages.get("Dialog.Title"));

        XForm mainForm = builder.createForm(messages.get("Dialog.Basic.Label"));
        addWSDLFields(mainForm, modelItem);

        mainForm.addTextField(OUTPUT, messages.get("Dialog.Basic.Output"), XForm.FieldType.PROJECT_FOLDER);
        mainForm.addCheckBox(SERVER_SIDE, messages.get("Dialog.Basic.ServerSide"));
        mainForm.addCheckBox(ALL, messages.get("Dialog.Basic.All"));

        mainForm.addComboBox(DEPLOY_SCOPE, new String[]{"none", "Application", "Session", "Request"},
                messages.get("Axis1XWSDL2JavaAction.Dialog.Basic.AddScope"));

        mainForm.addComboBox(SKELETON_DEPLOY, new String[]{"none", "true", "false"},
                messages.get("Dialog.Basic.DeploySkeleton"));

        mainForm.addCheckBox(NO_IMPORTS, messages.get("Dialog.Basic.NoImports"));
        mainForm.addCheckBox(NO_WRAPPED, messages.get("Dialog.Basic.NoWrapped"));
        mainForm.addCheckBox(TEST_CASE, messages.get("Dialog.Basic.TestCase"));
        mainForm.addCheckBox(HELPER_GEN, messages.get("Dialog.Basic.HelperGen"));
        mainForm.addCheckBox(WRAP_ARRAYS, messages.get("Dialog.Basic.WrapArrays"));

        XForm advForm = builder.createForm(messages.get("Dialog.Advanced.Label"));
        advForm.addComboBox(TYPE_MAPPING_VERSION, new String[]{"1.2", "1.1"},
                messages.get("Dialog.Advanced.TypeMappingVersion"));

        advForm.addTextField(IMPLCLASS, messages.get("Dialog.Advanced.ImplClass"), XForm.FieldType.JAVA_CLASS);
        advForm.addTextField(FACTORY, messages.get("Dialog.Advanced.Factory"), XForm.FieldType.JAVA_CLASS);

        advForm.addTextField(PACKAGE, messages.get("Dialog.Advanced.Package"), XForm.FieldType.JAVA_PACKAGE);
        advForm.addNameSpaceTable(NAMESPACE_MAPPING, modelItem);

        advForm.addTextField(USERNAME, messages.get("Dialog.Advanced.Username"), XForm.FieldType.TEXT);
        advForm.addTextField(PASSWORD, messages.get("Dialog.Advanced.Password"), XForm.FieldType.PASSWORD);

        buildArgsForm(builder, true, "WSDL2Java"); //$NON-NLS-1$

        return builder.buildDialog(buildDefaultActions(HelpUrls.AXIS1X_HELP_URL, modelItem),
                messages.get("Dialog.Description"), UISupport.TOOL_ICON);
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String axisDir = SoapUI.getSettings().getString(ToolsSettings.AXIS_1_X_LOCATION, null);
        if (Tools.isEmpty(axisDir)) {
            UISupport.showErrorMessage(messages.get("MissingAxisLocationError"));
            return;
        }

        File axisLibDir = new File(axisDir + File.separatorChar + "lib");
        if (!axisLibDir.exists()) {
            UISupport.showErrorMessage(messages.get("CouldNotFindLibDirectoryError", axisLibDir));
            return;
        }

        String classpath = buildClasspath(axisLibDir);

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(classpath, values, modelItem);

        builder.command(args.getArgs());
        builder.directory(axisLibDir);

        toolHost.run(new ProcessToolRunner(builder, messages.get("Axis1XWSDL2JavaAction.Runner.Title"), modelItem));
    }

    private ArgumentBuilder buildArgs(String classpath, StringToStringMap values, Interface modelItem) {
        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.addArgs("java");

        addJavaArgs(values, builder);

        builder.addArgs("-cp", classpath, "org.apache.axis.wsdl.WSDL2Java", "-v");

        builder.addBoolean(NO_IMPORTS, "-n");
        builder.addBoolean(NO_WRAPPED, "-W");
        builder.addBoolean(SERVER_SIDE, "-s");
        builder.addBoolean(TEST_CASE, "-t");
        builder.addBoolean(ALL, "-a");
        builder.addBoolean(HELPER_GEN, "-H");
        builder.addBoolean(WRAP_ARRAYS, "-w");

        if (!values.get(SKELETON_DEPLOY).equals("none")) {
            builder.addString(SKELETON_DEPLOY, "-S");
        }

        if (!values.get(DEPLOY_SCOPE).equals("none")) {
            builder.addString(DEPLOY_SCOPE, "-d");
        }

        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT)));

        builder.addString(TYPE_MAPPING_VERSION, "-T");
        builder.addString(PACKAGE, "-p");
        builder.addString(OUTPUT, "-o");
        builder.addString(FACTORY, "-F");
        builder.addString(IMPLCLASS, "-c");
        builder.addString(USERNAME, "-U");
        builder.addString(PASSWORD, "-P");

        try {
            StringToStringMap nsMappings = StringToStringMap.fromXml(values.get(NAMESPACE_MAPPING));
            for (Map.Entry<String, String> entry : nsMappings.entrySet()) {
                builder.addArgs("-N" + entry.getKey() + "=" + entry.getValue());
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        addToolArgs(values, builder);

        builder.addArgs(getWsdlUrl(values, modelItem));
        return builder;
    }
}
