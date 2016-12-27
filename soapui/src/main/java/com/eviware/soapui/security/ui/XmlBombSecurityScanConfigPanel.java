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

import com.eviware.soapui.security.scan.XmlBombSecurityScan;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleForm;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

@SuppressWarnings("serial")
public class XmlBombSecurityScanConfigPanel extends JPanel {
    private static final String ATTACHMENT_PREFIX_FIELD = "Attachment Prefix Field";
    private static final String ENABLE_ATTACHMENT_FIELD = "Send bomb as attachment";

    private List<String> xmlBombList;
    private JTextArea attachementArea;

    private int xmlBombPosition = -1;
    private PreviousAttachement previous;
    private NextAttachement next;
    private JLabel current;
    private JLabel max;
    protected int internalPosition;
    protected int externalPosition;
    private SimpleForm form;
    private XmlBombSecurityScan xmlChk;

    public XmlBombSecurityScanConfigPanel(XmlBombSecurityScan xmlCheck) {
        super(new BorderLayout());

        this.xmlChk = xmlCheck;
        this.xmlBombList = xmlCheck.getXmlBombList();
        form = new SimpleForm();
        form.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 3));
        form.addSpace(5);

        form.addComponent(new JLabel("Xml Bomb Attacments"));

        JCheckBox attachXml = form.appendCheckBox(ENABLE_ATTACHMENT_FIELD, null, xmlCheck.isAttachXmlBomb());
        attachXml.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                form.getComponent(ATTACHMENT_PREFIX_FIELD).setEnabled(
                        ((JCheckBox) form.getComponent(ENABLE_ATTACHMENT_FIELD)).isSelected());
                xmlChk.setAttachXmlBomb(((JCheckBox) form.getComponent(ENABLE_ATTACHMENT_FIELD)).isSelected());
            }
        });

        JTextField attachmentPrefixField = form.appendTextField(ATTACHMENT_PREFIX_FIELD, "Attachment Prefix Field");
        attachmentPrefixField.setMaximumSize(new Dimension(80, 10));
        attachmentPrefixField.setColumns(20);
        attachmentPrefixField.setText(xmlCheck.getAttachmentPrefix());
        attachmentPrefixField.setEnabled(xmlCheck.isAttachXmlBomb());
        attachmentPrefixField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                xmlChk.setAttachmentPrefix(((JTextField) form.getComponent(ATTACHMENT_PREFIX_FIELD)).getText());
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }
        });
        JXToolBar toolbar = UISupport.createSmallToolbar();
        toolbar.add(previous = new PreviousAttachement());
        previous.setEnabled(false);
        toolbar.add(next = new NextAttachement());
        toolbar.add(new AddXmlAttachement());
        toolbar.add(new RemoveAttachement());
        toolbar.add(new SaveAttachement());
        toolbar.add(current = new JLabel("  current:0"));
        toolbar.add(max = new JLabel("  max:" + xmlBombList.size()));
        form.append(toolbar);
        attachementArea = new JTextArea(10, 15);
        xmlBombPosition = 0;
        if (xmlBombList.size() > 0) {
            attachementArea.setText(xmlBombList.get(xmlBombPosition));
            if (xmlBombList.size() == 1) {
                next.setEnabled(false);
            }
        } else {
            next.setEnabled(false);
        }
        form.append(new JScrollPane(attachementArea));

        form.addSpace(5);

        add(form.getPanel());
    }

    private class PreviousAttachement extends AbstractAction {

        public PreviousAttachement() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/left_arrow.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Previous Xml Bomb");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            xmlBombPosition--;
            next.setEnabled(true);
            attachementArea.setText(xmlBombList.get(xmlBombPosition));
            if (xmlBombPosition == 0) {
                setEnabled(false);
            }
            current.setText("  current:" + xmlBombPosition);
        }

    }

    private class NextAttachement extends AbstractAction {

        public NextAttachement() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/right_arrow.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Next Xml Bomb");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            xmlBombPosition++;
            previous.setEnabled(true);
            attachementArea.setText(xmlBombList.get(xmlBombPosition));
            if (xmlBombPosition == xmlBombList.size() - 1) {
                setEnabled(false);
            }
            current.setText("  current:" + xmlBombPosition);
        }

    }

    private class SaveAttachement extends AbstractAction {

        public SaveAttachement() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/save_all.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Save Xml Bomb");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            xmlBombList.set(xmlBombPosition, attachementArea.getText());
        }

    }

    private class AddXmlAttachement extends AbstractAction {

        public AddXmlAttachement() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(Action.SHORT_DESCRIPTION, "Add new Xml Bomb");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            xmlBombList.add("");
            attachementArea.setText("");
            max.setText("  max:" + xmlBombList.size());
            xmlBombPosition = xmlBombList.size() - 1;
            current.setText("  current:" + xmlBombPosition);
            next.setEnabled(false);
            if (xmlBombList.size() > 1) {
                previous.setEnabled(true);
            }
        }

    }

    private class RemoveAttachement extends AbstractAction {

        public RemoveAttachement() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(Action.SHORT_DESCRIPTION, "Remove current Xml Bomb");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!xmlBombList.isEmpty()) {

                xmlBombList.remove(xmlBombPosition);
                if (xmlBombPosition >= xmlBombList.size() && !xmlBombList.isEmpty()) {
                    xmlBombPosition = xmlBombList.size() - 1;
                } else {
                    if (xmlBombList.isEmpty()) {
                        xmlBombPosition = 0;
                        next.setEnabled(false);
                        previous.setEnabled(false);
                    }

                }
                if (xmlBombList.size() == 1) {
                    xmlBombPosition = 0;
                    next.setEnabled(false);
                    previous.setEnabled(false);
                }
                if (xmlBombList.size() - 1 == xmlBombPosition) {
                    next.setEnabled(false);
                }
                if (xmlBombList.isEmpty()) {
                    attachementArea.setText("");
                } else {
                    attachementArea.setText(xmlBombList.get(xmlBombPosition));
                }
                current.setText("  current:" + xmlBombPosition);
                max.setText("  max:" + xmlBombList.size());
            }
        }
    }

}
