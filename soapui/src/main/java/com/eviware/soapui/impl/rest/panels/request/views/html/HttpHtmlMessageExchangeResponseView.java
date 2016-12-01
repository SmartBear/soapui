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

package com.eviware.soapui.impl.rest.panels.request.views.html;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponentFactory;
import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlEditor;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class HttpHtmlMessageExchangeResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements
        PropertyChangeListener {
    private final MessageExchangeModelItem messageExchangeModelItem;
    private JPanel panel;
    private WebViewBasedBrowserComponent browser;
    private JPanel contentPanel;
    private boolean initialized = false;

    public HttpHtmlMessageExchangeResponseView(XmlEditor editor, MessageExchangeModelItem messageExchangeModelItem) {
        super("HTML", editor, HttpHtmlResponseViewFactory.VIEW_ID);
        this.messageExchangeModelItem = messageExchangeModelItem;

        messageExchangeModelItem.addPropertyChangeListener(this);
    }

    public JComponent getComponent() {
        if (panel == null) {
            panel = new JPanel(new BorderLayout());

            panel.add(buildToolbar(), BorderLayout.NORTH);
            panel.add(buildContent(), BorderLayout.CENTER);
            panel.add(buildStatus(), BorderLayout.SOUTH);
        }

        return panel;
    }

    @Override
    public void release() {
        super.release();

        if (browser != null) {
            browser.close(true);
        }

        messageExchangeModelItem.removePropertyChangeListener(this);
    }

    private Component buildStatus() {
        JLabel statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return statusLabel;
    }

    private Component buildContent() {

        contentPanel = new JPanel(new BorderLayout());
        return contentPanel;
    }

    @Override
    public boolean activate(EditorLocation<HttpResponseDocument> location) {
        boolean activated = super.activate(location);
        if (activated && !initialized) {
            initialized = true;
            if (SoapUI.isBrowserDisabled()) {
                contentPanel.add(new JLabel("Browser component is disabled."));
            } else {
                browser = WebViewBasedBrowserComponentFactory.createBrowserComponent(false);
                Component component = browser.getComponent();
                component.setMinimumSize(new Dimension(100, 100));
                contentPanel.add(new JScrollPane(component));

                setEditorContent(messageExchangeModelItem);
            }
        }
        return activated;
    }

    @Override
    public boolean deactivate() {
        boolean deactivated = super.deactivate();
        if (deactivated && browser != null) {
            browser.setContent("");
        }
        return deactivated;
    }

    protected void setEditorContent(JProxyServletWsdlMonitorMessageExchange jproxyServletWsdlMonitorMessageExchange) {
        if (browser == null) {
            return;
        }
        if (jproxyServletWsdlMonitorMessageExchange != null) {
            String contentType = jproxyServletWsdlMonitorMessageExchange.getResponseContentType();
            if (contentType.contains("html") || contentType.contains("text")) {
                try {
                    String content = jproxyServletWsdlMonitorMessageExchange.getResponseContent();
                    browser.setContent(content, contentType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (isSupportedContentType(contentType)) {
                try {
                    String ext = ContentTypeHandler.getExtensionForContentType(contentType);
                    File temp = File.createTempFile("response", "." + ext);
                    FileOutputStream fileOutputStream = new FileOutputStream(temp);
                    writeHttpBody(jproxyServletWsdlMonitorMessageExchange.getRawResponseData(), fileOutputStream);
                    fileOutputStream.close();
                    browser.navigate(temp.toURI().toURL().toString());
                    temp.deleteOnExit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                browser.setContent("unsupported content-type [" + contentType + "]");
            }
        } else {
            browser.setContent("-missing content-");
        }
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType.toLowerCase().contains("image");
    }

    protected void setEditorContent(MessageExchangeModelItem messageExchangeModelItem2) {
        if (browser == null) {
            return;
        }
        if (messageExchangeModelItem2 != null && messageExchangeModelItem2.getMessageExchange() != null) {
            String contentType = messageExchangeModelItem2.getMessageExchange().getResponseHeaders()
                    .get("Content-Type", "");
            if (contentType.contains("html") || contentType.contains("text")) {
                try {

                    final String content = messageExchangeModelItem2.getMessageExchange().getResponseContent();
                    browser.setContent(content, contentType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (!contentType.contains("xml")) {
                try {
                    String ext = ContentTypeHandler.getExtensionForContentType(contentType);
                    File temp = File.createTempFile("response", "." + ext);
                    FileOutputStream fileOutputStream = new FileOutputStream(temp);
                    writeHttpBody(messageExchangeModelItem2.getMessageExchange().getRawResponseData(), fileOutputStream);
                    fileOutputStream.close();
                    browser.navigate(temp.toURI().toURL().toString());
                    temp.deleteOnExit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            browser.setContent("<missing content>");
        }
    }


    private void writeHttpBody(byte[] rawResponse, FileOutputStream out) throws IOException {
        int index = 0;
        byte[] divider = "\r\n\r\n".getBytes();
        for (; index < (rawResponse.length - divider.length); index++) {
            int i;
            for (i = 0; i < divider.length; i++) {
                if (rawResponse[index + i] != divider[i]) {
                    break;
                }
            }

            if (i == divider.length) {
                out.write(rawResponse, index + divider.length, rawResponse.length - (index + divider.length));
                return;
            }
        }

        out.write(rawResponse);
    }

    private Component buildToolbar() {
        return UISupport.createToolbar();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("messageExchange")) {
            if (browser != null && evt.getNewValue() != null && isActive()) {
                setEditorContent(((JProxyServletWsdlMonitorMessageExchange) evt.getNewValue()));
            }
        }
    }

    public boolean saveDocument(boolean validate) {
        return false;
    }

    public void setEditable(boolean enabled) {
    }

}
