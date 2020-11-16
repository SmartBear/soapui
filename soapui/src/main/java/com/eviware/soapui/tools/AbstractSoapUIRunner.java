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

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.filters.GlobalHttpHeadersRequestFilter;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.smartbear.soapui.core.Logging;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSoapUIRunner implements CmdLineRunner {
    public static final int NORMAL_TERMINATION = 0;
    public static final int ABNORMAL_TERMINATION = -1;

    private boolean groovyLogInitialized;
    private String projectFile;
    protected final Logger log = LogManager.getLogger(getClass());
    private String settingsFile;
    private String soapUISettingsPassword;
    private String projectPassword;

    private boolean enableUI;
    private String outputFolder;
    private String[] projectProperties;
    private Map<String, String> runnerGlobalProperties = new HashMap<String, String>();

    public AbstractSoapUIRunner(String title) {
        if (title != null) {
            System.out.println(title);
        }

        SoapUI.setCmdLineRunner(this);
    }

    protected void initGroovyLog() {
        if (!groovyLogInitialized) {
            ensureConsoleAppenderIsDefined(LogManager.getLogger("groovy.log"));
            groovyLogInitialized = true;
        }
    }

    /**
     * Ensure there is one (and only one) ConsoleAppender instance configured for <code>logger</code>.
     *
     * @param logger
     */
    protected void ensureConsoleAppenderIsDefined(Logger logger) {
        if (logger != null) {
            Map<String, Appender> appenderMap = ((org.apache.logging.log4j.core.Logger) logger).getAppenders();
            for (Map.Entry<String, Appender> appenderEntry : appenderMap.entrySet()) {
                if (appenderEntry.getValue() instanceof ConsoleAppender) {
                    return;
                }
            }
            PatternLayout patternLayout = PatternLayout.newBuilder().withPattern("%d{ABSOLUTE} %-5p [%c{1}] %m%n").build();
            ConsoleAppender consoleAppender = ConsoleAppender.newBuilder().withLayout(patternLayout).build();
            Logging.addAppender(logger.getName(), consoleAppender);
        }
    }

    /**
     * Validates the command line arguments and runs the test runner if vaild
     *
     * @param args the commandline arguments to the runner
     * @return status code to be used with System.exit()
     * @see java.lang.System
     */
    public int runFromCommandLine(String[] args) {
        int results = ABNORMAL_TERMINATION;
        if (validateCommandLineArgument(args)) {
            results = run(args);
        }
        return results;
    }

    public boolean validateCommandLineArgument(String[] args) {
        boolean commandLineArgumentsAreValid = false;
        try {
            commandLineArgumentsAreValid = initFromCommandLine(args, true);
        } catch (Exception e) {
            log.error(e);
            SoapUI.logError(e);
        }
        return commandLineArgumentsAreValid;
    }

    /**
     * Runs the testrunner
     *
     * @param args the command line arguments to be passed to the testrunner
     * @return status code to be used with System.exit()
     * @see java.lang.System
     */
    public int run(String[] args) {
        try {
            if (run()) {
                return NORMAL_TERMINATION;
            }
        } catch (Exception e) {
            log.error(e);
            SoapUI.logError(e);
        }
        return ABNORMAL_TERMINATION;
    }

    public boolean initFromCommandLine(String[] args, boolean printHelp) throws Exception {
        SoapUIOptions options = initCommandLineOptions();

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);


        if (requiresProjectArgument(cmd)) {
            args = cmd.getArgs();

            if (args.length != 1) {
                if (printHelp) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp(options.getRunnerName() + " [options] <soapui-project-file>", options);
                }

                System.err.println("Missing SoapUI project file..");
                return false;
            }

            setProjectFile(args[0]);
        }

        return processCommandLine(cmd);
    }

    /**
     * Checks if the command line arguments require a project file
     *
     * @param cmd The command line
     * @return true as default
     */
    protected boolean requiresProjectArgument(CommandLine cmd) {
        return true;
    }

    /**
     * Main method to use for running the configured tests. Call after setting
     * properties, etc as desired.
     *
     * @return true if execution should be blocked
     * @throws Exception if an error or failure occurs during test execution
     */

    public final boolean run() throws Exception {
        if (SoapUI.getSoapUICore() == null) {
            SoapUI.setSoapUICore(createSoapUICore(), true);
            SoapUI.initGCTimer();
        }
        for (String name : runnerGlobalProperties.keySet()) {
            PropertyExpansionUtils.getGlobalProperties().setPropertyValue(name, runnerGlobalProperties.get(name));
        }

        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

        try {
            return runRunner();
        } finally {
            state.restore();
        }
    }

    protected SoapUICore createSoapUICore() {
        if (enableUI) {
            StandaloneSoapUICore core = new StandaloneSoapUICore(settingsFile);
            log.info("Enabling UI Components");
            core.prepareUI();
            UISupport.setMainFrame(null);
            return core;
        } else {
            return new DefaultSoapUICore(null, settingsFile, soapUISettingsPassword);
        }
    }

    protected abstract boolean processCommandLine(CommandLine cmd);

    protected abstract SoapUIOptions initCommandLineOptions();

    protected abstract boolean runRunner() throws Exception;

    protected String getCommandLineOptionSubstSpace(CommandLine cmd, String key) {
        return cmd.getOptionValue(key).replaceAll("%20", " ");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.tools.CmdLineRunner#getProjectFile()
     */
    @Override
    public String getProjectFile() {
        return projectFile;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.tools.CmdLineRunner#getSettingsFile()
     */
    @Override
    public String getSettingsFile() {
        return settingsFile;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.tools.CmdLineRunner#getOutputFolder()
     */
    @Override
    public String getOutputFolder() {
        return this.outputFolder;
    }

    public String getAbsoluteOutputFolder(ModelItem modelItem) {
        String folder = PropertyExpander.expandProperties(modelItem, outputFolder);

        if (StringUtils.isNullOrEmpty(folder)) {
            folder = PathUtils.getExpandedResourceRoot(modelItem);
        } else if (PathUtils.isRelativePath(folder)) {
            folder = PathUtils.resolveResourcePath(folder, modelItem);
        }

        return folder;
    }

    public String getModelItemOutputFolder(ModelItem modelItem) {
        List<ModelItem> chain = new ArrayList<ModelItem>();
        ModelItem p = modelItem;

        while (!(p instanceof Project)) {
            chain.add(0, p);
            p = p.getParent();
        }

        File dir = new File(getAbsoluteOutputFolder(modelItem));
        dir.mkdir();

        for (ModelItem item : chain) {
            dir = new File(dir, StringUtils.createFileName(item.getName(), '-'));
            dir.mkdir();
        }

        return dir.getAbsolutePath();
    }

    protected void ensureOutputFolder(ModelItem modelItem) {
        ensureFolder(getAbsoluteOutputFolder(modelItem));
    }

    public void ensureFolder(String path) {
        if (path == null) {
            return;
        }

        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
    }

    /**
     * Sets the SoapUI project file containing the tests to run
     *
     * @param projectFile the SoapUI project file containing the tests to run
     */

    public void setProjectFile(String projectFile) {
        this.projectFile = projectFile;
    }

    /**
     * Sets the SoapUI settings file containing the tests to run
     *
     * @param settingsFile the SoapUI settings file to use
     */

    public void setSettingsFile(String settingsFile) {
        this.settingsFile = settingsFile;
    }

    public void setEnableUI(boolean enableUI) {
        this.enableUI = enableUI;
    }

    public static class SoapUIOptions extends Options {
        private final String runnerName;

        public SoapUIOptions(String runnerName) {
            this.runnerName = runnerName;
        }

        public String getRunnerName() {
            return runnerName;
        }
    }

    public String getSoapUISettingsPassword() {
        return soapUISettingsPassword;
    }

    public void setSoapUISettingsPassword(String soapUISettingsPassword) {
        this.soapUISettingsPassword = soapUISettingsPassword;
    }

    public void setSystemProperties(String[] optionValues) {
        for (String option : optionValues) {
            int ix = option.indexOf('=');
            if (ix != -1) {
                System.setProperty(option.substring(0, ix), option.substring(ix + 1));
            }
        }
    }

    public void setCustomHeaders(String[] optionValues) {
        for (String option : optionValues) {
            int ix = option.indexOf('=');
            if (ix != -1) {
                // not optimal - it would be nicer if the filter could access command-line options via some
                // generic mechanism.
                String name = option.substring(0, ix);
                String value = option.substring(ix + 1);
                log.info("Adding global HTTP Header [" + name + "] = [" + value + "]");

                GlobalHttpHeadersRequestFilter.addGlobalHeader(name, value);
            }
        }
    }

    public void setGlobalProperties(String[] optionValues) {
        for (String option : optionValues) {
            int ix = option.indexOf('=');
            if (ix != -1) {
                String name = option.substring(0, ix);
                String value = option.substring(ix + 1);
                log.info("Setting global property [" + name + "] to [" + value + "]");
                //				PropertyExpansionUtils.getGlobalProperties().setPropertyValue( name, value );
                runnerGlobalProperties.put(name, value);
            }
        }
    }

    public void setProjectProperties(String[] projectProperties) {
        this.projectProperties = projectProperties;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.tools.CmdLineRunner#getLog()
     */
    @Override
    public Logger getLog() {
        return log;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.tools.CmdLineRunner#getProjectProperties()
     */
    @Override
    public String[] getProjectProperties() {
        return projectProperties;
    }

    protected void initProjectProperties(WsdlProject project) {
        if (projectProperties != null) {
            for (String option : projectProperties) {
                int ix = option.indexOf('=');
                if (ix != -1) {
                    String name = option.substring(0, ix);
                    String value = option.substring(ix + 1);
                    log.info("Setting project property [" + name + "] to [" + value + "]");
                    project.setPropertyValue(name, value);
                }
            }
        }
    }

    public boolean isEnableUI() {
        return enableUI;
    }

    public String getProjectPassword() {
        return projectPassword;
    }

    public void setProjectPassword(String projectPassword) {
        this.projectPassword = projectPassword;
    }
}
