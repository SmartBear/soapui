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

package com.eviware.soapui.security.ui;

import com.eviware.soapui.config.MalformedXmlAttributeConfig;
import com.eviware.soapui.config.MalformedXmlConfig;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JTextFieldFormField;

import javax.swing.JComponent;

public class MalformedXmlAdvancedSettingsPanel {

    private JFormDialog dialog;
    private MalformedXmlConfig configuration;
    private MalformedXmlAttributeConfig attributeConfig;

    public MalformedXmlAdvancedSettingsPanel(MalformedXmlConfig malformedXmlConfig) {
        this.configuration = malformedXmlConfig;
        this.attributeConfig = malformedXmlConfig.getAttributeMutation();

        dialog = (JFormDialog) ADialogBuilder.buildDialog(AdvancedSettings.class);

        initDialog();

        ((JTextFieldFormField) dialog.getFormField(AdvancedSettings.NEW_ELEMENT_VALUE)).setWidth(20);
        ((JTextFieldFormField) dialog.getFormField(AdvancedSettings.NEW_ATTRIBUTE_NAME)).setWidth(20);
        ((JTextFieldFormField) dialog.getFormField(AdvancedSettings.NEW_ATTRIBUTE_VALUE)).setWidth(20);
    }

    private void initDialog() {
        dialog.setBooleanValue(AdvancedSettings.INSERT_NEW_ELEMENT, configuration.getInsertNewElement());
        dialog.setValue(AdvancedSettings.NEW_ELEMENT_VALUE, configuration.getNewElementValue());
        dialog.setBooleanValue(AdvancedSettings.CHANGE_TAG_NAME, configuration.getChangeTagName());
        dialog.setBooleanValue(AdvancedSettings.LEAVE_TAG_OPEN, configuration.getLeaveTagOpen());
        dialog.setBooleanValue(AdvancedSettings.INSERT_INVALID_CHARACTER, configuration.getInsertInvalidCharacter());

        dialog.setBooleanValue(AdvancedSettings.MUTATE_ATTRIBUTES, attributeConfig.getMutateAttributes());
        dialog.setBooleanValue(AdvancedSettings.INSERT_INVALID_CHARS, attributeConfig.getInsertInvalidChars());
        dialog.setBooleanValue(AdvancedSettings.LEAVE_ATTRIBUTE_OPEN, attributeConfig.getLeaveAttributeOpen());
        dialog.setBooleanValue(AdvancedSettings.ADD_NEW_ATTRIBUTE, attributeConfig.getAddNewAttribute());
        dialog.setValue(AdvancedSettings.NEW_ATTRIBUTE_NAME, attributeConfig.getNewAttributeName());
        dialog.setValue(AdvancedSettings.NEW_ATTRIBUTE_VALUE, attributeConfig.getNewAttributeValue());

        // listeners...
        dialog.getFormField(AdvancedSettings.INSERT_NEW_ELEMENT).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                configuration.setInsertNewElement(Boolean.parseBoolean(newValue));
            }
        });
        dialog.getFormField(AdvancedSettings.NEW_ELEMENT_VALUE).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                configuration.setNewElementValue(newValue);
            }
        });
        dialog.getFormField(AdvancedSettings.CHANGE_TAG_NAME).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                configuration.setChangeTagName(Boolean.parseBoolean(newValue));
            }
        });
        dialog.getFormField(AdvancedSettings.LEAVE_TAG_OPEN).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                configuration.setLeaveTagOpen(Boolean.parseBoolean(newValue));
            }
        });
        dialog.getFormField(AdvancedSettings.MUTATE_ATTRIBUTES).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                attributeConfig.setMutateAttributes(Boolean.parseBoolean(newValue));
            }
        });
        dialog.getFormField(AdvancedSettings.INSERT_INVALID_CHARS).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                attributeConfig.setInsertInvalidChars(Boolean.parseBoolean(newValue));
            }
        });
        dialog.getFormField(AdvancedSettings.LEAVE_ATTRIBUTE_OPEN).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                attributeConfig.setLeaveAttributeOpen(Boolean.parseBoolean(newValue));
            }
        });
        dialog.getFormField(AdvancedSettings.ADD_NEW_ATTRIBUTE).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                attributeConfig.setAddNewAttribute(Boolean.parseBoolean(newValue));
            }
        });
        dialog.getFormField(AdvancedSettings.NEW_ATTRIBUTE_NAME).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                attributeConfig.setNewAttributeName(newValue);
            }
        });
        dialog.getFormField(AdvancedSettings.NEW_ATTRIBUTE_VALUE).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                attributeConfig.setNewAttributeValue(newValue);
            }
        });
        dialog.getFormField(AdvancedSettings.INSERT_INVALID_CHARACTER).addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                configuration.setInsertInvalidCharacter(Boolean.parseBoolean(newValue));
            }
        });

    }

    public JComponent getPanel() {
        return dialog.getPanel();
    }

    @AForm(description = "Malformed XML Configuration", name = "Malformed XML Configuration")
    protected interface AdvancedSettings {

        @AField(description = "", name = "Insert new element", type = AFieldType.BOOLEAN)
        public final static String INSERT_NEW_ELEMENT = "Insert new element";

        @AField(description = "", name = "New element value", type = AFieldType.STRING)
        public final static String NEW_ELEMENT_VALUE = "New element value";

        @AField(description = "", name = "Change tag name", type = AFieldType.BOOLEAN)
        public final static String CHANGE_TAG_NAME = "Change tag name";

        @AField(description = "", name = "Leave tag open", type = AFieldType.BOOLEAN)
        public final static String LEAVE_TAG_OPEN = "Leave tag open";

        @AField(description = "", name = "Insert invalid char in xml", type = AFieldType.BOOLEAN)
        public final static String INSERT_INVALID_CHARACTER = "Insert invalid char in xml";

        @AField(description = "", name = "Mutate attributes", type = AFieldType.BOOLEAN)
        public final static String MUTATE_ATTRIBUTES = "Mutate attributes";

        @AField(description = "", name = "Insert invalid chars in attribute", type = AFieldType.BOOLEAN)
        public final static String INSERT_INVALID_CHARS = "Insert invalid chars in attribute";

        @AField(description = "", name = "Leave attribute open", type = AFieldType.BOOLEAN)
        public final static String LEAVE_ATTRIBUTE_OPEN = "Leave attribute open";

        @AField(description = "", name = "Add new attribute", type = AFieldType.BOOLEAN)
        public final static String ADD_NEW_ATTRIBUTE = "Add new attribute";

        @AField(description = "", name = "New attribute name", type = AFieldType.STRING)
        public final static String NEW_ATTRIBUTE_NAME = "New attribute name";

        @AField(description = "", name = "New attribute value", type = AFieldType.STRING)
        public final static String NEW_ATTRIBUTE_VALUE = "New attribute value";

    }

    public void release() {
        dialog.release();
        dialog = null;
        configuration = null;
        attributeConfig = null;
    }

}
