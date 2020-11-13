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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.wsi;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.actions.SoapUIPreferencesAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.RunnerContext;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.settings.WSISettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wsI.testing.x2003.x03.analyzerConfig.AssertionResults;
import org.wsI.testing.x2003.x03.analyzerConfig.Configuration;
import org.wsI.testing.x2003.x03.analyzerConfig.ConfigurationDocument;
import org.wsI.testing.x2003.x03.analyzerConfig.LogFile;
import org.wsI.testing.x2003.x03.analyzerConfig.ReportFile;
import org.wsI.testing.x2003.x03.analyzerConfig.WsdlElementReference;
import org.wsI.testing.x2003.x03.analyzerConfig.WsdlElementType;
import org.wsI.testing.x2003.x03.analyzerConfig.WsdlReference;
import org.wsI.testing.x2003.x03.common.AddStyleSheet;

import javax.swing.SwingUtilities;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Invokes WS-I Analyzer Tool
 *
 * @author Ole.Matzura
 */

@ActionConfiguration(targetType = WsdlInterface.class)
public class WSIAnalyzeAction extends AbstractToolsAction<Interface> {
    public final static String SOAPUI_ACTION_ID = "WSIAnalyzeAction";
    public final static Logger log = LogManager.getLogger(WSIAnalyzeAction.class);

    public static final String WSI_ANALYZER_CONFIG = "wsi-analyzer-config";
    public static final String ANALYZER_V10_NAME = "analyzerV10";
    public static final String ANALYZER_V11_NAME = "analyzerV11";
    public static final String WSI_REPORT_NAME = "wsi-report";
    public static final String REPORT_TEMPLATE_FILE_NAME = "report.xsl";
    public static final String WSI_HOME_ENV_VAR_NAME = "WSI_HOME";
    public static final String XML_EXTENSION = ".xml";
    public static final String WIN_BATCH_FILE_EXTENSION = ".bat";
    public static final String UNIX_BATCH_FILE_EXTENSION = ".sh";
    public static final String WSI_DIR_PROP_NAME = "wsi.dir";
    public static final String PROFILES_DIR_RELATED_PATH = "./profiles/";
    public static final String HTML_EXTENSION = ".html";
    public static final String ALL_RESULT_TYPE = "all";

    private String configFile;
    private String wsiDir;
    private String profile;

    public WSIAnalyzeAction() {
        super("Check WSI Compliance", "Validate this WSDL for WSI Basic Profile compliance");
    }

    protected void generate(StringToStringMap values, ToolHost toolHost, Interface modelItem) throws Exception {
        wsiDir = SoapUI.getSettings().getString(WSISettings.WSI_LOCATION,
                System.getProperty(WSI_DIR_PROP_NAME, System.getenv(WSI_HOME_ENV_VAR_NAME)));
        if (StringUtils.isNullOrEmpty(wsiDir)) {
            UISupport.showErrorMessage("WSI Test Tools directory must be set in global preferences");

            if (UISupport.getMainFrame() != null) {
                if (SoapUIPreferencesAction.getInstance().show(SoapUIPreferencesAction.WS_I_SETTINGS)) {
                    wsiDir = SoapUI.getSettings().getString(WSISettings.WSI_LOCATION, null);
                }
            }
        }

        if (StringUtils.isNullOrEmpty(wsiDir)) {
            return;
        }

        profile = SoapUI.getSettings().getString(WSISettings.PROFILE_TYPE, WSISettings.BASIC_PROFILE_10_TAD);

        File reportFile = File.createTempFile(WSI_REPORT_NAME, XML_EXTENSION);
        File wsiToolDir = new File(wsiDir);

        ArgumentBuilder args = buildArgs(wsiToolDir, reportFile, modelItem);
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(args.getArgs());
        builder.directory(wsiToolDir);

        toolHost.run(new WSIProcessToolRunner(builder, reportFile, modelItem));
    }

    private ArgumentBuilder buildArgs(File wsiToolDir, File reportFile, Interface modelItem) throws IOException {
        Settings settings = modelItem.getSettings();

        ConfigurationDocument configDoc = createConfigFile(reportFile, settings, (WsdlInterface) modelItem);
        configFile = configDoc.toString();

        File file = File.createTempFile(WSI_ANALYZER_CONFIG, XML_EXTENSION);
        configDoc.save(file);

        ArgumentBuilder builder = new ArgumentBuilder(new StringToStringMap());
        builder.startScript(wsiToolDir.getAbsolutePath() + File.separator +
                        (profile.equals(WSISettings.BASIC_PROFILE_10_TAD)?ANALYZER_V10_NAME:ANALYZER_V11_NAME),
                WIN_BATCH_FILE_EXTENSION, UNIX_BATCH_FILE_EXTENSION);
        builder.addArgs("-config", file.getAbsolutePath());

        return builder;
    }

    private ConfigurationDocument createConfigFile(File reportFile, Settings settings, WsdlInterface iface) throws IOException {
        ConfigurationDocument configDoc = ConfigurationDocument.Factory.newInstance();
        Configuration config = configDoc.addNewConfiguration();

        config.setVerbose(settings.getBoolean(WSISettings.VERBOSE));
        AssertionResults results = config.addNewAssertionResults();
        /*results.setType(AssertionResults.Type.Enum.forString(settings.getString(WSISettings.RESULTS_TYPE,
                AssertionResults.Type.ONLY_FAILED.toString())));*/
        results.setType(AssertionResults.Type.Enum.forString(ALL_RESULT_TYPE));

        results.setMessageEntry(settings.getBoolean(WSISettings.MESSAGE_ENTRY));
        results.setFailureMessage(settings.getBoolean(WSISettings.FAILURE_MESSAGE));
        results.setAssertionDescription(settings.getBoolean(WSISettings.ASSERTION_DESCRIPTION));

        ReportFile report = config.addNewReportFile();
        report.setLocation(reportFile.getAbsolutePath());
        report.setReplace(true);
        AddStyleSheet stylesheet = report.addNewAddStyleSheet();
        stylesheet.setHref(PROFILES_DIR_RELATED_PATH + profile);
        stylesheet.setType("text/xsl");
        stylesheet.setAlternate(false);

        config.setTestAssertionsFile(PROFILES_DIR_RELATED_PATH + profile);

        LogFile logFile = config.addNewLogFile();
        logFile.setCorrelationType(LogFile.CorrelationType.Enum.forString(settings.getString(WSISettings.CORRELATION_TYPE,
                WSISettings.ENDPOINT_LOG_FILE_CORRELATION_TYPE)));
        logFile.setStringValue("log-sample.xml");//TODO: left it as is since it doesn't work with other paths
        config.setLogFile(logFile);

        WsdlReference wsdlRef = config.addNewWsdlReference();

        StringToStringMap values = new StringToStringMap();
        values.put(WSDL, iface.getDefinition());
        values.put(CACHED_WSDL, Boolean.toString(iface.isCached()));

        wsdlRef.setWsdlURI(getWsdlUrl(values, iface));
        WsdlElementReference wsdlElement = wsdlRef.addNewWsdlElement();
        wsdlElement.setType(WsdlElementType.BINDING);
        wsdlElement.setStringValue(iface.getBindingName().getLocalPart());
        wsdlElement.setNamespace(iface.getBindingName().getNamespaceURI());

        return configDoc;
    }

    protected void showReport(File reportFile, String configFile) throws Exception {
        WSIReportPanel panel = new WSIReportPanel(reportFile, configFile, null, true);
        panel.setPreferredSize(new Dimension(600, 400));

        UISupport.showDesktopPanel(new DefaultDesktopPanel("WS-I Report", "WS-I Report for Interface ["
                + getModelItem().getName() + "]", panel));
    }

    public static File transformReport(File reportFile) throws Exception {
        String dir = SoapUI.getSettings().getString(WSISettings.WSI_LOCATION, null);
        File xsltFile = new File(dir + File.separatorChar + "xsl" + File.separatorChar
                + REPORT_TEMPLATE_FILE_NAME);

        Source xmlSource = new StreamSource(reportFile);
        Source xsltSource = new StreamSource(xsltFile);

        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);

        String outputFolder = SoapUI.getSettings().getString(WSISettings.OUTPUT_FOLDER, null);
        File output = StringUtils.isNullOrEmpty(outputFolder) ? null : new File(outputFolder);

        if (output == null){
            log.warn("WSI output folder is not specified!");
        }

        File tempFile = File.createTempFile(WSI_REPORT_NAME, HTML_EXTENSION, output);
        trans.transform(xmlSource, new StreamResult(new FileWriter(tempFile)));

        log.info("WSI Report created at [" + tempFile.getAbsolutePath() + "]");

        return tempFile;
    }

    private class WSIProcessToolRunner extends ProcessToolRunner {
        private File reportFile;
        private final Interface modelItem;

        public WSIProcessToolRunner(ProcessBuilder builder, File reportFile, Interface modelItem) {
            super(builder, "WSI Analyzer", modelItem);
            this.reportFile = reportFile;
            this.modelItem = modelItem;
        }

        public String getDescription() {
            return "Running WSI Analysis tools...";
        }

        protected void afterRun(int exitCode, RunnerContext context) {
            if (exitCode == 0 && context.getStatus() == RunnerContext.RunnerStatus.FINISHED) {
                try {
                    reportFile = transformReport(reportFile);
                } catch (Exception e) {
                    SoapUI.logError(e);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            showReport(reportFile, configFile);
                        } catch (Exception e) {
                            UISupport.showErrorMessage(e);
                        }
                    }
                });
            } else {
                ProcessBuilder processBuilder = getBuilders()[0];
                List<String> programAndArgs = processBuilder.command();
                log.error("WSI checking failed. Exit code " + new Integer(exitCode).toString() + ". Command line: " + getCommandDetails(programAndArgs));
            }

            closeDialog(modelItem);
        }

        private String getCommandDetails (List<String> command){
            String str = "";
            for (String entity: command){
                str += entity + " ";
            }

            return str;
        }

        public boolean showLog() {
            return modelItem.getSettings().getBoolean(WSISettings.SHOW_LOG);
        }

        @Override
        protected void beforeProcess(ProcessBuilder processBuilder, RunnerContext context) {
            super.beforeProcess(processBuilder, context);
            processBuilder.environment().put(WSI_HOME_ENV_VAR_NAME, wsiDir);
        }
    }
}
