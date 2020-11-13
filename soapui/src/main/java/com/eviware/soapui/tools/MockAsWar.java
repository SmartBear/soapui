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

package com.eviware.soapui.tools;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker.WorkerAdapter;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MockAsWar {
    protected static final String SOAPUI_SETTINGS = "[SoapUISettings]";
    protected static final String PROJECT_FILE_NAME = "[ProjectFileName]";
    protected static final String MOCKSERVICE_ENDPOINT = "[mockServiceEndpoint]";

    private static final String SOAPUI_HOME = "soapui.home";
    private static final String SOAPUI_BIN_FOLDER = "." + File.separator + "bin";
    private static final String SOAPUI_LIB_FOLDER = ".." + File.separator + "lib";

    protected File projectFile;
    protected File settingsFile;
    protected File warDir;
    private File warFile;
    protected File webInf;
    private File warLibDir;
    protected File soapUIDir;

    protected Logger log = LogManager.getLogger(MockAsWar.class);

    private boolean includeExt;
    protected boolean includeActions;
    protected boolean includeListeners;
    private File actionsDir;
    private File listenersDir;
    protected final String localEndpoint;
    protected boolean enableWebUI;

    private WsdlProject project;

    public MockAsWar(String projectPath, String settingsPath, String warDir, String warFile, boolean includeExt,
                     boolean actions, boolean listeners, String localEndpoint, boolean enableWebUI, WsdlProject project) {
        this.project = project;
        this.localEndpoint = localEndpoint;
        this.projectFile = new File(projectPath);
        this.settingsFile = StringUtils.hasContent(settingsPath) ? new File(settingsPath) : null;
        this.warDir = StringUtils.hasContent(warDir) ? new File(warDir) : new File(
                System.getProperty("java.io.tmpdir"), "warasmock");
        if (!this.warDir.exists()) {
            this.warDir.mkdirs();
        }
        this.warFile = !StringUtils.hasContent(warFile) ? null : new File(warFile);
        if (!warFile.contains(File.separator)) {
            this.warFile = new File(this.warDir, warFile);
        }
        this.includeExt = includeExt;
        this.includeActions = actions;
        this.includeListeners = listeners;
        this.enableWebUI = enableWebUI;
    }

    public void createMockAsWarArchive() {

        XProgressDialog progressDialog = UISupport.getDialogs().createProgressDialog("Creating War File", 3,
                "Building war file..", false);
        WorkerAdapter warWorker = new WorkerAdapter() {

            public Object construct(XProgressMonitor monitor) {
                if (prepareWarFile()) {
                    createWebXml();

                    if (warFile != null) {
                        warFile.getParentFile().mkdirs();
                        ArrayList<File> files = getAllFilesFrom(webInf);
                        files.add(new File(warDir, "stylesheet.css"));
                        files.add(new File(warDir, "header_logo.png"));

                        File[] filez = files.toArray(new File[files.size()]);
                        JarPackager.createJarArchive(warFile, warDir, filez);
                    }
                }
                return null;
            }
        };
        try {
            progressDialog.run(warWorker);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private ArrayList<File> getAllFilesFrom(File dir) {
        ArrayList<File> result = new ArrayList<File>();
        if (dir.isDirectory()) {
            result.addAll(Arrays.asList(dir.listFiles()));
            ArrayList<File> toAdd = new ArrayList<File>();
            for (File f : result) {
                if (f.isDirectory()) {
                    toAdd.addAll(getAllFilesFrom(f));
                }
            }
            result.addAll(toAdd);
        }
        return result;
    }

    protected void createWebXml() {
        URL url = SoapUI.class.getResource("/com/eviware/soapui/resources/mockaswar/web.xml");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine).append("\n");
            }

            createContent(content);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(webInf,
                    "web.xml"))));
            out.write(content.toString());
            out.flush();
            out.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void createContent(StringBuilder content) {
        content.replace(content.indexOf(PROJECT_FILE_NAME),
                content.indexOf(PROJECT_FILE_NAME) + PROJECT_FILE_NAME.length(), projectFile.getName());

        content.replace(
                content.indexOf(SOAPUI_SETTINGS),
                content.indexOf(SOAPUI_SETTINGS) + SOAPUI_SETTINGS.length(),
                settingsFile != null && settingsFile.exists() && settingsFile.isFile() ? "WEB-INF/soapui/"
                        + settingsFile.getName() : "");
        content.replace(content.indexOf(MOCKSERVICE_ENDPOINT), content.indexOf(MOCKSERVICE_ENDPOINT)
                + MOCKSERVICE_ENDPOINT.length(), localEndpoint);

        if (!includeActions) {
            String actionsString = "WEB-INF/actions";
            content.delete(content.indexOf(actionsString), content.indexOf(actionsString) + actionsString.length());
        }
        if (!includeListeners) {
            String listenersString = "WEB-INF/listeners";
            content.delete(content.indexOf(listenersString), content.indexOf(listenersString)
                    + listenersString.length());
        }
        if (!enableWebUI) {
            String webUIEnabled = "<param-value>true</param-value>";
            String webUIDisabled = "<param-value>false</param-value>";
            content.replace(content.indexOf(webUIEnabled),
                    content.indexOf(webUIEnabled) + webUIEnabled.length(),
                    webUIDisabled);
        }
    }

    protected boolean prepareWarFile() {
        // create file system first
        if (createWarFileSystem()) {
            String homePath = System.getProperty(SOAPUI_HOME) == null ? SOAPUI_BIN_FOLDER : System.getProperty(SOAPUI_HOME);

            // copy all from bin/../lib to soapui.home/war/WEB-INF/lib/

            File fromDir = new File(homePath, SOAPUI_LIB_FOLDER);

            JarPackager.copyAllFromTo(fromDir, warLibDir, new CaseInsensitiveFileFilter());

            if (includeExt) {
                String extDirPath = System.getProperty("soapui.ext.libraries");

                fromDir = extDirPath != null ? new File(extDirPath) : new File(new File(homePath), "ext");
                JarPackager.copyAllFromTo(fromDir, warLibDir, null);
            }

            // copy soapui jar to soapui.home/war/WEB-INF/lib/
            String[] mainJar = new File(homePath).list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().startsWith("soapui") && name.toLowerCase().endsWith(".jar");
                }
            });

            fromDir = new File(homePath, mainJar[0]);
            JarPackager.copyFileToDir(fromDir, warLibDir);
            // copy project and settings file to bin/war/WEB-INF/soapui/
            copyProjectFile();
            if (settingsFile != null && settingsFile.exists() && settingsFile.isFile()) {
                JarPackager.copyFileToDir(settingsFile, soapUIDir);
            }

            // actions
            if (includeActions) {
                fromDir = new File(System.getProperty("soapui.ext.actions"));
                JarPackager.copyAllFromTo(fromDir, actionsDir, null);
            }
            // listeners
            if (includeListeners) {
                fromDir = new File(System.getProperty("soapui.ext.listeners"));
                JarPackager.copyAllFromTo(fromDir, listenersDir, null);
            }

            copyWarResource("header_logo.png");
            copyWarResource("stylesheet.css");

            return true;
        }
        return false;
    }

    protected void copyProjectFile() {
        JarPackager.copyFileToDir(projectFile, soapUIDir);
    }

    private void copyWarResource(String resource) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(warDir, resource));
            Tools.writeAll(out,
                    SoapUI.class.getResourceAsStream("/com/eviware/soapui/resources/mockaswar/" + resource));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    protected boolean createWarFileSystem() {
        if (warDir.isDirectory()) {
            log.info("Creating WAR directory in [" + warDir.getAbsolutePath() + "]");
            webInf = new File(warDir, "WEB-INF");
            if (!directoryIsUsable(webInf)) {
                return false;
            } else {
                clearDir(webInf);
                warLibDir = new File(webInf, "lib");

                if (!directoryIsUsable(warLibDir)) {
                    return false;
                }

                soapUIDir = new File(webInf, "soapui");
                if (!directoryIsUsable(soapUIDir)) {
                    return false;
                }
                clearDir(soapUIDir);

                if (includeActions) {
                    actionsDir = new File(webInf, "actions");
                    if (!directoryIsUsable(actionsDir)) {
                        return false;
                    }
                    clearDir(actionsDir);
                }
                if (includeListeners) {
                    listenersDir = new File(webInf, "listeners");
                    if (!directoryIsUsable(listenersDir)) {
                        return false;
                    }
                    clearDir(listenersDir);
                }

                return true;
            }
        } else {
            UISupport.showErrorMessage(warDir.getName() + " needs to be a directory!");
            return false;
        }
    }

    private boolean directoryIsUsable(File dir) {
        if (!(dir.mkdir() || dir.exists())) {
            UISupport.showErrorMessage("Could not create directory " + dir.getAbsolutePath());
            return false;
        }
        return true;
    }

    /**
     * Deletes all files, just files, in directory
     *
     * @param dir
     */
    protected void clearDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }

    protected static class CaseInsensitiveFileFilter implements FileFilter {
        protected static final ArrayList<String> excludes = Lists.newArrayList("servlet", "xulrunner", "Mozilla", "l2fprod", "tuxpack", "winpack", "ActiveQueryBuilder", "jxbrowser", "protection");

        public boolean accept(final File file) {

            boolean pathNameExcluded = FluentIterable.from(excludes).anyMatch(new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String s) {
                    if (file == null || s == null || file.getName().isEmpty()) {
                        return true;
                    }

                    return file.getName().toLowerCase().contains(s.toLowerCase());
                }
            });
            return !pathNameExcluded;
        }
    }
}
