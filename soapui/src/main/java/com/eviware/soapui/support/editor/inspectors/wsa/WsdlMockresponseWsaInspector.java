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

package com.eviware.soapui.support.editor.inspectors.wsa;

import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.xml.XmlInspector;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class WsdlMockresponseWsaInspector extends AbstractWsaInspector implements XmlInspector {
    private JCheckBox generateMessageIdCheckBox;
    private JTextField messageIdTextField;
    private JCheckBox addDefaultToCheckBox;
    private JTextField toTextField;
    private JCheckBox addDefaultActionCheckBox;
    private JTextField actionTextField;

    public WsdlMockresponseWsaInspector(WsdlMockResponse response) {
        super(response);
    }

    public void buildContent(SimpleBindingForm form) {
        form.addSpace(5);
        form.appendCheckBox("wsaEnabled", "Enable WS-A addressing", "");
        form.addSpace(5);
        // add mustUnderstand drop down list
        form.appendComboBox("mustUnderstand", "Must understand", new String[]{
                MustUnderstandTypeConfig.NONE.toString(), MustUnderstandTypeConfig.TRUE.toString(),
                MustUnderstandTypeConfig.FALSE.toString()},
                "The  property for controlling use of the mustUnderstand attribute");

        form.appendComboBox("version", "WS-A Version", new String[]{WsaVersionTypeConfig.X_200508.toString(),
                WsaVersionTypeConfig.X_200408.toString()}, "The  property for managing WS-A version");

        addDefaultActionCheckBox = form.appendCheckBox("addDefaultAction", "Add default wsa:Action",
                "Add default wsa:Action");
        actionTextField = form
                .appendTextField("action", "Action",
                        "The action related to a message, will be generated if left empty and ws-a settings 'use default action...' checked ");
        actionTextField.setEnabled(!addDefaultActionCheckBox.isSelected());
        addDefaultActionCheckBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent arg0) {
                actionTextField.setEnabled(!addDefaultActionCheckBox.isSelected());
            }
        });

        addDefaultToCheckBox = form.appendCheckBox("addDefaultTo", "Add default wsa:To", "Add default wsa:To");
        toTextField = form.appendTextField("to", "To",
                "The destination endpoint reference, will be generated if left empty");
        toTextField.setEnabled(!addDefaultToCheckBox.isSelected());
        addDefaultToCheckBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent arg0) {
                toTextField.setEnabled(!addDefaultToCheckBox.isSelected());
            }
        });

        form.appendTextField(
                "relatesTo",
                "Relates to",
                "The endpoint reference Mock Response relates to, will be set to 'unspecified' if left empty and ws-a settings 'use default...' checked  ");
        form.appendTextField("relationshipType", "Relationship type",
                "Relationship type, will be set to 'reply' if left empty and ws-a settings 'use default...' checked  ");
        form.addSpace(10);
        form.appendTextField("from", "From", "The source endpoint reference");
        form.appendTextField("faultTo", "Fault to", "The fault endpoint reference");
        form.appendTextField("replyTo", "Reply to", "The reply endpoint reference");
        generateMessageIdCheckBox = form.appendCheckBox("generateMessageId", "Generate MessageID",
                "Randomly generate MessageId");
        messageIdTextField = form
                .appendTextField(
                        "messageID",
                        "MessageID",
                        " The ID of a message that can be used to uniquely identify a message, will be generated if left empty and ws-a settings 'generate message id' checked ");
        messageIdTextField.setEnabled(!generateMessageIdCheckBox.isSelected());
        generateMessageIdCheckBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent arg0) {
                messageIdTextField.setEnabled(!generateMessageIdCheckBox.isSelected());
            }
        });
        form.addSpace(5);
    }
}
