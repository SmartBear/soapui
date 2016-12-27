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
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.TreeNodeFactory;
import com.eviware.soapui.support.UISupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * SoapUITreeNode for Operation implementations
 *
 * @author Ole.Matzura
 */

public class RestResourceTreeNode extends AbstractModelItemTreeNode<RestResource> implements PropertyChangeListener {
    private List<RestResourceTreeNode> resourceNodes = new ArrayList<RestResourceTreeNode>();
    private List<RestMethodTreeNode> methodNodes = new ArrayList<RestMethodTreeNode>();
    private final RestResource restResource;

    private ReorderPropertyChangeListener propertyChangeListener = new ReorderPropertyChangeListener();

    public RestResourceTreeNode(RestResource restResource, SoapUITreeModel treeModel) {
        super(restResource, restResource.getParent(), treeModel);
        this.restResource = restResource;


        restResource.addPropertyChangeListener(RestResource.PATH_PROPERTY, this);

        treeModel.mapModelItem(this);

        for (int c = 0; c < restResource.getChildResourceCount(); c++) {
            resourceNodes.add(new RestResourceTreeNode(restResource.getChildResourceAt(c), getTreeModel()));
        }
        treeModel.mapModelItems(resourceNodes);

        for (int c = 0; c < restResource.getRestMethodCount(); c++) {
            methodAdded(restResource.getRestMethodAt(c));
        }
    }

    @Override
    public SoapUITreeNode getParentTreeNode() {
        return restResource.getParentResource() == null ? super.getParentTreeNode() : getTreeModel().getTreeNode(
                restResource.getParentResource());
    }

    @Override
    public String toString() {
        return restResource.getName() + " [" + restResource.getFullPath() + "]";
    }

    @Override
    public int getChildCount() {
        return restResource.getRestMethodCount() + restResource.getChildResourceCount();
    }

    @Override
    public SoapUITreeNode getChildNode(int index) {
        int childCount = methodNodes.size();
        if (index < childCount) {
            return methodNodes.get(index);
        } else {
            return resourceNodes.get(index - childCount);
        }
    }

    @Override
    public int getIndexOfChild(Object child) {
        int result = methodNodes.indexOf(child);
        if (result == -1) {
            result = resourceNodes.indexOf(child);
            if (result >= 0) {
                result += methodNodes.size();
            }
        }

        return result;
    }

    public void release() {
        super.release();

        for (RestMethodTreeNode treeNode : methodNodes) {
            treeNode.getModelItem().removePropertyChangeListener(Request.NAME_PROPERTY, propertyChangeListener);
            treeNode.release();
        }

        for (RestResourceTreeNode resource : resourceNodes) {
            resource.release();
        }
        restResource.removePropertyChangeListener(this);
    }

    public void addChildResource(RestResource restResource) {
        RestResourceTreeNode operationTreeNode = (RestResourceTreeNode) TreeNodeFactory.createTreeNode(restResource,
                getTreeModel());

        resourceNodes.add(operationTreeNode);
        getTreeModel().notifyNodeInserted(operationTreeNode);
    }

    public void removeChildResource(RestResourceTreeNode childResource) {
        if (resourceNodes.contains(childResource)) {
            getTreeModel().notifyNodeRemoved(childResource);
            resourceNodes.remove(childResource);
        }
    }

	/*
     * public void requestAdded(Request request) { if (request instanceof
	 * RestRequest) { RestMethod method = ((RestRequest)
	 * request).getRestMethod(); RestMethodTreeNode node = (RestMethodTreeNode)
	 * getTreeModel() .getTreeNode(method); if (methodNodes.contains(node)) {
	 * node.requestAdded(request); } } }
	 * 
	 * public void requestRemoved(Request request) { if (request instanceof
	 * RestRequest) { RestMethod method = ((RestRequest)
	 * request).getRestMethod(); RestMethodTreeNode node = (RestMethodTreeNode)
	 * getTreeModel() .getTreeNode(method); if (methodNodes.contains(node)) {
	 * node.requestRemoved(request); } } }
	 */

    public void methodAdded(final RestMethod method) {
        UISupport.invokeAndWaitIfNotInEDT(new Runnable() {
            @Override
            public void run() {
                RestMethodTreeNode methodTreeNode = new RestMethodTreeNode(method, getTreeModel());
                methodNodes.add(methodTreeNode);
                reorder(false);
                method.addPropertyChangeListener(Request.NAME_PROPERTY, propertyChangeListener);
                getTreeModel().notifyNodeInserted(methodTreeNode);
            }
        });
    }

    public void methodRemoved(RestMethod method) {
        SoapUITreeNode methodTreeNode = getTreeModel().getTreeNode(method);
        if (methodNodes.contains(methodTreeNode)) {
            getTreeModel().notifyNodeRemoved(methodTreeNode);
            methodNodes.remove(methodTreeNode);
            method.removePropertyChangeListener(propertyChangeListener);
        } else {
            throw new RuntimeException("Removing unknown method");
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (evt.getPropertyName().equals("childMethods")) {
            if (evt.getNewValue() != null) {
                methodAdded((RestMethod) evt.getNewValue());
            } else {
                methodRemoved((RestMethod) evt.getOldValue());
            }
        } else if (evt.getPropertyName().equals(RestResource.PATH_PROPERTY)) {
            getTreeModel().notifyNodeChanged(this);
        }
    }
}
