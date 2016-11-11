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

package com.eviware.soapui.security.support;

import com.eviware.soapui.config.MaliciousAttachmentConfig;
import com.eviware.soapui.config.MaliciousAttachmentElementConfig;
import com.eviware.soapui.config.MaliciousAttachmentSecurityScanConfig;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.security.tools.AttachmentElement;
import com.eviware.soapui.security.ui.MaliciousAttachmentMutationsPanel.MutationTables;
import com.eviware.soapui.support.UISupport;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class MaliciousAttachmentFilesListForm extends JPanel {
    private DefaultListModel listModel;
    private JList list;
    private AttachmentElement oldSelection;
    private AttachmentElement currentSelection;
    private MaliciousAttachmentSecurityScanConfig config;
    final MaliciousAttachmentListToTableHolder holder;

    public MaliciousAttachmentFilesListForm(MaliciousAttachmentSecurityScanConfig config,
                                            MaliciousAttachmentListToTableHolder holder) {
        super(new BorderLayout());

        this.config = config;
        this.holder = holder;

        JPanel p = UISupport.createEmptyPanel(3, 3, 3, 3);
        p.add(new JLabel("<html><b>Existing Attachments</b></html>"), BorderLayout.WEST);
        add(p, BorderLayout.NORTH);

        listModel = new DefaultListModel();
        list = new JList(listModel);
        list.setToolTipText("Choose file");
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(30, 50));
        add(scrollPane, BorderLayout.CENTER);

        list.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                currentSelection = (list.getSelectedIndex() == -1) ? null : (AttachmentElement) listModel.get(list
                        .getSelectedIndex());
                MaliciousAttachmentFilesListForm.this.holder.refresh(oldSelection, currentSelection);
                oldSelection = currentSelection;
            }
        });

        setBorder(null);
    }

    public AttachmentElement getFirstItem() {
        if (list.getModel().getSize() != 0) {
            list.setSelectedIndex(0);
            return (AttachmentElement) list.getSelectedValue();
        }
        return null;
    }

    public JList getList() {
        return list;
    }

    public AttachmentElement[] getData() {
        AttachmentElement[] result = new AttachmentElement[listModel.size()];
        for (int c = 0; c < result.length; c++) {
            result[c] = (AttachmentElement) listModel.get(c);
        }
        return result;
    }

    public void setData(Attachment[] attachments) {
        MaliciousAttachmentSecurityScanConfig copy = (MaliciousAttachmentSecurityScanConfig) config.copy();

        listModel.clear();
        config.getElementList().clear();
        holder.getGenerateTableModel().clear();
        holder.getReplaceTableModel().clear();
        holder.getTablesDialog().setBooleanValue(MutationTables.REMOVE_FILE, new Boolean(false));

        if (attachments != null) {
            for (Attachment att : attachments) {
                AttachmentElement attEl = new AttachmentElement(att, att.getId());
                listModel.addElement(attEl);

                holder.getGenerateTableModel().clear();
                holder.getReplaceTableModel().clear();
                holder.getTablesDialog().setBooleanValue(MutationTables.REMOVE_FILE, new Boolean(false));

                // add empty element
                MaliciousAttachmentElementConfig newElement = config.addNewElement();

                newElement.setKey(attEl.getId());

                for (MaliciousAttachmentElementConfig element : copy.getElementList()) {
                    if (attEl.getId().equals(element.getKey())) {
                        newElement.setRemove(element.getRemove());
                        holder.getTablesDialog().setBooleanValue(MutationTables.REMOVE_FILE, element.getRemove());

                        for (MaliciousAttachmentConfig el : element.getGenerateAttachmentList()) {
                            MaliciousAttachmentConfig newEl = newElement.addNewGenerateAttachment();
                            newEl.setFilename(el.getFilename());
                            newEl.setSize(el.getSize());
                            newEl.setContentType(el.getContentType());
                            newEl.setEnabled(el.getEnabled());
                            newEl.setCached(el.getCached());

                            holder.addResultToGenerateTable(newEl);
                        }

                        for (MaliciousAttachmentConfig el : element.getReplaceAttachmentList()) {
                            MaliciousAttachmentConfig newEl = newElement.addNewReplaceAttachment();
                            newEl.setFilename(el.getFilename());
                            newEl.setSize(el.getSize());
                            newEl.setContentType(el.getContentType());
                            newEl.setEnabled(el.getEnabled());
                            newEl.setCached(el.getCached());

                            holder.addResultToReplaceTable(newEl);
                        }

                        holder.refresh(attEl, null);
                        break;
                    }
                }
            }
        }
    }

    public void updateConfig(MaliciousAttachmentSecurityScanConfig config) {
        this.config = config;
    }

    public void release() {
        list = null;
        config = null;
    }
}
