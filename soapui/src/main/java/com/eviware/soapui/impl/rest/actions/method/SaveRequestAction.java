package com.eviware.soapui.impl.rest.actions.method;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.RestServiceBuilder;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.explorer.RequestInspectionData;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.tools.JavaFXTools;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ListView;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
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
    private Project selectedProject;
    private Map<String, Object> context;

    public SaveRequestAction(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showNewRestRequestDialog();
    }

    public boolean showNewRestRequestDialog() {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(SaveRequestAction.Form.class);
        } else {
            dialog.setValue(SaveRequestAction.Form.RESOURCENAME, "");
        }
        dialog.getFormField(Form.PROJECTS).setProperty("component", getProjectListComponent());
        dialog.getFormField(Form.PROJECTS).setProperty("preferredSize", PROJECTS_FORM_SIZE);
        dialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        boolean dialogResult = dialog.show();
        if (dialogResult) {
            String requestName = dialog.getValue(SaveRequestAction.Form.RESOURCENAME);
            RestRequest request = addRequest(context, requestName);
            if (dialog.getBooleanValue(SaveRequestAction.Form.OPENSREQUEST)) {
                UISupport.selectAndShow(request);
            } else {
                //SOAPUIOS-447
                UISupport.select(request.getResource().getService().getProject());
            }
        }
        return dialogResult;
    }

    private JPanel getProjectListComponent() {
        Workspace workspace = SoapUI.getWorkspace();
        ObservableList<String> projectNames = FXCollections.observableArrayList();
        projectNames.add(NEW_PROJECT_OPTION);
        for (Project project : workspace.getProjectList()) {
            if (project.isOpen()) {
                projectNames.add(project.getName());
            }
        }

        ListView<String> projectsListView = new ListView<>(projectNames);
        projectsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                selectedProject = workspace.getProjectByName(newValue);
            }
        });
        Scene scene = new Scene(projectsListView);
        JFXPanel panel = new JFXPanel();
        JavaFXTools.runAndWait(new Runnable() {
            @Override
            public void run() {
                panel.setScene(scene);
            }
        });
        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        return container;
    }

    private RestRequest addRequest(Map<String, Object> context, String requestName) {
        RestServiceBuilder serviceBuilder = new RestServiceBuilder();
        WsdlProject project = null;
        RestRequest restRequest = null;
        WorkspaceImpl workspace = (WorkspaceImpl) SoapUI.getWorkspace();
        List<String> urls = (List<String>) context.get("URLs");
        List<RestRequestInterface.HttpMethod> methods = (List<RestRequestInterface.HttpMethod>) context.get("Methods");
        List<RequestInspectionData> inspectionDataList = (List<RequestInspectionData>) context.get("InspectionData");
        try {
            String url = urls == null ? null : urls.get(0);
            RestRequestInterface.HttpMethod method = methods == null ? null : methods.get(0);
            RequestInspectionData inspectionData = inspectionDataList == null ? null : inspectionDataList.get(0);
            if (selectedProject == null) {
                project = workspace.createProject(ModelItemNamer.createName(DEFAULT_PROJECT_NAME, workspace.getProjectList()), null);
            } else {
                project = (WsdlProject) selectedProject;
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

    @AForm(name = "Form.Title", description = "Form.Description")
    public interface Form {
        @AField(description = "Form.ResourceName.Description", type = AField.AFieldType.STRING)
        public final static String RESOURCENAME = messages.get("Form.ResourceName.Label");

        @AField(description = "Form.OpenRequest.Description", type = AField.AFieldType.BOOLEAN)
        public final static String OPENSREQUEST = messages.get("Form.OpenRequest.Label");

        @AField(description = "Form.Projects.Description", type = AField.AFieldType.COMPONENT)
        public final static String PROJECTS = messages.get("Form.Projects.Label");
    }
}
