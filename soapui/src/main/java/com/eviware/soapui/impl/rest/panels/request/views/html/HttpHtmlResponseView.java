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
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseMessageEditor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponentFactory;
import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@SuppressWarnings("unchecked")
public class HttpHtmlResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements PropertyChangeListener {
    private HttpRequestInterface<?> httpRequest;
    private JPanel panel = new JPanel(new BorderLayout());
    private WebViewBasedBrowserComponent browser;
    private MessageExchangeModelItem messageExchangeModelItem;
    private boolean initialized = false;

    public HttpHtmlResponseView(HttpResponseMessageEditor httpRequestMessageEditor, HttpRequestInterface<?> httpRequest) {
        super("HTML", httpRequestMessageEditor, HttpHtmlResponseViewFactory.VIEW_ID);
        this.httpRequest = httpRequest;
        httpRequest.addPropertyChangeListener(this);
    }

    public JComponent getComponent() {
        return panel;
    }

    @Override
    public boolean activate(EditorLocation<HttpResponseDocument> location) {
        boolean activated = super.activate(location);
        if (activated) {
            ensureComponentIsInitialized();
            HttpResponse response = httpRequest.getResponse();
            if (response != null) {
                setEditorContent(response);
            }
        }
        return activated;
    }

    private void ensureComponentIsInitialized() {
        if (!initialized) {
            if (SoapUI.isBrowserDisabled()) {
                panel.add(new JLabel("Browser component is disabled."));
            } else {
                browser = WebViewBasedBrowserComponentFactory.createBrowserComponent(false);
                Component component = browser.getComponent();
                component.setMinimumSize(new Dimension(100, 100));
                panel.add(component, BorderLayout.CENTER);
            }
            initialized = true;
        }
    }

    @Override
    public boolean deactivate() {
        boolean deactivated = super.deactivate();
        if (deactivated) {
            browser.setContent("");
        }
        return deactivated;
    }

    @Override
    public void release() {
        super.release();

        if (browser != null) {
            browser.close(true);
        }

        if (messageExchangeModelItem != null) {
            messageExchangeModelItem.removePropertyChangeListener(this);
        } else {
            httpRequest.removePropertyChangeListener(this);
        }

        httpRequest = null;
        messageExchangeModelItem = null;
    }

    protected void setEditorContent(HttpResponse httpResponse) {
        if (httpResponse == null || SoapUI.isBrowserDisabled()) {
            return;
        }
        String content = httpResponse.getContentAsString();
        if (content != null) {
            String contentType = httpResponse.getContentType();

            if (contentType != null && isSupportedContentType(contentType)) {
                try {
                    browser.setContent(content, contentType);
                } catch (Exception e) {
                    SoapUI.logError(e, "Could not display response from " + httpResponse.getURL() + " as HTML");
                }
            } else {
                browser.setContent("unsupported content-type [" + contentType + "]");
            }
        } else {
            browser.setContent("<missing content>");
        }
    }


    private boolean isSupportedContentType(String contentType) {
        return contentType != null && (contentType.trim().toLowerCase().startsWith("text") ||
                contentType.trim().toLowerCase().startsWith("image"));
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AbstractHttpRequestInterface.RESPONSE_PROPERTY)) {
            if (browser != null) {
                setEditorContent(((HttpResponse) evt.getNewValue()));
            }
        }
    }

    public boolean saveDocument(boolean validate) {
        return false;
    }

    public void setEditable(boolean enabled) {
    }

    public HttpRequestInterface<?> getHttpRequest() {
        return httpRequest;
    }
}
