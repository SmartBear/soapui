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

package com.eviware.soapui.support.dnd;

import com.eviware.soapui.SoapUI;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

public abstract class AbstractSoapUIDropTarget implements DropTargetListener {
    public AbstractSoapUIDropTarget() {
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        if (!isAcceptable(dtde.getTransferable(), dtde.getLocation())) {
            dtde.rejectDrag();
        }
    }

    public void dragExit(DropTargetEvent dtde) {
    }

    public void dragOver(DropTargetDragEvent dtde) {
        if (!isAcceptable(dtde.getTransferable(), dtde.getLocation())) {
            dtde.rejectDrag();
        } else {
            dtde.acceptDrag(dtde.getDropAction());
        }
    }

    public void drop(DropTargetDropEvent dtde) {
        if (!isAcceptable(dtde.getTransferable(), dtde.getLocation())) {
            dtde.rejectDrop();
        } else {
            try {
                Object testCase = getTransferData(dtde.getTransferable());
                if (testCase != null) {
                    dtde.acceptDrop(dtde.getDropAction());

                    handleDrop(testCase, dtde.getLocation());

                    dtde.dropComplete(true);
                }
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
    }

    protected abstract boolean handleDrop(Object target, Point point);

    protected abstract boolean isAcceptable(Object target, Point point);

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public boolean isAcceptable(Transferable transferable, Point point) {
        return isAcceptable(getTransferData(transferable), point);
    }

    @SuppressWarnings("unchecked")
    private Object getTransferData(Transferable transferable) {
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            DataFlavor flavor = flavors[i];
            if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
                try {
                    return transferable.getTransferData(flavor);
                } catch (Exception ex) {
                    SoapUI.logError(ex);
                }
            }
        }

        return null;
    }

    public static void addDropTarget(Component component, AbstractSoapUIDropTarget target) {
        DropTarget dropTarget = new DropTarget(component, target);
        dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);
    }
}
