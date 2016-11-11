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

package com.eviware.soapui.security.actions;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;

/**
 * Options dialog for securitytests
 *
 * @author dragica.soldo
 */

public class SecurityTestOptionsAction extends AbstractSoapUIAction<SecurityTest> {
    private static final String FAIL_ON_ERROR = "Abort on Error";
    private static final String FAIL_SECURITYTEST_ON_ERROR = "Fail SecurityTest on Error";
    public static final String SOAPUI_ACTION_ID = "SecurityTestOptionsAction";

    private XFormDialog dialog;
    private XForm form;

    public SecurityTestOptionsAction() {
        super("Options", "Sets options for this SecurityTest");
    }

    public void perform(SecurityTest securityTest, Object param) {
        if (dialog == null) {
            XFormDialogBuilder builder = XFormFactory.createDialogBuilder("SecurityTest Options");
            form = builder.createForm("Basic");
            form.addCheckBox(FAIL_ON_ERROR, "Fail on error").addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    form.getFormField(FAIL_SECURITYTEST_ON_ERROR).setEnabled(!Boolean.parseBoolean(newValue));
                }
            });
            form.addCheckBox(FAIL_SECURITYTEST_ON_ERROR, "Fail SecurityTest if it has failed TestSteps");

            dialog = builder.buildDialog(builder.buildOkCancelHelpActions(HelpUrls.SECURITYTESTEDITOR_HELP_URL),
                    "Specify general options for this SecurityTest", UISupport.OPTIONS_ICON);
        }

        StringToStringMap values = new StringToStringMap();

        values.put(FAIL_ON_ERROR, String.valueOf(securityTest.getFailOnError()));
        values.put(FAIL_SECURITYTEST_ON_ERROR, String.valueOf(securityTest.getFailSecurityTestOnScanErrors()));
        values = dialog.show(values);

        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            try {
                securityTest.setFailOnError(Boolean.parseBoolean(values.get(FAIL_ON_ERROR)));
                securityTest.setFailSecurityTestOnScanErrors(Boolean
                        .parseBoolean(values.get(FAIL_SECURITYTEST_ON_ERROR)));

            } catch (Exception e1) {
                UISupport.showErrorMessage(e1.getMessage());
            }
        }
    }
}
