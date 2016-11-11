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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.wscompile;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ShowConfigFileAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.sun.java.xml.ns.jaxRpc.ri.config.ConfigurationDocument;
import com.sun.java.xml.ns.jaxRpc.ri.config.ConfigurationDocument.Configuration;
import com.sun.java.xml.ns.jaxRpc.ri.config.NamespaceMappingRegistryType;
import com.sun.java.xml.ns.jaxRpc.ri.config.NamespaceMappingType;
import com.sun.java.xml.ns.jaxRpc.ri.config.WsdlType;

import java.io.File;
import java.io.IOException;

/**
 * Invokes JWSDP wscompile
 *
 * @author Ole.Matzura
 */

public class WSCompileAction extends AbstractToolsAction<Interface> {
    private final class WSCompileShowConfigFileAction extends ShowConfigFileAction {
        private final Interface modelItem;

        private WSCompileShowConfigFileAction(String title, String description, Interface modelItem) {
            super(title, description);
            this.modelItem = modelItem;
        }

        protected String getConfigFile() {
            ConfigurationDocument configDocument = createConfigFile(getDialog().getValues(), modelItem);
            return configDocument.toString();
        }
    }

    private static final String OUTPUT = "directory";
    private static final String DATAHANDLERONLY = "datahandleronly";
    private static final String DONOTUNWRAP = "donotunwrap";
    private static final String PACKAGE = "package";
    private static final String KEEP = "keep";
    private static final String MAPPING = "mapping";
    private static final String SOURCE = "source";
    private static final String OPTIMIZE = "optimize";
    private static final String SOURCE_VERSION = "source version";
    private static final String MODEL = "model";
    private static final String NONCLASS = "non-class";
    private static final String SECURITY = "security";
    private static final String DEBUG = "debug";
    private static final String EXPLICITCONTEXT = "explicitcontext";
    private static final String JAXBENUMTYPE = "jaxbenumtype";
    private static final String NODATABINDING = "nodatabinding";
    private static final String NOENCODEDTYPES = "noencodedtypes";
    private static final String NOMULTIREFS = "nomultirefs";
    private static final String NORPCSTRUCTURES = "norpcstructures";
    private static final String NOVALIDATION = "novalidation";
    private static final String RESOLVEIDREF = "resolveidref";
    private static final String SEARCHSCHEMA = "searchschema";
    private static final String SERIALIZEINTERFACES = "serializeinterfaces";
    private static final String STRICT = "strict";
    private static final String UNWRAP = "unwrap";
    private static final String WSI = "wsi";
    private static final String PROXY = "proxy";
    private static final String NAMESPACE_MAPPING = "Namespace mapping";
    public static final String SOAPUI_ACTION_ID = "WSCompileAction";

    public WSCompileAction() {
        super("JAX-RPC Artifacts", "Generates JAX-RPC artifacts using wscompile");
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("WSCompile");

        XForm mainForm = builder.createForm("Basic");
        addWSDLFields(mainForm, modelItem);

        mainForm
                .addTextField(PACKAGE, "the package of the classes generated by wscompile", XForm.FieldType.JAVA_PACKAGE);
        mainForm.addTextField(OUTPUT, "where to place generated output files", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addCheckBox(KEEP, "(Keep generated files)");
        mainForm.addTextField(MAPPING, "Generate a J2EE mapping.xml file", XForm.FieldType.PROJECT_FILE);
        mainForm.addTextField(MODEL, "Write the internal model to the given file", XForm.FieldType.PROJECT_FILE);
        mainForm.addTextField(SOURCE, "Where to place generated source files", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(NONCLASS, "Where to place non-class generated files", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addCheckBox(OPTIMIZE, "(Optimize generated code)");
        mainForm.addCheckBox(DEBUG, "(Generate debugging info)");
        mainForm.addComboBox(SOURCE_VERSION, new String[]{"1.0.1", "1.0.3", "1.1", "1.1.1", "1.1.2"},
                "Generate code for the specified JAX-RPC SI version");
        mainForm.addTextField(SECURITY, "Security configuration file to generate security code",
                XForm.FieldType.PROJECT_FILE);
        mainForm.addTextField(PROXY, "Specify a HTTP proxy server", XForm.FieldType.URL);

        XForm featuresForm = builder.createForm("Features");

        featuresForm.addCheckBox(DATAHANDLERONLY, "(Always map attachments to the DataHandler type)");
        featuresForm.addCheckBox(DONOTUNWRAP, "(Disable unwrapping of document/literal wrapper elements in WSI mode)");
        featuresForm.addCheckBox(EXPLICITCONTEXT, "(Turn on explicit service context mapping)");
        featuresForm.addCheckBox(JAXBENUMTYPE, "(Map anonymous enumeration to its base type)");
        featuresForm.addCheckBox(NODATABINDING, "(Turn off data binding for literal encoding)");
        featuresForm.addCheckBox(NOENCODEDTYPES, "(Turn off encoding type information)");
        featuresForm.addCheckBox(NOMULTIREFS, "(Turn off support for multiple references)");
        featuresForm.addCheckBox(NORPCSTRUCTURES, "(Do not generate RPC structures)");
        featuresForm.addCheckBox(NOVALIDATION, "(Turn off full validation of imported WSDL documents)");
        featuresForm.addCheckBox(RESOLVEIDREF, "(Resolve xsd:IDREF)");
        featuresForm.addCheckBox(SEARCHSCHEMA, "(Search schema aggressively for types)");
        featuresForm.addCheckBox(SERIALIZEINTERFACES, "(Turn on direct serialization of interface types)");
        featuresForm.addCheckBox(STRICT, "(Generate code strictly compliant with JAXRPC spec)");
        featuresForm.addCheckBox(UNWRAP, "(Enable unwrapping of document/literal wrapper elements in WSI mode)");
        featuresForm.addCheckBox(WSI,
                "(Enable WSI-Basic Profile features, to be used for document/literal and rpc/literal)");

        XForm advForm = builder.createForm("Advanced");
        advForm.addNameSpaceTable(NAMESPACE_MAPPING, modelItem);

        buildArgsForm(builder, false, "wscompile");

        ActionList actions = buildDefaultActions(HelpUrls.WSCOMPILE_HELP_URL, modelItem);
        actions.addAction(new WSCompileShowConfigFileAction("JAX-RPC wscompile",
                "Contents of generated config.xml file", modelItem));

        return builder.buildDialog(actions, "Specify arguments for JAX-RPC wscompile", UISupport.TOOL_ICON);
    }

    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);
        values.putIfMissing(SOURCE_VERSION, "1.1.2");
        values.putIfMissing(WSI, Boolean.toString(true));

        return values;
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String wscompileDir = SoapUI.getSettings().getString(ToolsSettings.JWSDP_WSCOMPILE_LOCATION, null);
        if (Tools.isEmpty(wscompileDir)) {
            UISupport.showErrorMessage("wscompile directory must be set in global preferences");
            return;
        }

        String wscompileExtension = UISupport.isWindows() ? ".bat" : ".sh";

        File wscompileFile = new File(wscompileDir + File.separatorChar + "wscompile" + wscompileExtension);
        if (!wscompileFile.exists()) {
            UISupport.showErrorMessage("Could not find wscompile script at [" + wscompileFile + "]");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(values, UISupport.isWindows(), modelItem);
        builder.command(args.getArgs());
        builder.directory(new File(wscompileDir));

        toolHost.run(new ProcessToolRunner(builder, "JAX-RPC wscompile", modelItem));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, boolean isWindows, Interface modelItem)
            throws IOException {
        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.startScript("wscompile");

        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT), ""));

        values.put(SOURCE, Tools.ensureDir(values.get(SOURCE), values.get(OUTPUT)));
        values.put(NONCLASS, Tools.ensureDir(values.get(NONCLASS), values.get(OUTPUT)));

        values.put(MAPPING, Tools.ensureFileDir(values.get(MAPPING), values.get(OUTPUT)));
        values.put(MODEL, Tools.ensureFileDir(values.get(MODEL), values.get(OUTPUT)));

        builder.addString(OUTPUT, "-d");
        builder.addBoolean(KEEP, "-keep");
        builder.addString(MAPPING, "-mapping");
        builder.addString(MODEL, "-model");
        builder.addString(SOURCE, "-s");
        builder.addString(NONCLASS, "-nd");
        builder.addBoolean(OPTIMIZE, "-O");
        builder.addBoolean(DEBUG, "-g");
        builder.addString(SOURCE_VERSION, "-source");
        builder.addString(SECURITY, "-security");
        builder.addString(PROXY, "httpproxy", ":");

        builder.addBoolean(DATAHANDLERONLY, "-f:datahandleronly");
        builder.addBoolean(DONOTUNWRAP, "-f:donotunwrap");
        builder.addBoolean(EXPLICITCONTEXT, "-f:explicitcontext");
        builder.addBoolean(JAXBENUMTYPE, "-f:jaxbenumtype");
        builder.addBoolean(NODATABINDING, "-f:nodatabinding");
        builder.addBoolean(NOENCODEDTYPES, "-f:noencodedtypes");
        builder.addBoolean(NOMULTIREFS, "-f:nomultirefs");
        builder.addBoolean(NORPCSTRUCTURES, "-f:norpcstructures");
        builder.addBoolean(NOVALIDATION, "-f:novalidation");
        builder.addBoolean(RESOLVEIDREF, "-f:resolveidref");
        builder.addBoolean(SEARCHSCHEMA, "-f:searchschema");
        builder.addBoolean(SERIALIZEINTERFACES, "-f:serializeinterfaces");
        builder.addBoolean(STRICT, "-f:strict");
        builder.addBoolean(UNWRAP, "-f:unwrap");
        builder.addBoolean(WSI, "-f:wsi");

        builder.addArgs("-import");
        builder.addArgs("-verbose");
        addToolArgs(values, builder);
        builder.addArgs(buildConfigFile(values, modelItem));
        return builder;
    }

    private String buildConfigFile(StringToStringMap values, Interface modelItem) throws IOException {
        File file = File.createTempFile("wscompile-config", ".xml");
        ConfigurationDocument configDocument = createConfigFile(values, modelItem);
        configDocument.save(file);
        return file.getAbsolutePath();
    }

    private ConfigurationDocument createConfigFile(StringToStringMap values, Interface modelItem) {
        ConfigurationDocument configDocument = ConfigurationDocument.Factory.newInstance();
        Configuration config = configDocument.addNewConfiguration();

        WsdlType wsdl = config.addNewWsdl();
        wsdl.setLocation(getWsdlUrl(values, modelItem));
        wsdl.setPackageName(values.get(PACKAGE).toString());

        try {
            StringToStringMap nsMappings = StringToStringMap.fromXml(values.get(NAMESPACE_MAPPING));
            if (!nsMappings.isEmpty()) {
                NamespaceMappingRegistryType nsMappingRegistry = wsdl.addNewNamespaceMappingRegistry();

                for (String namespace : nsMappings.keySet()) {
                    String packageName = nsMappings.get(namespace);

                    NamespaceMappingType newMapping = nsMappingRegistry.addNewNamespaceMapping();
                    newMapping.setNamespace(namespace);
                    newMapping.setPackageName(packageName);
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }
        return configDocument;
    }
}
