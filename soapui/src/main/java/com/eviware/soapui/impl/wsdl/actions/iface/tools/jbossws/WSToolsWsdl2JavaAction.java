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
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ShowConfigFileAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.XFormTextField;
import org.jboss.jbosswsTools.ConfigurationDocument;
import org.jboss.jbosswsTools.ConfigurationType;
import org.jboss.jbosswsTools.GlobalType;
import org.jboss.jbosswsTools.PkgNSType;
import org.jboss.jbosswsTools.WsdlToJavaType;
import org.jboss.jbosswsTools.WsdlToJavaType.ParameterStyle;
import org.jboss.jbosswsTools.WsxmlType;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Invokes jbossws wsdl2java tools
 *
 * @author Ole.Matzura
 */

public class WSToolsWsdl2JavaAction extends AbstractToolsAction<Interface> {
    public static final String SOAPUI_ACTION_ID = "WSToolsWsdl2JavaAction";

    private static final String NAMESPACE_MAPPING = "Namespace mapping";
    private static final String OUTPUT = "Output Directory";
    private static final String MAPPING = "Mapping file";
    private static final String UNWRAP = "Unwrap";
    private static final String APPEND = "Append";
    private static final String SERVLET_LINK = "Servlet Link";
    private static final String EJB_LINK = "EJB Link";
    private XFormTextField ejbLinkField;
    private XFormTextField servletLinkField;
    private XFormField appendField;

    public WSToolsWsdl2JavaAction() {
        super("JBossWS Artifacts", "Generates JBossWS artifacts using the jboss wstools utility");
    }

    @Override
    public boolean applies(Interface target) {
        Interface iface = (Interface) target;
        return !iface.getProject().hasNature(Project.JBOSSWS_NATURE_ID);
    }

    @Override
    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);

        boolean hasEjbLink = values.get(EJB_LINK, "").length() > 0;
        boolean hasServletLink = values.get(SERVLET_LINK, "").length() > 0;

        if (!hasEjbLink && !hasServletLink) {
            ejbLinkField.setEnabled(true);
            servletLinkField.setEnabled(true);
        } else {
            ejbLinkField.setEnabled(hasEjbLink && !hasServletLink);
            servletLinkField.setEnabled(hasServletLink && !hasEjbLink);

            if (hasEjbLink && hasServletLink) {
                values.put(SERVLET_LINK, "");
            }
        }

        appendField.setEnabled(hasEjbLink || hasServletLink);

        return values;
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("JBossWS Artifacts");

        XForm mainForm = builder.createForm("Basic");
        addWSDLFields(mainForm, modelItem);

        mainForm.addTextField(OUTPUT, "The root directory for all emitted files.", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(MAPPING, "mapping file to generate", XForm.FieldType.PROJECT_FILE);
        mainForm.addCheckBox(UNWRAP, "unwrap doc-literal operations");

        mainForm.addNameSpaceTable(NAMESPACE_MAPPING, modelItem);

        mainForm.addSeparator("webservices.xml generation options");
        ejbLinkField = mainForm.addTextField(EJB_LINK, "The ejb-jar.xml ejb-link for Stateless Session Bean endpoints",
                XForm.FieldType.TEXT);
        ejbLinkField.addFormFieldListener(new XFormFieldListener() {
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                servletLinkField.setEnabled(newValue.length() == 0);
                appendField.setEnabled(newValue.length() > 0);
            }
        });

        servletLinkField = mainForm.addTextField(SERVLET_LINK,
                "The web.xml servlet-link that is used by Java Service Endpoints (WAR)", XForm.FieldType.TEXT);
        servletLinkField.addFormFieldListener(new XFormFieldListener() {
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                ejbLinkField.setEnabled(newValue.length() == 0);
                appendField.setEnabled(newValue.length() > 0);
            }
        });

        appendField = mainForm.addCheckBox(APPEND, "append to existing file");
        appendField.setEnabled(false);
        buildArgsForm(builder, false, "wstools");

        ActionList actions = buildDefaultActions(HelpUrls.WSTOOLS_HELP_URL, modelItem);
        actions.addAction(new JBossWSShowConfigFileAction("JBossWS Wsdl2Java",
                "Contents of generated wsconfig.xml file", modelItem));
        return builder.buildDialog(actions, "Specify arguments for JBossWS wstools wsdl2java functionality",
                UISupport.TOOL_ICON);
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
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
        ArgumentBuilder args = buildArgs(values, UISupport.isWindows(), modelItem);
        builder.command(args.getArgs());
        builder.directory(new File(wstoolsDir));

        toolHost.run(new ProcessToolRunner(builder, "JBossWS wstools", modelItem, args));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, boolean isWindows, Interface modelItem)
            throws IOException {
        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT), ""));

        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.startScript("wstools");

        builder.addArgs("-config", buildConfigFile(values, modelItem));
        builder.addString(OUTPUT, "-dest");
        addToolArgs(values, builder);
        return builder;
    }

    private String buildConfigFile(StringToStringMap values, Interface modelItem) throws IOException {
        File file = File.createTempFile("wstools-config", ".xml");
        ConfigurationDocument configDocument = createConfigFile(values, modelItem);

        configDocument.save(file);

        return file.getAbsolutePath();
    }

    private ConfigurationDocument createConfigFile(StringToStringMap values, Interface modelItem) {
        ConfigurationDocument configDocument = ConfigurationDocument.Factory.newInstance();
        ConfigurationType config = configDocument.addNewConfiguration();

        try {
            StringToStringMap nsMappings = StringToStringMap.fromXml(values.get(NAMESPACE_MAPPING));
            if (!nsMappings.isEmpty()) {
                GlobalType global = config.addNewGlobal();

                for (Map.Entry<String, String> namespaceEntry : nsMappings.entrySet()) {
                    PkgNSType entry = global.addNewPackageNamespace();
                    entry.setNamespace(namespaceEntry.getKey());
                    entry.setPackage(namespaceEntry.getValue());
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        WsdlToJavaType wsdl2Java = config.addNewWsdlJava();

        String wsdlUrl = getWsdlUrl(values, modelItem);
        try {
            new URL(wsdlUrl);
            wsdl2Java.setLocation(wsdlUrl);
        } catch (MalformedURLException e) {
            ((Element) wsdl2Java.getDomNode()).setAttribute("file", wsdlUrl);
        }

        if (values.getBoolean(UNWRAP)) {
            wsdl2Java.setParameterStyle(ParameterStyle.BARE);
        } else {
            wsdl2Java.setParameterStyle(ParameterStyle.WRAPPED);
        }

        if (values.get(EJB_LINK) != null && values.get(EJB_LINK).length() > 0) {
            WsxmlType webservices = wsdl2Java.addNewWebservices();
            webservices.setEjbLink(values.get(EJB_LINK));
            webservices.setAppend(values.getBoolean(APPEND));
        } else if (values.get(SERVLET_LINK) != null && values.get(SERVLET_LINK).length() > 0) {
            WsxmlType webservices = wsdl2Java.addNewWebservices();
            webservices.setServletLink(values.get(SERVLET_LINK));
            webservices.setAppend(values.getBoolean(APPEND));
        }

        String mappingFile = values.get(MAPPING).toString().trim();
        if (mappingFile.length() > 0) {
            wsdl2Java.addNewMapping().setFile(mappingFile);
        }
        return configDocument;
    }

    private final class JBossWSShowConfigFileAction extends ShowConfigFileAction {
        private final Interface modelItem;

        private JBossWSShowConfigFileAction(String title, String description, Interface modelItem) {
            super(title, description);
            this.modelItem = modelItem;
        }

        protected String getConfigFile() {
            ConfigurationDocument configDocument = createConfigFile(getDialog().getValues(), modelItem);
            return configDocument.toString();
        }
    }

}
