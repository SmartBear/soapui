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

package com.eviware.soapui.impl.wsdl.actions.mockservice;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Clones a WsdlMockService
 *
 * @author Ole.Matzura
 */

public class CloneMockServiceAction extends AbstractSoapUIAction<WsdlMockService> {
    public final static String SOAPUI_ACTION_ID = "CloneMockServiceAction";
    private XFormDialog dialog;

    public CloneMockServiceAction() {
        super("Clone MockService", "Clones this MockService");
    }

    public void perform(WsdlMockService mockService, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
        }

        dialog.getFormField(Form.CLONE_DESCRIPTION).addFormFieldListener(new XFormFieldListener() {

            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                if (dialog.getBooleanValue(Form.CLONE_DESCRIPTION)) {
                    dialog.getFormField(Form.DESCRIPTION).setEnabled(false);
                } else {
                    dialog.getFormField(Form.DESCRIPTION).setEnabled(true);
                }

            }
        });
        dialog.setValue(Form.NAME, "Copy of " + mockService.getName());
        dialog.setBooleanValue(Form.CLONE_DESCRIPTION, true);
        dialog.getFormField(Form.DESCRIPTION).setEnabled(false);
        dialog.setValue(Form.DESCRIPTION, mockService.getDescription());
        WorkspaceImpl workspace = mockService.getProject().getWorkspace();
        dialog.setOptions(Form.PROJECT,
                ModelSupport.getNames(workspace.getOpenProjectList(), new String[]{"<Create New>"}));

        dialog.setValue(Form.PROJECT, mockService.getProject().getName());

        if (dialog.show()) {
            String targetProjectName = dialog.getValue(Form.PROJECT);
            String name = dialog.getValue(Form.NAME);

            WsdlProject project = mockService.getProject();
            WsdlMockService clonedService = null;

            // within same project?
            boolean cloneDescription = dialog.getBooleanValue(Form.CLONE_DESCRIPTION);
            String description = mockService.getDescription();
            if (!cloneDescription) {
                description = dialog.getValue(Form.DESCRIPTION);
            }
            if (targetProjectName.equals(mockService.getProject().getName())) {
                clonedService = cloneMockServiceWithinProject(mockService, name, project, description);
            } else {
                clonedService = cloneToAnotherProject(mockService, targetProjectName, name, description);
            }

            if (clonedService != null) {
                UISupport.select(clonedService);
            }

            if (dialog.getBooleanValue(Form.MOVE)) {
                project.removeMockService(mockService);
            }
        }
    }

    public WsdlMockService cloneToAnotherProject(WsdlMockService mockService, String targetProjectName, String name,
                                                 String description) {
        WorkspaceImpl workspace = mockService.getProject().getWorkspace();
        WsdlProject targetProject = (WsdlProject) workspace.getProjectByName(targetProjectName);
        if (targetProject == null) {
            targetProjectName = UISupport.prompt("Enter name for new Project", "Clone MockService", "");
            if (targetProjectName == null) {
                return null;
            }

            try {
                targetProject = workspace.createProject(targetProjectName, null);
            } catch (SoapUIException e) {
                UISupport.showErrorMessage(e);
            }

            if (targetProject == null) {
                return null;
            }
        }

        Set<WsdlInterface> requiredInterfaces = getRequiredInterfaces(mockService, targetProject);

        if (requiredInterfaces.size() > 0) {
            String msg = "Target project [" + targetProjectName + "] is missing required interfaces;\r\n\r\n";
            for (WsdlInterface iface : requiredInterfaces) {
                msg += iface.getName() + " [" + iface.getTechnicalId() + "]\r\n";
            }
            msg += "\r\nThese will be cloned to the targetProject as well";

            if (!UISupport.confirm(msg, "Clone MockService")) {
                return null;
            }

            for (WsdlInterface iface : requiredInterfaces) {
                targetProject.importInterface(iface, false, true);
            }
        }

        mockService = targetProject.importMockService(mockService, name, true, description);
        UISupport.select(mockService);
        return mockService;
    }

    public WsdlMockService cloneMockServiceWithinProject(WsdlMockService mockService, String name, WsdlProject project,
                                                         String description) {
        WsdlMockService newMockService = project.importMockService(mockService, name, true, description);
        UISupport.select(newMockService);
        return newMockService;
    }

    private Set<WsdlInterface> getRequiredInterfaces(WsdlMockService mockService, WsdlProject targetProject) {
        Set<WsdlInterface> requiredInterfaces = new HashSet<WsdlInterface>();

        for (int i = 0; i < mockService.getMockOperationCount(); i++) {
            WsdlOperation operation = mockService.getMockOperationAt(i).getOperation();
            if (operation != null) {
                requiredInterfaces.add(operation.getInterface());
            }
        }

        if (requiredInterfaces.size() > 0 && targetProject.getInterfaceCount() > 0) {
            Map<String, WsdlInterface> bindings = new HashMap<String, WsdlInterface>();
            for (WsdlInterface iface : requiredInterfaces) {
                bindings.put(iface.getTechnicalId(), iface);
            }

            for (Interface iface : targetProject.getInterfaceList()) {
                bindings.remove(iface.getTechnicalId());
            }

            requiredInterfaces.retainAll(bindings.values());
        }

        return requiredInterfaces;
    }

    @AForm(description = "Specify target Project and name of cloned MockService", name = "Clone MockService", helpUrl = HelpUrls.CLONEMOCKSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    public interface Form {
        @AField(name = "MockService Name", description = "The name of the cloned MockService", type = AFieldType.STRING)
        public final static String NAME = "MockService Name";

        @AField(name = "Target Project", description = "The target Project for the cloned MockService", type = AFieldType.ENUMERATION)
        public final static String PROJECT = "Target Project";

        @AField(name = "Move instead", description = "Moves the selected MockService instead of copying", type = AFieldType.BOOLEAN)
        public final static String MOVE = "Move instead";

        @AField(name = "Clone description", description = "Clones the description of selected TestCase", type = AFieldType.BOOLEAN)
        public final static String CLONE_DESCRIPTION = "Clone description";

        @AField(name = "Description", description = "Descroption of new TestCase", type = AFieldType.STRINGAREA)
        public final static String DESCRIPTION = "Description";
    }
}
