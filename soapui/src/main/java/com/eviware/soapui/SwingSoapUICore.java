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

package com.eviware.soapui;

import com.eviware.soapui.config.SoapuiSettingsDocumentConfig;
import com.eviware.soapui.impl.rest.panels.request.inspectors.representations.RestRepresentationsInspectorFactory;
import com.eviware.soapui.impl.rest.panels.request.inspectors.schema.InferredSchemaInspectorFactory;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SwingToolHost;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.inspectors.amfheader.AMFHeadersInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.attachments.AttachmentsInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.httpheaders.HttpHeadersInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.jms.header.JMSHeaderInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.jms.property.JMSHeaderAndPropertyInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.jms.property.JMSPropertyInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.script.ScriptInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.ssl.SSLInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.wsa.WsaInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.wsrm.WsrmInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.wss.WssInspectorFactory;
import com.eviware.soapui.support.editor.registry.InspectorRegistry;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.SwingFormFactory;

import java.io.File;
import java.io.FileInputStream;

public class SwingSoapUICore extends DefaultSoapUICore {
    public SwingSoapUICore() {
        super();
    }

    public SwingSoapUICore(String root, String settingsFile) {
        super(root, settingsFile);
    }

    public SwingSoapUICore(boolean settingPassword, String soapUISettingsPassword) {
        super(settingPassword, soapUISettingsPassword);
    }

    public void prepareUI() {
        UISupport.setToolHost(new SwingToolHost());
        XFormFactory.Factory.instance = new SwingFormFactory();
    }

    public void afterStartup(Workspace workspace) {

        InspectorRegistry inspectorRegistry = InspectorRegistry.getInstance();
        inspectorRegistry.addFactory(new ScriptInspectorFactory());
        inspectorRegistry.addFactory(new AuthInspectorFactory());
        inspectorRegistry.addFactory(new HttpHeadersInspectorFactory());
        inspectorRegistry.addFactory(new AttachmentsInspectorFactory());
        inspectorRegistry.addFactory(new SSLInspectorFactory());
        inspectorRegistry.addFactory(new WssInspectorFactory());
        inspectorRegistry.addFactory(new WsaInspectorFactory());
        inspectorRegistry.addFactory(new WsrmInspectorFactory());
        // inspectorRegistry.addFactory( new WsrmPiggybackInspectorFactory());
        inspectorRegistry.addFactory(new RestRepresentationsInspectorFactory());
        inspectorRegistry.addFactory(new InferredSchemaInspectorFactory());
        inspectorRegistry.addFactory(new JMSHeaderInspectorFactory());
        inspectorRegistry.addFactory(new JMSPropertyInspectorFactory());
        inspectorRegistry.addFactory(new JMSHeaderAndPropertyInspectorFactory());
        inspectorRegistry.addFactory(new AMFHeadersInspectorFactory());

        String actionsDir = System.getProperty("soapui.ext.actions");
        addExternalActions(actionsDir == null ? getRoot() == null ? "actions" : getRoot() + File.separatorChar
                + "actions" : actionsDir, getExtensionClassLoader());
    }

    @Override
    protected Settings initSettings(String fileName) {
        String fn = fileName;

        if (!new File(fileName).exists()) {
            try {
                fileName = importSettingsOnStartup(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Settings result = super.initSettings(fileName);

        if (!fileName.equals(fn)) {
            setSettingsFile(fn);
        }

        return result;
    }

    protected String importSettingsOnStartup(String fileName) throws Exception {
        if (UISupport.getDialogs().confirm("Missing SoapUI Settings, import from existing installation?",
                "Import Preferences")) {
            while (true) {
                File settingsFile = UISupport.getFileDialogs().open(null, "Import Preferences", ".xml",
                        "SoapUI settings XML", fileName);
                if (settingsFile != null) {
                    try {
                        SoapuiSettingsDocumentConfig.Factory.parse(settingsFile);
                        log.info("imported soapui-settings from [" + settingsFile.getAbsolutePath() + "]");
                        return settingsFile.getAbsolutePath();
                    } catch (Exception e) {
                        if (!UISupport.getDialogs().confirm(
                                "Error loading settings from [" + settingsFile.getAbsolutePath() + "]\r\nspecify another?",
                                "Error Importing")) {
                            break;
                        }
                    }
                }
            }
        }

        return fileName;
    }

    protected void addExternalActions(String folder, ClassLoader classLoader) {
        File[] actionFiles = new File(folder).listFiles();
        if (actionFiles != null) {
            for (File actionFile : actionFiles) {
                if (actionFile.isDirectory()) {
                    addExternalActions(actionFile.getAbsolutePath(), classLoader);
                    continue;
                }

                if (!actionFile.getName().toLowerCase().endsWith("-actions.xml")) {
                    continue;
                }

                try {
                    log.info("Adding actions from [" + actionFile.getAbsolutePath() + "]");

                    SoapUI.getActionRegistry().addConfig(new FileInputStream(actionFile), classLoader);
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        }
    }
}
