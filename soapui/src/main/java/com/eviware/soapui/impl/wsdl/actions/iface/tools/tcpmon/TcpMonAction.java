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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.tcpmon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

import javax.swing.Action;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Invokes Apache TCPmon tool
 *
 * @author Ole.Matzura
 */

public class TcpMonAction extends AbstractToolsAction<WsdlInterface> {
    private static final String ENDPOINT = "Endpoint";
    private static final String PORT = "Local Port";
    private static final String ADD_ENDPOINT = "Add local endpoint";
    private XForm mainForm;
    public static final String SOAPUI_ACTION_ID = "TcpMonAction";

    public TcpMonAction() {
        super("Launch TcpMon", "Launch Tcp Mon for monitoring SOAP traffic");
    }

    protected XFormDialog buildDialog(WsdlInterface modelItem) {
        if (modelItem == null) {
            return null;
        }

        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Launch TcpMon");

        mainForm = builder.createForm("Basic");
        mainForm.addComboBox(ENDPOINT, new String[]{""}, "endpoint to forward to");
        mainForm.addTextField(PORT, "Local port to listen on.", XForm.FieldType.TEXT);
        mainForm.addCheckBox(ADD_ENDPOINT, "adds an endpoint to the interface pointing to the started monitor");

        return builder.buildDialog(buildDefaultActions(HelpUrls.TCPMON_HELP_URL, modelItem),
                "Specify arguments for launching TcpMon", UISupport.TOOL_ICON);
    }

    protected Action createRunOption(WsdlInterface modelItem) {
        Action action = super.createRunOption(modelItem);
        action.putValue(Action.NAME, "Launch");
        return action;
    }

    protected StringToStringMap initValues(WsdlInterface modelItem, Object param) {
        if (modelItem != null) {
            List<String> endpoints = new ArrayList<String>(Arrays.asList(modelItem.getEndpoints()));
            endpoints.add(0, null);
            mainForm.setOptions(ENDPOINT, endpoints.toArray());
        } else if (mainForm != null) {
            mainForm.setOptions(ENDPOINT, new String[]{null});
        }

        StringToStringMap values = super.initValues(modelItem, param);
        if (!values.isEmpty()) {
            return values;
        }

        values.put(ENDPOINT, getDefinition(modelItem));
        values.put(PORT, "8080");

        return values;
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, WsdlInterface modelItem) throws Exception {
        String tcpMonDir = SoapUI.getSettings().getString(ToolsSettings.TCPMON_LOCATION, null);
        if (Tools.isEmpty(tcpMonDir)) {
            UISupport.showErrorMessage("TcpMon directory must be set in global preferences");
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        ArgumentBuilder args = buildArgs(modelItem);
        builder.command(args.getArgs());
        builder.directory(new File(tcpMonDir + File.separatorChar + "build"));

        SoapUI.log("Launching tcpmon in directory [" + builder.directory() + "] with arguments [" + args.toString()
                + "]");

        builder.start();
        closeDialog(modelItem);
    }

    private ArgumentBuilder buildArgs(WsdlInterface modelItem) throws IOException {
        XFormDialog dialog = getDialog();
        if (dialog == null) {
            ArgumentBuilder builder = new ArgumentBuilder(new StringToStringMap());
            builder.startScript("tcpmon", ".bat", ".sh");
            return builder;
        }

        StringToStringMap values = dialog.getValues();

        ArgumentBuilder builder = new ArgumentBuilder(values);
        builder.startScript("tcpmon", ".bat", ".sh");

        builder.addArgs(values.get(PORT));
        String endpoint = values.get(ENDPOINT);
        if (endpoint != null && !endpoint.equals("- none available -")) {
            URL url = new URL(endpoint);
            builder.addArgs(url.getHost());
            builder.addArgs((url.getPort() == -1) ? "80" : "" + url.getPort());

            if (values.getBoolean(ADD_ENDPOINT)) {
                modelItem.addEndpoint("http://localhost:" + values.get(PORT) + url.getPath());
            }
        }

        addToolArgs(values, builder);
        return builder;
    }
}
