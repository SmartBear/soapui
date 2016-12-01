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

package com.eviware.soapui.impl.wsdl.actions.monitor;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.monitor.ContentTypes;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;
import com.eviware.soapui.impl.wsdl.panels.monitor.SoapMonitorContainer;
import com.eviware.soapui.impl.wsdl.panels.monitor.SoapMonitorDesktopPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.APage;

public class SoapMonitorAction extends AbstractSoapUIAction<WsdlProject> {

    public static final String SOAPUI_ACTION_ID = "SoapMonitorAction";

    private static final String HTTPS_PROTOCOL = "https://";
    private static final String HTTP_TUNNEL = "HTTP Tunnel";
    private static final String HTTP_PROXY = "HTTP Proxy";
    private SoapMonitor soapMonitor;

    public SoapMonitorAction() {
        super("Launch HTTP Monitor", "Launches a HTTP traffic monitor for this project");
    }

    public void perform(WsdlProject project, Object param) {
        DialogHandler dialogHandler = createDialogHandler(project, param);
        final XFormDialog dialog = dialogHandler.buildDialog();

        Settings settings = project.getSettings();

        dialogHandler.setDialogValues(settings);
        soapMonitor = null;
        if (dialog.show()) {
            try {
                UISupport.setHourglassCursor();

                dialogHandler.saveDialogValues(settings);
                loadAllInterfacesIn(project);

                soapMonitor = dialogHandler.createSoapMonitor();
            } catch (Exception e) {
                SoapUI.logError(e);
            } finally {
                UISupport.resetCursor();
            }
        }
    }

    private void loadAllInterfacesIn(WsdlProject project) throws Exception {
        for (Interface iface : project.getInterfaceList()) {
            iface.getDefinitionContext().loadIfNecessary();
        }
    }


    protected DialogHandler createDialogHandler(WsdlProject project, Object param) {
        return new FullMonitorDialogHandler(project);
    }

    public static ContentTypes defaultContentTypes() {
        return ContentTypes.of("*/html, */xml, */soap+xml, */json, */x-json, */javascript, */x-amf, */http");
    }

    public SoapMonitor getSoapMonitor() {
        return soapMonitor;
    }

    protected interface DialogHandler {
        XFormDialog buildDialog();

        void setDialogValues(Settings settings);

        void saveDialogValues(Settings settings);

        SoapMonitor createSoapMonitor();
    }

    protected SoapMonitor openSoapMonitor(WsdlProject target, int listenPort, String incomingRequestWss,
                                          String incomingResponseWss, boolean setAsProxy, String sslEndpoint) {
        DesktopPanel desktopPanel = null;
        if (sslEndpoint == null) {
            desktopPanel = createDesktopPanel(target, listenPort, incomingRequestWss, incomingResponseWss, setAsProxy, null);
            UISupport.showDesktopPanel(desktopPanel);
        } else {
            String ssl = validate(sslEndpoint);
            if (ssl == null) {
                UISupport.showErrorMessage("SSL Monitor needs endpoint.");
            } else {
                desktopPanel = createDesktopPanel(target, listenPort, incomingRequestWss, incomingResponseWss, setAsProxy, ssl);
                UISupport.showDesktopPanel(desktopPanel);
            }
        }
        return desktopPanel != null ? ((SoapMonitorContainer) desktopPanel).getSoapMonitor() : null;
    }

    protected DesktopPanel createDesktopPanel(WsdlProject target, int listenPort, String incomingRequestWss, String incomingResponseWss, boolean setAsProxy, String ssl) {
        return new SoapMonitorDesktopPanel(target, listenPort, incomingRequestWss,
                incomingResponseWss, setAsProxy, ssl);
    }

    protected String validate(String sslEndpoint) {
        if (sslEndpoint.trim().length() > 0) {
            return sslEndpoint.trim();
        }
        return null;
    }

    private void setDialogState(XFormDialog dialog, String newValue) {
        if (HTTP_PROXY.equals(newValue)) {
            dialog.getFormField(LaunchForm.SETSSLMON).setEnabled(false);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_KEYSTORE).setEnabled(false);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_PASSWORD).setEnabled(false);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_KEYPASSWORD).setEnabled(false);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_TRUSTSTORE).setEnabled(false);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_TRUSTSTORE_PASSWORD).setEnabled(false);
            dialog.getFormField(LaunchForm.SSLTUNNEL_REUSESTATE).setEnabled(false);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_KEYSTOREPATH).setEnabled(false);
            dialog.getFormField(LaunchForm.SET_CONTENT_TYPES).setEnabled(true);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_KEYSTOREPASSWORD).setEnabled(false);

            dialog.getFormField(LaunchForm.SETASPROXY).setEnabled(true);
            dialog.getFormField(LaunchForm.REQUEST_WSS).setEnabled(true);
            dialog.getFormField(LaunchForm.RESPONSE_WSS).setEnabled(true);
        } else {
            dialog.getFormField(LaunchForm.SETSSLMON).setEnabled(true);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_KEYSTORE).setEnabled(true);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_PASSWORD).setEnabled(true);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_KEYPASSWORD).setEnabled(true);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_TRUSTSTORE).setEnabled(true);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_TRUSTSTORE_PASSWORD).setEnabled(true);
            dialog.getFormField(LaunchForm.SSLTUNNEL_REUSESTATE).setEnabled(true);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_KEYSTOREPATH).setEnabled(true);
            dialog.getFormField(LaunchForm.SET_CONTENT_TYPES).setEnabled(true);
            dialog.getFormField(SecurityTabForm.SSLTUNNEL_KEYSTOREPASSWORD).setEnabled(true);

            dialog.getFormField(LaunchForm.SETASPROXY).setEnabled(false);
            dialog.getFormField(LaunchForm.REQUEST_WSS).setEnabled(false);
            dialog.getFormField(LaunchForm.RESPONSE_WSS).setEnabled(false);
        }
    }

    @AForm(description = "Specify HTTP Monitor settings", name = "General Options", helpUrl = HelpUrls.SOAPMONITOR_GENERAL_OPTIONS, icon = UISupport.TOOL_ICON_PATH)
    private interface WizardForm {
        @APage(name = "General")
        public final static LaunchForm general = null;

        @APage(name = "Security")
        public final static SecurityTabForm security = null;
    }

    @AForm(description = "Specify HTTP Monitor settings", name = "Launch HTTP Monitor", helpUrl = HelpUrls.SOAPMONITOR_GENERAL_OPTIONS)
    public interface LaunchForm {
        @AField(description = "SSL tunnel or HTTP proxy", name = "Choose one:", type = AFieldType.RADIOGROUP)
        public final static String SSLORHTTP = "Choose one:";

        @AField(description = "The local port to listen on", name = "Port", type = AFieldType.INT)
        public final static String PORT = "Port";

        @AField(description = "The Incoming WSS configuration to use for processing requests", name = "Incoming Request WSS", type = AFieldType.ENUMERATION)
        public final static String REQUEST_WSS = "Incoming Request WSS";

        @AField(description = "The Outgoing WSS configuration to use for processing responses", name = "Incoming Response WSS", type = AFieldType.ENUMERATION)
        public final static String RESPONSE_WSS = "Incoming Response WSS";

        @AField(description = "Set as Global Proxy", name = "Set as Proxy", type = AFieldType.BOOLEAN)
        public final static String SETASPROXY = "Set as Proxy";

        @AField(description = "Set endpoint", name = "Set endpoint for HTTP Tunnel:", type = AFieldType.STRING)
        public final static String SETSSLMON = "Set endpoint for HTTP Tunnel:";

        @AField(description = "Keep request state", name = "Reuse request state", type = AFieldType.BOOLEAN)
        public final static String SSLTUNNEL_REUSESTATE = "Reuse request state";

        @AField(description = "Content types to monitor, if blank default types will be set!", name = "Content types to monitor", type = AFieldType.STRINGAREA)
        public final static String SET_CONTENT_TYPES = "Content types to monitor";

    }

    @AForm(description = "Specify HTTP tunnel security settings", name = "HTTP tunnel security", helpUrl = HelpUrls.SOAPMONITOR_GENERAL_OPTIONS)
    public interface SecurityTabForm {
        @AField(description = "Set SSL Tunnel KeyStore", name = "HTTP tunnel - KeyStore", type = AFieldType.FILE)
        public final static String SSLTUNNEL_KEYSTORE = "HTTP tunnel - KeyStore";

        @AField(description = "Set SSL Tunnel Password", name = "HTTP tunnel - Password", type = AFieldType.PASSWORD)
        public final static String SSLTUNNEL_PASSWORD = "HTTP tunnel - Password";

        @AField(description = "Set SSL Tunnel KeyPassword", name = "HTTP tunnel - KeyPassword", type = AFieldType.PASSWORD)
        public final static String SSLTUNNEL_KEYPASSWORD = "HTTP tunnel - KeyPassword";

        @AField(description = "Set SSL Tunnel TrustStore", name = "HTTP tunnel - TrustStore", type = AFieldType.FILE)
        public final static String SSLTUNNEL_TRUSTSTORE = "HTTP tunnel - TrustStore";

        @AField(description = "Set SSL Tunnel TrustStore Password", name = "HTTP tunnel - TrustStore Password", type = AFieldType.PASSWORD)
        public final static String SSLTUNNEL_TRUSTSTORE_PASSWORD = "HTTP tunnel - TrustStore Password";

        @AField(description = "Set SSL Client Key Store", name = "HTTP tunnel - Set SSL Client Key Store path", type = AFieldType.FILE)
        public final static String SSLTUNNEL_KEYSTOREPATH = "HTTP tunnel - Set SSL Client Key Store path";

        @AField(description = "Set SSL Client Key Store Password", name = "HTTP tunnel - Set SSL Client Key Store Password", type = AFieldType.PASSWORD)
        public final static String SSLTUNNEL_KEYSTOREPASSWORD = "HTTP tunnel - Set SSL Client Key Store Password";
    }

    protected class FullMonitorDialogHandler implements DialogHandler {


        private WsdlProject project;
        private XFormDialog dialog;

        public FullMonitorDialogHandler(WsdlProject project) {
            this.project = project;
        }

        @Override
        public XFormDialog buildDialog() {
            dialog = ADialogBuilder.buildTabbedDialog(WizardForm.class, null);
            dialog.setSize(650, 500);
            return dialog;
        }

        @Override
        public void setDialogValues(Settings settings) {
            dialog.setIntValue(LaunchForm.PORT, (int) settings.getLong(LaunchForm.PORT, 8081));
            dialog.setOptions(LaunchForm.REQUEST_WSS,
                    StringUtils.merge(project.getWssContainer().getIncomingWssNames(), "<none>"));
            dialog.setOptions(LaunchForm.RESPONSE_WSS,
                    StringUtils.merge(project.getWssContainer().getIncomingWssNames(), "<none>"));
            dialog.setValue(LaunchForm.SETSSLMON,
                    settings.getString(LaunchForm.SETSSLMON, "").length() > 0 ? settings.getString(LaunchForm.SETSSLMON, "")
                            : HTTPS_PROTOCOL);
            dialog.setOptions(LaunchForm.SSLORHTTP, new String[]{HTTP_TUNNEL, HTTP_PROXY});

            dialog.setValue(SecurityTabForm.SSLTUNNEL_KEYSTORE, settings.getString(SecurityTabForm.SSLTUNNEL_KEYSTORE, ""));
            dialog.setValue(SecurityTabForm.SSLTUNNEL_PASSWORD, settings.getString(SecurityTabForm.SSLTUNNEL_PASSWORD, ""));
            dialog.setValue(SecurityTabForm.SSLTUNNEL_KEYPASSWORD,
                    settings.getString(SecurityTabForm.SSLTUNNEL_KEYPASSWORD, ""));
            dialog.setValue(SecurityTabForm.SSLTUNNEL_TRUSTSTORE,
                    settings.getString(SecurityTabForm.SSLTUNNEL_TRUSTSTORE, ""));
            dialog.setValue(SecurityTabForm.SSLTUNNEL_TRUSTSTORE_PASSWORD,
                    settings.getString(SecurityTabForm.SSLTUNNEL_TRUSTSTORE_PASSWORD, ""));
            dialog.setBooleanValue(LaunchForm.SSLTUNNEL_REUSESTATE, settings.getBoolean(LaunchForm.SSLTUNNEL_REUSESTATE));
            dialog.setValue(LaunchForm.SET_CONTENT_TYPES,
                    settings.getString(LaunchForm.SET_CONTENT_TYPES, defaultContentTypes().toString()));
            dialog.setValue(SecurityTabForm.SSLTUNNEL_KEYSTOREPATH,
                    settings.getString(SecurityTabForm.SSLTUNNEL_KEYSTOREPATH, ""));
            dialog.setValue(SecurityTabForm.SSLTUNNEL_KEYSTOREPASSWORD,
                    settings.getString(SecurityTabForm.SSLTUNNEL_KEYSTOREPASSWORD, ""));

            XFormField sslOrHttp = dialog.getFormField(LaunchForm.SSLORHTTP);
            sslOrHttp.setValue(HTTP_PROXY);
            setDialogState(dialog, HTTP_PROXY);
            sslOrHttp.addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    setDialogState(dialog, newValue);
                }

            });
        }

        @Override
        public void saveDialogValues(Settings settings) {
            int listenPort = dialog.getIntValue(LaunchForm.PORT, 8080);
            settings.setLong(LaunchForm.PORT, listenPort);

            settings.setString(LaunchForm.SETSSLMON, dialog.getValue(LaunchForm.SETSSLMON));

            settings.setString(SecurityTabForm.SSLTUNNEL_KEYSTORE,
                    dialog.getValue(SecurityTabForm.SSLTUNNEL_KEYSTORE));
            settings.setString(SecurityTabForm.SSLTUNNEL_PASSWORD,
                    dialog.getValue(SecurityTabForm.SSLTUNNEL_PASSWORD));
            settings.setString(SecurityTabForm.SSLTUNNEL_KEYPASSWORD,
                    dialog.getValue(SecurityTabForm.SSLTUNNEL_KEYPASSWORD));
            settings.setString(SecurityTabForm.SSLTUNNEL_TRUSTSTORE,
                    dialog.getValue(SecurityTabForm.SSLTUNNEL_TRUSTSTORE));
            settings.setString(SecurityTabForm.SSLTUNNEL_TRUSTSTORE_PASSWORD,
                    dialog.getValue(SecurityTabForm.SSLTUNNEL_TRUSTSTORE_PASSWORD));
            settings.setString(LaunchForm.SSLTUNNEL_REUSESTATE, dialog.getValue(LaunchForm.SSLTUNNEL_REUSESTATE));
            settings.setString(SecurityTabForm.SSLTUNNEL_KEYSTOREPATH,
                    dialog.getValue(SecurityTabForm.SSLTUNNEL_KEYSTOREPATH));
            if (dialog.getValue(LaunchForm.SET_CONTENT_TYPES) != null
                    && dialog.getValue(LaunchForm.SET_CONTENT_TYPES).trim().equals("")) {
                settings.setString(LaunchForm.SET_CONTENT_TYPES, defaultContentTypes().toString());
            } else {
                settings.setString(LaunchForm.SET_CONTENT_TYPES, dialog.getValue(LaunchForm.SET_CONTENT_TYPES));
            }

            settings.setString(SecurityTabForm.SSLTUNNEL_KEYSTOREPASSWORD,
                    dialog.getValue(SecurityTabForm.SSLTUNNEL_KEYSTOREPASSWORD));
        }

        @Override
        public SoapMonitor createSoapMonitor() {
            int listenPort = dialog.getIntValue(LaunchForm.PORT, 8080);
            if (HTTP_PROXY.equals(dialog.getValue(LaunchForm.SSLORHTTP))) {
                return openSoapMonitor(project, listenPort, dialog.getValue(LaunchForm.REQUEST_WSS),
                        dialog.getValue(LaunchForm.RESPONSE_WSS), dialog.getBooleanValue(LaunchForm.SETASPROXY), null);
            } else {
                return openSoapMonitor(project, listenPort, dialog.getValue(LaunchForm.REQUEST_WSS),
                        dialog.getValue(LaunchForm.RESPONSE_WSS), false,
                        dialog.getValue(LaunchForm.SETSSLMON));
            }
        }


    }
}
