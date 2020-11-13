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

package com.eviware.x.impl.swing;

import com.eviware.soapui.settings.ProjectSettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.x.form.XForm.FieldType;
import com.eviware.x.form.XFormTextField;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.io.File;

public class FileFormField extends AbstractSwingXFormField<JPanel> implements XFormTextField {
    private final static Logger log = LogManager.getLogger(FileFormField.class);

    private JTextField textField;
    private final FieldType type;
    private JButton selectDirectoryButton;
    private String projectRoot;

    private boolean updating;
    private String oldValue;
    private String currentDirectory;

    public FileFormField(String tooltip, FieldType type, String name) {
        super(new JPanel());
        this.type = type;

        ButtonBarBuilder builder = new ButtonBarBuilder(getComponent());
        textField = new JUndoableTextField(30);
        textField.setName(name);
        textField.setToolTipText(tooltip);
        builder.addGriddedGrowing(textField);
        builder.addRelatedGap();
        selectDirectoryButton = new JButton(new SelectDirectoryAction());
        builder.addFixed(selectDirectoryButton);

        textField.getDocument().addDocumentListener(new DocumentListenerAdapter() {

            @Override
            public void update(Document document) {
                String text = textField.getText();

                if (!updating) {
                    fireValueChanged(text, oldValue);
                }

                oldValue = text;
            }
        });
    }

    public void setValue(String value) {
        updating = true;
        oldValue = null;
        updateValue(value);
        updating = false;
    }

    private void updateValue(String value) {
        if (value != null && projectRoot != null && value.startsWith(projectRoot)) {
            if (value.equals(projectRoot)) {
                value = "";
            } else if (value.length() > projectRoot.length() + 1) {
                value = value.substring(projectRoot.length() + 1);
            }
        }

        textField.setText(value);
    }

    public String getValue() {
        String text = textField.getText().trim();

        if (projectRoot != null && text.length() > 0) {
            String tempName = projectRoot + File.separatorChar + text;
            if (new File(tempName).exists()) {
                text = tempName;
            }
        }

        return text;
    }

    public void setEnabled(boolean enabled) {
        textField.setEnabled(enabled);
        selectDirectoryButton.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return textField.isEnabled();
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public class SelectDirectoryAction extends AbstractAction {
        private JFileChooser fileChooser;

        public SelectDirectoryAction() {
            super("Browse...");
        }

        public void actionPerformed(ActionEvent e) {
            if (fileChooser == null) {
                if (type == FieldType.FILE_OR_FOLDER) {
                    fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                } else if (type == FieldType.FOLDER || type == FieldType.PROJECT_FOLDER) {
                    fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                } else {
                    fileChooser = new JFileChooser();
                }

            }

            File file = null;
            String startingDirectory = StringUtils.hasContent(currentDirectory) ? currentDirectory : StringUtils.hasContent(projectRoot) ? projectRoot : null;
            if (startingDirectory != null) {
                startingDirectory = FilenameUtils.normalize(startingDirectory);
            }

            String value = FileFormField.this.getValue();
            if (StringUtils.hasContent(value)) {
                file = new File(FilenameUtils.normalize(value));
                if (!file.isAbsolute()) {
                    if (startingDirectory != null) {
                        file = new File(FilenameUtils.normalize(startingDirectory + File.separator + value));
                    } else {
                        file = file.getAbsoluteFile();
                    }
                }
            } else {
                file = new File((startingDirectory != null) ? startingDirectory : System.getProperty("user.dir", ".")).getAbsoluteFile();
            }

            if (file.exists()) {
                fileChooser.setSelectedFile(file);
                if (file.isDirectory()) {
                    fileChooser.setCurrentDirectory(file);
                } else {
                    fileChooser.setCurrentDirectory(file.getParentFile());
                }
            } else {
                while (file != null && !file.exists()) {
                    file = file.getParentFile();
                }
                if (file == null) {
                    file = new File(System.getProperty("user.dir", ".")).getAbsoluteFile();
                }
                fileChooser.setCurrentDirectory(file);
            }

            int returnVal = fileChooser.showOpenDialog(UISupport.getMainFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                updateValue(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    public void setProperty(String name, Object value) {
        super.setProperty(name, value);

        if (name.equals(ProjectSettings.PROJECT_ROOT) && type == FieldType.PROJECT_FOLDER) {
            projectRoot = (String) value;
            log.debug("Set projectRoot to [" + projectRoot + "]");
        } else if (name.equals(CURRENT_DIRECTORY)) {
            currentDirectory = (String) value;
            log.debug("Set currentDirectory to [" + currentDirectory + "]");
        }
    }

    public void setWidth(int columns) {
        textField.setColumns(columns);
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }
}
