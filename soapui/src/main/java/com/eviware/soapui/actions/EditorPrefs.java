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

package com.eviware.soapui.actions;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.l2fprod.common.swing.JFontChooser;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Preferences class for UISettings
 *
 * @author ole.matzura
 */

public class EditorPrefs implements Prefs {
    public static final String NO_RESIZE_REQUEST_EDITOR = "Disable auto-resize";
    public static final String START_WITH_REQUEST_TABS = "Tabbed request view";
    public static final String AUTO_VALIDATE_REQUEST = "Validate Requests";
    public static final String ABORT_ON_INVALID_REQUEST = "Abort on invalid";
    public static final String AUTO_VALIDATE_RESPONSE = "Validate Responses";
    public static final String XML_LINE_NUMBERS = "XML Line Numbers";
    public static final String GROOVY_LINE_NUMBERS = "Groovy Line Numbers";

    private JTextField editorFontTextField;
    private SimpleForm editorForm;
    private final String title;
    private JCheckBox abortCheckBox;
    private JCheckBox autoValidateCheckBox;

    public EditorPrefs(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public SimpleForm getForm() {
        if (editorForm == null) {
            ButtonBarBuilder builder = new ButtonBarBuilder();
            editorFontTextField = new JTextField(20);
            editorFontTextField.setEnabled(false);
            builder.addFixed(editorFontTextField);
            builder.addRelatedGap();
            builder.addFixed(new JButton(new AbstractAction("Select Font..") {
                public void actionPerformed(ActionEvent e) {
                    Font font = JFontChooser.showDialog(UISupport.getMainFrame(), "Select XML Editor Font",
                            Font.decode(editorFontTextField.getText()));

                    if (font != null) {
                        editorFontTextField.setText(encodeFont(font));
                    }
                }
            }));

            editorForm = new SimpleForm();
            editorForm.addSpace(5);
            editorForm.append("Editor Font", builder.getPanel());
            editorForm.appendSeparator();
            editorForm.appendCheckBox(XML_LINE_NUMBERS, "Show line numbers in XML editors by default", true);
            editorForm.appendCheckBox(GROOVY_LINE_NUMBERS, "Show line numbers in Groovy editors by default", true);
            editorForm.appendSeparator();
            editorForm.appendCheckBox(NO_RESIZE_REQUEST_EDITOR, "Disables automatic resizing of Request editors", true);
            editorForm.appendCheckBox(START_WITH_REQUEST_TABS, "Defaults the Request editor to the tabbed layout", true);
            editorForm.appendSeparator();

            autoValidateCheckBox = editorForm.appendCheckBox(AUTO_VALIDATE_REQUEST,
                    "Always validate request messages before they are sent", true);
            abortCheckBox = editorForm.appendCheckBox(ABORT_ON_INVALID_REQUEST, "Abort invalid requests", true);
            editorForm.appendCheckBox(AUTO_VALIDATE_RESPONSE, "Always validate response messages", true);

            autoValidateCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    abortCheckBox.setEnabled(autoValidateCheckBox.isSelected());
                }
            });
        }

        return editorForm;
    }

    public void getFormValues(Settings settings) {
        StringToStringMap values = new StringToStringMap();
        editorForm.getValues(values);
        storeValues(values, settings);
    }

    public void storeValues(StringToStringMap values, Settings settings) {
        if (editorFontTextField != null) {
            settings.setString(UISettings.EDITOR_FONT, editorFontTextField.getText());
        }

        settings.setBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR, values.getBoolean(NO_RESIZE_REQUEST_EDITOR));
        settings.setBoolean(UISettings.START_WITH_REQUEST_TABS, values.getBoolean(START_WITH_REQUEST_TABS));
        settings.setBoolean(UISettings.AUTO_VALIDATE_REQUEST, values.getBoolean(AUTO_VALIDATE_REQUEST));
        settings.setBoolean(UISettings.ABORT_ON_INVALID_REQUEST, values.getBoolean(ABORT_ON_INVALID_REQUEST));
        settings.setBoolean(UISettings.AUTO_VALIDATE_RESPONSE, values.getBoolean(AUTO_VALIDATE_RESPONSE));
        settings.setBoolean(UISettings.SHOW_XML_LINE_NUMBERS, values.getBoolean(XML_LINE_NUMBERS));
        settings.setBoolean(UISettings.SHOW_GROOVY_LINE_NUMBERS, values.getBoolean(GROOVY_LINE_NUMBERS));
    }

    public static String encodeFont(Font font) {
        String editorFont = font.getFontName() + " ";
        if (font.isBold()) {
            editorFont += "bold ";
        }
        if (font.isItalic()) {
            editorFont += "italic ";
        }
        editorFont += font.getSize();

        return editorFont;
    }

    public void setFormValues(Settings settings) {
        editorFontTextField.setText(encodeFont(UISupport.getEditorFont()));
        editorForm.setValues(getValues(settings));

        abortCheckBox.setEnabled(settings.getBoolean(UISettings.AUTO_VALIDATE_REQUEST));
    }

    public StringToStringMap getValues(Settings settings) {
        StringToStringMap values = new StringToStringMap();
        values.put(NO_RESIZE_REQUEST_EDITOR, settings.getBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR));
        values.put(START_WITH_REQUEST_TABS, settings.getBoolean(UISettings.START_WITH_REQUEST_TABS));
        values.put(AUTO_VALIDATE_REQUEST, settings.getBoolean(UISettings.AUTO_VALIDATE_REQUEST));
        values.put(ABORT_ON_INVALID_REQUEST, settings.getBoolean(UISettings.ABORT_ON_INVALID_REQUEST));
        values.put(AUTO_VALIDATE_RESPONSE, settings.getBoolean(UISettings.AUTO_VALIDATE_RESPONSE));
        values.put(XML_LINE_NUMBERS, settings.getBoolean(UISettings.SHOW_XML_LINE_NUMBERS));
        values.put(GROOVY_LINE_NUMBERS, settings.getBoolean(UISettings.SHOW_GROOVY_LINE_NUMBERS));

        return values;
    }
}
