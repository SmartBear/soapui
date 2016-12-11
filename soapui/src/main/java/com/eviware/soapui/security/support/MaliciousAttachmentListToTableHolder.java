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
import com.eviware.soapui.security.tools.AttachmentElement;
import com.eviware.soapui.security.ui.MaliciousAttachmentMutationsPanel.MutationTables;
import com.eviware.x.impl.swing.JFormDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaliciousAttachmentListToTableHolder {

    MaliciousAttachmentFilesListForm filesList;
    MaliciousAttachmentTableModel generateTableModel;
    MaliciousAttachmentTableModel replaceTableModel;
    JFormDialog tablesDialog;

    Map<String, List<MaliciousAttachmentConfig>> generateMap = new HashMap<String, List<MaliciousAttachmentConfig>>();
    Map<String, List<MaliciousAttachmentConfig>> replaceMap = new HashMap<String, List<MaliciousAttachmentConfig>>();
    Map<String, Boolean> removeMap = new HashMap<String, Boolean>();

    public JFormDialog getTablesDialog() {
        return tablesDialog;
    }

    public void setTablesDialog(JFormDialog tablesDialog) {
        this.tablesDialog = tablesDialog;
    }

    public MaliciousAttachmentFilesListForm getFilesList() {
        return filesList;
    }

    public void setFilesList(MaliciousAttachmentFilesListForm filesList) {
        this.filesList = filesList;
    }

    public MaliciousAttachmentTableModel getGenerateTableModel() {
        return generateTableModel;
    }

    public void setGenerateTableModel(MaliciousAttachmentTableModel generateTableModel) {
        this.generateTableModel = generateTableModel;
    }

    public MaliciousAttachmentTableModel getReplaceTableModel() {
        return replaceTableModel;
    }

    public void setReplaceTableModel(MaliciousAttachmentTableModel replaceTableModel) {
        this.replaceTableModel = replaceTableModel;
    }

    public void refresh() {
        if (filesList != null) {
            AttachmentElement item = filesList.getFirstItem();
            // String label = ( item != null ) ? item.getAttachment().getName() :
            // "";
            // tablesDialog.getFormField( MutationTables.LABEL ).setValue( label );
            if (item != null) {
                load(item);
            }
        }
    }

    public void refresh(AttachmentElement oldItem, AttachmentElement newItem) {
        if (oldItem != null) {
            save(oldItem);

            if (newItem != null) {
                load(newItem);
            }
        }
    }

    public void addResultToGenerateTable(MaliciousAttachmentConfig config) {
        generateTableModel.addResult(config);
    }

    public void addResultToReplaceTable(MaliciousAttachmentConfig config) {
        replaceTableModel.addResult(config);
    }

    private void save(AttachmentElement item) {
        List<MaliciousAttachmentConfig> generateList = new ArrayList<MaliciousAttachmentConfig>();
        List<MaliciousAttachmentConfig> replaceList = new ArrayList<MaliciousAttachmentConfig>();

        for (int i = 0; i < generateTableModel.getRowCount(); i++) {
            generateList.add(generateTableModel.getRowValue(i));
        }

        for (int i = 0; i < replaceTableModel.getRowCount(); i++) {
            replaceList.add(replaceTableModel.getRowValue(i));
        }

        Boolean remove = tablesDialog.getBooleanValue(MutationTables.REMOVE_FILE);

        generateMap.put(item.getId(), generateList);
        replaceMap.put(item.getId(), replaceList);
        removeMap.put(item.getId(), remove);
    }

    private void load(AttachmentElement item) {
        List<MaliciousAttachmentConfig> generateList = generateMap.get(item.getId());
        List<MaliciousAttachmentConfig> replaceList = replaceMap.get(item.getId());
        Boolean remove = removeMap.get(item.getId());

        // tablesDialog.setValue( MutationTables.LABEL,
        // item.getAttachment().getName() );

        generateTableModel.clear();
        replaceTableModel.clear();
        tablesDialog.setBooleanValue(MutationTables.REMOVE_FILE, new Boolean(false));

        if (remove != null) {
            tablesDialog.setBooleanValue(MutationTables.REMOVE_FILE, remove);
        }

        if (generateList != null) {
            for (MaliciousAttachmentConfig element : generateList) {
                generateTableModel.addResult(element);
            }
        }

        if (replaceList != null) {
            for (MaliciousAttachmentConfig element : replaceList) {
                replaceTableModel.addResult(element);
            }
        }
    }

    public void removeAttachment(String key) {
        generateMap.remove(key);
        replaceMap.remove(key);
        removeMap.remove(key);
    }

    public void release() {
        filesList.release();
        filesList = null;
        tablesDialog.release();
        generateMap = null;
        replaceMap = null;
        removeMap = null;
    }
}
