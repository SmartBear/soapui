/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

package com.eviware.soapui.impl.rest.actions.resource;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;


/**
 * Actions for importing an existing SoapUI project file into the current
 * workspace
 *
 * @author Ole.Matzura
 */

public class NewRestMethodAction extends AbstractSoapUIAction<RestResource> {
    public static final String SOAPUI_ACTION_ID = "NewRestMethodAction";
    public static final MessageSupport messages = MessageSupport.getMessages(NewRestMethodAction.class);
    private XFormDialog dialog;

    public NewRestMethodAction() {
        super(messages.get("title"), messages.get("description"));
    }

    public void perform(RestResource resource, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
            dialog.setBooleanValue(Form.CREATEREQUEST, true);
        }

        dialog.setValue(Form.RESOURCENAME, "Method " + (resource.getRestMethodCount() + 1));

        XmlBeansRestParamsTestPropertyHolder params;
        if (param instanceof XmlBeansRestParamsTestPropertyHolder) {
            params = (XmlBeansRestParamsTestPropertyHolder) param;
        } else {
            params = new XmlBeansRestParamsTestPropertyHolder(null, RestParametersConfig.Factory.newInstance(), ParamLocation.METHOD);
        }


        RestParamsTableModel paramsTableModel = new RestParamsTableModel(params, RestParamsTableModel.Mode.MEDIUM);
        RestParamsTable paramsTable = new RestParamsTable(params, false, paramsTableModel, ParamLocation.METHOD, true, false);

        dialog.getFormField(Form.PARAMSTABLE).setProperty("component", paramsTable);

        if (dialog.show()) {
            RestMethod method = resource.addNewMethod(dialog.getValue(Form.RESOURCENAME));
            method.setMethod(RestRequestInterface.HttpMethod.valueOf(dialog.getValue(Form.METHOD)));
            paramsTable.extractParams(method.getParams(), ParamLocation.METHOD);
            method.addPropertyChangeListener(paramsTableModel);
            UISupport.select(method);

            if (dialog.getBooleanValue(Form.CREATEREQUEST)) {
                createRequest(method, method.getParams());
            }
        }
    }

    protected void createRequest(RestMethod method, RestParamsPropertyHolder params) {
        RestRequest request = method.addNewRequest("Request " + (method.getRequestCount() + 1));
        for (TestProperty param : params.getProperties().values()) {
            ((RestParamProperty) param).addPropertyChangeListener(request);
        }

        UISupport.showDesktopPanel(request);
    }

    @AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWRESTSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    public interface Form {
        @AField(description = "Form.ResourceName.Description", type = AFieldType.STRING)
        public final static String RESOURCENAME = messages.get("Form.ResourceName.Label");

        @AField(description = "Form.Method.Description", type = AFieldType.ENUMERATION, values = {"GET", "POST", "PUT",
                "DELETE", "HEAD", "PATCH", "PROPFIND", "LOCK", "UNLOCK", "COPY", "PURGE"})
        public final static String METHOD = messages.get("Form.Method.Label");

        @AField(description = "Form.ParamsTable.Description", type = AFieldType.COMPONENT)
        public final static String PARAMSTABLE = messages.get("Form.ParamsTable.Label");

        @AField(description = "Form.CreateRequest.Description", type = AFieldType.BOOLEAN)
        public final static String CREATEREQUEST = messages.get("Form.CreateRequest.Label");
    }
}
