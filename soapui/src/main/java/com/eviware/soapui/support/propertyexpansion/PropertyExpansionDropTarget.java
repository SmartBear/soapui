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

package com.eviware.soapui.support.propertyexpansion;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionImpl;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.tree.nodes.PropertyModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;

import javax.swing.text.JTextComponent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

public final class PropertyExpansionDropTarget implements DropTargetListener {
    private final PropertyExpansionTarget target;

    public PropertyExpansionDropTarget(PropertyExpansionTarget target) {
        this.target = target;
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        if (!isAcceptable(dtde.getTransferable())) {
            dtde.rejectDrag();
        }
    }

    public void dragExit(DropTargetEvent dtde) {
        if (dtde.getDropTargetContext().getComponent() instanceof JTextComponent) {
            ((JTextComponent) dtde.getDropTargetContext().getComponent()).getCaret().setVisible(false);
        }
    }

    public void dragOver(DropTargetDragEvent dtde) {
        if (!isAcceptable(dtde.getTransferable())) {
            dtde.rejectDrag();
        }

        if (dtde.getDropTargetContext().getComponent() instanceof JTextComponent) {
            JTextComponent textField = (JTextComponent) dtde.getDropTargetContext().getComponent();
            int pos = textField.viewToModel(dtde.getLocation());
            if (pos != -1) {
                textField.setCaretPosition(pos);
                textField.getCaret().setVisible(true);
            }
        }

        dtde.acceptDrag(dtde.getDropAction());
    }

    public void drop(DropTargetDropEvent dtde) {
        if (!isAcceptable(dtde.getTransferable())) {
            dtde.rejectDrop();
        } else {
            try {
                Transferable transferable = dtde.getTransferable();
                Object transferData = transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
                if (transferData instanceof PropertyModelItem) {
                    dtde.acceptDrop(dtde.getDropAction());
                    PropertyModelItem modelItem = (PropertyModelItem) transferData;

                    String xpath = modelItem.getXPath();
                    if (xpath == null && XmlUtils.seemsToBeXml(modelItem.getProperty().getValue())) {
                        xpath = UISupport.selectXPath("Create PropertyExpansion", "Select XPath below", modelItem
                                .getProperty().getValue(), null);

                        if (xpath != null) {
                            xpath = PropertyExpansionUtils.shortenXPathForPropertyExpansion(xpath, modelItem.getProperty()
                                    .getValue());
                        }
                    }

                    target.insertPropertyExpansion(new PropertyExpansionImpl(modelItem.getProperty(), xpath),
                            dtde.getLocation());

                    dtde.dropComplete(true);
                }
            } catch (Exception e) {
                SoapUI.logError(e);
            }

            if (dtde.getDropTargetContext().getComponent() instanceof JTextComponent) {
                ((JTextComponent) dtde.getDropTargetContext().getComponent()).getCaret().setVisible(false);
            }
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public boolean isAcceptable(Transferable transferable) {
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            DataFlavor flavor = flavors[i];
            if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
                try {
                    Object modelItem = transferable.getTransferData(flavor);
                    if (modelItem instanceof PropertyModelItem) {
                        return PropertyExpansionUtils.canExpandProperty(target.getContextModelItem(),
                                ((PropertyModelItem) modelItem).getProperty());
                    }
                } catch (Exception ex) {
                    SoapUI.logError(ex);
                }
            }
        }

        return false;
    }
}
