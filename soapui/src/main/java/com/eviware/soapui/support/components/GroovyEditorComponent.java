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

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.eviware.soapui.impl.wsdl.teststeps.Script.SCRIPT_PROPERTY;

public class GroovyEditorComponent extends JPanel implements PropertyChangeListener {
    private GroovyEditor editor;
    private JButton insertCodeButton;
    private Action runAction;
    private JXToolBar toolBar;
    private final GroovyEditorModel editorModel;
    private final String helpUrl;

    public GroovyEditorComponent(GroovyEditorModel editorModel, String helpUrl) {
        super(new BorderLayout());
        this.editorModel = editorModel;
        this.helpUrl = helpUrl;

        editor = new GroovyEditor(editorModel);
        editor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3),
                editor.getBorder()));
        add(editor, BorderLayout.CENTER);
        buildToolbar(editorModel, helpUrl);

        editorModel.addPropertyChangeListener(this);
    }

    public GroovyEditor getEditor() {
        return editor;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        editor.setEnabled(enabled);
        if (runAction != null) {
            runAction.setEnabled(enabled);
        }

        insertCodeButton.setEnabled(enabled);
    }

    protected void buildToolbar(GroovyEditorModel editorModel, String helpUrl) {
        if (toolBar == null) {
            toolBar = UISupport.createSmallToolbar();
        } else {
            remove(toolBar);
            toolBar.removeAll();
        }

        runAction = editorModel.getRunAction();
        if (runAction != null) {
            JButton runButton = UISupport.createToolbarButton(runAction);
            if (runButton.getIcon() == null) {
                runButton.setIcon(UISupport.createImageIcon("/run.png"));
            }

            if (runButton.getToolTipText() == null) {
                runButton.setToolTipText("Runs this script");
            }

            toolBar.add(runButton);
            toolBar.addRelatedGap();
        }

        if (insertCodeButton == null) {
            insertCodeButton = new JButton(new InsertCodeAction());
            insertCodeButton.setIcon(UISupport.createImageIcon("/down_arrow.gif"));
            insertCodeButton.setHorizontalTextPosition(SwingConstants.LEFT);
        }

        toolBar.addFixed(insertCodeButton);

        toolBar.add(Box.createHorizontalGlue());

        String[] args = editorModel.getKeywords();
        if (args != null && args.length > 0) {
            String scriptName = editorModel.getScriptName();
            if (scriptName == null) {
                scriptName = "";
            } else {
                scriptName = scriptName.trim() + " ";
            }

            StringBuilder text = new StringBuilder("<html>" + scriptName + "Script is invoked with ");
            for (int c = 0; c < args.length; c++) {
                if (c > 0) {
                    text.append(", ");
                }

                text.append("<font face=\"courier\">").append(args[c]).append("</font>");
            }
            text.append(" variables</html>");

            JLabel label = new JLabel(text.toString());
            label.setToolTipText(label.getText());
            label.setMaximumSize(label.getPreferredSize());

            toolBar.addFixed(label);
            toolBar.addUnrelatedGap();
        }

        if (helpUrl != null) {
            toolBar.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(helpUrl)));
        }

        add(toolBar, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    public class InsertCodeAction extends AbstractAction {
        public InsertCodeAction() {
            super("Edit");
            putValue(Action.SHORT_DESCRIPTION, "Inserts code at caret");
        }

        public void actionPerformed(ActionEvent e) {
            JPopupMenu popup = editor.getEditArea().getComponentPopupMenu();
            popup.show(insertCodeButton, insertCodeButton.getWidth() / 2, insertCodeButton.getHeight() / 2);
        }
    }

    public void release() {
        editorModel.removePropertyChangeListener(this);
        getEditor().release();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals(SCRIPT_PROPERTY)) {
            buildToolbar(editorModel, helpUrl);
        }
    }
}
