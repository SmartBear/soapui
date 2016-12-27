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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.gsoap;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Invokes GSoap wsdl2h
 *
 * @author Ole.Matzura
 */

/*
 * http://www.cs.fsu.edu/~engelen/soap.html
 * 
 * Option Description -a generate indexed struct names for local elements with
 * anonymous types -c generate C source code -d use DOM to populate xs:any and
 * xsd:anyType elements -e don't qualify enum names This option is for backward
 * compatibility with gSOAP 2.4.1 and earlier. The option does not produce code
 * that conforms to WS-I Basic Profile 1.0a. -f generate flat C++ class
 * hierarchy for schema extensions -g generate global top-level element
 * declarations -h print help information -I path use path to find files -l
 * include license information in output -m use xsd.h module to import primitive
 * types -n name use name as the base namespace prefix name instead of ns -N
 * name use name as the base namespace prefix name for service namespaces -o
 * file output to file -p create polymorphic types with C++ inheritance
 * hierarchy with base xsd__anyType This is automatically performed when WSDL
 * contains polymorphic definitions -r host:port connect via proxy host and port
 * -s don't generate STL code (no std::string and no std::vector) -t file use
 * type map file instead of the default file typemap.dat -u don't generate
 * unions -v verbose output -w always wrap response parameters in a response
 * struct -x don't generate _XML any/anyAttribute extensibility elements -y
 * generate typedef synonyms for structs and enums -? print help information
 */

public class GSoapAction extends AbstractToolsAction<Interface> {
    // private static final String WSDL2H_PATH = "path";
    private static final String WSDL2H_OUTPUT = "file";

    // WSDL2H settings
    private static final String GENERATE_INDEXED_STRUCT = "generate indexed struct names";
    private static final String GENERATE_C_SOURCE = "generate C source code";
    private static final String DOM = "use DOM";
    private static final String DONT_QUALIFY_ENUM_NAMES = "don't qualify enum names";
    private static final String FLAT_CPP_CLASS_HIERARCHY = "generate flat C++ class hierarchy";
    private static final String GLOBAL_TOP_LEVEL_DECLARATIONS = "generate global top-level declarations";
    private static final String PATH = "path to find files";
    private static final String INCLUDE_LICENSE_INFORMATION = "include license information";
    private static final String USE_XSD_H = "use xsd.h module";
    private static final String BASE_NAMESPACE_PREFIX_INSTEAD_OF_NS = "prefixNs";
    private static final String BASE_NAMESPACE_PREFIX_FOR_SERVICE_NS = "servicePrefixNs";
    private static final String POLYMORPHIC_TYPES = "create polymorphic types";
    private static final String PROXY_HOST_PORT = "proxy host:port";
    private static final String NO_STL = "don't generate STL code";
    private static final String TYPE_MAP_FILE = "use type map file";
    private static final String NO_UNIONS = "don't generate unions";
    private static final String VERBOSE = "verbose output";
    private static final String WRAP_RESPONSE_STRUCT = "wrap response in struct";
    private static final String NO_ANY = "don't generate _XML any/anyAttribute";
    private static final String TYPEDEF_SYNONYMS = "generate typedef synonyms for structs and enums";

    // SOAPCPP2 settings
    private static final String SOAP_11 = "generate SOAP 1.1 bindings";
    private static final String SOAP_12 = "generate SOAP 1.2 bindings";
    private static final String CLIENT_SIDE = "generate client-side code only";
    private static final String SERVER_SIDE = "generate server-side code only";
    private static final String NO_LIB = "don't generate soapClientLib/soapServerLib";
    private static final String SOAP_ACTION = "use value of SOAPAction HTTP header to dispatch method at server side";
    private static final String GENERATE_C_SOURCE_CPP = "generate C source code";
    // private static final String SAVE_PATH = "use path to save files";
    private static final String SOAP_RPC = "generate SOAP RPC encoding style bindings";
    private static final String SERVICE_PROXIES = "generate service proxies and objects inherited from soap struct";
    private static final String IMPORT_PATH = "import path(s)";
    private static final String GENERATE_LINKABLE_MODULES = "generate linkable modules (experimental)";
    private static final String GENERATE_MATLAB_CODE = "generate Matlab(tm) code for MEX compiler";
    private static final String SERVICE_NAME = "use service name to rename service functions and namespace table";
    private static final String NAME_PREFIX = "file prefix";
    private static final String XSI_TYPED = "generate code for fully xsi:type typed SOAP/XML messaging";
    private static final String NO_GEN_WSDL_SCHEMA = "don't generate WSDL and schema files";
    private static final String NO_GEN_SAMPLE_XML = "don't generate sample XML message files";

    private static final String WSDL2H = "run wsdl2h";
    private static final String SOAPCPP2 = "run soapcpp2";
    public static final String SOAPUI_ACTION_ID = "GSoapAction";

    public GSoapAction() {
        super("GSoap Artifacts", "Generates GSoap artifacts using wsdl2h and soap2cpp");
    }

    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);

        values.putIfMissing(WSDL2H, Boolean.toString(true));
        values.putIfMissing(SOAPCPP2, Boolean.toString(true));
        return values;
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("GSoap Artifacts");

        XForm wsdl2hAdvForm = builder.createForm("wsdl2h");

        wsdl2hAdvForm.addCheckBox(WSDL2H, null);
        addWSDLFields(wsdl2hAdvForm, modelItem);

        wsdl2hAdvForm.addTextField(WSDL2H_OUTPUT, "name of output file", XForm.FieldType.PROJECT_FILE);

        XForm soapcpp2AdvForm = builder.createForm("soapcpp2");

        soapcpp2AdvForm.addCheckBox(SOAPCPP2, null);

        wsdl2hAdvForm.addCheckBox(GENERATE_INDEXED_STRUCT, null);
        wsdl2hAdvForm.addCheckBox(GENERATE_C_SOURCE, null);
        wsdl2hAdvForm.addCheckBox(DOM, null);
        wsdl2hAdvForm.addCheckBox(DONT_QUALIFY_ENUM_NAMES, null);
        wsdl2hAdvForm.addCheckBox(FLAT_CPP_CLASS_HIERARCHY, null);
        wsdl2hAdvForm.addCheckBox(GLOBAL_TOP_LEVEL_DECLARATIONS, null);
        wsdl2hAdvForm.addTextField(PATH, "use path to find files", XForm.FieldType.PROJECT_FOLDER);
        wsdl2hAdvForm.addCheckBox(INCLUDE_LICENSE_INFORMATION, null);
        wsdl2hAdvForm.addCheckBox(USE_XSD_H, null);
        wsdl2hAdvForm.addTextField(BASE_NAMESPACE_PREFIX_INSTEAD_OF_NS,
                "use name as the base namespace prefix name instead of ns", XForm.FieldType.TEXT);
        wsdl2hAdvForm.addTextField(BASE_NAMESPACE_PREFIX_FOR_SERVICE_NS,
                "use name as the base namespace prefix name for service namespaces", XForm.FieldType.TEXT);

        wsdl2hAdvForm.addCheckBox(POLYMORPHIC_TYPES, null);
        wsdl2hAdvForm.addTextField(PROXY_HOST_PORT, "connect via proxy host and port (host:port)", XForm.FieldType.TEXT);
        wsdl2hAdvForm.addCheckBox(NO_STL, null);
        wsdl2hAdvForm.addTextField(TYPE_MAP_FILE, "use type map file instead of the default file typemap.dat",
                XForm.FieldType.PROJECT_FILE);
        wsdl2hAdvForm.addCheckBox(NO_UNIONS, null);
        wsdl2hAdvForm.addCheckBox(VERBOSE, null);
        wsdl2hAdvForm.addCheckBox(WRAP_RESPONSE_STRUCT, null);
        wsdl2hAdvForm.addCheckBox(NO_ANY, null);
        wsdl2hAdvForm.addCheckBox(TYPEDEF_SYNONYMS, null);

        soapcpp2AdvForm.addCheckBox(SOAP_11, null);
        soapcpp2AdvForm.addCheckBox(SOAP_12, null);
        soapcpp2AdvForm.addCheckBox(CLIENT_SIDE, null);
        soapcpp2AdvForm.addCheckBox(SERVER_SIDE, null);
        soapcpp2AdvForm.addCheckBox(NO_LIB, null);
        soapcpp2AdvForm.addCheckBox(SOAP_ACTION, null);
        soapcpp2AdvForm.addCheckBox(GENERATE_C_SOURCE_CPP, null);
        // soapcpp2AdvForm.addTextField(SAVE_PATH, "", XForm.FieldType.DIRECTORY);
        soapcpp2AdvForm.addCheckBox(SOAP_RPC, null);
        soapcpp2AdvForm.addCheckBox(SERVICE_PROXIES, null);
        soapcpp2AdvForm.addTextField(IMPORT_PATH, "use path(s) for #import", XForm.FieldType.PROJECT_FOLDER);
        soapcpp2AdvForm.addCheckBox(GENERATE_LINKABLE_MODULES, null);
        soapcpp2AdvForm.addCheckBox(GENERATE_MATLAB_CODE, null);
        soapcpp2AdvForm.addCheckBox(SERVICE_NAME, null);
        soapcpp2AdvForm.addTextField(NAME_PREFIX, "save files with new prefix name instead of 'soap'",
                XForm.FieldType.TEXT);
        soapcpp2AdvForm.addCheckBox(XSI_TYPED, null);
        soapcpp2AdvForm.addCheckBox(NO_GEN_WSDL_SCHEMA, null);
        soapcpp2AdvForm.addCheckBox(NO_GEN_SAMPLE_XML, null);

        return builder.buildDialog(buildDefaultActions(HelpUrls.GSOAP_HELP_URL, modelItem),
                "Specify arguments for GSoap wsdl2h and soap2cpp", UISupport.TOOL_ICON);
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String gsoapDir = SoapUI.getSettings().getString(ToolsSettings.GSOAP_LOCATION, null);
        if (Tools.isEmpty(gsoapDir)) {
            UISupport.showErrorMessage("GSoap directory must be set in global preferences");
            return;
        }

        List<ProcessBuilder> builders = new ArrayList<ProcessBuilder>();

        if (values.getBoolean(WSDL2H)) {
            ProcessBuilder wsdl2hBuilder = new ProcessBuilder();
            ArgumentBuilder wsdl2hArgs = buildWsdl2HArgs(values, modelItem);
            wsdl2hBuilder.command(wsdl2hArgs.getArgs());
            // wsdl2hBuilder.directory(new File(gsoapDir));
            wsdl2hBuilder.directory(new File(Tools.getDir(values.get(WSDL2H_OUTPUT))));
            builders.add(wsdl2hBuilder);
        }

        if (values.getBoolean(SOAPCPP2)) {
            ProcessBuilder soapcpp2Builder = new ProcessBuilder();
            ArgumentBuilder soapcpp2Args = buildSoapcpp2Args(values);
            soapcpp2Builder.command(soapcpp2Args.getArgs());
            soapcpp2Builder.directory(new File(Tools.getDir(values.get(WSDL2H_OUTPUT))));

            builders.add(soapcpp2Builder);
        }

        if (builders.isEmpty()) {
            UISupport.showErrorMessage("Nothing to run!");
        } else {
            toolHost.run(new ProcessToolRunner(builders.toArray(new ProcessBuilder[builders.size()]), "GSoap",
                    modelItem));
        }
    }

    private ArgumentBuilder buildWsdl2HArgs(StringToStringMap values, Interface modelItem) {
        String gsoapDir = SoapUI.getSettings().getString(ToolsSettings.GSOAP_LOCATION, null);
        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.startScript(gsoapDir + File.separator + "wsdl2h", "", "");
        builder.addArgs(new String[]{"-v"});

        values.put(WSDL2H_OUTPUT, Tools.getFilename(values.get(WSDL2H_OUTPUT)));

        builder.addString(WSDL2H_OUTPUT, "-o", "");
        builder.addArgs(getWsdlUrl(values, modelItem));

        builder.addBoolean(GENERATE_INDEXED_STRUCT, "-a");
        builder.addBoolean(GENERATE_C_SOURCE, "-c");
        builder.addBoolean(DOM, "-c");
        builder.addBoolean(DONT_QUALIFY_ENUM_NAMES, "-e");
        builder.addBoolean(FLAT_CPP_CLASS_HIERARCHY, "-f");
        builder.addBoolean(GLOBAL_TOP_LEVEL_DECLARATIONS, "-g");
        builder.addString(PATH, "-I", "");
        builder.addBoolean(INCLUDE_LICENSE_INFORMATION, "-l");
        builder.addBoolean(USE_XSD_H, "-m");
        builder.addString(BASE_NAMESPACE_PREFIX_INSTEAD_OF_NS, "-n", "");
        builder.addString(BASE_NAMESPACE_PREFIX_FOR_SERVICE_NS, "-N", "");
        builder.addBoolean(POLYMORPHIC_TYPES, "-p");
        builder.addString(PROXY_HOST_PORT, "-r", "");
        builder.addBoolean(NO_STL, "-s");
        builder.addString(TYPE_MAP_FILE, "-t", "");
        builder.addBoolean(NO_UNIONS, "-u");
        builder.addBoolean(VERBOSE, "-v");
        builder.addBoolean(WRAP_RESPONSE_STRUCT, "-w");
        builder.addBoolean(NO_ANY, "-x");
        builder.addBoolean(TYPEDEF_SYNONYMS, "-y");
        return builder;
    }

    private ArgumentBuilder buildSoapcpp2Args(StringToStringMap values) {
        String gsoapDir = SoapUI.getSettings().getString(ToolsSettings.GSOAP_LOCATION, null);
        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.startScript(gsoapDir + File.separator + "soapcpp2", "", "");
        builder.addArgs(new String[]{"-Iimport", values.get(WSDL2H_OUTPUT)});

        builder.addBoolean(SOAP_11, "-1");
        builder.addBoolean(SOAP_12, "-2");
        builder.addBoolean(CLIENT_SIDE, "-C");
        builder.addBoolean(SERVER_SIDE, "-S");
        builder.addBoolean(NO_LIB, "-L");
        builder.addBoolean(SOAP_ACTION, "-a");
        builder.addBoolean(GENERATE_C_SOURCE_CPP, "-c");
        builder.addBoolean(SOAP_RPC, "-e");
        builder.addBoolean(SERVICE_PROXIES, "-i");
        builder.addString(IMPORT_PATH, "-I", "");
        builder.addBoolean(GENERATE_LINKABLE_MODULES, "-l");
        builder.addBoolean(GENERATE_MATLAB_CODE, "-m");
        builder.addBoolean(SERVICE_NAME, "-n");
        builder.addString(NAME_PREFIX, "-p", "");
        builder.addBoolean(XSI_TYPED, "-t");
        builder.addBoolean(NO_GEN_WSDL_SCHEMA, "-w");
        builder.addBoolean(NO_GEN_SAMPLE_XML, "-x");

        return builder;
    }
}
