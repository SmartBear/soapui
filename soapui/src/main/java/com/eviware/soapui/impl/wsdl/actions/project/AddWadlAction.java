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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.actions.service.GenerateRestTestSuiteAction;
import com.eviware.soapui.impl.rest.support.WadlImporter;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.io.File;

/**
 * Action for creating a new WSDL project
 *
 * @author Ole.Matzura
 */

public class AddWadlAction extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "NewWsdlProjectAction";
    private XFormDialog dialog;

    public static final MessageSupport messages = MessageSupport.getMessages(AddWadlAction.class);

    public AddWadlAction() {
        super(messages.get("Title"), messages.get("Description"));
    }

    public void perform(WsdlProject project, Object param) {
        createOrUpdateDialog();

        while (dialog.show()) {
            try {
                String url = dialog.getValue(Form.INITIALWSDL).trim();
                if (StringUtils.hasContent(url)) {
                    String expandedUrl = PathUtils.expandPath(url, project);

                    if (new File(expandedUrl).exists()) {
                        expandedUrl = new File(expandedUrl).toURI().toURL().toString();
                    }

                    RestService result = importWadl(project, expandedUrl);
                    if (!url.equals(expandedUrl) && result != null) {
                        result.setWadlUrl(url);
                        if (dialog.getBooleanValue(Form.GENERATETESTSUITE)) {
                            new GenerateRestTestSuiteAction().perform(result, true);
                        }
                    }
                    break;
                }
            } catch (Exception ex) {
                UISupport.showErrorMessage(ex);
            }
        }
    }

    private void createOrUpdateDialog() {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
            dialog.getFormField(Form.INITIALWSDL).addFormFieldListener(new XFormFieldListener() {
                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {

                    dialog.getFormField(Form.GENERATETESTSUITE).setEnabled(newValue.trim().length() > 0);
                }
            });
        } else {
            dialog.setValue(Form.INITIALWSDL, "");
            dialog.getFormField(Form.GENERATETESTSUITE).setEnabled(false);
        }
    }

    private RestService importWadl(WsdlProject project, String url) {
        RestService restService = (RestService) project
                .addNewInterface(project.getName(), RestServiceFactory.REST_TYPE);
        UISupport.select(restService);
        try {
            new WadlImporter(restService).initFromWadl(url);
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }

        Analytics.trackAction(SoapUIActions.IMPORT_WADL.getActionName());

        return restService;
    }

    @AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    public interface Form {
        @AField(description = "Form.InitialWadl.Description", type = AFieldType.FILE)
        public final static String INITIALWSDL = messages.get("Form.InitialWadl.Label");

        @AField(description = "Form.GenerateTestSuite.Description", type = AFieldType.BOOLEAN, enabled = false)
        public final static String GENERATETESTSUITE = messages.get("Form.GenerateTestSuite.Label");
    }
}
