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

package com.eviware.soapui.autoupdate;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.settings.VersionUpdateSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.install4j.api.context.UserCanceledException;
import com.install4j.api.launcher.ApplicationLauncher;
import com.install4j.api.update.ApplicationDisplayMode;
import com.install4j.api.update.UpdateChecker;
import com.install4j.api.update.UpdateDescriptor;
import com.install4j.api.update.UpdateDescriptorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class Install4jSoapUIUpdateProvider extends Thread implements SoapUIUpdateProvider {
    private final static String APPLICATION_SILENT_VERSION_CHECK_ID = SoapUISystemProperties.SOAP_UI_UPDATER_APP_ID;
    private final static String APPLICATION_UPDATES_XML_URL = SoapUISystemProperties.SOAP_UI_UPDATE_URL;
    private final static String DEFAULT_UNREACHABLE_VALUE_FOR_SKIPPED_VERSION = "-1";
    private final String TERMINATED = "Terminated {EE9BF704-944A-43ae-8B53-7C9AE5SOAPUI}";
    private final static Logger logger = LoggerFactory.getLogger(SoapUIUpdateProvider.class);
    private final static String NEXT_AUTO_UPDATE_CHECK = "NextAU";//TODO: move to SoapUI settings

    private final TestMonitor testMonitor;
    private final String currentVersion;

    private volatile boolean autoCheckCancelled = false;

    private static class UpdateCheckResult {
        public SoapUIVersionInfo version = null;
        public String comments = null;
        public String errorText = null;

        private UpdateCheckResult() {
        }

        public static UpdateCheckResult error(String errorText) {
            UpdateCheckResult result = new UpdateCheckResult();
            result.errorText = errorText;
            return result;
        }

        public static UpdateCheckResult found(SoapUIVersionInfo version, String comments) {
            UpdateCheckResult result = new UpdateCheckResult();
            result.version = version;
            result.comments = comments;
            return result;
        }

        public static UpdateCheckResult noUpdate() {
            return new UpdateCheckResult();
        }

    }

    public Install4jSoapUIUpdateProvider(String soapuiVersion, TestMonitor testMonitor) {
        this.testMonitor = testMonitor;
        this.currentVersion = soapuiVersion;
    }

    public void showUpdateStatus() {
        autoCheckCancelled = true;
        final XProgressDialog waitDialog = UISupport.getDialogs().createProgressDialog("Checking for update", 100, "Update is checking...", true);
        try {
            waitDialog.run(new Worker() {
                private UpdateCheckResult result;
                private boolean cancelled = false;

                @Override
                public Object construct(XProgressMonitor monitor) {
                    String error = checkURLisReachable(APPLICATION_UPDATES_XML_URL);
                    if (StringUtils.isNullOrEmpty(error) || cancelled) {
                        result = checkUpdate();
                    } else {
                        result = UpdateCheckResult.error(error);
                    }
                    return null;
                }

                @Override
                public void finished() {
                    if (cancelled) {
                        return;
                    }
                    waitDialog.setVisible(false);
                    if (StringUtils.hasContent(result.errorText)) {
                        if (!TERMINATED.equals(result.errorText)) {
                            UISupport.showErrorMessage(result.errorText);
                        }
                    } else {
                        if (result.version == null) {
                            UISupport.showInfoMessage("You are using the latest accessible release of SoapUI");
                        } else {
                            if (!updatePostponedByUser(result.version, new SoapUIVersionInfo(currentVersion), result.comments)) {
                                update(false);
                            }
                        }
                    }
                }

                @Override
                public boolean onCancel() {
                    cancelled = true;
                    waitDialog.setVisible(false);
                    return true;
                }
            });
        } catch (Exception e) {
            UISupport.showErrorMessage(e.getMessage());
        }
    }

    private boolean update(boolean blocking) {
        try {
            ApplicationLauncher.launchApplication(APPLICATION_SILENT_VERSION_CHECK_ID, null, blocking,
                    new ApplicationLauncher.Callback() {
                        @Override
                        public void exited(int i) {

                        }

                        @Override
                        public void prepareShutdown() {
                            if (testMonitor.hasRunningTests()) {
                                testMonitor.cancelAllTests("Terminated because of auto-update.");
                            }
                            DoExit();
                        }
                    });
        } catch (IOException exception) {
            return false;
        }

        return true;
    }

    private void DoExit(){
        try {
            SoapUI.saveSettings();
            SaveStatus saveStatus = SoapUI.getWorkspace().onClose();
            if (saveStatus == SaveStatus.CANCELLED || saveStatus == SaveStatus.FAILED) {
                return;
            }
        } catch (Exception e1) {
            logger.error("Error saving settings during exit", e1);
        }
        SoapUI.shutdown();
    }

    @Override
    public void run() {
        Date now = new Date();
        Date whenCheck = new Date(SoapUI.getSettings().getLong(NEXT_AUTO_UPDATE_CHECK, now.getTime()));
        if (!now.before(whenCheck)) {
            String error = checkURLisReachable(APPLICATION_UPDATES_XML_URL);
            if (StringUtils.hasContent(error)) {
                logger.info(error);
            } else {
                UpdateCheckResult checkResult = checkUpdate();
                if (StringUtils.hasContent(checkResult.errorText)) {
                    logger.info(checkResult.errorText);
                    return;
                }
                if (checkResult.version != null){
                    String skippedVersion = SoapUI.getSettings().getString(NewSoapUIVersionAvailableDialog.SKIPPED_VERSION_SETTING, DEFAULT_UNREACHABLE_VALUE_FOR_SKIPPED_VERSION);
                    if (skippedVersion != null){
                        if (skippedVersion.equals(checkResult.version.toString())){
                            logger.info("Found new version (" + skippedVersion + ") but it was skipped according to previous user's choice.");
                            return;
                        }
                    }
                }
                if (checkResult.version != null && !autoCheckCancelled) {
                    if (!updatePostponedByUser(checkResult.version, new SoapUIVersionInfo(this.currentVersion), checkResult.comments)) {
                        update(true);
                    }
                }
            }
        }
    }

    private NewSoapUIVersionAvailableDialog.ReadyApiUpdateDialogResult showUpdateIsAvailableDialog(SoapUIVersionInfo newVersion, SoapUIVersionInfo curVersion, String comments) {

        NewSoapUIVersionAvailableDialog dialog = new NewSoapUIVersionAvailableDialog(newVersion, curVersion, comments);
        return dialog.showDialog();
    }

    private boolean updatePostponedByUser(SoapUIVersionInfo newVersion, SoapUIVersionInfo curVersion, String comments) {
        final long A_DAY = 24 * 60 * 60 * 1000;
        boolean result = true;
        Date nextCheck = null;
        switch (showUpdateIsAvailableDialog(newVersion, curVersion, comments)) {
            case Update:
                if (SoapUI.getSettings().getBoolean(VersionUpdateSettings.AUTO_CHECK_VERSION_UPDATE)) {
                    nextCheck = new Date();
                }
                result = false;
                break;
            case Delay_1Day:
                nextCheck = new Date(new Date().getTime() + A_DAY);
                break;
            case Delay_3Days:
                nextCheck = new Date(new Date().getTime() + 3 * A_DAY);
                break;
            case Delay_7Days:
                nextCheck = new Date(new Date().getTime() + 7 * A_DAY);
                break;
            case DoNotUpdate:
                break;
            case SkipThisVersion:
                break;
        }
        if (nextCheck != null) {
            SoapUI.getSettings().setLong(NEXT_AUTO_UPDATE_CHECK, nextCheck.getTime());
            SoapUI.getSettings().setBoolean(VersionUpdateSettings.AUTO_CHECK_VERSION_UPDATE, true);
        }
        return result;
    }

    private String checkURLisReachable(String url) {
        try {
            int timeout = 10 * 1000;//10 seconds
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            int responseCode = connection.getResponseCode();
            if (200 <= responseCode && responseCode <= 399) {
                return null;
            }
            return String.format("The update server is unreachable: The attempt to connect to the update server has resulted in %d code.", responseCode);
        } catch (IOException exception) {
            return String.format("The update server is unreachable: %s", exception.getMessage());
        }
    }

    private UpdateCheckResult checkUpdate() {
        String newVersion = null;
        UpdateDescriptorEntry entry = null;
        try {
            UpdateDescriptor descriptor = getUpdateDescriptor();
            if (descriptor != null) {
                entry = descriptor.getPossibleUpdateEntry();
                if (entry != null) {
                    newVersion = entry.getNewVersion();
                }
            }
        } catch (UserCanceledException ex) {
            return UpdateCheckResult.error(TERMINATED);
        } catch (IOException ex) {
            return UpdateCheckResult.error(ex.getMessage());
        }
        if (newVersion != null && newVersion.length() != 0) {
            SoapUIVersionInfo newVersionObj = new SoapUIVersionInfo(newVersion);
            SoapUIVersionInfo currentVersionObj = new SoapUIVersionInfo(currentVersion);

            if (currentVersionObj.compare(currentVersionObj, newVersionObj) < 0) {
                return UpdateCheckResult.found(newVersionObj, entry.getComment());
            }
        }
        return UpdateCheckResult.noUpdate();
    }

    private UpdateDescriptor getUpdateDescriptor() throws UserCanceledException, IOException {
        return UpdateChecker.getUpdateDescriptor(APPLICATION_UPDATES_XML_URL, ApplicationDisplayMode.GUI);
    }
}
