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

package com.eviware.soapui.model.tree.nodes;

import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.settings.UISettings;

import java.util.ArrayList;
import java.util.List;

/**
 * SoapUITreeNode for Operation implementations
 *
 * @author Ole.Matzura
 */

public class OperationTreeNode extends AbstractModelItemTreeNode<Operation> {
    private List<RequestTreeNode> requestNodes = new ArrayList<RequestTreeNode>();
    private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();

    public OperationTreeNode(Operation operation, SoapUITreeModel treeModel) {
        super(operation, operation.getInterface(), treeModel);

        for (int c = 0; c < operation.getRequestCount(); c++) {
            Request request = operation.getRequestAt(c);
            request.addPropertyChangeListener(Request.NAME_PROPERTY, propertyChangeListener);
            requestNodes.add(new RequestTreeNode(request, getTreeModel()));
        }

        initOrdering(requestNodes, UISettings.ORDER_REQUESTS);
        treeModel.mapModelItems(requestNodes);
    }

    public void release() {
        super.release();

        for (RequestTreeNode treeNode : requestNodes) {
            treeNode.getModelItem().removePropertyChangeListener(Request.NAME_PROPERTY, propertyChangeListener);
            treeNode.release();
        }
    }

    public Operation getOperation() {
        return (Operation) getModelItem();
    }

    public void requestAdded(Request request) {
        RequestTreeNode requestTreeNode = new RequestTreeNode(request, getTreeModel());
        requestNodes.add(requestTreeNode);
        reorder(false);
        request.addPropertyChangeListener(Request.NAME_PROPERTY, propertyChangeListener);
        getTreeModel().notifyNodeInserted(requestTreeNode);
    }

    public void requestRemoved(Request request) {
        SoapUITreeNode requestTreeNode = getTreeModel().getTreeNode(request);
        if (requestNodes.contains(requestTreeNode)) {
            getTreeModel().notifyNodeRemoved(requestTreeNode);
            requestNodes.remove(requestTreeNode);
            request.removePropertyChangeListener(propertyChangeListener);
        } else {
            throw new RuntimeException("Removing unknown request");
        }
    }
}
