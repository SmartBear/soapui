/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.config.ProjectConfig;
import com.eviware.soapui.config.SoapuiWorkspaceDocumentConfig;
import com.eviware.soapui.config.WorkspaceProjectConfig;
import com.eviware.soapui.config.WorkspaceProjectConfig.Status;
import com.eviware.soapui.config.WorkspaceProjectConfig.Type;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceListener;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveDialog;
import com.eviware.soapui.support.types.StringToStringMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import javax.swing.ImageIcon;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.eviware.soapui.analytics.SoapUIActions.IMPORT_PROJECT_FROM_HIGHER_VERSION;
import static com.eviware.soapui.analytics.SoapUIActions.IMPORT_PRO_PROJECT;
import static com.eviware.soapui.impl.wsdl.WsdlProject.ProjectEncryptionStatus;
import static com.eviware.soapui.impl.wsdl.WsdlProject.ProjectEncryptionStatus.NOT_ENCRYPTED;

/**
 * Default Workspace implementation
 *
 * @author Ole.Matzura
 */

public class WorkspaceImpl extends AbstractModelItem implements Workspace {
    private final static Logger log = LogManager.getLogger(WorkspaceImpl.class);
    public static final MessageSupport messages = MessageSupport.getMessages(WorkspaceImpl.class);

    private List<Project> projectList = new ArrayList<Project>();
    private SoapuiWorkspaceDocumentConfig workspaceConfig;
    private String path = null;
    private Set<WorkspaceListener> listeners = new HashSet<WorkspaceListener>();
    private ImageIcon workspaceIcon;
    private XmlBeansSettingsImpl settings;
    private StringToStringMap projectOptions;
    private ResolveDialog resolver;

    public WorkspaceImpl(String path, StringToStringMap projectOptions) throws XmlException, IOException {
        if (projectOptions == null) {
            this.projectOptions = new StringToStringMap();
        } else {
            this.projectOptions = projectOptions;
        }
        File file = new File(path);
        this.path = file.getAbsolutePath();
        loadWorkspace(file);
        for (WorkspaceListener listener : SoapUI.getListenerRegistry().getListeners(WorkspaceListener.class)) {
            addWorkspaceListener(listener);
        }
    }

    public void switchWorkspace(File file) throws SoapUIException {
        // check first if valid workspace file
        if (file.exists()) {
            try {
                SoapuiWorkspaceDocumentConfig.Factory.parse(file);
            } catch (Exception e) {
                throw new SoapUIException(messages.get("FailedToLoadWorkspaceException") + e.toString());
            }
        }

        fireWorkspaceSwitching();

        while (projectList.size() > 0) {
            Project project = projectList.remove(0);
            try {
                fireProjectRemoved(project);
            } finally {
                project.release();
            }
        }

        try {
            String oldName = getName();

            loadWorkspace(file);
            this.path = file.getAbsolutePath();

            for (Project project : projectList) {
                fireProjectAdded(project);
            }

            notifyPropertyChanged(ModelItem.NAME_PROPERTY, oldName, getName());
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        fireWorkspaceSwitched();
    }

    public void loadWorkspace(File file) throws XmlException, IOException {
        if (file.exists()) {
            log.info(messages.get("FailedToLoadWorkspaceFrom", file.getAbsolutePath()));
            workspaceConfig = SoapuiWorkspaceDocumentConfig.Factory.parse(file);
            if (workspaceConfig.getSoapuiWorkspace().getSettings() == null) {
                workspaceConfig.getSoapuiWorkspace().addNewSettings();
            }
            setPath(file.getAbsolutePath());
            settings = new XmlBeansSettingsImpl(this, SoapUI.getSettings(), workspaceConfig.getSoapuiWorkspace()
                    .getSettings());

            boolean closeOnStartup = getSettings().getBoolean(UISettings.CLOSE_PROJECTS);
            List<WorkspaceProjectConfig> projects = workspaceConfig.getSoapuiWorkspace().getProjectList();
            for (WorkspaceProjectConfig wsc : projects) {
                String str = PathUtils.denormalizePath(wsc.getStringValue());

                str = PathUtils.adjustRelativePath(str, getProjectRoot(), this);

                try {
                    WsdlProject project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(str,
                            this, !closeOnStartup && wsc.getStatus() != Status.CLOSED && wsc.getType() != Type.REMOTE,
                            wsc.getName(), null);

                    projectList.add(project);
                } catch (Exception e) {
                    UISupport.showErrorMessage(messages.get("FailedToLoadProjectInWorkspace", str) + e.getMessage());

                    SoapUI.logError(e);
                }
            }
            ensureProjectsCompatibility(projectList);
        } else {
            workspaceConfig = SoapuiWorkspaceDocumentConfig.Factory.newInstance();
            workspaceConfig.addNewSoapuiWorkspace().setName(messages.get("DefaultWorkspaceName"));
            workspaceConfig.getSoapuiWorkspace().addNewSettings();

            settings = new XmlBeansSettingsImpl(this, SoapUI.getSettings(), workspaceConfig.getSoapuiWorkspace()
                    .getSettings());
        }
    }

    private void ensureProjectsCompatibility(List<Project> projects) {
        List<String> newerProjectsList = new ArrayList<>();
        List<String> readyProjectsList = new ArrayList<>();
        for (Project project : projects) {
            if (project instanceof WsdlProject) {
                if (((WsdlProject) project).isFromReadyApi()) {
                    ProjectConfig config = ((WsdlProject) project).getProjectDocument().getSoapuiProject();
                    String version = StringUtils.isNullOrEmpty(config.getUpdated()) ? "" : StringUtils.getSubstringBeforeFirstWhitespace(config.getUpdated());
                    Analytics.trackAction(IMPORT_PRO_PROJECT, "project_version", StringUtils.hasContent(version) ? version : "UNDEFINED");
                    readyProjectsList.add(
                            messages.get(
                                    "Compatibility.with.ReadyAPI.one.project",
                                    config.getName(),
                                    StringUtils.hasContent(version) ? "(" + version + ")" : ""));
                } else if (((WsdlProject) project).isFromNewerVersion()) {
                    ProjectConfig config = ((WsdlProject) project).getProjectDocument().getSoapuiProject();
                    String version = config.getSoapuiVersion();
                    Analytics.trackAction(IMPORT_PROJECT_FROM_HIGHER_VERSION, "project_version", version);
                    newerProjectsList.add(
                            messages.get(
                                    "Compatibility.with.SoapUI.one.project",
                                    config.getName(),
                                    version,
                                    SoapUI.PRODUCT_NAME));
                }
            }
        }
        String message = messages.get(
                "WorkspaceImpl.Compatibility.Text",
                SoapUI.PRODUCT_NAME,
                SoapUI.SOAPUI_VERSION);
        if (!readyProjectsList.isEmpty()) {
            UISupport.showInfoMessage(String.join("\r\n", readyProjectsList) + message,
                    messages.get("Compatibility.with.ReadyAPI.Title"));
        }
        if (!newerProjectsList.isEmpty()) {
            UISupport.showInfoMessage(String.join("\r\n", newerProjectsList) + message,
                    messages.get("Compatibility.with.SoapUI.Title"));
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Project> getProjects() {
        Map<String, Project> result = new HashMap<String, Project>();

        for (Project project : projectList) {
            result.put(project.getName(), project);
        }

        return result;
    }

    public void setName(String name) {
        String oldName = getName();

        workspaceConfig.getSoapuiWorkspace().setName(name);
        notifyPropertyChanged(ModelItem.NAME_PROPERTY, oldName, name);
    }

    public void setDescription(String description) {
        String oldDescription = getDescription();

        workspaceConfig.getSoapuiWorkspace().setDescription(description);
        notifyPropertyChanged(ModelItem.DESCRIPTION_PROPERTY, oldDescription, description);
    }

    public String getName() {
        return workspaceConfig.getSoapuiWorkspace().isSetName() ? workspaceConfig.getSoapuiWorkspace().getName()
                : messages.get("DefaultWorkspaceName");
    }

    public Project getProjectAt(int index) {
        return projectList.get(index);
    }

    public Project getProjectByName(String projectName) {
        for (Project project : projectList) {
            if (project.getName().equals(projectName)) {
                return project;
            }
        }

        return null;
    }

    public int getProjectCount() {
        return projectList.size();
    }

    public SaveStatus onClose() {
        return save(!getSettings().getBoolean(UISettings.AUTO_SAVE_PROJECTS_ON_EXIT));
    }

    public SaveStatus save(boolean workspaceOnly) {
        return save(workspaceOnly, false);
    }

    public SaveStatus save(boolean saveWorkspaceOnly, boolean skipProjectsWithRunningTests) {
        try {
            // not saved?
            if (path == null) {
                File file = UISupport.getFileDialogs().saveAs(this, messages.get("SaveWorkspace.Title"), ".xml",
                        "XML Files (*.xml)", null);
                if (file == null) {
                    return SaveStatus.CANCELLED;
                }

                path = file.getAbsolutePath();
            }

            List<WorkspaceProjectConfig> projects = new ArrayList<WorkspaceProjectConfig>();

            // save projects first
            for (int c = 0; c < getProjectCount(); c++) {
                WsdlProject project = (WsdlProject) getProjectAt(c);

                if (!saveWorkspaceOnly) {
                    SaveStatus status = saveProject(skipProjectsWithRunningTests, project);

                    if (status == SaveStatus.CANCELLED || status == SaveStatus.FAILED) {
                        return status;
                    }
                }
                saveWorkspaceProjectConfig(projects, project);
            }

            saveWorkspaceConfig(projects);
        } catch (IOException e) {
            log.error(messages.get("FailedToSaveWorkspace.Error") + e.getMessage(), e); //$NON-NLS-1$
            return SaveStatus.FAILED;
        }
        return SaveStatus.SUCCESS;
    }

    private void saveWorkspaceConfig(List<WorkspaceProjectConfig> projects) throws IOException {
        workspaceConfig.getSoapuiWorkspace().setProjectArray(
                projects.toArray(new WorkspaceProjectConfig[projects.size()]));
        workspaceConfig.getSoapuiWorkspace().setSoapuiVersion(SoapUI.SOAPUI_VERSION);

        File workspaceFile = new File(path);
        workspaceConfig.save(workspaceFile, new XmlOptions().setSavePrettyPrint());

        log.info(messages.get("SavedWorkspace.Info", workspaceFile.getAbsolutePath())); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void saveWorkspaceProjectConfig(List<WorkspaceProjectConfig> projects, WsdlProject project) {
        String path = project.getPath();
        if (path != null) {
            path = PathUtils.createRelativePath(path, getProjectRoot(), this);

            WorkspaceProjectConfig wpc = WorkspaceProjectConfig.Factory.newInstance();
            wpc.setStringValue(PathUtils.normalizePath(path));
            if (project.isRemote()) {
                wpc.setType(Type.REMOTE);
            }

            if (!project.isOpen()) {
                if (project.getEncryptionStatus() == NOT_ENCRYPTED) {
                    wpc.setStatus(Status.CLOSED);
                } else {
                    wpc.setStatus(Status.CLOSED_AND_ENCRYPTED);
                }
            }

            wpc.setName(project.getName());
            projects.add(wpc);
        }
    }

    private SaveStatus saveProject(boolean skipProjectsWithRunningTests, WsdlProject project) throws IOException {
        if (skipProjectsWithRunningTests && SoapUI.getTestMonitor().hasRunningTests(project)) {
            log.warn(messages.get("ProjectHasRunningTests.Warning", project.getName()));
        } else {
            if (!StringUtils.hasContent(project.getPath())) {
                Boolean shouldSave = UISupport.confirmOrCancel(messages.get("ProjectHasNotBeenSaved.Label", project.getName()),
                        messages.get("ProjectHasNotBeenSaved.Title"));

                if (shouldSave == null) {
                    return SaveStatus.CANCELLED;
                }

                if (shouldSave) {
                    return project.save();
                } else {
                    return SaveStatus.DONT_SAVE;
                }
            } else {
                return project.save();
            }
        }
        return SaveStatus.SUCCESS;
    }

    public void addWorkspaceListener(WorkspaceListener listener) {
        listeners.add(listener);
    }

    public void removeWorkspaceListener(WorkspaceListener listener) {
        listeners.remove(listener);
    }

    public Project importProject(String fileName) throws SoapUIException {
        File projectFile = new File(fileName);
        WsdlProject project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(
                projectFile.getAbsolutePath(), this);

        ensureProjectsCompatibility(Arrays.asList(project));

        afterProjectImport(project);

        return project;
    }

    @Override
    public Project importProject(InputStream inputStream) {
        WsdlProject project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(inputStream, this);

        ensureProjectsCompatibility(Arrays.asList(project));

        afterProjectImport(project);

        return project;
    }

    public void resolveProject(WsdlProject project) {
        if (resolver == null) {
            resolver = new ResolveDialog("Resolve Project", "Resolve imported project", null);
            resolver.setShowOkMessage(false);
        }

        resolver.resolve(project);
    }

    public WsdlProject createProject(String name) throws SoapUIException {
        File projectFile = new File(createProjectFileName(name));
        File file = UISupport.getFileDialogs().saveAs(this, messages.get("CreateProject.Title"), ".xml",
                "XML Files (*.xml)", projectFile);
        if (file == null) {
            return null;
        }

        return createProject(name, file);
    }

    public WsdlProject createProject(String name, File file) throws SoapUIException {
        File projectFile = file;
        while (projectFile != null && projectFile.exists()) {
            Boolean result = Boolean.FALSE;
            while (!result) {
                result = UISupport.confirmOrCancel(messages.get("OverwriteProject.Label"),
                        messages.get("OverwriteProject.Title"));
                if (result == null) {
                    return null;
                }
                if (result) {
                    projectFile.delete();
                } else {
                    projectFile = UISupport.getFileDialogs().saveAs(this, messages.get("CreateProject.Title"), ".xml",
                            "XML Files (*.xml)", projectFile); //$NON-NLS-1$
                    if (projectFile != null) {
                        break;
                    } else {
                        return null;
                    }
                }
            }
        }

        WsdlProject project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory(WsdlProjectFactory.WSDL_TYPE)
                .createNew((String) null, this);

        project.setName(name);
        projectList.add(project);

        fireProjectAdded(project);

        try {
            if (projectFile != null) {
                project.saveAs(projectFile.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error(messages.get("FailedToSaveProject.Error") + e.getMessage(), e);
        }

        return project;
    }

    private void afterProjectImport(WsdlProject project) {
        projectList.add(project);
        fireProjectAdded(project);

        resolveProject(project);

        save(true);
    }

    private void fireProjectOpened(Project project) {
        for (WorkspaceListener listener : listeners) {
            listener.projectOpened(project);
        }
    }

    private void fireProjectClosed(Project project) {
        for (WorkspaceListener listener : listeners) {
            listener.projectClosed(project);
        }
    }

    private void fireProjectAdded(Project project) {
        for (WorkspaceListener listener : listeners) {
            listener.projectAdded(project);
        }
    }

    private void fireWorkspaceSwitching() {
        for (WorkspaceListener listener : listeners) {
            listener.workspaceSwitching(this);
        }
    }

    private void fireWorkspaceSwitched() {
        for (WorkspaceListener listener : listeners) {
            listener.workspaceSwitched(this);
        }
    }

    private String createProjectFileName(String name) {
        return name + "-soapui-project.xml"; //$NON-NLS-1$
    }

    public void removeProject(Project project) {
        int ix = projectList.indexOf(project);
        if (ix == -1) {
            throw new RuntimeException("Project [" + project.getName() + "] not available in workspace for removal");
        }

        projectList.remove(ix);

        try {
            fireProjectRemoved(project);
        } finally {
            project.release();
        }
    }

    public Project reloadProject(Project project) throws SoapUIException {
        int ix = projectList.indexOf(project);
        if (ix == -1) {
            throw new RuntimeException("Project [" + project.getName() //$NON-NLS-1$
                    + "] not available in workspace for reload"); //$NON-NLS-1$
        }

        projectList.remove(ix);
        fireProjectRemoved(project);

        String tempName = project.getName();
        project.release();
        project = ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(project.getPath(), this,
                true, tempName, null);
        projectList.add(ix, project);

        fireProjectAdded(project);
        fireProjectOpened(project);

        return project;
    }

    private void fireProjectRemoved(Project project) {
        for (WorkspaceListener listener : listeners) {
            listener.projectRemoved(project);
        }
    }

    public ImageIcon getIcon() {
        return workspaceIcon;
    }

    public Settings getSettings() {
        return settings;
    }

    public int getIndexOfProject(Project project) {
        return projectList.indexOf(project);
    }

    public String getPath() {
        return path;
    }

    public String getProjectRoot() {
        return workspaceConfig.getSoapuiWorkspace().getProjectRoot();
    }

    public void setProjectRoot(String workspaceRoot) {
        workspaceConfig.getSoapuiWorkspace().setProjectRoot(workspaceRoot);
    }

    public void release() {
        settings.release();

        for (Project project : projectList) {
            project.release();
        }
    }

    public List<? extends Project> getProjectList() {
        return projectList;
    }

    public String getDescription() {
        return workspaceConfig.getSoapuiWorkspace().getDescription();
    }

    public WsdlProject importRemoteProject(String url) throws SoapUIException {
        WsdlProject project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(url, this);

        ensureProjectsCompatibility(Arrays.asList(project));

        afterProjectImport(project);

        return project;
    }

    public void closeProject(Project project) {
        ProjectEncryptionStatus oldProjectEncrypt = ((WsdlProject) project).getEncryptionStatus();
        int ix = projectList.indexOf(project);
        if (ix == -1) {
            throw new RuntimeException("Project [" + project.getName() + "] not available in workspace for close");
        }

        projectList.remove(ix);
        fireProjectRemoved(project);
        fireProjectClosed(project);

        String name = project.getName();
        project.release();

        try {
            project = ProjectFactoryRegistry.getProjectFactory(WsdlProjectFactory.WSDL_TYPE).createNew(
                    project.getPath(), this, false, name, null);
            ((WsdlProject) project).setEncryptionStatus(oldProjectEncrypt);
            projectList.add(ix, project);
            fireProjectAdded(project);
        } catch (Exception e) {
            UISupport.showErrorMessage(messages.get("FailedToCloseProject.Error", name) + e.getMessage());
            SoapUI.logError(e);
        }
    }

    public List<Project> getOpenProjectList() {
        List<Project> availableProjects = new ArrayList<Project>();

        for (Project project : projectList) {
            if (project.isOpen()) {
                availableProjects.add(project);
            }
        }

        return availableProjects;
    }

    public Project openProject(Project project) throws SoapUIException {
        return reloadProject(project);
    }

    public String getId() {
        return String.valueOf(hashCode());
    }

    public List<? extends ModelItem> getChildren() {
        return getProjectList();
    }

    public ModelItem getParent() {
        return null;
    }

    public void inspectProjects() {
        for (Project project : projectList) {
            if (project.isOpen()) {
                project.inspect();
            }
        }
    }

    public String getProjectPassword(String name) {
        return projectOptions.get(name);
    }

    public void clearProjectPassword(String name) {
        projectOptions.remove(name);
    }

    public boolean isSupportInformationDialog() {
        boolean isCollect = false;
        if (workspaceConfig != null) {
            if (!workspaceConfig.getSoapuiWorkspace().isSetCollectInfoForSupport()) {
                return true;
            }
            isCollect = workspaceConfig.getSoapuiWorkspace().getCollectInfoForSupport();
        }
        return isCollect;
    }

    public void setSupportInformationDialog(boolean value) {
        if (workspaceConfig != null) {
            workspaceConfig.getSoapuiWorkspace().setCollectInfoForSupport(value);
        }
    }
}
