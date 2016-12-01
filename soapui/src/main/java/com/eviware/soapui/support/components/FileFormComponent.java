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

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.io.File;

public class FileFormComponent extends JPanel implements JFormComponent {
    private JTextField textField;
    private AbstractWsdlModelItem<?> modelItem;

    public FileFormComponent(String tooltip) {
        ButtonBarBuilder builder = new ButtonBarBuilder(this);
        textField = new JTextField(30);
        textField.setToolTipText(tooltip);
        builder.addGriddedGrowing(textField);
        builder.addRelatedGap();
        builder.addFixed(new JButton(new SelectFileAction()));
    }

    public void setValue(String value) {
        textField.setText(value);
    }

    public JTextField getTextField() {
        return textField;
    }

    public String getValue() {
        return textField.getText();
    }

    public void setFile(File file) {
        setValue(file.getAbsolutePath());
    }

    public void setModelItem(AbstractWsdlModelItem<?> modelItem) {
        this.modelItem = modelItem;
    }

    public class SelectFileAction extends AbstractAction {
        public SelectFileAction() {
            super("Browse...");
        }

        public void actionPerformed(ActionEvent e) {
            String value = FileFormComponent.this.getValue();
            File file = UISupport.getFileDialogs().open(this, "Select file", null, null,
                    StringUtils.hasContent(value) ? value : PathUtils.getExpandedResourceRoot(modelItem));
            if (file != null) {
                setFile(file);
            }
        }
    }
}
