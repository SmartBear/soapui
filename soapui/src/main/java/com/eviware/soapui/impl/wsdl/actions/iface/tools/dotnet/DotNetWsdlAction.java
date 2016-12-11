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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.dotnet;

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

/**
 * Invokes .NET wsdl.exe
 *
 * @author Ole.Matzura
 */

public class DotNetWsdlAction extends AbstractToolsAction<Interface> {
    private static final String OUTPUT = "output directory";
    private static final String LANGUAGE = "language";
    private static final String SERVER = "serverInterface";
    private static final String NAMESPACE = "namespace";
    private static final String PROTOCOL = "protocol";

    private static final String SHARETYPES = "sharetypes";
    private static final String FIELDS = "fields";
    private static final String ORDER = "order";
    private static final String ENABLEDATABINDING = "enableDataBinding";
    private static final String URLKEY = "url key";
    private static final String BASEURL = "base url";

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String DOMAIN = "domain";
    private static final String PROXY = "proxy";
    private static final String PROXYUSERNAME = "proxy username";
    private static final String PROXYPASSWORD = "proxy password";
    private static final String PROXYDOMAIN = "proxy domain";
    public static final String SOAPUI_ACTION_ID = "DotNetWsdlAction";

    public DotNetWsdlAction() {
        super(".NET 2.0 Artifacts", "Generates .NET 2.0 artifacts using wsdl.exe");
    }

    protected StringToStringMap initValues(Interface modelItem, Object param) {
        StringToStringMap values = super.initValues(modelItem, param);
        values.putIfMissing(LANGUAGE, "CS");
        return values;
    }

    protected XFormDialog buildDialog(Interface modelItem) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(".NET 2.0 artifacts");

        XForm mainForm = builder.createForm("Basic");
        addWSDLFields(mainForm, modelItem);

        mainForm.addTextField(OUTPUT, "root directory for generated files.", XForm.FieldType.PROJECT_FOLDER);
        mainForm.addTextField(NAMESPACE, "The namespace for the generated proxy or template", XForm.FieldType.TEXT);

        mainForm.addCheckBox(SERVER, "(Generates interfaces for server-side implementation of an ASP.Net Web Service)");
        mainForm.addComboBox(LANGUAGE, new String[]{"CS", "VB", "JS", "VJS", "CPP"}, "add scope to deploy.wsdd");

        mainForm.addComboBox(PROTOCOL, new String[]{"SOAP", "SOAP12", "HttpGet", "HttpPost"},
                "override the default protocol to implement");

        XForm advForm = builder.createForm("Advanced");

        advForm.addCheckBox(SHARETYPES, "(turns on type sharing feature)");
        advForm.addCheckBox(FIELDS, "(generate fields instead of properties)");
        advForm.addCheckBox(ORDER, "(generate explicit order identifiers on particle members)");
        advForm.addCheckBox(ENABLEDATABINDING, "(implement INotifyPropertyChanged interface on all generated types)");
        advForm.addTextField(URLKEY, "configuration key to use in the code generation to read the default URL value",
                XForm.FieldType.URL);
        advForm.addTextField(BASEURL, "base url to use when calculating the url fragment", XForm.FieldType.URL);

        XForm httpForm = builder.createForm("HTTP settings");
        httpForm.addTextField(USERNAME, "username to access the WSDL-URI", XForm.FieldType.TEXT);
        httpForm.addTextField(PASSWORD, "password to access the WSDL-URI", XForm.FieldType.PASSWORD);
        httpForm.addTextField(DOMAIN, "domain to access the WSDL-URI", XForm.FieldType.TEXT);
        httpForm.addTextField(PROXY, "username to access the WSDL-URI", XForm.FieldType.TEXT);
        httpForm.addTextField(PROXYUSERNAME, "proxy username to access the WSDL-URI", XForm.FieldType.TEXT);
        httpForm.addTextField(PROXYPASSWORD, "proxy password to access the WSDL-URI", XForm.FieldType.PASSWORD);
        httpForm.addTextField(PROXYDOMAIN, "proxy domain to access the WSDL-URI", XForm.FieldType.TEXT);

        buildArgsForm(builder, false, "wsdl.exe");

        return builder.buildDialog(buildDefaultActions(HelpUrls.DOTNET_HELP_URL, modelItem),
                "Specify arguments for .NET 2 wsdl.exe", UISupport.TOOL_ICON);
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        String dotnetDir = SoapUI.getSettings().getString(ToolsSettings.DOTNET_WSDL_LOCATION, null);
        if (Tools.isEmpty(dotnetDir)) {
            UISupport.showErrorMessage(".NET 2.0 wsdl.exe directory must be set in global preferences");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(values, modelItem);
        builder.command(args.getArgs());
        builder.directory(new File(dotnetDir));

        toolHost.run(new ProcessToolRunner(builder, ".NET wsdl.exe", modelItem));
    }

    private ArgumentBuilder buildArgs(StringToStringMap values, Interface modelItem) {
        values.put(OUTPUT, Tools.ensureDir(values.get(OUTPUT), ""));

        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.addArgs("cmd.exe", "/C", "wsdl.exe", "/nologo", "/verbose");

        builder.addString(NAMESPACE, "/namespace", ":");
        builder.addString(OUTPUT, "/out", ":");
        builder.addString(PROTOCOL, "/protocol", ":");
        builder.addString(LANGUAGE, "/language", ":");

        builder.addBoolean(SERVER, "/serverInterface");
        builder.addBoolean(SHARETYPES, "/sharetypes");
        builder.addBoolean(FIELDS, "/fields");
        builder.addBoolean(ORDER, "/order");
        builder.addBoolean(ENABLEDATABINDING, "/enableDataBinding");

        builder.addString(URLKEY, "/appsettingurlkey", ":");
        builder.addString(BASEURL, "/appsettingbaseurl", ":");

        builder.addString(USERNAME, "/username", ":");
        builder.addString(PASSWORD, "/password", ":");
        builder.addString(DOMAIN, "/domain", ":");
        builder.addString(PROXY, "/proxy", ":");
        builder.addString(PROXYUSERNAME, "/proxyusername", ":");
        builder.addString(PROXYPASSWORD, "/proxypassword", ":");
        builder.addString(PROXYDOMAIN, "/proxydomain", ":");

        addToolArgs(values, builder);

        builder.addArgs(getWsdlUrl(values, modelItem));

        return builder;
    }
}
