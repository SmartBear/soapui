package com.eviware.soapui.impl.rest.actions.method;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.RestServiceBuilder;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.explorer.RequestInspectionData;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveRequestAction extends AbstractAction {
    private static final MessageSupport messages = MessageSupport.getMessages(SaveRequestAction.class);
    private static final String NEW_PROJECT_OPTION = "[new project]";
    private static final String DEFAULT_PROJECT_NAME = "Project";
    private static final int DIALOG_WIDTH = 520;
    private static final int DIALOG_HEIGHT = 300;
    private static final Dimension PROJECTS_FORM_SIZE = new Dimension(400, 100);
    private XFormDialog dialog;
    private ProjectListItem selectedProject;
    private Map<String, Object> context;

    public SaveRequestAction(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showNewRestRequestDialog();
    }

    public void showNewRestRequestDialog() {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(SaveRequestAction.Form.class);
        } else {
            dialog.setValue(SaveRequestAction.Form.RESOURCENAME, "");
        }
        dialog.getFormField(Form.PROJECTS).setProperty("component", getProjectListComponent());
        dialog.getFormField(Form.PROJECTS).setProperty("preferredSize", PROJECTS_FORM_SIZE);
        dialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        if (dialog.show()) {
            String requestName = dialog.getValue(SaveRequestAction.Form.RESOURCENAME);
            RestRequest request = addRequest(context, requestName);
            if (dialog.getBooleanValue(SaveRequestAction.Form.OPENSREQUEST)) {
                UISupport.select(request);
                UISupport.showDesktopPanel(request);
            }
        }
    }

    private JPanel getProjectListComponent() {
        /*ObservableList<ProjectListItem> projects = FXCollections.observableArrayList();
        projects.add(new ProjectListItem(NEW_PROJECT_OPTION, null));
        for (Project project : SoapUI.getWorkspace().getProjectList()) {
            if (project.isOpen()) {
                projects.add(new ProjectListItem(project.getName(), project));
            }
        }

        ListView<ProjectListItem> projectsListView = new ListView<>(projects);
        projectsListView.setCellFactory(param -> new ListCell<ProjectListItem>() {
            @Override
            protected void updateItem(ProjectListItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getDisplayValue() == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayValue());
                }
            }
        });
        projectsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProjectListItem>() {
            @Override
            public void changed(ObservableValue<? extends ProjectListItem> observable, ProjectListItem oldValue, ProjectListItem newValue) {
                selectedProject = newValue;
            }
        });

        Scene scene = new Scene(projectsListView);
        JFXPanel panel = new JFXPanel();
        Platform.runLater(() -> {
            panel.setScene(scene);
        });*/
        JPanel container = new JPanel(new BorderLayout());
        //container.add(panel, BorderLayout.CENTER);
        return container;
    }

    private RestRequest addRequest(Map<String, Object> context, String requestName) {
        RestServiceBuilder serviceBuilder = new RestServiceBuilder();
        WsdlProject project = null;
        RestRequest restRequest = null;
        WorkspaceImpl workspace = (WorkspaceImpl) SoapUI.getWorkspace();
        try {
            String url = ((List<String>) context.get("URLs")).get(0);
            RestRequestInterface.HttpMethod method = ((List<RestRequestInterface.HttpMethod>) context.get("Methods")).get(0);
            RequestInspectionData inspectionData = ((List<RequestInspectionData>) context.get("InspectionData")).get(0);
            if (selectedProject.project == null) {
                project = workspace.createProject(ModelItemNamer.createName(DEFAULT_PROJECT_NAME, workspace.getProjectList()), null);
            } else {
                project = (WsdlProject) selectedProject.project;
            }

            if (inspectionData == null) {
                restRequest = serviceBuilder.createRestServiceWithMethod(project, url, method, false, requestName);
            } else {
                restRequest = serviceBuilder.createRestServiceFromInspectionData(project, url, method,
                        inspectionData, false, requestName);
            }
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex.getMessage());
            if (project != null) {
                workspace.removeProject(project);
            }
        }

        return restRequest;
    }

    private class ProjectListItem {
        String displayValue;
        Project project;

        ProjectListItem(String displayValue, Project project) {
            this.project = project;
            this.displayValue = displayValue;
        }

        Project getProject() {
            return project;
        }

        String getDisplayValue() {
            return displayValue;
        }
    }

    @AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWRESTSERVICE_HELP_URL)
    public interface Form {
        @AField(description = "Form.ResourceName.Description", type = AField.AFieldType.STRING)
        public final static String RESOURCENAME = messages.get("Form.ResourceName.Label");

        @AField(description = "Form.OpenRequest.Description", type = AField.AFieldType.BOOLEAN)
        public final static String OPENSREQUEST = messages.get("Form.OpenRequest.Label");

        @AField(description = "Form.Projects.Description", type = AField.AFieldType.COMPONENT)
        public final static String PROJECTS = messages.get("Form.Projects.Label");
    }
}
