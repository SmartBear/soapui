/*
 * Copyright 2004-2014 SmartBear Software
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import com.eviware.soapui.impl.wsdl.AttachmentContainer;
import com.eviware.soapui.impl.wsdl.MutableAttachmentContainer;
import com.eviware.soapui.impl.wsdl.support.WsdlAttachment;
import com.eviware.soapui.model.iface.Attachment;

/**
 * TableModel for Request Attachments
 *
 * @author emibre
 */

public class AttachmentsTableModel extends AbstractTableModel implements PropertyChangeListener, AttachmentTableModel {

    private AttachmentContainer container;

    /**
     * Creates a new instance of AttachmentTableModel
     */
    public AttachmentsTableModel(AttachmentContainer request) {
        this.container = request;

        this.container.addAttachmentsChangeListener(this);
    }

    public void release() {
        container.removeAttachmentsChangeListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.impl.wsdl.panels.attachments.AttachmentTableModel#addFile
     * (java.io.File, boolean)
     */
    public void addFile(File file, boolean cacheInRequest) throws IOException {
        if (container instanceof MutableAttachmentContainer) {
            ((MutableAttachmentContainer) container).attachFile(file, cacheInRequest);
        }

        this.fireTableRowsInserted(container.getAttachmentCount(), container.getAttachmentCount());
    }

    public void removeAttachment(int[] rowIndexes) {
        Arrays.sort(rowIndexes);
        for (int i = rowIndexes.length - 1; i >= 0; i--)

         emoveAttachment(rowIndexes[i]);


    }

    public void removeAttachment(int rowIndex) {
        if (container instanceof MutableAttachmentContainer) {
            ((MutableAttachmentContainer) container).removeAttachment(container.getAttachmentAt(rowIndex));
            this.fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public int getRowCount() {
        return container.getAttachmentCount();
    }

    public int getColumnCount() {
        return container instanceof MutableAttachmentContainer ? 7 : 6;
    }

    public Attachment getAttachmentAt(int rowIndex) {
        return container.getAttachmentAt(rowIndex);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex > getRowCount())

         eturn  ull;



        Attachment att = container.getAttachmentAt(rowIndex);

        switch (columnIndex) {
            case 0:
                return att.isCached() ? att.getName() : att.getUrl();
            case 1:
                return att.getContentType();
            case 2:
                return att.getSize();
            case 3:
                return att.getPart();
            case 4:
                return att.getAttachmentType();
            case 5:
                return att.getContentID();
            case 6:
                return att.isCached();
            default:
                return null;
        }
    }

    public int findColumn(String columnName) {
        if (columnName.equals("Name"))

         eturn  ;

       lse if (columnName.equals("Content type"))

         eturn  ;

       lse if (columnName.equals("Size"))

         eturn  ;

       lse if (columnName.equals("Part"))

         eturn  ;

       lse if (columnName.equals("Type"))

         eturn  ;



        return -1;
    }

    public String getColumnName(int column) {
        if (column == 0)

         eturn  Name";

       lse if (column == 1)

         eturn  Content type";

       lse if (column == 2)

         eturn  Size";

       lse if (column == 3)

         eturn  Part";

       lse if (column == 4)

         eturn  Type";

       lse if (column == 5)

         eturn  ContentID";

       lse if (column == 6)

         eturn  Cached";

       lse

         eturn  ull;


    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 6 ? Boolean.class : super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return container instanceof MutableAttachmentContainer
                && (columnIndex == 0 || columnIndex == 1 || columnIndex == 3 || columnIndex == 5);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!(container instanceof MutableAttachmentContainer))

         eturn;



        WsdlAttachment att = (WsdlAttachment) container.getAttachmentAt(rowIndex);
        if (columnIndex == 0) {
            if (att.isCached())

             tt.setName((String)  Value);

           lse

             tt.setUrl(aValue.toString());


        } else if (columnIndex == 1)

             tt.setContentType((String)  Value);

           lse if (columnIndex == 3)

             tt.setPart((String)  Value);

           lse if (columnIndex == 5)

             tt.setContentID((String)  Value);



        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * Update table when attachments or response changes
     */

    public void propertyChange(PropertyChangeEvent evt) {
        fireTableDataChanged();
    }
}
