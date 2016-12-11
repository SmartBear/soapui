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

import com.eviware.soapui.support.UISupport;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FileListFormComponent extends JPanel implements JFormComponent, ActionListener {
    private DefaultListModel listModel;
    private JButton addButton;
    private JButton removeButton;
    private JList list;

    public FileListFormComponent(String tooltip) {
        listModel = new DefaultListModel();
        list = new JList(listModel);
        list.setToolTipText(tooltip);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(300, 70));
        add(scrollPane, BorderLayout.CENTER);
        Box box = new Box(BoxLayout.Y_AXIS);
        addButton = new JButton("Add..");
        addButton.addActionListener(this);
        box.add(addButton);
        box.add(Box.createVerticalStrut(5));
        removeButton = new JButton("Remove..");
        removeButton.addActionListener(this);
        box.add(removeButton);
        box.add(Box.createVerticalGlue());

        add(box, BorderLayout.EAST);

        list.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                removeButton.setEnabled(list.getSelectedIndex() != -1);
            }
        });

        removeButton.setEnabled(list.getSelectedIndex() != -1);
    }

    public void setValue(String value) {
        listModel.clear();
        String[] files = value.split(";");
        for (String file : files) {
            if (file.trim().length() > 0) {
                listModel.addElement(file);
            }
        }
    }

    public String getValue() {
        Object[] values = listModel.toArray();
        StringBuffer buf = new StringBuffer();
        for (int c = 0; c < values.length; c++) {
            if (c > 0) {
                buf.append(';');
            }

            buf.append(values[c]);
        }

        return buf.toString();
    }

    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() == addButton) {
            File file = UISupport.getFileDialogs().open(this, "Add file", null, null, null);
            if (file != null) {
                listModel.addElement(file.getAbsolutePath());
            }
        } else if (arg0.getSource() == removeButton && list.getSelectedIndex() != -1) {
            Object elm = listModel.getElementAt(list.getSelectedIndex());
            if (UISupport.confirm("Remove [" + elm.toString() + "] from list", "Remove")) {
                listModel.remove(list.getSelectedIndex());
            }
        }
    }
}
