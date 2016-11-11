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
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.RunnerContext;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolRunner;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.support.UISupport;
import org.apache.commons.cli.CommandLine;

import java.io.File;

/**
 * Standalone tool-runner used from maven-plugin, can also be used from
 * command-line (see xdocs) or directly from other classes.
 * <p>
 * For standalone usage, set the project file (with setProjectFile) and other
 * desired properties before calling run
 * </p>
 *
 * @author Ole.Matzura
 * @author <a href="mailto:nenadn@eviware.com">Nenad V. Nikolic</a>
 */

public class SoapUIToolRunner extends AbstractSoapUIRunner implements ToolHost, RunnerContext {
    private String iface;
    private String tool;

    private RunnerStatus status;
    private String projectPassword;

    public static String TITLE = "SoapUI " + SoapUI.SOAPUI_VERSION + " Tool Runner";

    /**
     * Runs the specified tool in the specified soapUI project file, see SoapUI
     * xdocs for details.
     *
     * @param args
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {
        System.exit(new SoapUIToolRunner().runFromCommandLine(args));
    }

    /**
     * Sets the tool(s) to run, can be a comma-seperated list
     *
     * @param tool the tools to run
     */

    public void setTool(String tool) {
        this.tool = tool;
    }

    public void setInterface(String iface) {
        this.iface = iface;
    }

    public SoapUIToolRunner() {
        super(TITLE);
    }

    public SoapUIToolRunner(String title) {
        super(title);
    }

    public boolean runRunner() throws Exception {
        UISupport.setToolHost(this);
        String projectFile = getProjectFile();

        if (!new File(projectFile).exists()) {
            throw new Exception("SoapUI project file [" + projectFile + "] not found");
        }

        // WsdlProject project = new WsdlProject( projectFile,
        // getProjectPassword() );
        WsdlProject project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory(WsdlProjectFactory.WSDL_TYPE)
                .createNew(projectFile, getProjectPassword());

        log.info("Running tools [" + tool + "] for interface [" + iface + "] in project [" + project.getName() + "]");

        long startTime = System.nanoTime();

        for (int c = 0; c < project.getInterfaceCount(); c++) {
            Interface i = project.getInterfaceAt(c);
            if (iface == null || i.getName().equals(iface)) {
                runTool(i);
            }
        }

        long timeTaken = (System.nanoTime() - startTime) / 1000000;
        log.info("time taken: " + timeTaken + "ms");

        return true;
    }

    /**
     * Runs the configured tool(s) for the specified interface.
     *
     * @param iface an interface that exposes an invokable operation
     */
    public void runTool(Interface iface) {
        AbstractToolsAction<Interface> action = null;

        String[] tools = tool.split(",");
        for (String toolName : tools) {
            if (toolName == null || toolName.trim().length() == 0) {
                continue;
            }

            action = ToolActionFactory.createToolAction(toolName);
            try {
                if (action != null) {
                    log.info("Running tool [" + toolName + "] for Interface [" + iface.getName() + "]");
                    action.performHeadless(iface, null);
                } else {
                    log.error("Specified tool [" + toolName + "] is unknown or unsupported.");
                }
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
    }

    public void run(ToolRunner runner) throws Exception {
        status = RunnerStatus.RUNNING;
        runner.setContext(this);
        Analytics.trackAction(SoapUIActions.RUN_TOOL_FROM_COMMAND_LINE.getActionName(), "Tool", runner.getName());

        runner.run();
    }

    public RunnerStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return getClass().getSimpleName();
    }

    public String getProjectPassword() {
        return projectPassword;
    }

    public void log(String msg) {
        System.out.print(msg);
    }

    public void logError(String msg) {
        System.err.println(msg);
    }

    public void setStatus(RunnerStatus status) {
        this.status = status;
    }

    public void disposeContext() {
    }

    @Override
    protected SoapUIOptions initCommandLineOptions() {
        SoapUIOptions options = new SoapUIOptions("toolrunner");
        options.addOption("i", true, "Sets the interface");
        options.addOption("t", true, "Sets the tool to run");
        options.addOption("s", true, "Sets the soapui-settings.xml file to use");
        options.addOption("x", true, "Sets project password for decryption if project is encrypted");
        options.addOption("v", true, "Sets password for soapui-settings.xml file");
        options.addOption("f", true, "Sets report output folder");
        options.addOption("D", true, "Sets system property with name=value");
        options.addOption("G", true, "Sets global property with name=value");

        return options;
    }

    @Override
    protected boolean processCommandLine(CommandLine cmd) {
        setTool(cmd.getOptionValue("t"));

        if (cmd.hasOption("i")) {
            setInterface(cmd.getOptionValue("i"));
        }

        if (cmd.hasOption("s")) {
            setSettingsFile(getCommandLineOptionSubstSpace(cmd, "s"));
        }

        if (cmd.hasOption("x")) {
            setProjectPassword(cmd.getOptionValue("x"));
        }

        if (cmd.hasOption("v")) {
            setSoapUISettingsPassword(cmd.getOptionValue("v"));
        }

        if (cmd.hasOption("D")) {
            setSystemProperties(cmd.getOptionValues("D"));
        }

        if (cmd.hasOption("G")) {
            setGlobalProperties(cmd.getOptionValues("G"));
        }

        if (cmd.hasOption("f")) {
            setOutputFolder(cmd.getOptionValue("f"));
        }

        return true;
    }

    public void setProjectPassword(String projectPassword) {
        this.projectPassword = projectPassword;
    }
}
