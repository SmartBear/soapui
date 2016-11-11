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
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.report.JUnitSecurityReportCollector;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.result.SecurityResult;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.support.SecurityTestRunListener;
import com.eviware.soapui.security.support.SecurityTestRunListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone security test-runner used from maven-plugin, can also be used from
 * command-line (see xdocs) or directly from other classes.
 * <p>
 * For standalone usage, set the project file (with setProjectFile) and other
 * desired properties before calling run
 * </p>
 *
 * @author nebojsa.tasic
 */

public class SoapUISecurityTestRunner extends SoapUITestCaseRunner implements SecurityTestRunListener {
    public static final String SOAPUI_EXPORT_SEPARATOR = "soapui.export.separator";

    public static final String TITLE = "SoapUI " + SoapUI.SOAPUI_VERSION + " Security Test Runner";
    private String securityTestName;
    private int securityTestCount;
    private int securityScanCount;
    private int securityScanRequestCount;
    private int securityScanAlertCount;
    private List<SecurityTestStepResult> failedResults = new ArrayList<SecurityTestStepResult>();
    private JUnitSecurityReportCollector reportCollector = new JUnitSecurityReportCollector();

    /**
     * Runs the tests in the specified soapUI project file, see SoapUI xdocs for
     * details.
     *
     * @param args
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {
        System.exit(new SoapUISecurityTestRunner().runFromCommandLine(args));
    }

    protected boolean processCommandLine(CommandLine cmd) {
        if (cmd.hasOption("n")) {
            setSecurityTestName(cmd.getOptionValue("n"));
        }

        return super.processCommandLine(cmd);
    }

    public void setSecurityTestName(String securityTestName) {
        this.securityTestName = securityTestName;
    }

    protected SoapUIOptions initCommandLineOptions() {
        SoapUIOptions options = super.initCommandLineOptions();
        options.addOption("n", true, "Sets the security test name");

        return options;
    }

    public SoapUISecurityTestRunner() {
        super(SoapUISecurityTestRunner.TITLE);
    }

    public SoapUISecurityTestRunner(String title) {
        super(title);
    }

    public boolean runRunner() throws Exception {
        initGroovyLog();
        getAssertions().clear();
        String projectFile = getProjectFile();

        WsdlProject project = (WsdlProject) ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(projectFile,
                getProjectPassword());

        if (project.isDisabled()) {
            throw new Exception("Failed to load SoapUI project file [" + projectFile + "]");
        }

        initProject(project);
        ensureOutputFolder(project);

        log.info("Running SoapUI tests in project [" + project.getName() + "]");

        String testSuite = getTestSuite();
        String testCase = getTestCase();

        long startTime = System.nanoTime();

        List<TestCase> testCasesToRun = new ArrayList<TestCase>();

        // start by listening to all testcases.. (since one testcase can call
        // another)
        for (int c = 0; c < project.getTestSuiteCount(); c++) {
            TestSuite suite = project.getTestSuiteAt(c);
            for (int i = 0; i < suite.getTestCaseCount(); i++) {
                TestCase tc = suite.getTestCaseAt(i);
                if ((testSuite == null || suite.getName().equals(suite.getName())) && testCase != null
                        && tc.getName().equals(testCase)) {
                    testCasesToRun.add(tc);
                }

                addListeners(tc);
            }
        }

        // decide what to run
        if (testCasesToRun.size() > 0) {
            for (TestCase tc : testCasesToRun) {
                runTestCase((WsdlTestCase) tc);
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

        if (isPrintReport()) {
            printReport(timeTaken);
        }

        exportReports(project);

        if (isSaveAfterRun() && !project.isRemote()) {
            try {
                project.save();
            } catch (Throwable t) {
                log.error("Failed to save project", t);
            }
        }

        if (securityScanAlertCount > 0 && !isIgnoreErrors()) {
            throw new Exception("SecurityTest execution failed with " + securityScanAlertCount + " alert"
                    + (securityScanAlertCount > 1 ? "s" : ""));
        }

        return true;
    }

    protected void runProject(WsdlProject project) {
        try {
            log.info(("Running Project [" + project.getName() + "], runType = " + project.getRunType()));
            for (TestSuite testSuite : project.getTestSuiteList()) {
                runSuite((WsdlTestSuite) testSuite);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initProject(WsdlProject project) throws Exception {
        initProjectProperties(project);
    }

    protected void exportReports(WsdlProject project) throws Exception {
        if (isJUnitReport()) {
            exportJUnitReports(reportCollector, getAbsoluteOutputFolder(project), project);
        }
    }

    protected void addListeners(TestCase tc) {
        tc.addTestRunListener(this);
        if (isJunitReport()) {
            tc.addTestRunListener(reportCollector);
        }
    }

    public void exportJUnitReports(JUnitSecurityReportCollector collector, String folder, WsdlProject project)
            throws Exception {
        collector.saveReports(folder == null ? "" : folder);
    }

    public void printReport(long timeTaken) {
        System.out.println();
        System.out.println("SoapUI " + SoapUI.SOAPUI_VERSION + " Security TestCaseRunner Summary");
        System.out.println("-----------------------------");
        System.out.println("Time Taken: " + timeTaken + "ms");
        System.out.println("Total SecurityTests: " + securityTestCount);
        System.out.println("Total SecurityScans: " + securityScanCount);
        System.out.println("Total SecurityScan Requests: " + securityScanRequestCount);
        System.out.println("Total Failed SecurityScan Requests: " + securityScanAlertCount);
    }

    /**
     * Run tests in the specified TestSuite
     *
     * @param suite the TestSuite to run
     */

    protected void runSuite(WsdlTestSuite suite) {
        try {
            for (TestCase testCase : suite.getTestCaseList()) {
                runTestCase((WsdlTestCase) testCase);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Runs the SecurityTests in the specified TestCase
     *
     * @param testCase the testcase to run
     * @param context
     */

    protected void runTestCase(WsdlTestCase testCase) {
        try {
            for (SecurityTest securityTest : testCase.getSecurityTestList()) {

                securityTest.addSecurityTestRunListener(this);

                if (StringUtils.isNullOrEmpty(securityTestName) || securityTest.getName().equals(securityTestName)) {
                    runSecurityTest(securityTest);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param securityTest
     */
    protected void runSecurityTest(SecurityTest securityTest) {
        securityTest.addSecurityTestRunListener(new SecurityTestRunListenerAdapter() {
            private int requestIndex = 0;

            @Override
            public void afterSecurityScanRequest(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                                 SecurityScanRequestResult securityCheckReqResult) {
                securityScanRequestCount++;
                if (securityCheckReqResult.getStatus() == ResultStatus.FAILED) {
                    securityScanAlertCount++;
                }

                log.info(securityCheckReqResult.getSecurityScan().getName() + " - "
                        + securityCheckReqResult.getChangedParamsInfo(++requestIndex));
            }

            @Override
            public void afterSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                          SecurityScanResult securityCheckResult) {
                securityScanCount++;
            }

            @Override
            public void beforeSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                           SecurityScan securityCheck) {
                requestIndex = 0;
            }

            @Override
            public void afterStep(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                  SecurityTestStepResult result) {
                if (result.getStatus() == ResultStatus.FAILED) {
                    failedResults.add(result);
                }
            }
        });

        if (isJUnitReport()) {
            securityTest.addSecurityTestRunListener(reportCollector);
        }

        log.info("Running SecurityTest [" + securityTest.getName() + "] in TestCase ["
                + securityTest.getTestCase().getName() + "] in TestSuite ["
                + securityTest.getTestCase().getTestSuite().getName() + "]");

        SecurityTestRunner runner = securityTest.run(null, false);
        // log.info( "\n" + securityTest.getSecurityTestLog().getMessages() );
        log.info("SecurityTest [" + securityTest.getName() + "] finished with status [" + runner.getStatus() + "] in "
                + (runner.getTimeTaken()) + "ms");

        if (isJUnitReport()) {
            securityTest.removeSecurityTestRunListener(reportCollector);
        }
    }

    @Override
    public void afterStep(TestCaseRunner testRunner, SecurityTestRunContext runContext, SecurityTestStepResult result) {
        if (!isPrintReport()) {
            return;
        }

        TestStep currentStep = runContext.getCurrentStep();

        String securityTestName = "";
        String securityScanName = "";
        if (!result.getSecurityScanResultList().isEmpty()) {
            securityTestName = result.getSecurityScanResultList().get(0).getSecurityScan().getParent().getName();
            securityScanName = result.getSecurityScanResultList().get(0).getSecurityScanName();
        }

        String countPropertyName = currentStep.getName() + " run count";
        Long count = new Long(getExportCount());// ( Long
        // )runContext.getProperty(
        // countPropertyName );
        if (count == null) {
            count = new Long(0);
        }

        runContext.setProperty(countPropertyName, new Long(count.longValue() + 1));

        if (result.getStatus() == SecurityResult.ResultStatus.FAILED || isExportAll()) {
            try {
                String exportSeparator = System.getProperty(SOAPUI_EXPORT_SEPARATOR, "-");

                TestCase tc = currentStep.getTestCase();

                String nameBase = StringUtils.createFileName(securityTestName, '_') + exportSeparator
                        + StringUtils.createFileName(securityScanName, '_') + exportSeparator
                        + StringUtils.createFileName(tc.getTestSuite().getName(), '_') + exportSeparator
                        + StringUtils.createFileName(tc.getName(), '_') + exportSeparator
                        + StringUtils.createFileName(currentStep.getName(), '_') + "-" + count.longValue() + "-"
                        + result.getStatus();

                WsdlTestCaseRunner callingTestCaseRunner = (WsdlTestCaseRunner) runContext
                        .getProperty("#CallingTestCaseRunner#");

                if (callingTestCaseRunner != null) {
                    WsdlTestCase ctc = callingTestCaseRunner.getTestCase();
                    WsdlRunTestCaseTestStep runTestCaseTestStep = (WsdlRunTestCaseTestStep) runContext
                            .getProperty("#CallingRunTestCaseStep#");

                    nameBase = StringUtils.createFileName(securityTestName, '_') + exportSeparator
                            + StringUtils.createFileName(ctc.getTestSuite().getName(), '_') + exportSeparator
                            + StringUtils.createFileName(ctc.getName(), '_') + exportSeparator
                            + StringUtils.createFileName(runTestCaseTestStep.getName(), '_') + exportSeparator
                            + StringUtils.createFileName(tc.getTestSuite().getName(), '_') + exportSeparator
                            + StringUtils.createFileName(tc.getName(), '_') + exportSeparator
                            + StringUtils.createFileName(currentStep.getName(), '_') + "-" + count.longValue() + "-"
                            + result.getStatus();
                }

                String absoluteOutputFolder = getAbsoluteOutputFolder(ModelSupport.getModelItemProject(tc));
                String fileName = absoluteOutputFolder + File.separator + nameBase + ".txt";

                if (result.getStatus() == SecurityResult.ResultStatus.FAILED) {
                    log.error(currentStep.getName() + " failed, exporting to [" + fileName + "]");
                }

                File file = new File(fileName);
                file.getParentFile().mkdirs();

                PrintWriter writer = new PrintWriter(file);
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

                setExportCount(getExportCount() + 1);
            } catch (Exception e) {
                log.error("Error saving failed result: " + e, e);
            }
        }

        setTestStepCount(getTestStepCount() + 1);

    }

    public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
    }

    @Override
    public void afterOriginalStep(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                  SecurityTestStepResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterRun(TestCaseRunner testRunner, SecurityTestRunContext runContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                  SecurityScanResult securityScanResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterSecurityScanRequest(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                         SecurityScanRequestResult securityScanReqResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeRun(TestCaseRunner testRunner, SecurityTestRunContext runContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                   SecurityScan securityScan) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeStep(TestCaseRunner testRunner, SecurityTestRunContext runContext, TestStepResult testStepResult) {
        // TODO Auto-generated method stub

    }

}
