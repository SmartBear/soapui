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
import com.eviware.soapui.analytics.AnalyticsHelper;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlProjectRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.ProjectRunListenerAdapter;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.report.JUnitReportCollector;
import com.eviware.soapui.report.JUnitSecurityReportCollector;
import com.eviware.soapui.report.TestCaseRunLogReport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone test-runner used from maven-plugin, can also be used from
 * command-line (see xdocs) or directly from other classes.
 * <p>
 * For standalone usage, set the project file (with setProjectFile) and other
 * desired properties before calling run
 * </p>
 *
 * @author Ole.Matzura
 */

public class SoapUITestCaseRunner extends AbstractSoapUITestRunner {
    public static final String SOAPUI_EXPORT_SEPARATOR = "soapui.export.separator";
    public static final String TITLE = "SoapUI " + SoapUI.SOAPUI_VERSION + " TestCase Runner";

    private String testSuite;
    private String testCase;
    private List<TestAssertion> assertions = new ArrayList<TestAssertion>();
    private Map<TestAssertion, WsdlTestStepResult> assertionResults = new HashMap<TestAssertion, WsdlTestStepResult>();
    private List<TestCase> failedTests = new ArrayList<TestCase>();

    private int testSuiteCount;
    private int testCaseCount;
    private int testStepCount;

    private int testAssertionCount;

    private boolean printReport;
    private boolean printAlertSiteReport;
    private boolean exportAll;
    private boolean ignoreErrors;
    private boolean junitReport;
    private boolean junitReportWithProperties;
    private int exportCount;
    private int maxErrors = 5;
    private JUnitReportCollector reportCollector;
    private String projectPassword;
    private boolean saveAfterRun;
    private TestCaseRunLogReport testCaseRunLogReport;

    /**
     * Runs the tests in the specified soapUI project file, see SoapUI xdocs for
     * details.
     *
     * @param args
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {
        System.exit(new SoapUITestCaseRunner().runFromCommandLine(args));
    }

    @Override
    protected boolean processCommandLine(CommandLine cmd) {
        String message = "";
        if (cmd.hasOption("e")) {
            setEndpoint(cmd.getOptionValue("e"));
        }

        if (cmd.hasOption("s")) {
            String testSuite = getCommandLineOptionSubstSpace(cmd, "s");
            setTestSuite(testSuite);
        }

        if (cmd.hasOption("c")) {
            String testCase = getCommandLineOptionSubstSpace(cmd, "c");
            setTestCase(testCase);
        }

        if (cmd.hasOption("u")) {
            setUsername(cmd.getOptionValue("u"));
        }

        if (cmd.hasOption("p")) {
            setPassword(cmd.getOptionValue("p"));
        }

        if (cmd.hasOption("w")) {
            setWssPasswordType(cmd.getOptionValue("w"));
        }

        if (cmd.hasOption("d")) {
            setDomain(cmd.getOptionValue("d"));
        }

        if (cmd.hasOption("h")) {
            setHost(cmd.getOptionValue("h"));
        }

        if (cmd.hasOption("f")) {
            setOutputFolder(getCommandLineOptionSubstSpace(cmd, "f"));
        }

        if (cmd.hasOption("t")) {
            setSettingsFile(getCommandLineOptionSubstSpace(cmd, "t"));
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

        if( cmd.hasOption( "H" ) )
        {
            setCustomHeaders( cmd.getOptionValues( "H" ) );
        }

        if (cmd.hasOption("G")) {
            setGlobalProperties(cmd.getOptionValues("G"));
        }

        if (cmd.hasOption("P")) {
            setProjectProperties(cmd.getOptionValues("P"));
        }

        setIgnoreError(cmd.hasOption("I"));
        setEnableUI(cmd.hasOption("i"));
        setPrintReport(cmd.hasOption("r"));
        setPrintAlertSiteReport(cmd.hasOption("M"));
        setExportAll(cmd.hasOption("a"));

        if (cmd.hasOption("A")) {
            setExportAll(true);
            System.setProperty(SOAPUI_EXPORT_SEPARATOR, File.separator);
        }

        setJUnitReport(cmd.hasOption("j"));
        setJUnitReportWithProperties(cmd.hasOption("J"));

        if (cmd.hasOption("m")) {
            setMaxErrors(Integer.parseInt(cmd.getOptionValue("m")));
        }

        setSaveAfterRun(cmd.hasOption("S"));

        if (message.length() > 0) {
            log.error(message);
            return false;
        }

        return true;
    }

    public void setMaxErrors(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    protected int getMaxErrors() {
        return maxErrors;
    }

    public void setSaveAfterRun(boolean saveAfterRun) {
        this.saveAfterRun = saveAfterRun;
    }

    @Override
    public void setProjectPassword(String projectPassword) {
        this.projectPassword = projectPassword;
    }

    @Override
    public String getProjectPassword() {
        return projectPassword;
    }

    @Override
    protected SoapUIOptions initCommandLineOptions()
    {
        SoapUIOptions options = new SoapUIOptions( "testrunner" );
        options.addOption( "e", true, "Sets the endpoint" );
        options.addOption( "s", true, "Sets the testsuite" );
        options.addOption( "c", true, "Sets the testcase" );
        options.addOption( "u", true, "Sets the username" );
        options.addOption( "p", true, "Sets the password" );
        options.addOption( "w", true, "Sets the WSS password type, either 'Text' or 'Digest'" );
        options.addOption( "i", false, "Enables Swing UI for scripts" );
        options.addOption( "d", true, "Sets the domain" );
        options.addOption( "h", true, "Sets the host" );
        options.addOption( "r", false, "Prints a small summary report" );
        options.addOption( "M", false, "Creates a Test Run Log Report in XML format" );
        options.addOption( "f", true, "Sets the output folder to export results to" );
        options.addOption( "j", false, "Sets the output to include JUnit XML reports" );
        options.addOption( "J", false, "Sets the output to include JUnit XML reports adding test properties to the report" );
        options.addOption( "m", false, "Sets the maximum number of TestStep errors to save for each testcase" );
        options.addOption( "a", false, "Turns on exporting of all results" );
        options.addOption( "A", false, "Turns on exporting of all results using folders instead of long filenames" );
        options.addOption( "t", true, "Sets the soapui-settings.xml file to use" );
        options.addOption( "x", true, "Sets project password for decryption if project is encrypted" );
        options.addOption( "v", true, "Sets password for soapui-settings.xml file" );
        options.addOption( "D", true, "Sets system property with name=value" );
        options.addOption( "G", true, "Sets global property with name=value" );
        options.addOption( "P", true, "Sets or overrides project property with name=value" );
        options.addOption( "I", false, "Do not stop if error occurs, ignore them" );
        options.addOption( "S", false , "Saves the project after running the tests" );
        options.addOption( "H", true , "Adds a custom HTTP Header to all outgoing requests (name=value), can be specified multiple times" );

        return options;
    }

    /**
     * Add console appender to groovy log
     */

    public void setExportAll(boolean exportAll) {
        this.exportAll = exportAll;
    }

    public void setJUnitReport(boolean junitReport) {
        this.junitReport = junitReport;
        if (junitReport) {
            reportCollector = createJUnitSecurityReportCollector();
        }
    }

    public void setJUnitReportWithProperties(boolean shouldIncludePropertiesInTheReport) {
        this.junitReportWithProperties = shouldIncludePropertiesInTheReport;
        if (this.junitReport && junitReportWithProperties) {
            reportCollector.setIncludeTestPropertiesInReport(junitReportWithProperties);
        }
    }

    protected JUnitSecurityReportCollector createJUnitSecurityReportCollector() {
        return new JUnitSecurityReportCollector();
    }

    public SoapUITestCaseRunner() {
        super(SoapUITestCaseRunner.TITLE);
    }

    public SoapUITestCaseRunner(String title) {
        super(title);
    }

    /**
     * Controls if a short test summary should be printed after the test runs
     *
     * @param printReport a flag controlling if a summary should be printed
     */

    public void setPrintReport(boolean printReport) {
        this.printReport = printReport;
    }

    public void setPrintAlertSiteReport(boolean printAlertSiteReport) {
        this.printAlertSiteReport = printAlertSiteReport;
    }

    public boolean isPrintAlertSiteReport() {
        return printAlertSiteReport;
    }

    public void setIgnoreError(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }

    @Override
    public boolean runRunner() throws Exception {
        AnalyticsHelper.initializeAnalytics();
        Analytics.trackSessionStart();

        initGroovyLog();

        assertions.clear();

        String projectFile = getProjectFile();

        WsdlProject project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(projectFile,
                getProjectPassword());

        if (project.isDisabled()) {
            throw new Exception("Failed to load SoapUI project file [" + projectFile + "]");
        }

        initProject(project);
        ensureOutputFolder(project);

        if (this.printAlertSiteReport) {
            testCaseRunLogReport = new TestCaseRunLogReport(getAbsoluteOutputFolder(project));
        }

        log.info("Running SoapUI tests in project [" + project.getName() + "]");

        long startTime = System.nanoTime();

        List<TestCase> testCasesToRun = new ArrayList<TestCase>();

        // validate testSuite argument
        if (testSuite != null && project.getTestSuiteByName(testSuite) == null) {
            throw new Exception("TestSuite with name [" + testSuite + "] is missing in Project [" + project.getName()
                    + "]");
        }

        // start by listening to all testcases.. (since one testcase can call
        // another)
        for (int c = 0; c < project.getTestSuiteCount(); c++) {
            TestSuite suite = project.getTestSuiteAt(c);
            for (int i = 0; i < suite.getTestCaseCount(); i++) {
                TestCase tc = suite.getTestCaseAt(i);
                if ((testSuite == null || suite.getName().equals(testSuite)) && testCase != null
                        && tc.getName().equals(testCase)) {
                    testCasesToRun.add(tc);
                }

                addListeners(tc);
            }
        }

        try {
            // validate testSuite argument
            if (testCase != null && testCasesToRun.size() == 0) {
                if (testSuite == null) {
                    throw new Exception("TestCase with name [" + testCase + "] is missing in Project [" + project.getName()
                            + "]");
                } else {
                    throw new Exception("TestCase with name [" + testCase + "] in TestSuite [" + testSuite
                            + "] is missing in Project [" + project.getName() + "]");
                }
            }

            // decide what to run
            if (testCasesToRun.size() > 0) {
                for (TestCase testCase : testCasesToRun) {
                    runTestCase((WsdlTestCase) testCase);
                }
            } else if (testSuite != null) {
                WsdlTestSuite ts = project.getTestSuiteByName(testSuite);
                if (ts == null) {
                    throw new Exception("TestSuite with name [" + testSuite + "] not found in project");
                } else {
                    runSuite(ts);
                }
            } else {
                runProject(project);
            }

            long timeTaken = (System.nanoTime() - startTime) / 1000000;

            if (printReport) {
                printReport(timeTaken);
            }

            exportReports(project);

            if (saveAfterRun && !project.isRemote()) {
                try {
                    project.save();
                } catch (Throwable t) {
                    log.error("Failed to save project", t);
                }
            }

            if ((assertions.size() > 0 || failedTests.size() > 0) && !ignoreErrors) {
                throwFailureException();
            }

            return true;
        } finally {
            for (int c = 0; c < project.getTestSuiteCount(); c++) {
                TestSuite suite = project.getTestSuiteAt(c);
                for (int i = 0; i < suite.getTestCaseCount(); i++) {
                    TestCase tc = suite.getTestCaseAt(i);
                    removeListeners(tc);
                }
            }
        }
    }

    protected void removeListeners(TestCase tc) {
        tc.removeTestRunListener(this);
        if (junitReport) {
            tc.removeTestRunListener(reportCollector);
        }
    }

    protected void runProject(WsdlProject project) {
        // add listener for counting..
        InternalProjectRunListener projectRunListener = new InternalProjectRunListener();
        project.addProjectRunListener(projectRunListener);

        try {
            log.info(("Running Project [" + project.getName() + "], runType = " + project.getRunType()));
            WsdlProjectRunner runner = project.run(new StringToObjectMap(), false);
            log.info("Project [" + project.getName() + "] finished with status [" + runner.getStatus() + "] in "
                    + runner.getTimeTaken() + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            project.removeProjectRunListener(projectRunListener);
        }
    }

    protected void initProject(WsdlProject project) throws Exception {
        initProjectProperties(project);
    }

    protected void exportReports(WsdlProject project) throws Exception {
        if (junitReport) {
            exportJUnitReports(reportCollector, getAbsoluteOutputFolder(project), project);
        }
    }

    protected void addListeners(TestCase tc) {
        tc.addTestRunListener(this);
        if (junitReport) {
            tc.addTestRunListener(reportCollector);
        }
        if (printAlertSiteReport) {
            tc.addTestRunListener(testCaseRunLogReport);
        }

    }

    protected void throwFailureException() throws Exception {
        StringBuffer buf = new StringBuffer();

        for (int c = 0; c < assertions.size(); c++) {
            TestAssertion assertion = assertions.get(c);
            Assertable assertable = assertion.getAssertable();
            if (assertable instanceof WsdlTestStep) {
                failedTests.remove(((WsdlTestStep) assertable).getTestCase());
            }

            buf.append(assertion.getName() + " in [" + assertable.getModelItem().getName() + "] failed;\n");
            buf.append(Arrays.toString(assertion.getErrors()) + "\n");

            WsdlTestStepResult result = assertionResults.get(assertion);
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            result.writeTo(writer);
            buf.append(stringWriter.toString());
        }

        while (!failedTests.isEmpty()) {
            buf.append("TestCase [" + failedTests.remove(0).getName() + "] failed without assertions\n");
        }

        throw new Exception(buf.toString());
    }

    public boolean isExportAll() {
        return exportAll;
    }

    public void exportJUnitReports(JUnitReportCollector collector, String folder, WsdlProject project)
            throws Exception {
        collector.saveReports(folder == null ? "" : folder);
    }

    public void printReport(long timeTaken) {
        System.out.println();
        System.out.println("SoapUI " + SoapUI.SOAPUI_VERSION + " TestCaseRunner Summary");
        System.out.println("-----------------------------");
        System.out.println("Time Taken: " + timeTaken + "ms");
        System.out.println("Total TestSuites: " + testSuiteCount);
        System.out.println("Total TestCases: " + testCaseCount + " (" + failedTests.size() + " failed)");
        System.out.println("Total TestSteps: " + testStepCount);
        System.out.println("Total Request Assertions: " + testAssertionCount);
        System.out.println("Total Failed Assertions: " + assertions.size());
        System.out.println("Total Exported Results: " + exportCount);
    }

    /**
     * Run tests in the specified TestSuite
     *
     * @param suite the TestSuite to run
     */

    protected void runSuite(WsdlTestSuite suite) {
        try {
            log.info(("Running TestSuite [" + suite.getName() + "], runType = " + suite.getRunType()));
            WsdlTestSuiteRunner runner = suite.run(new StringToObjectMap(), false);
            log.info("TestSuite [" + suite.getName() + "] finished with status [" + runner.getStatus() + "] in "
                    + (runner.getTimeTaken()) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            testSuiteCount++;
        }
    }

    /**
     * Runs the specified TestCase
     *
     * @param testCase the testcase to run
     * @param context
     */

    protected void runTestCase(WsdlTestCase testCase) {
        try {
            log.info("Running TestCase [" + testCase.getName() + "]");
            WsdlTestCaseRunner runner = testCase.run(new StringToObjectMap(), false);
            log.info("TestCase [" + testCase.getName() + "] finished with status [" + runner.getStatus() + "] in "
                    + (runner.getTimeTaken()) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the testcase to run
     *
     * @param testCase the testcase to run
     */

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    /**
     * Sets the TestSuite to run. If not set all TestSuites in the specified
     * project file are run
     *
     * @param testSuite the testSuite to run.
     */

    public void setTestSuite(String testSuite) {
        this.testSuite = testSuite;
    }

    @Override
    public void beforeRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
        log.info("Running SoapUI testcase [" + testRunner.getTestCase().getName() + "]");
    }

    @Override
    public void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep currentStep) {
        super.beforeStep(testRunner, runContext, currentStep);

        if (currentStep != null) {
            log.info("running step [" + currentStep.getName() + "]");
        }
    }

    @Override
    public void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result) {
        super.afterStep(testRunner, runContext, result);
        TestStep currentStep = runContext.getCurrentStep();

        if (currentStep instanceof Assertable) {
            Assertable requestStep = (Assertable) currentStep;
            for (int c = 0; c < requestStep.getAssertionCount(); c++) {
                TestAssertion assertion = requestStep.getAssertionAt(c);
                log.info("Assertion [" + assertion.getName() + "] has status " + assertion.getStatus());
                if (assertion.getStatus() == AssertionStatus.FAILED) {
                    for (AssertionError error : assertion.getErrors()) {
                        log.error("ASSERTION FAILED -> " + error.getMessage());
                    }

                    assertions.add(assertion);
                    assertionResults.put(assertion, (WsdlTestStepResult) result);
                }

                testAssertionCount++;
            }
        }

        String countPropertyName = currentStep.getName() + " run count";
        Long count = (Long) runContext.getProperty(countPropertyName);
        if (count == null) {
            count = new Long(0);
        }

        runContext.setProperty(countPropertyName, new Long(count.longValue() + 1));

        if (result.getStatus() == TestStepStatus.FAILED || exportAll) {
            try {
                String exportSeparator = System.getProperty(SOAPUI_EXPORT_SEPARATOR, "-");

                TestCase tc = currentStep.getTestCase();
                String nameBase = StringUtils.createFileName(tc.getTestSuite().getName(), '_') + exportSeparator
                        + StringUtils.createFileName(tc.getName(), '_') + exportSeparator
                        + StringUtils.createFileName(currentStep.getName(), '_') + "-" + count.longValue() + "-"
                        + result.getStatus();

                WsdlTestCaseRunner callingTestCaseRunner = (WsdlTestCaseRunner) runContext
                        .getProperty("#CallingTestCaseRunner#");

                if (callingTestCaseRunner != null) {
                    WsdlTestCase ctc = callingTestCaseRunner.getTestCase();
                    WsdlRunTestCaseTestStep runTestCaseTestStep = (WsdlRunTestCaseTestStep) runContext
                            .getProperty("#CallingRunTestCaseStep#");

                    nameBase = StringUtils.createFileName(ctc.getTestSuite().getName(), '_') + exportSeparator
                            + StringUtils.createFileName(ctc.getName(), '_') + exportSeparator
                            + StringUtils.createFileName(runTestCaseTestStep.getName(), '_') + exportSeparator
                            + StringUtils.createFileName(tc.getTestSuite().getName(), '_') + exportSeparator
                            + StringUtils.createFileName(tc.getName(), '_') + exportSeparator
                            + StringUtils.createFileName(currentStep.getName(), '_') + "-" + count.longValue() + "-"
                            + result.getStatus();
                }

                String absoluteOutputFolder = getAbsoluteOutputFolder(ModelSupport.getModelItemProject(tc));
                String fileName = absoluteOutputFolder + File.separator + nameBase + ".txt";

                if (result.getStatus() == TestStepStatus.FAILED) {
                    log.error(currentStep.getName() + " failed, exporting to [" + fileName + "]");
                }

                new File(fileName).getParentFile().mkdirs();

                PrintWriter writer = new PrintWriter(fileName);
                result.writeTo(writer);
                writer.close();

                // write attachments
                if (result instanceof MessageExchange) {
                    Attachment[] attachments = ((MessageExchange) result).getResponseAttachments();
                    if (attachments != null && attachments.length > 0) {
                        for (int c = 0; c < attachments.length; c++) {
                            fileName = nameBase + "-attachment-" + (c + 1) + ".";

                            Attachment attachment = attachments[c];
                            String contentType = attachment.getContentType();
                            if (!"application/octet-stream".equals(contentType) && contentType != null
                                    && contentType.indexOf('/') != -1) {
                                fileName += contentType.substring(contentType.lastIndexOf('/') + 1);
                            } else {
                                fileName += "dat";
                            }

                            fileName = absoluteOutputFolder + File.separator + fileName;

                            FileOutputStream outFile = new FileOutputStream(fileName);
                            Tools.writeAll(outFile, attachment.getInputStream());
                            outFile.close();
                        }
                    }
                }

                exportCount++;
            } catch (Exception e) {
                log.error("Error saving failed result: " + e, e);
            }
        }

        testStepCount++;

    }

    @Override
    public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
        log.info("Finished running SoapUI testcase [" + testRunner.getTestCase().getName() + "], time taken: "
                + testRunner.getTimeTaken() + "ms, status: " + testRunner.getStatus());

        if (testRunner.getStatus() == Status.FAILED) {
            failedTests.add(testRunner.getTestCase());
        }

        testCaseCount++;
    }

    protected class InternalProjectRunListener extends ProjectRunListenerAdapter {
        @Override
        public void afterTestSuite(ProjectRunner projectRunner, ProjectRunContext runContext, TestSuiteRunner testRunner) {
            testSuiteCount++;
        }
    }

    public String getTestSuite() {
        return testSuite;
    }

    public String getTestCase() {
        return testCase;
    }

    public boolean isJUnitReport() {
        return junitReport;
    }

    public boolean isIgnoreErrors() {
        return ignoreErrors;
    }

    public void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }

    public boolean isPrintReport() {
        return printReport;
    }

    public boolean isSaveAfterRun() {
        return saveAfterRun;
    }

    public List<TestCase> getFailedTests() {
        return failedTests;
    }

    public List<TestAssertion> getAssertions() {
        return assertions;
    }

    public boolean isJunitReport() {
        return junitReport;
    }

    public int getExportCount() {
        return exportCount;
    }

    public void setExportCount(int exportCount) {
        this.exportCount = exportCount;
    }

    public Map<TestAssertion, WsdlTestStepResult> getAssertionResults() {
        return assertionResults;
    }

    public int getTestStepCount() {
        return testStepCount;
    }

    public void setTestStepCount(int testStepCount) {
        this.testStepCount = testStepCount;
    }
}
