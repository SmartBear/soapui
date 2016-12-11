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

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.WadlImporter;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

import java.io.File;
import java.util.List;

/**
 * Action class to create new Generic project.
 *
 * @author Ole.Matzura
 */

public class NewWadlProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "NewWadlProjectAction";
    public static final String DEFAULT_PROJECT_NAME = "REST Project";
    private XFormDialog dialog;

    public static final MessageSupport messages = MessageSupport.getMessages(NewWadlProjectAction.class);

    public NewWadlProjectAction() {
        super(messages.get("Title"), messages.get("Description"));
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
        }

        if (param instanceof String) {
            dialog.setValue(Form.INITIALWADL, param.toString());
        } else {
            dialog.setValue(Form.INITIALWADL, "");
        }

        while (dialog.show()) {
            WsdlProject project = null;
            try {
                String projectName = createProjectName(dialog.getFormField(Form.INITIALWADL).getValue(), workspace.getProjectList());

                if (projectName.length() == 0) {
                    UISupport.showErrorMessage(messages.get("MissingProjectNameError"));
                } else {
                    project = workspace.createProject(projectName, null);

                    if (project != null) {
                        UISupport.select(project);
                        String url = dialog.getValue(Form.INITIALWADL).trim();

                        if (url.length() > 0) {
                            if (new File(url).exists()) {
                                url = new File(url).toURI().toURL().toString();
                            }

                            if (url.toUpperCase().endsWith("WADL")) {
                                importWadl(project, url);
                            }
                        }
                        showDeepestEditor(project);

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

    private void showDeepestEditor(WsdlProject project) {
        ModelItem item = findLeafItem(project);

        if (item != null) {
            UISupport.select(item);
            UISupport.showDesktopPanel(item);
        }
    }

    private ModelItem findLeafItem(ModelItem item) {
        if (item.getChildren().isEmpty()) {
            return item;
        }

        return findLeafItem(item.getChildren().get(0));
    }

    public String createProjectName(String filePath, List<? extends Project> projectList) {
        if (StringUtils.hasContent(filePath)) {
            String projectName = filePath;

            int ix = projectName.lastIndexOf('.');
            if (ix > 0) {
                projectName = projectName.substring(0, ix);
            }

            ix = projectName.lastIndexOf('/');
            if (ix == -1) {
                ix = projectName.lastIndexOf('\\');
            }

            if (ix != -1) {
                projectName = projectName.substring(ix + 1);
            }

            if (!StringUtils.isNullOrEmpty(projectName)) {
                return projectName;
            }
        }
        return ModelItemNamer.createName(DEFAULT_PROJECT_NAME, projectList);
    }

    private void importWadl(WsdlProject project, String url) {
        RestService restService = (RestService) project
                .addNewInterface(project.getName(), RestServiceFactory.REST_TYPE);
        UISupport.select(restService);
        try {
            new WadlImporter(restService).initFromWadl(url);

        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }
    }

    @AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEW_WADL_PROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    public interface Form {
        @AField(description = "Form.InitialWadl.Description", type = AField.AFieldType.FILE)
        public final static String INITIALWADL = messages.get("Form.InitialWadl.Label");
    }
}
