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

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.support.UISupport;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * SoapUITreeNode for RestRequest implementations
 *
 * @author dain.nilsson
 */

public class RestMethodTreeNode extends AbstractModelItemTreeNode<RestMethod> {
    private List<RequestTreeNode> requestNodes = new ArrayList<RequestTreeNode>();
    private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();

    public RestMethodTreeNode(RestMethod method, SoapUITreeModel treeModel) {
        super(method, method.getParent(), treeModel);
        treeModel.mapModelItem(this);

        for (RestRequest request : method.getRequestList()) {
            requestAdded(request);
        }
    }

    @Override
    public int getChildCount() {
        return requestNodes.size();
    }

    @Override
    public SoapUITreeNode getChildNode(int index) {
        return requestNodes.get(index);
    }

    @Override
    public int getIndexOfChild(Object child) {
        return requestNodes.indexOf(child);
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

    public void release() {
        super.release();

        for (RequestTreeNode treeNode : requestNodes) {
            treeNode.getModelItem().removePropertyChangeListener(Request.NAME_PROPERTY, propertyChangeListener);
            treeNode.release();
        }
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        UISupport.invokeAndWaitIfNotInEDT(new Runnable() {
            @Override
            public void run() {
                RestMethodTreeNode.super.propertyChange(evt);
                if (evt.getPropertyName().equals("childRequests")) {
                    if (evt.getNewValue() != null) {
                        requestAdded((RestRequest) evt.getNewValue());
                    } else {
                        requestRemoved((RestRequest) evt.getOldValue());
                    }
                }
            }
        });
    }
}
