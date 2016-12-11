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
import com.eviware.soapui.support.UISupport;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Handles drop of files on the AttachmentPanel
 *
 * @author emibre
 */

public class FileTransferHandler extends TransferHandler {
    private DataFlavor fileFlavor;
    private AttachmentTableModel attachmentModel;

    /**
     * Creates a new instance of FileTransferHandler
     */
    public FileTransferHandler(AttachmentTableModel attachmentModel) {
        fileFlavor = DataFlavor.javaFileListFlavor;
        this.attachmentModel = attachmentModel;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        return hasFileFlavor(flavors);
    }

    private boolean hasFileFlavor(DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (fileFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean importData(JComponent c, Transferable t) {
        try {
            List<File> files = (List<File>) t.getTransferData(fileFlavor);
            for (File f : files) {
                System.out.println("Got a file: " + f.getName());
                Boolean retval = UISupport.confirmOrCancel("Cache attachment in request?", "Att Attachment");
                if (retval == null) {
                    return false;
                }

                attachmentModel.addFile(f, retval);
            }

        } catch (IOException ex) {
            SoapUI.logError(ex);
        } catch (UnsupportedFlavorException ex) {
            SoapUI.logError(ex);
        }
        return false;
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
}
