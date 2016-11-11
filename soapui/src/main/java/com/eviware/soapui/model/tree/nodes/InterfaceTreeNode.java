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

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.InterfaceListener;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.TreeNodeFactory;
import com.eviware.soapui.support.UISupport;

import java.util.ArrayList;
import java.util.List;

/**
 * SoapUITreeNode for Interface implementations
 *
 * @author Ole.Matzura
 */

public class InterfaceTreeNode extends AbstractModelItemTreeNode<Interface> {
    private InternalInterfaceListener interfaceListener;
    private List<SoapUITreeNode> operationNodes = new ArrayList<SoapUITreeNode>();

    public InterfaceTreeNode(Interface iface, SoapUITreeModel treeModel) {
        super(iface, iface.getProject(), treeModel);

        interfaceListener = new InternalInterfaceListener();
        iface.addInterfaceListener(interfaceListener);

        for (int c = 0; c < iface.getOperationCount(); c++) {
            operationNodes.add(TreeNodeFactory.createTreeNode(iface.getOperationAt(c), getTreeModel()));
        }

        treeModel.mapModelItems(operationNodes);
    }

    public void release() {
        super.release();

        getInterface().removeInterfaceListener(interfaceListener);

        for (SoapUITreeNode treeNode : operationNodes) {
            treeNode.release();
        }
    }

    public Interface getInterface() {
        return (Interface) getModelItem();
    }

    public int getChildCount() {
        return operationNodes.size();
    }

    public int getIndexOfChild(Object child) {
        return operationNodes.indexOf(child);
    }

    public SoapUITreeNode getChildNode(int index) {
        return operationNodes.get(index);
    }

    private class InternalInterfaceListener implements InterfaceListener {
        public void requestAdded(final Request request) {
            UISupport.invokeAndWaitIfNotInEDT(new Runnable() {
                @Override
                public void run() {
                    SoapUITreeNode operationTreeNode = getTreeModel().getTreeNode(request.getOperation());
                    if (operationTreeNode != null && operationTreeNode instanceof OperationTreeNode) {
                        ((OperationTreeNode) operationTreeNode).requestAdded(request);
                    }
                }
            });
        }

        public void requestRemoved(Request request) {
            SoapUITreeNode operationTreeNode = getTreeModel().getTreeNode(request.getOperation());
            if (operationTreeNode != null && operationTreeNode instanceof OperationTreeNode) {
                ((OperationTreeNode) operationTreeNode).requestRemoved(request);
            }
        }

        public void operationAdded(final Operation operation) {
            UISupport.invokeAndWaitIfNotInEDT(new Runnable() {
                @Override
                public void run() {
                    if (operation instanceof RestResource) {
                        RestResource restResource = (RestResource) operation;
                        if (restResource.getParentResource() != null) {
                            RestResourceTreeNode treeNode = (RestResourceTreeNode) getTreeModel().getTreeNode(
                                    restResource.getParentResource());
                            treeNode.addChildResource(restResource);
                            return;
                        }
                    }

                    SoapUITreeNode operationTreeNode = TreeNodeFactory.createTreeNode(operation, getTreeModel());

                    operationNodes.add(operationTreeNode);
                    getTreeModel().notifyNodeInserted(operationTreeNode);
                }
            });
        }

        public void operationRemoved(Operation operation) {
            SoapUITreeNode treeNode = getTreeModel().getTreeNode(operation);
            if (operationNodes.contains(treeNode)) {
                getTreeModel().notifyNodeRemoved(treeNode);
                operationNodes.remove(treeNode);
            } else if (treeNode instanceof RestResourceTreeNode) {
                SoapUITreeNode parentNode = treeNode.getParentTreeNode();
                if (parentNode instanceof RestResourceTreeNode) {
                    ((RestResourceTreeNode) parentNode).removeChildResource((RestResourceTreeNode) treeNode);
                }
            } else {
                throw new RuntimeException("Removing unknown operation");
            }
        }

        public void operationUpdated(Operation operation) {
        }
    }
}
