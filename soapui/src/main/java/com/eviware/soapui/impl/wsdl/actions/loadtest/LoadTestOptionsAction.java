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

package com.eviware.soapui.impl.wsdl.actions.loadtest;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.APage;

/**
 * Displays the LoadTest Options dialog
 *
 * @author Ole.Matzura
 */

public class LoadTestOptionsAction extends AbstractSoapUIAction<WsdlLoadTest> {
    public static final String SOAPUI_ACTION_ID = "LoadTestOptionsAction";
    private XFormDialog dialog;

    public LoadTestOptionsAction() {
        super("Options", "Sets options for this LoadTest");
    }

    public void perform(WsdlLoadTest loadTest, Object param) {
        if (dialog == null) {
            buildDialog();
        }

        dialog.setIntValue(SettingsForm.THREAD_STARTUP_DELAY, loadTest.getStartDelay());
        dialog.setBooleanValue(SettingsForm.RESET_STATISTICS, loadTest.getResetStatisticsOnThreadCountChange());
        dialog.setBooleanValue(SettingsForm.CALC_TPS, loadTest.getCalculateTPSOnTimePassed());
        dialog.setIntValue(SettingsForm.SAMPLE_INTERVAL, (int) loadTest.getSampleInterval());
        dialog.setBooleanValue(SettingsForm.DISABLE_HISTORY, loadTest.getHistoryLimit() == 0);
        dialog.setIntValue(SettingsForm.MAX_ASSERTIONS, (int) loadTest.getMaxAssertionErrors());
        dialog.setBooleanValue(SettingsForm.CANCEL_RUNNING, loadTest.getCancelOnReachedLimit());
        dialog.setIntValue(SettingsForm.STRATEGY_INTERVAL, (int) loadTest.getStrategyInterval());
        dialog.setBooleanValue(SettingsForm.CANCEL_EXCESSIVE, loadTest.getCancelExcessiveThreads());
        dialog.setBooleanValue(SettingsForm.TESTSTEP_STATISTICS, loadTest.getUpdateStatisticsPerTestStep());

        Settings settings = loadTest.getSettings();

        dialog.setBooleanValue(SettingsForm.INCLUDE_REQUEST,
                settings.getBoolean(HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN));
        dialog.setBooleanValue(SettingsForm.INCLUDE_RESPONSE,
                settings.getBoolean(HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN));
        dialog.setBooleanValue(SettingsForm.CLOSE_CONNECTIONS, settings.getBoolean(HttpSettings.CLOSE_CONNECTIONS));

        dialog.setValue(LogForm.LOG_FOLDER, loadTest.getStatisticsLogFolder());
        dialog.setIntValue(LogForm.LOG_INTERVAL, (int) loadTest.getStatisticsLogInterval());
        dialog.setBooleanValue(LogForm.LOG_ON_THREADCOUNT_CHANGE, loadTest.getLogStatisticsOnThreadChange());

        if (dialog.show() && !loadTest.isRunning()) {
            try {
                loadTest.setStartDelay(dialog.getIntValue(SettingsForm.THREAD_STARTUP_DELAY, loadTest.getStartDelay()));
                loadTest.setResetStatisticsOnThreadCountChange(dialog.getBooleanValue(SettingsForm.RESET_STATISTICS));
                loadTest.setCalculateTPSOnTimePassed(dialog.getBooleanValue(SettingsForm.CALC_TPS));
                loadTest.setSampleInterval(dialog.getIntValue(SettingsForm.SAMPLE_INTERVAL,
                        (int) loadTest.getSampleInterval()));
                loadTest.setHistoryLimit(dialog.getBooleanValue(SettingsForm.DISABLE_HISTORY) ? 0 : -1);
                loadTest.setMaxAssertionErrors(dialog.getIntValue(SettingsForm.MAX_ASSERTIONS, 1000));
                loadTest.setCancelOnReachedLimit(dialog.getBooleanValue(SettingsForm.CANCEL_RUNNING));
                loadTest.setStrategyInterval(dialog.getIntValue(SettingsForm.STRATEGY_INTERVAL,
                        WsdlLoadTest.DEFAULT_STRATEGY_INTERVAL));
                loadTest.setCancelExcessiveThreads(dialog.getBooleanValue(SettingsForm.CANCEL_EXCESSIVE));
                loadTest.setUpdateStatisticsPerTestStep(dialog.getBooleanValue(SettingsForm.TESTSTEP_STATISTICS));

                settings.setBoolean(HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN,
                        dialog.getBooleanValue(SettingsForm.INCLUDE_REQUEST));
                settings.setBoolean(HttpSettings.INCLUDE_RESPONSE_IN_TIME_TAKEN,
                        dialog.getBooleanValue(SettingsForm.INCLUDE_RESPONSE));
                settings.setBoolean(HttpSettings.CLOSE_CONNECTIONS,
                        dialog.getBooleanValue(SettingsForm.CLOSE_CONNECTIONS));

                loadTest.setLogStatisticsOnThreadChange(dialog.getBooleanValue(LogForm.LOG_ON_THREADCOUNT_CHANGE));
                loadTest.setStatisticsLogFolder(dialog.getValue(LogForm.LOG_FOLDER));
                loadTest.setStatisticsLogInterval(dialog.getIntValue(LogForm.LOG_INTERVAL,
                        (int) loadTest.getStatisticsLogInterval()));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void buildDialog() {
        dialog = ADialogBuilder.buildTabbedDialog(WizardForm.class, null);
        dialog.getFormField(SettingsForm.DISABLE_HISTORY).addFormFieldListener(new XFormFieldListener() {
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                dialog.getFormField(SettingsForm.SAMPLE_INTERVAL).setEnabled(!Boolean.parseBoolean(newValue));
            }
        });
    }

    @AForm(description = "Set options for this LoadTest", name = "LoadTest Options", helpUrl = HelpUrls.LOADTESTOPTIONS_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    private interface WizardForm {
        @APage(name = "Settings")
        public final static SettingsForm INPUT = null;

        @APage(name = "Statistics Log")
        public final static LogForm LogForm = null;
    }

    @AForm(name = "LoadTest Options", description = "", helpUrl = HelpUrls.LOADTESTOPTIONS_HELP_URL, icon = UISupport.OPTIONS_ICON_PATH)
    private interface SettingsForm {
        @AField(name = "Thread Startup Delay", description = "The delay before starting a thread in ms", type = AFieldType.INT)
        public final static String THREAD_STARTUP_DELAY = "Thread Startup Delay";

        @AField(name = "Reset Statistics", description = "when the number of threads changes", type = AFieldType.BOOLEAN)
        public final static String RESET_STATISTICS = "Reset Statistics";

        @AField(name = "Calculate TPS/BPS", description = "based on actual time passed", type = AFieldType.BOOLEAN)
        public final static String CALC_TPS = "Calculate TPS/BPS";

        @AField(name = "TestStep Statistics", description = "update statistics every TestStep", type = AFieldType.BOOLEAN)
        public final static String TESTSTEP_STATISTICS = "TestStep Statistics";

        @AField(name = "Include Request Write", description = "in calculated time", type = AFieldType.BOOLEAN)
        public final static String INCLUDE_REQUEST = "Include Request Write";

        @AField(name = "Include Response Read", description = "in calculated time", type = AFieldType.BOOLEAN)
        public final static String INCLUDE_RESPONSE = "Include Response Read";

        @AField(name = "Close Connections", description = "between each request", type = AFieldType.BOOLEAN)
        public final static String CLOSE_CONNECTIONS = "Close Connections";

        @AField(name = "Sample Interval", description = "statistics sample interval in milliseconds", type = AFieldType.INT)
        public final static String SAMPLE_INTERVAL = "Sample Interval";

        @AField(name = "Disable History", description = "to preserve memory (will disable diagrams)", type = AFieldType.BOOLEAN)
        public final static String DISABLE_HISTORY = "Disable History";

        @AField(name = "Max Assertions in Log", description = "the maximum number of assertion errors to keep in log (to preserve memory)", type = AFieldType.INT)
        public final static String MAX_ASSERTIONS = "Max Assertions in Log";

        @AField(name = "Cancel Running", description = "Cancel running TestCases when Limit has been reached", type = AFieldType.BOOLEAN)
        public final static String CANCEL_RUNNING = "Cancel Running";

        @AField(name = "Cancel Excessive", description = "Cancel excessive threads when ThreadCount decreases", type = AFieldType.BOOLEAN)
        public final static String CANCEL_EXCESSIVE = "Cancel Excessive";

        @AField(name = "Strategy Interval", description = "LoadTest Strategy application interval in milliseconds", type = AFieldType.INT)
        public final static String STRATEGY_INTERVAL = "Strategy Interval";

    }

    @AForm(name = "Logging", description = "", helpUrl = HelpUrls.LOADTESTOPTIONS_HELP_URL, icon = UISupport.OPTIONS_ICON_PATH)
    private interface LogForm {
        @AField(name = "Log Folder", description = "The folder in which to create log files", type = AFieldType.FOLDER)
        public final static String LOG_FOLDER = "Log Folder";

        @AField(name = "Log Interval", description = "The log interval in milliseconds, 0 only logs at end", type = AFieldType.INT)
        public final static String LOG_INTERVAL = "Log Interval";

        @AField(name = "Log on ThreadCount change", description = "Log every time the number of threads changes", type = AFieldType.BOOLEAN)
        public final static String LOG_ON_THREADCOUNT_CHANGE = "Log on ThreadCount change";
    }
}
