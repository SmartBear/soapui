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

package com.eviware.soapui.support.editor.inspectors.attachments;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.AttachmentContainer;
import com.eviware.soapui.impl.wsdl.MutableAttachmentContainer;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.WsdlAttachment;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart.AttachmentPart;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Utility Panel for displaying a table of attachments
 *
 * @author emibre
 */

public class AttachmentsPanel extends javax.swing.JPanel {
    private DropTarget dropTarget;
    private FileTransferHandler fileTransferHandler;
    private AttachmentsTableModel tableModel;
    private JFileChooser fc;
    private final AttachmentContainer container;
    private JButton exportBtn;
    private JButton reloadBtn;

    /**
     * Creates new form FileTableList
     */
    public AttachmentsPanel(AttachmentContainer container) {
        this.container = container;
        initComponents();
        initFileTransfer();
    }

    public void release() {
        tableModel.release();
        if (attachmentPartCellEditor != null) {
            attachmentPartCellEditor.release();
        }
    }

    private void initFileTransfer() {
        if (container instanceof MutableAttachmentContainer) {
            fileTransferHandler = new FileTransferHandler(tableModel);
            fileTable.setDragEnabled(true);
            fileTable.setTransferHandler(fileTransferHandler);

            dropTarget = new DropTarget();
            dropTarget.setActive(true);
            try {
                dropTarget.addDropTargetListener(new DropTargetListener() {
                    public void dragEnter(DropTargetDragEvent dtde) {
                    }

                    public void dragExit(DropTargetEvent dte) {
                    }

                    public void dragOver(DropTargetDragEvent dtde) {
                    }

                    @SuppressWarnings("unchecked")
                    public void drop(DropTargetDropEvent dtde) {
                        try {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                            Transferable trans = dtde.getTransferable();
                            List<File> files = (List<File>) trans.getTransferData(DataFlavor.javaFileListFlavor);
                            for (File f : files) {
                                System.out.println("Dropping file: " + f.getName());

                                Boolean retval = UISupport.confirmOrCancel("Cache attachment in request?", "Att Attachment");
                                if (retval == null) {
                                    return;
                                }

                                tableModel.addFile(f, retval);
                            }

                        } catch (Exception e) {
                            SoapUI.logError(e);
                        }
                    }

                    public void dropActionChanged(DropTargetDragEvent dtde) {
                    }
                });
            } catch (Exception e) {
                SoapUI.logError(e);
            }

            jScrollPane1.getViewport().setDropTarget(dropTarget);
        }
    }

    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        tableModel = new AttachmentsTableModel(container);
        fileTable = JTableFactory.getInstance().makeJTable(tableModel);

        if (container instanceof MutableAttachmentContainer) {
            attachmentPartCellEditor = new AttachmentPartCellEditor();
            fileTable.getColumnModel().getColumn(3).setCellEditor(attachmentPartCellEditor);
        }

        setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(fileTable);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1 = UISupport.createSmallToolbar();

        if (container instanceof MutableAttachmentContainer) {
            addFileBtn = UISupport.createToolbarButton(UISupport.createImageIcon("/add.png"));
            removeBtn = UISupport.createToolbarButton(UISupport.createImageIcon("/delete.png"));
            reloadBtn = UISupport.createToolbarButton(UISupport.createImageIcon("/reload_properties.gif"));

            addFileBtn.setToolTipText("Adds an attachment");
            addFileBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    addFileBtnActionPerformed(evt);
                }
            });

            jPanel1.addFixed(addFileBtn);

            removeBtn.setToolTipText("Removes the selected attachment");
            removeBtn.setEnabled(false);
            removeBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    removeBtnActionPerformed(evt);
                }
            });

            jPanel1.addFixed(removeBtn);

            reloadBtn.setToolTipText("Reloads the selected attachment");
            reloadBtn.setEnabled(false);
            reloadBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    reloadBtnActionPerformed(evt);
                }
            });

            jPanel1.addFixed(reloadBtn);
        }

        exportBtn = UISupport.createToolbarButton(UISupport.createImageIcon("/export.png"));
        exportBtn.setToolTipText("Exports the selected attachment to a file");
        exportBtn.setEnabled(false);
        exportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportBtnActionPerformed(evt);
            }
        });

        jPanel1.addFixed(exportBtn);
        jPanel1.addGlue();
        jPanel1.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.ATTACHMENTS_HELP_URL)));
        add(jPanel1, java.awt.BorderLayout.NORTH);

        fileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (removeBtn != null) {
                    removeBtn.setEnabled(fileTable.getSelectedRowCount() > 0);
                    reloadBtn.setEnabled(fileTable.getSelectedRowCount() > 0);
                }

                exportBtn.setEnabled(fileTable.getSelectedRowCount() > 0);
            }
        });

        fileTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) {
                    return;
                }

                int ix = fileTable.getSelectedRow();
                if (ix == -1) {
                    return;
                }

                Attachment attachment = container.getAttachmentAt(ix);

                if (attachment.isCached()) {
                    String name = attachment.getName();
                    try {
                        name = StringUtils.createFileName(name, '-');
                        File tempFile = File.createTempFile("attachment-" + name,
                                "." + ContentTypeHandler.getExtensionForContentType(attachment.getContentType()));
                        exportAttachment(tempFile, attachment, false);
                    } catch (Exception e1) {
                        UISupport.showErrorMessage(e1);
                    }
                } else {
                    Tools.openURL(attachment.getUrl());
                }
            }
        });
    }

    protected void exportBtnActionPerformed(ActionEvent evt) {
        File file = UISupport.getFileDialogs().saveAs(this, "Export Attachment..");
        while (file != null && file.exists()
                && !UISupport.confirm("File " + file.getName() + " exists, overwrite?", "Export Attachment")) {
            file = UISupport.getFileDialogs().saveAs(this, "Export Attachment..");
        }

        if (file != null) {
            Attachment attachment = tableModel.getAttachmentAt(fileTable.getSelectedRow());
            try {
                exportAttachment(file, attachment, true);
            } catch (Exception e) {
                UISupport.showErrorMessage(e);
            }
        }
    }

    private void exportAttachment(File file, Attachment attachment, boolean showOpenQuery)
            throws FileNotFoundException, IOException, Exception, MalformedURLException {
        FileOutputStream out = new FileOutputStream(file);

        long total = Tools.writeAll(out, attachment.getInputStream());
        out.close();
        if (!showOpenQuery
                || UISupport.confirm("Written [" + total + "] bytes to " + file.getName() + ", open in browser?",
                "Saved File")) {
            Tools.openURL(file.toURI().toURL().toString());
        }
    }

    protected void reloadBtnActionPerformed(ActionEvent evt) {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        WsdlAttachment attachment = (WsdlAttachment) tableModel.getAttachmentAt(selectedRow);
        if (attachment == null) {
            return;
        }

        File file = UISupport.getFileDialogs().open(this, "Reload Attachment..", "*", "Any File", attachment.getUrl());
        if (file != null) {
            Boolean retval = UISupport.confirmOrCancel("Cache attachment in request?", "Reload Attachment");
            if (retval == null) {
                return;
            }

            try {
                attachment.reload(file, retval);
                tableModel.fireTableRowsUpdated(selectedRow, selectedRow);
            } catch (IOException e) {
                UISupport.showErrorMessage(e);
            }
        }
    }

    private void addFileBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addFileBtnActionPerformed
        if (fc == null) {
            fc = new JFileChooser();
        }

        String root = PathUtils.getExpandedResourceRoot(container.getModelItem());
        if (StringUtils.hasContent(root)) {
            fc.setCurrentDirectory(new File(root));
        }

        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            Boolean retval = UISupport.confirmOrCancel("Cache attachment in request?", "Add Attachment");
            if (retval == null) {
                return;
            }
            try {
                tableModel.addFile(file, retval);
            } catch (IOException e) {
                UISupport.showErrorMessage(e);
            }
        } else {
            System.out.println("Open command cancelled by user.");
        }
    }// GEN-LAST:event_addFileBtnActionPerformed

    private void removeBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_removeBtnActionPerformed
        if (UISupport.confirm("Remove selected attachments?", "Remove Attachments")) {
            tableModel.removeAttachment(fileTable.getSelectedRows());
        }
    }// GEN-LAST:event_removeBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFileBtn;
    private JTable fileTable;
    private JXToolBar jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeBtn;
    private AttachmentPartCellEditor attachmentPartCellEditor;

    // End of variables declaration//GEN-END:variables

    private class AttachmentPartCellEditor extends DefaultCellEditor {
        public AttachmentPartCellEditor() {
            super(new JComboBox(new PartsComboBoxModel()));
        }

        public void release() {
            ((PartsComboBoxModel) ((JComboBox) editorComponent).getModel()).release();
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            PartsComboBoxModel model = ((PartsComboBoxModel) ((JComboBox) editorComponent).getModel());
            ((JComboBox) editorComponent).setModel(model.init(tableModel.getAttachmentAt(row)));

            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }

    private final class PartsComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {
        private Attachment attachment;
        private AttachmentPart[] parts;

        public PartsComboBoxModel() {
            container.addAttachmentsChangeListener(this);
        }

        public void release() {
            container.removeAttachmentsChangeListener(this);
        }

        public PartsComboBoxModel init(Attachment attachment) {
            this.attachment = attachment;

            int previousPartsCount = parts == null ? 0 : parts.length;

            parts = container.getDefinedAttachmentParts();
            if (previousPartsCount < parts.length) {
                fireIntervalAdded(this, previousPartsCount, parts.length);
            } else if (previousPartsCount > parts.length) {
                fireIntervalRemoved(this, parts.length - 1, previousPartsCount);
            }

            fireContentsChanged(this, 0, parts.length - 1);

            return this;
        }

        public Object getElementAt(int index) {
            return parts == null ? null : parts[index].getName();
        }

        public int getSize() {
            return parts == null ? 0 : parts.length;
        }

        public Object getSelectedItem() {
            return attachment == null ? null : attachment.getPart();
        }

        public void setSelectedItem(Object anItem) {
            if (attachment != null) {
                attachment.setPart((String) anItem);
            }
        }

        public void propertyChange(PropertyChangeEvent arg0) {
            // delete our current one?
            if (arg0.getOldValue() == attachment && arg0.getNewValue() == null) {
                attachment = null;
                parts = null;
            }
        }
    }
}
