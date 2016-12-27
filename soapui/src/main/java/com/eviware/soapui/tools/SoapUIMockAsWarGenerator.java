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

package com.eviware.soapui.tools;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.settings.ProjectSettings;
import com.eviware.soapui.support.StringUtils;
import org.apache.commons.cli.CommandLine;

import java.io.File;

public class SoapUIMockAsWarGenerator extends AbstractSoapUIRunner {
    public static String TITLE = "SoapUI " + SoapUI.SOAPUI_VERSION + " War Generator";

    private boolean includeActions;
    private boolean includeListeners;
    private boolean includeLibraries;
    private boolean enableWebUI;
    private String localEndpoint;
    private String warFile;

    public SoapUIMockAsWarGenerator() {
        super(TITLE);
    }

    public SoapUIMockAsWarGenerator(String title) {
        super(title);
    }

    /**
     * Runs the specified tool in the specified soapUI project file, see SoapUI
     * xdocs for details.
     *
     * @param args
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {
        System.exit(new SoapUIMockAsWarGenerator().runFromCommandLine(args));
    }

    @Override
    protected boolean processCommandLine(CommandLine cmd) {
        setEnableWebUI(cmd.hasOption('w'));
        setIncludeActions(cmd.hasOption('a'));
        setIncludeLibraries(cmd.hasOption('x'));
        setIncludeListeners(cmd.hasOption('l'));

        if (cmd.hasOption("s")) {
            setSettingsFile(getCommandLineOptionSubstSpace(cmd, "s"));
        }

        if (cmd.hasOption("p")) {
            setProjectPassword(cmd.getOptionValue("p"));
        }

        if (cmd.hasOption("v")) {
            setSoapUISettingsPassword(cmd.getOptionValue("v"));
        }

        if (cmd.hasOption("d")) {
            setOutputFolder(cmd.getOptionValue("d"));
        }

        if (cmd.hasOption("f")) {
            setWarFile(cmd.getOptionValue("f"));
        }

        if (cmd.hasOption("e")) {
            setLocalEndpoint(cmd.getOptionValue("e"));
        }

        return true;
    }

    @Override
    protected SoapUIOptions initCommandLineOptions() {
        SoapUIOptions options = new SoapUIOptions("wargenerator");
        options.addOption("x", true, "Specify if libraries in ext folder should be included");
        options.addOption("a", true, "Specify if custom actions should be included");
        options.addOption("l", true, "Specify if custom listeners should be included");
        options.addOption("w", true, "Specify if web UI should be enabled");
        options.addOption("e", true, "Set the local endpoint of the MockService");
        options.addOption("f", true, "Specify the name of the generated WAR file");
        options.addOption("d", true, "Sets the local folder to use for war generation");
        options.addOption("s", true, "Sets the soapui-settings.xml file to use");
        options.addOption("p", true, "Sets project password for decryption if project is encrypted");
        options.addOption("v", true, "Sets password for soapui-settings.xml file");

        return options;
    }

    @Override
    protected boolean runRunner() throws Exception {
        WsdlProject project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(
                getProjectFile(), getProjectPassword());

        String pFile = getProjectFile();

        project.getSettings().setString(ProjectSettings.SHADOW_PASSWORD, null);

        File tmpProjectFile = new File(System.getProperty("java.io.tmpdir"));
        tmpProjectFile = new File(tmpProjectFile, project.getName() + "-project.xml");

        project.beforeSave();
        project.saveIn(tmpProjectFile);

        pFile = tmpProjectFile.getAbsolutePath();

        String endpoint = StringUtils.hasContent(localEndpoint) ? localEndpoint : project.getName();

        log.info("Creating WAR file with endpoint [" + endpoint + "]");

        MockAsWar mockAsWar = new MockAsWar(pFile, getSettingsFile(), getOutputFolder(), warFile, includeLibraries,
                includeActions, includeListeners, endpoint, enableWebUI, project);

        mockAsWar.createMockAsWarArchive();
        log.info("WAR Generation complete");
        return true;
    }

    public boolean isIncludeActions() {
        return includeActions;
    }

    public void setIncludeActions(boolean includeActions) {
        this.includeActions = includeActions;
    }

    public boolean isIncludeListeners() {
        return includeListeners;
    }

    public void setIncludeListeners(boolean includeListeners) {
        this.includeListeners = includeListeners;
    }

    public boolean isIncludeLibraries() {
        return includeLibraries;
    }

    public void setIncludeLibraries(boolean includeLibraries) {
        this.includeLibraries = includeLibraries;
    }

    public boolean isEnableWebUI() {
        return enableWebUI;
    }

    public void setEnableWebUI(boolean enableWebUI) {
        this.enableWebUI = enableWebUI;
    }

    public String getLocalEndpoint() {
        return localEndpoint;
    }

    public void setLocalEndpoint(String localEndpoint) {
        this.localEndpoint = localEndpoint;
    }

    public String getWarFile() {
        return warFile;
    }

    public void setWarFile(String warFile) {
        this.warFile = warFile;
    }
}
