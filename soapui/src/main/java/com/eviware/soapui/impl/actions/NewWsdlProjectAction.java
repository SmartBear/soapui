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

package com.eviware.soapui.impl.actions;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.GenerateWsdlTestSuiteAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.io.File;

/**
 * Action for creating a new WSDL project
 *
 * @author Ole.Matzura
 */

public class NewWsdlProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "NewWsdlProjectAction";
    private XFormDialog dialog;

    public static final MessageSupport messages = MessageSupport.getMessages(NewWsdlProjectAction.class);

    public NewWsdlProjectAction() {
        super(messages.get("Title"), messages.get("Description"));
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
            dialog.setValue(Form.CREATEREQUEST, Boolean.toString(true));
            dialog.getFormField(Form.INITIALWSDL).addFormFieldListener(new XFormFieldListener() {
                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    String value = newValue.toLowerCase().trim();

                    dialog.getFormField(Form.CREATEREQUEST)
                            .setEnabled(value.length() > 0 && !newValue.endsWith(".wadl"));
                    dialog.getFormField(Form.GENERATETESTSUITE).setEnabled(newValue.trim().length() > 0);

                    initProjectName(newValue);
                }
            });
        } else {
            dialog.setValue(Form.INITIALWSDL, "");
            dialog.setValue(Form.PROJECTNAME, "");

            dialog.getFormField(Form.CREATEREQUEST).setEnabled(false);
            dialog.getFormField(Form.GENERATETESTSUITE).setEnabled(false);
        }

        if (param instanceof String) {
            dialog.setValue(Form.INITIALWSDL, param.toString());
            initProjectName(param.toString());
        }

        while (dialog.show()) {
            WsdlProject project = null;
            try {
                String projectName = dialog.getValue(Form.PROJECTNAME).trim();
                if (projectName.length() == 0) {
                    UISupport.showErrorMessage(messages.get("MissingProjectNameError"));
                } else {
                    project = workspace.createProject(projectName, null);

                    if (project != null) {
                        UISupport.select(project);
                        String url = dialog.getValue(Form.INITIALWSDL).trim();

                        if (dialog.getBooleanValue(Form.RELATIVEPATHS)) {
                            String folder = workspace.getProjectRoot();

                            if (PathUtils.isFilePath(url) && PathUtils.isAbsolutePath(url)) {
                                folder = new File(url).getParent();
                            }

                            if (project.save(folder) != SaveStatus.SUCCESS) {
                                UISupport
                                        .showErrorMessage("Project was not saved, paths will not be stored relatively until configured.");
                            } else {
                                project.setResourceRoot("${projectDir}");
                            }
                        }

                        if (url.length() > 0) {
                            if (new File(url).exists()) {
                                url = new File(url).toURI().toURL().toString();
                            }

                            importWsdl(project, url);
                        }
                        Analytics.trackAction(SoapUIActions.CREATE_SOAP_PROJECT.getActionName());

                        break;
                    }
                }
            } catch (InvalidDefinitionException ex) {
                ex.show();
            } catch (Exception ex) {
                UISupport.showErrorMessage(ex);
                if (project != null) {
                    workspace.removeProject(project);
                }
            }
        }
    }

    public void initProjectName(String newValue) {
        if (StringUtils.isNullOrEmpty(dialog.getValue(Form.PROJECTNAME)) && StringUtils.hasContent(newValue)) {
            int ix = newValue.lastIndexOf('.');
            if (ix > 0) {
                newValue = newValue.substring(0, ix);
            }

            ix = newValue.lastIndexOf('/');
            if (ix == -1) {
                ix = newValue.lastIndexOf('\\');
            }

            if (ix != -1) {
                dialog.setValue(Form.PROJECTNAME, newValue.substring(ix + 1));
            }
        }
    }

    private void importWsdl(WsdlProject project, String url) throws SoapUIException {
        WsdlInterface[] results = WsdlInterfaceFactory.importWsdl(project, url, dialog.getValue(Form.CREATEREQUEST)
                .equals("true"));
        for (WsdlInterface iface : results) {
            UISupport.select(iface);

            if (dialog.getBooleanValue(Form.GENERATETESTSUITE)) {
                GenerateWsdlTestSuiteAction generateTestSuiteAction = new GenerateWsdlTestSuiteAction();
                generateTestSuiteAction.generateTestSuite(iface, true);
            }

        }
    }

    @AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    public interface Form {
        @AField(description = "Form.ProjectName.Description", type = AFieldType.STRING)
        public final static String PROJECTNAME = messages.get("Form.ProjectName.Label");

        @AField(description = "Form.InitialWsdl.Description", type = AFieldType.FILE)
        public final static String INITIALWSDL = messages.get("Form.InitialWsdl.Label");

        @AField(description = "Form.CreateRequests.Description", type = AFieldType.BOOLEAN, enabled = false)
        public final static String CREATEREQUEST = messages.get("Form.CreateRequests.Label");

        @AField(description = "Form.GenerateTestSuite.Description", type = AFieldType.BOOLEAN, enabled = false)
        public final static String GENERATETESTSUITE = messages.get("Form.GenerateTestSuite.Label");

        @AField(description = "Form.RelativePaths.Description", type = AFieldType.BOOLEAN, enabled = true)
        public final static String RELATIVEPATHS = messages.get("Form.RelativePaths.Label");

    }
}
