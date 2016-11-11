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

package com.eviware.soapui.model.tree;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.tree.nodes.OperationTreeNode;
import com.eviware.soapui.model.tree.nodes.RestResourceTreeNode;

public class TreeNodeFactory {
    public static SoapUITreeNode createTreeNode(ModelItem modelItem, SoapUITreeModel treeModel) {
        if (modelItem instanceof WsdlOperation) {
            return new OperationTreeNode((Operation) modelItem, treeModel);
        } else if (modelItem instanceof RestResource) {
            return new RestResourceTreeNode((RestResource) modelItem, treeModel);
        }

        return null;
    }
}
