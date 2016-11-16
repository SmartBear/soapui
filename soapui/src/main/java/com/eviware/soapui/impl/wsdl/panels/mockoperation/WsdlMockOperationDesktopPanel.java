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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.NewMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.OpenRequestForMockOperationAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.util.ModelItemNames;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.ExtendedComboBoxModel;
import com.eviware.soapui.ui.support.AbstractMockOperationDesktopPanel;

import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * DesktopPanel for WsdlGroovyTestSteps
 *
 * @author Ole.Matzura
 */

public class WsdlMockOperationDesktopPanel extends AbstractMockOperationDesktopPanel<WsdlMockOperation> {
    private WsdlInterface currentInterface;
    private InternalInterfaceListener interfaceListener = new InternalInterfaceListener();
    private JComboBox interfaceCombo;
    private JComboBox operationCombo;
    private InternalProjectListener projectListener = new InternalProjectListener();


    public WsdlMockOperationDesktopPanel(WsdlMockOperation mockOperation) {
        super(mockOperation);

        WsdlOperation operation = getModelItem().getOperation();
        if (operation != null) {
            currentInterface = operation.getInterface();
            currentInterface.addInterfaceListener(interfaceListener);
        }

        mockOperation.getMockService().getProject().addProjectListener(projectListener);
    }

    @Override
    protected String getAddToMockOperationIconPath() {
        return "/addToMockService.gif";
    }

    protected Component buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();
        toolbar.addSpace(3);

        toolbar.addFixed(UISupport.createToolbarButton(SwingActionDelegate.createDelegate(
                NewMockResponseAction.SOAPUI_ACTION_ID, getModelItem(), null, "/addToMockService.gif")));
        toolbar.addFixed(UISupport.createToolbarButton(SwingActionDelegate.createDelegate(
                OpenRequestForMockOperationAction.SOAPUI_ACTION_ID, getModelItem(), null, "/open_request.gif")));
        toolbar.addUnrelatedGap();

        ModelItemNames<WsdlInterface> names = new ModelItemNames<WsdlInterface>(ModelSupport.getChildren(getModelItem()
                .getMockService().getProject(), WsdlInterface.class));

        interfaceCombo = new JComboBox(names.getNames());
        interfaceCombo.setSelectedIndex(-1);
        interfaceCombo.addItemListener(new InterfaceComboListener());

        toolbar.addLabeledFixed("Interface", interfaceCombo);
        toolbar.addUnrelatedGap();
        operationCombo = new JComboBox(new ExtendedComboBoxModel());
        operationCombo.setPreferredSize(new Dimension(150, 20));
        operationCombo.addItemListener(new OperationComboListener());

        toolbar.addLabeledFixed("Operation", operationCombo);

        WsdlOperation operation = getModelItem().getOperation();
        interfaceCombo.setSelectedItem(operation == null ? null : operation.getInterface().getName());
        operationCombo.setSelectedItem(operation == null ? null : operation.getName());

        toolbar.addGlue();
        toolbar.addFixed(createActionButton(new ShowOnlineHelpAction(HelpUrls.MOCKOPERATION_HELP_URL), true));

        return toolbar;
    }

    public boolean onClose(boolean canCancel) {
        super.onClose(canCancel);

        if (currentInterface != null) {
            currentInterface.removeInterfaceListener(interfaceListener);
        }

        getModelItem().getMockService().getProject().removeProjectListener(projectListener);

        return release();
    }


    private final class InternalInterfaceListener extends InterfaceListenerAdapter {
        @Override
        public void operationAdded(Operation operation) {
            operationCombo.addItem(operation.getName());
        }

        @Override
        public void operationRemoved(Operation operation) {
            Object selectedItem = operationCombo.getSelectedItem();
            operationCombo.removeItem(operation.getName());

            if (selectedItem.equals(operation.getName())) {
                getModelItem().setOperation(null);
                interfaceCombo.setSelectedIndex(-1);
            }
        }

        @Override
        public void operationUpdated(Operation operation) {
            ExtendedComboBoxModel model = ((ExtendedComboBoxModel) operationCombo.getModel());
            int ix = model.getIndexOf(operation.getName());
            if (ix != -1) {
                model.setElementAt(operation.getName(), ix);
            }
        }
    }

    private final class InterfaceComboListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (currentInterface != null) {
                currentInterface.removeInterfaceListener(interfaceListener);
            }

            Object selectedItem = interfaceCombo.getSelectedItem();
            if (selectedItem == null) {
                operationCombo.setModel(new ExtendedComboBoxModel());
                currentInterface = null;
            } else {
                currentInterface = (WsdlInterface) getModelItem().getMockService().getProject()
                        .getInterfaceByName(selectedItem.toString());
                ModelItemNames<Operation> names = new ModelItemNames<Operation>(currentInterface.getOperationList());
                operationCombo.setModel(new ExtendedComboBoxModel(names.getNames()));

                currentInterface.addInterfaceListener(interfaceListener);
            }
        }
    }

    private final class OperationComboListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            WsdlInterface iface = (WsdlInterface) getModelItem().getMockService().getProject()
                    .getInterfaceByName(interfaceCombo.getSelectedItem().toString());
            WsdlOperation operation = iface.getOperationByName(operationCombo.getSelectedItem().toString());
            getModelItem().setOperation(operation);
        }
    }

    private final class InternalProjectListener extends ProjectListenerAdapter {
        @Override
        public void interfaceAdded(Interface iface) {
            interfaceCombo.addItem(iface.getName());
        }

        @Override
        public void interfaceRemoved(Interface iface) {
            if (interfaceCombo.getSelectedItem().equals(iface.getName())) {
                getModelItem().setOperation(null);
            }
        }
    }


}
