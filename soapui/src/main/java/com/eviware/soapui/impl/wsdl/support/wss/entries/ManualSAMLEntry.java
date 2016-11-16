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

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSAMLToken;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         Used to create a manual SAML assertion using a test editor field
 */

public class ManualSAMLEntry extends WssEntryBase {
    public static final String TYPE = "SAML (XML)";

    private String samlAssertion;

    private RSyntaxTextArea editor;

    public void init(WSSEntryConfig config, OutgoingWss container) {
        super.init(config, container, TYPE);
    }

    @Override
    protected JComponent buildUI() {
        JPanel panel = new JPanel(new BorderLayout());

        editor = SyntaxEditorUtil.createDefaultXmlSyntaxTextArea();

        RTextScrollPane scrollPane = new RTextScrollPane(editor);
        scrollPane.setFoldIndicatorEnabled(true);
        scrollPane.setLineNumbersEnabled(true);

        editor = SyntaxEditorUtil.addDefaultActions(editor, scrollPane, false);

        editor.setText(samlAssertion == null ? "" : samlAssertion);
        editor.getDocument().addDocumentListener(new DocumentListenerAdapter() {

            @Override
            public void update(javax.swing.text.Document document) {
                samlAssertion = editor.getText();
                saveConfig();

            }
        });
        panel.add(scrollPane, BorderLayout.CENTER);

        return UISupport.addTitledBorder(panel, "Enter SAML Assertion");
    }

    @Override
    protected void load(XmlObjectConfigurationReader reader) {
        samlAssertion = reader.readString("samlAssertion", null);

    }

    @Override
    protected void save(XmlObjectConfigurationBuilder builder) {
        builder.add("samlAssertion", samlAssertion);
    }

    public void process(WSSecHeader secHeader, Document doc, PropertyExpansionContext context) {
        if (StringUtils.isNullOrEmpty(samlAssertion)) {
            return;
        }

        try {
            String samlAssertionValue = context.expand(samlAssertion);
            // don't strip white space if the SAML assertion is signed because it will break the signature
            if (!(samlAssertionValue != null && samlAssertionValue.contains(WSConstants.SIG_NS) && samlAssertionValue.contains("SignatureValue"))) {
                samlAssertionValue = XmlUtils.stripWhitespaces(samlAssertionValue);
            }
            Document samlAssertionDOM = XmlUtils.parseXml(samlAssertionValue);
            Element samlAssertionRootElement = samlAssertionDOM.getDocumentElement();
            AssertionWrapper assertion = new AssertionWrapper(samlAssertionRootElement);
            WSSecSAMLToken wsSign = new WSSecSAMLToken();
            wsSign.build(doc, assertion, secHeader);
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public String getSamlAssertion() {
        return samlAssertion;
    }

    public void setSamlAssertion(String samlAssertion) {
        this.samlAssertion = samlAssertion;
        saveConfig();

        if (editor != null) {
            editor.setText(samlAssertion);
        }
    }

    @Override
    protected void addPropertyExpansions(PropertyExpansionsResult result) {
        super.addPropertyExpansions(result);
        result.extractAndAddAll("samlAssertion");
    }
}
