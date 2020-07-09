/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSEndpoint;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import hermes.Domain;
import hermes.Hermes;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddJMSEndpointAction extends AbstractSoapUIAction<AbstractInterface<?>> {
    public static final String SOAPUI_ACTION_ID = "AddJMSEndpointAction";
    private static final String HERMES_IMPL_CLASS_NAME = "hermes.impl.DefaultHermesImpl";
    private static final String DESTINATION_CONFIG_CLASS_NAME = "hermes.config.DestinationConfig";
    private static final String SESSION = "Session";
    private static final String HERMES_CONFIG = "Hermes Config";
    private static final String SEND = "Send/Publish destination";
    private static final String RECEIVE = "Receive/Subscribe destination";
    private XForm mainForm;
    List<Destination> destinationNameList;

    public AddJMSEndpointAction() {
        super("Add JMS endpoint", "Wizard for creating JMS endpoint");
    }

    public void perform(AbstractInterface<?> iface, Object param) {

        XFormDialog dialog = buildDialog(iface);

        initValues(iface);

        if (dialog.show()) {
            String session = dialog.getValue(SESSION);
            int i = dialog.getValueIndex(SEND);
            if (i == -1) {
                UISupport.showErrorMessage("Not supported endpoint");
                return;
            }
            String send = destinationNameList.get(i).getDestinationName();
            int j = dialog.getValueIndex(RECEIVE);
            if (j == -1) {
                UISupport.showErrorMessage("Not supported endpoint");
                return;
            }
            String receive = destinationNameList.get(j).getDestinationName();
            if (JMSEndpoint.JMS_EMPTY_DESTIONATION.equals(send) && JMSEndpoint.JMS_EMPTY_DESTIONATION.equals(receive)) {
                UISupport.showErrorMessage("Not supported endpoint");
                return;
            }

            iface.addEndpoint(createEndpointString(session, send, receive));
        }
    }

    private String createEndpointString(String session, String send, String receive) {
        StringBuilder sb = new StringBuilder(JMSEndpoint.JMS_ENDPOINT_PREFIX);
        sb.append(session + JMSEndpoint.JMS_ENDPOINT_SEPARATOR);
        sb.append(send);
        if (!JMSEndpoint.JMS_EMPTY_DESTIONATION.equals(receive)) {
            sb.append(JMSEndpoint.JMS_ENDPOINT_SEPARATOR + receive);
        }
        return sb.toString();
    }

    private String[] getSessionOptions(AbstractInterface<?> iface, String hermesConfigPath) {

        List<Hermes> hermesList = new ArrayList<Hermes>();
        try {
            Context ctx = getHermesContext(iface, hermesConfigPath);
            if (ctx != null) {
                NamingEnumeration<NameClassPair> sessions = ctx.list("");
                while (sessions.hasMore()) {
                    NameClassPair pair = sessions.next();
                    if (pair.getClassName().equals(HERMES_IMPL_CLASS_NAME)) {
                        hermesList.add((Hermes) ctx.lookup(pair.getName()));
                    }
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
            SoapUI.log.warn("no HermesJMS context!");
        }
        List<String> hermesSessionList = new ArrayList<String>();
        for (Hermes h : hermesList) {
            if (!h.getSessionConfig().getId().equals("<new>")) {
                hermesSessionList.add(h.getSessionConfig().getId());
            }
        }
        return hermesSessionList.toArray(new String[hermesSessionList.size()]);
    }

    private void initValues(AbstractInterface<?> iface) {
        String hermesConfigPath = PropertyExpander.expandProperties(iface, iface.getProject().getHermesConfig());
        mainForm.getComponent(HERMES_CONFIG).setValue(hermesConfigPath);

        String[] sessionOptions = getSessionOptions(iface, hermesConfigPath);

        mainForm.setOptions(SESSION, sessionOptions);
        Context ctx = null;
        try {
            ctx = getHermesContext(iface, hermesConfigPath);
        } catch (Exception e) {
            SoapUI.log.info("no hermes context");
        }
        Hermes hermes = null;
        try {
            if (sessionOptions != null && sessionOptions.length > 0) {
                hermes = (Hermes) ctx.lookup(sessionOptions[0]);
            }

            if (hermes != null) {
                updateDestinations(hermes);
            }
        } catch (NamingException e) {
            SoapUI.logError(e);
        }

    }

    private void updateDestinations(Hermes hermes) {
        destinationNameList = new ArrayList<Destination>();
        destinationNameList.add(new Destination(JMSEndpoint.JMS_EMPTY_DESTIONATION, Domain.UNKNOWN));
        extractDestinations(hermes, destinationNameList);
        mainForm.setOptions(SEND, destinationNameList.toArray());
        mainForm.setOptions(RECEIVE, destinationNameList.toArray());
    }

    private Context getHermesContext(AbstractInterface<?> iface, String hermesConfigPath)
            throws MalformedURLException, NamingException, IOException {
        WsdlProject project = iface.getProject();
        HermesUtils.flushHermesCache();
        Context ctx = HermesUtils.hermesContext(project, hermesConfigPath);
        return ctx;

    }

    protected XFormDialog buildDialog(final AbstractInterface<?> iface) {
        if (iface == null) {
            return null;
        }

        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Add JMS endpoint");

        mainForm = builder.createForm("Basic");
        mainForm.addTextField(HERMES_CONFIG, "choose folder where hermes-config.xml is", XForm.FieldType.FOLDER)
                .addFormFieldListener(new XFormFieldListener() {
                    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                        if (!"".equals(newValue)) {
                            Hermes hermes = null;
                            try {
                                Context ctx = getHermesContext(iface, newValue);
                                iface.getProject().setHermesConfig(newValue);
                                String[] sessions = getSessionOptions(iface, newValue);
                                mainForm.setOptions(SESSION, sessions);
                                if (sessions != null && sessions.length > 0) {
                                    hermes = (Hermes) ctx.lookup(sessions[0]);
                                }
                            } catch (Exception e) {
                                SoapUI.logError(e);
                            }
                            if (hermes != null) {
                                updateDestinations(hermes);
                            } else {
                                mainForm.setOptions(SESSION, new String[]{});
                                mainForm.setOptions(SEND, new String[]{});
                                mainForm.setOptions(RECEIVE, new String[]{});
                            }
                        }
                    }
                });
        mainForm.addComboBox(SESSION, new String[]{}, "Session name from HermesJMS").addFormFieldListener(
                new XFormFieldListener() {

                    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                        String hermesConfigPath = mainForm.getComponent(HERMES_CONFIG).getValue();

                        Hermes hermes = null;
                        try {
                            Context ctx = getHermesContext(iface, hermesConfigPath);
                            hermes = (Hermes) ctx.lookup(newValue);
                        } catch (Exception e) {
                            SoapUI.logError(e);
                        }
                        if (hermes != null) {
                            updateDestinations(hermes);
                        } else {
                            mainForm.setOptions(SEND, new String[]{});
                            mainForm.setOptions(RECEIVE, new String[]{});
                        }
                    }

                });
        mainForm.addComboBox(SEND, new String[]{}, "Queue/Topic  sending/publishing");
        mainForm.addComboBox(RECEIVE, new String[]{}, "Queue/Topic  receive/subscribe");

        return builder.buildDialog(builder.buildOkCancelActions(), "create JMS endpoint by selecting proper values",
                null);
    }

    private void extractDestinations(Hermes hermes, List<Destination> destinationList) {
        try {
            ClassLoader hermesClassLoader = HermesUtils.getHermesClassLoader();
            Class cl = hermesClassLoader.loadClass(DESTINATION_CONFIG_CLASS_NAME);
            if (cl != null) {
                Iterator<?> hermesDestinations = hermes.getDestinations();
                while (hermesDestinations.hasNext()) {
                    Object dest = hermesDestinations.next();
                    Field nameField = cl.getDeclaredField("name");
                    nameField.setAccessible(true);
                    String name = (String) nameField.get(dest);
                    Field domainField = cl.getDeclaredField("domain");
                    domainField.setAccessible(true);
                    Integer domain = (Integer) domainField.get(dest);
                    Destination temp = new Destination(name, Domain.getDomain(domain));
                    destinationList.add(temp);
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    private class Destination {
        public Destination(String destinationName, Domain domain) {
            this.domain = domain;
            if (destinationName.equals(JMSEndpoint.JMS_EMPTY_DESTIONATION) || destinationName.equals("")) {
                this.destinationName = destinationName;
            } else {
                if (domain.equals(Domain.QUEUE)) {
                    this.destinationName = JMSEndpoint.QUEUE_ENDPOINT_PREFIX + destinationName;
                } else {
                    this.destinationName = JMSEndpoint.TOPIC_ENDPOINT_PREFIX + destinationName;
                }
            }

        }

        private String destinationName;
        private Domain domain;

        public String getDestinationName() {
            return destinationName;
        }

        public Domain getDomain() {
            return domain;
        }

        public String toString() {
            return this.getDestinationName().replace(JMSEndpoint.QUEUE_ENDPOINT_PREFIX, "")
                    .replace(JMSEndpoint.TOPIC_ENDPOINT_PREFIX, "");
        }
    }

}
