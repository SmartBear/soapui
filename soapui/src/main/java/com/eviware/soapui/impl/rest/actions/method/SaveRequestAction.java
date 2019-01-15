package com.eviware.soapui.impl.rest.actions.method;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.MessageSupport;
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

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;


public class SaveRequestAction {
    public static final MessageSupport messages = MessageSupport.getMessages(SaveRequestAction.class);
    private static final String NEW_PROJECT_OPTION = "[new project]";
    private XFormDialog dialog;
    private ProjectListItem selectedProject;

    public SaveRequestAction() {
    }

    public void showNewRestRequestDialog(HashMap<String, Object> context) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(SaveRequestAction.Form.class);
        } else {
            dialog.setValue(SaveRequestAction.Form.RESOURCENAME, "");
        }
        dialog.getFormField(Form.PROJECTS).setProperty("component", getProjectListComponent());
        dialog.getFormField(Form.PROJECTS).setProperty("preferredSize", new Dimension(400, 100));
        dialog.setSize(520, 300);

        if (dialog.show()) {
            String methodName = dialog.getValue(SaveRequestAction.Form.RESOURCENAME);

            RestRequest request = addRequest(context, methodName);

            UISupport.select(request);
            if (dialog.getBooleanValue(SaveRequestAction.Form.OPENSREQUEST)) {
                UISupport.showDesktopPanel(request);
            }
        }
    }

    private JPanel getProjectListComponent() {
        ObservableList<ProjectListItem> projects = FXCollections.observableArrayList();
        projects.add(new ProjectListItem(NEW_PROJECT_OPTION, null));
        for (Project project : SoapUI.getWorkspace().getProjectList()) {
            if (project.isOpen()) {
                projects.add(new ProjectListItem(project.getName(), project));
            }
        }

        ListView<ProjectListItem> list = new ListView<>(projects);
        list.setCellFactory(param -> new ListCell<ProjectListItem>() {
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
        list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProjectListItem>() {
            @Override
            public void changed(ObservableValue<? extends ProjectListItem> observable, ProjectListItem oldValue, ProjectListItem newValue) {
                selectedProject = newValue;
            }
        });

        Scene scene = new Scene(list);
        JFXPanel panel = new JFXPanel();
        Platform.runLater(() -> {
            panel.setScene(scene);
        });
        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        return container;
    }

    private RestRequest addRequest(HashMap<String, Object> context, String methodName) {
        return null;
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
