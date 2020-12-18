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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.panels.request.StringToStringMapTableModel;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeRequestMessageEditor;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeResponseMessageEditor;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.RequestAssertedMessageExchange;
import com.eviware.soapui.model.testsuite.ResponseAssertedMessageExchange;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Shows a desktop-panel with the TestStepResult for a WsdlTestRequestStepResult
 *
 * @author Ole.Matzura
 */

public class ShowMessageExchangeAction extends AbstractAction {
    private DefaultDesktopPanel desktopPanel;
    private final MessageExchange messageExchange;
    private final String ownerName;
    private MessageExchangeResponseMessageEditor responseMessageEditor;
    private MessageExchangeRequestMessageEditor requestMessageEditor;

    public ShowMessageExchangeAction(MessageExchange messageExchange, String ownerName) {
        super("Show Message Exchange");
        this.ownerName = ownerName;
        this.messageExchange = messageExchange;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            UISupport.showDesktopPanel(buildFrame());
        } catch (Exception ex) {
            SoapUI.logError(ex);
        }
    }

    private DesktopPanel buildFrame() {
        if (desktopPanel == null) {
            desktopPanel = new MessageExchangeDesktopPanel("Message Viewer", "Message for " + ownerName, buildContent());
        }

        return desktopPanel;
    }

    private JComponent buildContent() {
        JTabbedPane messageTabs = new JTabbedPane();
        messageTabs.addTab("Request Message", buildRequestTab());
        messageTabs.addTab("Response Message", buildResponseTab());
        messageTabs.addTab("Properties", buildPropertiesTab());

        messageTabs.addTab("Messages", buildMessagesTab());

        if (getAssertedXPaths().size() > 0) {
            messageTabs.addTab("XPath Assertions", buildAssertionsTab());
        }

        messageTabs.setPreferredSize(new Dimension(500, 400));

        JPanel tabPanel = UISupport.createTabPanel(messageTabs, true);

        Component descriptionPanel = UISupport.buildDescription("MessageExchange Results",
                "See the request/response message below", null);
        tabPanel.add(descriptionPanel, BorderLayout.NORTH);

        return tabPanel;
    }

    private Component buildAssertionsTab() {
        List<AssertedXPath> assertedXPaths = getAssertedXPaths();

        DefaultTableModel tm = new DefaultTableModel(assertedXPaths.size(), 2);
        tm.setColumnIdentifiers(new String[]{"Label", "XPath"});

        JXTable table = JTableFactory.getInstance().makeJXTable(tm);
        table.setHorizontalScrollEnabled(true);
        table.getColumn(0).setPreferredWidth(100);

        for (int c = 0; c < assertedXPaths.size(); c++) {
            tm.setValueAt(assertedXPaths.get(c).getLabel(), c, 0);
            tm.setValueAt(assertedXPaths.get(c).getPath(), c, 1);
        }

        return new JScrollPane(table);
    }

    private List<AssertedXPath> getAssertedXPaths() {
        List<AssertedXPath> assertedXPaths = new ArrayList<AssertedXPath>();

        if (messageExchange instanceof RequestAssertedMessageExchange) {
            AssertedXPath[] xpaths = ((RequestAssertedMessageExchange) messageExchange).getAssertedXPathsForRequest();
            if (xpaths != null && xpaths.length > 0) {
                assertedXPaths.addAll(Arrays.asList(xpaths));
            }
        }

        if (messageExchange instanceof ResponseAssertedMessageExchange) {
            AssertedXPath[] xpaths = ((ResponseAssertedMessageExchange) messageExchange).getAssertedXPathsForResponse();
            if (xpaths != null && xpaths.length > 0) {
                assertedXPaths.addAll(Arrays.asList(xpaths));
            }
        }
        return assertedXPaths;
    }

    private Component buildPropertiesTab() {
        StringToStringMap properties = new StringToStringMap();
        if (messageExchange != null && messageExchange.getProperties() != null) {
            properties.putAll(messageExchange.getProperties());

            // for( String name : messageExchange.getResponse().getPropertyNames())
            // {
            // properties.put( name, messageExchange.getResponse().getProperty(
            // name ) );
            // }

            properties.put("Timestamp", new Date(messageExchange.getTimestamp()).toString());
            properties.put("Time Taken", String.valueOf(messageExchange.getTimeTaken()));
        }
        JTable table = JTableFactory.getInstance().makeJTable(new StringToStringMapTableModel(properties, "Name", "Value", false));
        return new JScrollPane(table);
    }

    private Component buildMessagesTab() {
        return hasMessages() ? new JScrollPane(new JList<>(messageExchange.getMessages())) : new JLabel("No messages to display");
    }

    private Component buildResponseTab() {
        responseMessageEditor = new MessageExchangeResponseMessageEditor(messageExchange);
        return responseMessageEditor;
    }

    private Component buildRequestTab() {
        requestMessageEditor = new MessageExchangeRequestMessageEditor(messageExchange);
        return requestMessageEditor;
    }

    private boolean hasMessages() {
        return messageExchange != null && messageExchange.getMessages() != null && messageExchange.getMessages().length > 0;
    }

    private final class MessageExchangeDesktopPanel extends DefaultDesktopPanel {
        private MessageExchangeDesktopPanel(String title, String description, JComponent component) {
            super(title, description, component);
        }

        @Override
        public boolean onClose(boolean canCancel) {
            requestMessageEditor.release();
            responseMessageEditor.release();

            desktopPanel = null;

            return super.onClose(canCancel);
        }

        @Override
        public boolean dependsOn(ModelItem modelItem) {
            return ModelSupport.dependsOn(messageExchange.getModelItem(), modelItem);
        }
    }
}
