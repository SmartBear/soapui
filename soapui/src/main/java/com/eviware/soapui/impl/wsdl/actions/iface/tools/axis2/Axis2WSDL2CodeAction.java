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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.axis2;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.NamespaceTable;
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
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormTextField;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.Map;

/**
 * Invokes axis 2 WSDL2Code
 *
 * @author Ole.Matzura
 */

public class Axis2WSDL2CodeAction extends AbstractToolsAction<Interface> {
    private static final String WSDL2JAVA_SCRIPT_NAME = "wsdl2java";
    private static final String PACKAGE = "Package";
    private static final String OUTPUT = "Output Directory";
    private static final String SERVICE_NAME = "service name";
    private static final String PORT_NAME = "port name";
    private static final String ASYNC = "async";
    private static final String SYNC = "sync";
    private static final String TESTCASE = "test-case";
    private static final String SERVERSIDE = "server-side";
    private static final String SERICEDESCRIPTOR = "service descriptor";
    private static final String DATABINDING = "databinding method";
    private static final String GENERATEALL = "generate all";
    private static final String UNPACK = "unpack classes";
    private static final String SERVERSIDEINTERFACE = "serverside-interface";

    private static final String ADB_WRITE = "adb writeClasses";
    private static final String ADB_WRAP = "adb wrapClasses";
    private static final String NAMESPACE_MAPPING = "namespace mapping";
    private static final String JIBX_BINDING_FILE = "JIBX bindingfile";
    public static final String SOAPUI_ACTION_ID = "Axis2WSDL2CodeAction";

    public Axis2WSDL2CodeAction() {
        super("Axis 2 Artifacts", "Generates Axis 2 artifacts using wsdl2java");
    }

    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);

        if (!values.hasValue(PORT_NAME) || !values.hasValue(SERVICE_NAME)) {
            initServiceAndPort(values, (WsdlInterface) modelItem);
        }

        return values;
    }

    @SuppressWarnings("unchecked")
    private void initServiceAndPort(StringToStringMap values, WsdlInterface modelItem) {
        if (modelItem == null) {
            return;
        }

        try {
            QName bindingName = modelItem.getBindingName();
            Definition definition = modelItem.getWsdlContext().getDefinition();
            Map<QName, Service> services = definition.getAllServices();

            for (Map.Entry<QName, Service> entry : services.entrySet()) {
                Service service = entry.getValue();

                Map<String, Port> portMap = service.getPorts();
                for (String portName : portMap.keySet()) {
                    Port port = portMap.get(portName);
                    if (port.getBinding().getQName().equals(bindingName)) {
                        values.put(SERVICE_NAME, entry.getKey().getLocalPart());
                        values.put(PORT_NAME, portName);

                        break;
                    }
                }

                if (values.containsKey(PORT_NAME)) {
                    break;
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Axis2 artifacts");
        XForm mainForm = builder.createForm("Basic");

        addWSDLFields(mainForm, modelItem);

        mainForm.addTextField(OUTPUT, "root directory for generated files.", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(PACKAGE, "target package nam", XForm.FieldType.JAVA_PACKAGE);

        XFormField dbComboBox = mainForm.addComboBox(DATABINDING, new String[]{"xmlbeans", "adb", "jibx", "jaxme"},
                "Specifies the Databinding framework.");

        mainForm.addCheckBox(ASYNC, "(generate code only for async style)");
        mainForm.addCheckBox(SYNC, "(generate code only for sync style)");
        mainForm.addCheckBox(TESTCASE, "(Generate a test case)");

        XFormField serverSideCB = mainForm.addCheckBox(SERVERSIDE, "(Generate server side code (i.e. skeletons))");

        XFormField ssiCB = mainForm.addCheckBox(SERVERSIDEINTERFACE, "(Generate interface for server side)");
        XFormField sdCB = mainForm.addCheckBox(SERICEDESCRIPTOR, "(Generate the service descriptor (i.e. server.xml).)");
        serverSideCB.addComponentEnabler(ssiCB, "true");
        serverSideCB.addComponentEnabler(sdCB, "true");

        XForm advForm = builder.createForm("Advanced");

        advForm.addCheckBox(GENERATEALL, "(Genrates all the classes)");
        advForm.addCheckBox(UNPACK, "(Unpacks the databinding classes)");

        advForm.addTextField(SERVICE_NAME, "the service name to be code generated", XForm.FieldType.TEXT);
        advForm.addTextField(PORT_NAME, "the port name to be code generated", XForm.FieldType.TEXT);

        advForm.addComponent(NAMESPACE_MAPPING, new NamespaceTable((WsdlInterface) modelItem));

        XFormField adbWrapCB = advForm.addCheckBox(ADB_WRAP,
                "(Sets the packing flag. if true the classes will be packed.)");
        XFormField adbWriteCB = advForm.addCheckBox(ADB_WRITE,
                "(Sets the write flag. If set to true the classes will be written by ADB)");
        XFormTextField jibxCB = advForm.addTextField(JIBX_BINDING_FILE, "The JIBX binding file to use",
                XForm.FieldType.PROJECT_FILE);
        dbComboBox.addComponentEnabler(adbWrapCB, "adb");
        dbComboBox.addComponentEnabler(adbWriteCB, "adb");
        dbComboBox.addComponentEnabler(jibxCB, "jibx");

        buildArgsForm(builder, false, "WSDL2Java");

        return builder.buildDialog(buildDefaultActions(HelpUrls.AXIS2X_HELP_URL, modelItem),
                "Specify arguments for Axis 2.X Wsdl2Java", UISupport.TOOL_ICON);
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String axis2Dir = SoapUI.getSettings().getString(ToolsSettings.AXIS_2_LOCATION, null);
        if (Tools.isEmpty(axis2Dir)) {
            UISupport.showErrorMessage("Axis 2 wsdl2java directory must be set in global preferences");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(values, (WsdlInterface) modelItem);
        builder.command(args.getArgs());
        builder.directory(new File(axis2Dir + File.separatorChar + "bin"));

        toolHost.run(new ProcessToolRunner(builder, "Axis2 wsdl2java", modelItem));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, WsdlInterface modelItem) {
        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT), ""));

        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.startScript(WSDL2JAVA_SCRIPT_NAME);
        builder.addArgs("-uri", getWsdlUrl(values, modelItem));
        builder.addString(OUTPUT, "-o");
        builder.addString(PACKAGE, "p");
        builder.addString(DATABINDING, "-d");
        builder.addBoolean(ASYNC, "-a");
        builder.addBoolean(SYNC, "-s");
        builder.addBoolean(TESTCASE, "-t");
        builder.addBoolean(SERVERSIDE, "-ss");
        builder.addBoolean(SERVERSIDEINTERFACE, "-ssi");
        builder.addBoolean(SERICEDESCRIPTOR, "-sd");
        builder.addBoolean(GENERATEALL, "-g");
        builder.addBoolean(UNPACK, "-u");

        builder.addString(SERVICE_NAME, "-sn");
        builder.addString(PORT_NAME, "-pn");

        if ("adb".equals(values.get(DATABINDING))) {
            builder.addBoolean(ADB_WRAP, "-Ew", "true", "false");
            builder.addBoolean(ADB_WRITE, "-Er");
        }

        if ("jibx".equals(values.get(DATABINDING))) {
            builder.addString(JIBX_BINDING_FILE, "-E", "");
        }

        try {
            StringBuilder nsMapArg = new StringBuilder();
            StringToStringMap nsMappings = StringToStringMap.fromXml(values.get(NAMESPACE_MAPPING));
            for (Map.Entry<String, String> entry : nsMappings.entrySet()) {
                if (nsMapArg.length() > 0) {
                    nsMapArg.append(',');
                }

                nsMapArg.append(entry.getKey()).append('=').append(entry.getValue());
            }

            builder.addArgs("-ns2p", nsMapArg.toString());
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        addToolArgs(values, builder);

        return builder;
    }
}
