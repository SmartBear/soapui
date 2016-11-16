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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.jbossws;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.RunnerContext;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ShowConfigFileAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import org.jboss.jbosswsTools.ConfigurationDocument;
import org.jboss.jbosswsTools.ConfigurationType;
import org.jboss.jbosswsTools.JavaToWsdlType;
import org.jboss.jbosswsTools.MappingType;
import org.jboss.jbosswsTools.NamespacesType;
import org.jboss.jbosswsTools.ServiceType;
import org.jboss.jbosswsTools.ServiceType.ParameterStyle;
import org.jboss.jbosswsTools.ServiceType.Style;
import org.jboss.jbosswsTools.WsxmlType;

import java.io.File;
import java.io.IOException;

/**
 * Invokes jbossws java2wsdl tools
 *
 * @author Ole.Matzura
 */

public class WSToolsJava2WsdlAction extends AbstractToolsAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "WSToolsJava2WsdlAction";

    private static final String CLASSPATH = "Classpath";
    private static final String OUTPUT = "Output Directory";
    private static final String ENDPOINT = "Endpoint";
    private static final String MAPPING = "Mapping file";
    private static final String SERVICE_NAME = "Service Name";
    private static final String STYLE = "Style";
    private static final String PARAMETER_STYLE = "Parameter Style";
    private static final String TARGET_NAMESPACE = "Target Namespace";
    private static final String TYPES_NAMESPACE = "Types Namespace";
    private static final String EJB_LINK = "ejb-link";
    private static final String SERVLET_LINK = "servlet-link";

    public WSToolsJava2WsdlAction() {
        super("Generate WSDL with JBossWS", "Generates WSDL with the jbossws wstools utility");
    }

    protected XFormDialog buildDialog(WsdlProject project) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Generate JBossWS WSDL Artifacts");

        XForm mainForm = builder.createForm("Basic");

        mainForm.addTextField(ENDPOINT, "Serice Endpoint Interface", XForm.FieldType.JAVA_CLASS);
        mainForm.addTextField(SERVICE_NAME, "The name of the generated Service", XForm.FieldType.TEXT);
        mainForm
                .addComboBox(STYLE, new String[]{Style.DOCUMENT.toString(), Style.RPC.toString()}, "The style to use");
        mainForm.addComboBox(PARAMETER_STYLE,
                new String[]{ParameterStyle.BARE.toString(), ParameterStyle.WRAPPED.toString()}, "The style to use");
        mainForm.addTextField(CLASSPATH, "Classpath to use", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(OUTPUT, "The root directory for all emitted files.", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(MAPPING, "mapping file to generate", XForm.FieldType.PROJECT_FILE);
        mainForm.addTextField(TARGET_NAMESPACE, "The target namespace for the generated WSDL", XForm.FieldType.TEXT);
        mainForm.addTextField(TYPES_NAMESPACE, "The namespace for the generated types", XForm.FieldType.TEXT);
        mainForm.addTextField(EJB_LINK, "The name of the source EJB to link to", XForm.FieldType.TEXT);
        mainForm.addTextField(SERVLET_LINK, "The name of the source Servlet to link to", XForm.FieldType.TEXT);

        buildArgsForm(builder, false, "wstools");

        ActionList actions = buildDefaultActions(HelpUrls.WSTOOLS_HELP_URL, project);
        actions.addAction(new ShowConfigFileAction("JBossWS Java2Wsdl", "Contents of generated wsconfig.xml file") {
            protected String getConfigFile() {
                ConfigurationDocument configDocument = createConfigFile(getDialog().getValues());
                return configDocument.toString();
            }
        });

        return builder.buildDialog(actions, "Specify arguments for JBossWS wstools java2wsdl functionality",
                UISupport.TOOL_ICON);
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, WsdlProject project) throws Exception {
        String wstoolsDir = SoapUI.getSettings().getString(ToolsSettings.JBOSSWS_WSTOOLS_LOCATION, null);
        if (Tools.isEmpty(wstoolsDir)) {
            UISupport.showErrorMessage("wstools directory must be set in global preferences");
            return;
        }

        String wsToolsExtension = UISupport.isWindows() ? ".bat" : ".sh";

        File wstoolsFile = new File(wstoolsDir + File.separatorChar + "wstools" + wsToolsExtension);
        if (!wstoolsFile.exists()) {
            UISupport.showErrorMessage("Could not find wstools script at [" + wstoolsFile + "]");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(values, UISupport.isWindows());
        builder.command(args.getArgs());
        builder.directory(new File(wstoolsDir));

        toolHost.run(new ToolRunner(builder, new File(values.get(OUTPUT)), values.get(SERVICE_NAME), project));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, boolean isWindows) throws IOException {
        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT), ""));

        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.startScript("wstools");

        builder.addString(CLASSPATH, "-cp");
        builder.addArgs("-config", buildConfigFile(values));
        builder.addString(OUTPUT, "-dest");
        addToolArgs(values, builder);
        return builder;
    }

    private String buildConfigFile(StringToStringMap values) throws IOException {
        File file = File.createTempFile("wstools-config", ".xml");
        ConfigurationDocument configDocument = createConfigFile(values);

        configDocument.save(file);

        return file.getAbsolutePath();
    }

    private ConfigurationDocument createConfigFile(StringToStringMap values) {
        ConfigurationDocument configDocument = ConfigurationDocument.Factory.newInstance();
        ConfigurationType config = configDocument.addNewConfiguration();

        JavaToWsdlType java2Wsdl = config.addNewJavaWsdl();
        ServiceType service = java2Wsdl.addNewService();
        service.setEndpoint(values.get(ENDPOINT));
        service.setStyle(Style.Enum.forString(values.get(STYLE)));
        service.setParameterStyle(ParameterStyle.Enum.forString(values.get(PARAMETER_STYLE)));
        service.setName(values.get(SERVICE_NAME));

        MappingType mapping = java2Wsdl.addNewMapping();
        mapping.setFile(values.get(MAPPING));

        NamespacesType namespaces = java2Wsdl.addNewNamespaces();
        namespaces.setTargetNamespace(values.get(TARGET_NAMESPACE));
        namespaces.setTypeNamespace(values.get(TYPES_NAMESPACE));

        WsxmlType webservices = java2Wsdl.addNewWebservices();
        if (values.get(EJB_LINK) != null && values.get(EJB_LINK).length() > 0) {
            webservices.setEjbLink(values.get(EJB_LINK));
        }
        if (values.get(SERVLET_LINK) != null && values.get(SERVLET_LINK).length() > 0) {
            webservices.setServletLink(values.get(SERVLET_LINK));
        }
        return configDocument;
    }

    private class ToolRunner extends ProcessToolRunner {
        private final File outDir;
        private final String serviceName;
        private final WsdlProject project;

        public ToolRunner(ProcessBuilder builder, File outDir, String serviceName, WsdlProject modelItem) {
            super(builder, "JBossWS wstools", modelItem);
            this.outDir = outDir;
            this.serviceName = serviceName;
            this.project = modelItem;
        }

        protected void afterRun(RunnerContext context) {
            if (context.getStatus() != RunnerContext.RunnerStatus.FINISHED) {
                return;
            }

            try {
                String wsdlUrl = "file:" + outDir.getAbsolutePath() + File.separatorChar + "wsdl" + File.separatorChar
                        + serviceName + ".wsdl";
                Interface[] ifaces = WsdlInterfaceFactory.importWsdl(project, wsdlUrl, true);

                if (ifaces.length > 0) {
                    context.log("Added Interface [" + ifaces[0].getName() + "] to project");
                    ifaces[0].getSettings().setString(WSToolsRegenerateJava2WsdlAction.class.getName() + "@values",
                            getDialog().getValues().toXml());
                    UISupport.select(ifaces[0]);
                }
            } catch (SoapUIException e) {
                SoapUI.logError(e);
            }
        }
    }
}
