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

package com.eviware.soapui.security.ui;

import com.eviware.soapui.config.FuzzerScanConfig;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JFormDialog;

public class FuzzerScanAdvancedConfigPanel {
    private JFormDialog dialog;
    private FuzzerScanConfig fuzzerScanConfig;

    public FuzzerScanAdvancedConfigPanel(FuzzerScanConfig fuzzerScanConfig) {
        this.fuzzerScanConfig = fuzzerScanConfig;
        initDialog();
    }

    public JFormDialog getDialog() {
        return dialog;
    }

    private JFormDialog initDialog() {
        dialog = (JFormDialog) ADialogBuilder.buildDialog(AdvancedSettings.class);
        minimalField(fuzzerScanConfig);
        maximalField(fuzzerScanConfig);
        numberOfRequestField(fuzzerScanConfig);
        return dialog;
    }

    private void minimalField(final FuzzerScanConfig fuzzerScanConfig) {
        XFormField minimal = dialog.getFormField(AdvancedSettings.MINIMAL);
        minimal.setValue(String.valueOf(fuzzerScanConfig.getMinimal()));

        minimal.addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                try {
                    if ("".equals(newValue)) {
                        return;
                    }
                    Integer.valueOf(newValue);
                    fuzzerScanConfig.setMinimal(Integer.valueOf(newValue));
                } catch (Exception e) {
                    UISupport.showErrorMessage("Value must be integer number");
                }
            }
        });
    }

    private void maximalField(final FuzzerScanConfig fuzzerScanConfig) {
        XFormField maximal = dialog.getFormField(AdvancedSettings.MAXIMAL);
        maximal.setValue(String.valueOf(fuzzerScanConfig.getMaximal()));

        maximal.addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                try {
                    if ("".equals(newValue)) {
                        return;
                    }
                    Integer.valueOf(newValue);
                    fuzzerScanConfig.setMaximal(Integer.valueOf(newValue));
                } catch (Exception e) {
                    UISupport.showErrorMessage("Value must be integer number");
                }
            }
        });
    }

    private void numberOfRequestField(final FuzzerScanConfig fuzzerScanConfig) {
        XFormField numberOfRequest = dialog.getFormField(AdvancedSettings.NUMBER_OF_REQUEST);
        numberOfRequest.setValue(String.valueOf(fuzzerScanConfig.getNumberOfRequest()));

        numberOfRequest.addFormFieldListener(new XFormFieldListener() {

            @Override
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                try {
                    if ("".equals(newValue)) {
                        return;
                    }
                    Integer.valueOf(newValue);
                    fuzzerScanConfig.setNumberOfRequest(Integer.valueOf(newValue));
                } catch (Exception e) {
                    UISupport.showErrorMessage("Value must be integer number");
                }
            }
        });
    }


    @AForm(description = "Fuzzer Scan", name = "Fuzzer Scan")
    protected interface AdvancedSettings {

        @AField(description = "Minimal length of Fuzzed value", name = "Minimal length", type = AFieldType.INT)
        public final static String MINIMAL = "Minimal length";

        @AField(description = "Maximal length of Fuzzed value", name = "Maximal length", type = AFieldType.INT)
        public final static String MAXIMAL = "Maximal length";

        @AField(description = "Number of Fuzzed Requests to do", name = "Number of Requests", type = AFieldType.INT)
        public final static String NUMBER_OF_REQUEST = "Number of Requests";

    }

}
