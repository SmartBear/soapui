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

package com.eviware.soapui.impl.rest.panels.request.views.content;


import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel;

public class RestTestRequestContentView extends RestRequestContentView {

    public RestTestRequestContentView(AbstractHttpXmlRequestDesktopPanel.HttpRequestMessageEditor restRequestMessageEditor, RestRequestInterface restRequest) {
        super(restRequestMessageEditor, restRequest);
    }

    @Override
    protected RestParamsTable buildParamsTable() {
        RestParamsTableModel restTestParamsTableModel = new RestParamsTableModel(super.getRestRequest().getParams()) {
            public int getColumnCount() {
                return 4;
            }

            @Override
            public void setValueAt(Object value, int rowIndex, int columnIndex) {
                RestParamProperty prop = params.getProperty((String) getValueAt(rowIndex, 0));
                if (columnIndex == 1) {
                    prop.setValue(value.toString());
                }
            }

            @Override
            public String getColumnName(int columnIndex) {
                if (columnIndex == 1) {
                    return "Value";
                }

                return super.getColumnName(columnIndex);
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                // Only value is editable
                return columnIndex == 1;
            }
        };

        return new RestParamsTable(super.getRestRequest().getParams(), false, restTestParamsTableModel, NewRestResourceActionBase.ParamLocation.RESOURCE, false, true);
    }

}
