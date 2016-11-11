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

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.io.File;

public class DirectoryFormComponent extends JPanel implements JFormComponent {
    private JTextField textField;
    private String initialFolder;

    public DirectoryFormComponent(String tooltip) {
        ButtonBarBuilder builder = new ButtonBarBuilder(this);
        textField = new JTextField(30);
        textField.setToolTipText(tooltip);
        builder.addGriddedGrowing(textField);
        builder.addRelatedGap();
        builder.addFixed(new JButton(new SelectDirectoryAction()));
    }

    public void setValue(String value) {
        textField.setText(value);
    }

    public String getValue() {
        return textField.getText();
    }

    public class SelectDirectoryAction extends AbstractAction {
        public SelectDirectoryAction() {
            super("Browse...");
        }

        public void actionPerformed(ActionEvent e) {
            File currentDirectory = StringUtils.hasContent(initialFolder) ? new File(initialFolder) : null;
            if (textField.getText().length() > 0) {
                currentDirectory = new File(textField.getText());
            }
            File file = UISupport.getFileDialogs().openDirectory(this, "Select directory", currentDirectory);
            if (file != null) {
                textField.setText(file.getAbsolutePath());
            }
        }
    }

    public JTextComponent getTextField() {
        return textField;
    }

    public void setInitialFolder(String initialFolder) {
        this.initialFolder = initialFolder;
    }
}
