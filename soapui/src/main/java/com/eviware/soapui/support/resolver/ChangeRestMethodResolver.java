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

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;
import com.eviware.soapui.support.swing.ModelItemListCellRenderer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class ChangeRestMethodResolver implements Resolver {
    private boolean resolved = false;
    private WsdlProject project;
    private RestMethod selectedMethod;

    public ChangeRestMethodResolver(RestTestRequestStep testStep) {
        this.project = testStep.getTestCase().getTestSuite().getProject();
    }

    public String getResolvedPath() {
        return "";
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean resolve() {
        PropertyChangeDialog pDialog = new PropertyChangeDialog("Resolve REST Method");
        pDialog.setVisible(true);
        resolved = update();
        return resolved;
    }

    public abstract boolean update();

    protected abstract Interface[] getInterfaces(WsdlProject project);

    public String getDescription() {
        return "Resolve: Select another REST Method";
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @SuppressWarnings("serial")
    private class PropertyChangeDialog extends SimpleDialog {
        private JComboBox serviceCombo;
        private JComboBox resourceCombo;
        private JComboBox methodCombo;

        public PropertyChangeDialog(String title) {
            super(title, getDescription(), null);
        }

        protected Component buildContent() {
            SimpleForm form = new SimpleForm();

            form.addSpace(5);
            Interface[] ifaces = getInterfaces(project);
            DefaultComboBoxModel serviceComboModel = new DefaultComboBoxModel();
            serviceCombo = form.appendComboBox("REST Services", serviceComboModel, "Target Service");
            serviceCombo.setRenderer(new ModelItemListCellRenderer());
            for (Interface element : ifaces) {
                if (element instanceof RestService) {
                    serviceComboModel.addElement(element);
                }
            }

            resourceCombo = form.appendComboBox("REST Resources", ((RestService) serviceCombo.getSelectedItem())
                    .getOperationList().toArray(), "Target Resource");
            resourceCombo.setRenderer(new ModelItemListCellRenderer());

            methodCombo = form.appendComboBox("REST Methods", ((RestResource) resourceCombo.getSelectedItem())
                    .getRestMethodList().toArray(), "Target Method");
            methodCombo.setRenderer(new ModelItemListCellRenderer());

            serviceCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Interface iface = project.getInterfaceByName(((Interface) serviceCombo.getSelectedItem()).getName());
                    resourceCombo.removeAllItems();
                    if (iface != null) {
                        resourceCombo.setEnabled(true);
                        for (Operation op : iface.getOperationList()) {
                            resourceCombo.addItem(op);
                        }
                    } else {
                        resourceCombo.setEnabled(false);
                    }
                }
            });

            resourceCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    RestResource resource = (RestResource) resourceCombo.getSelectedItem();
                    methodCombo.removeAllItems();
                    if (resource != null) {
                        methodCombo.setEnabled(true);
                        for (RestMethod method : resource.getRestMethodList()) {
                            methodCombo.addItem(method);
                        }
                    } else {
                        methodCombo.setEnabled(false);
                    }
                }
            });

            form.addSpace(5);
            return form.getPanel();
        }

        protected boolean handleOk() {
            selectedMethod = (RestMethod) methodCombo.getSelectedItem();
            return true;
        }
    }

    public RestMethod getSelectedRestMethod() {
        return selectedMethod;
    }

}
