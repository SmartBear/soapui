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

package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * @author Joel
 */
public class RestParamsTableTest {
    private JTable paramTable;
    private RestParamsPropertyHolder params;

    @Before
    public void setUp() throws Exception {
        params = ModelItemFactory.makeRestRequest().getParams();
        paramTable = new RestParamsTable(params, false, new RestParamsTableModel(params, RestParamsTableModel.Mode.FULL), NewRestResourceActionBase.ParamLocation.RESOURCE, false, false).paramsTable;
    }

    @Test
    public void disallowsTemplateParameterForMethodLevel() throws Exception {
        RestParamProperty prop = params.addProperty("prop");
        prop.setParamLocation(NewRestResourceActionBase.ParamLocation.METHOD);
        List<RestParamsPropertyHolder.ParameterStyle> availableStyles = getParameterStyles();
        assertThat(availableStyles, not(hasItem(RestParamsPropertyHolder.ParameterStyle.TEMPLATE)));
    }

    @Test
    public void allowsTemplateParameterForResourceLevel() throws Exception {
        RestParamProperty prop = params.addProperty("prop");
        prop.setParamLocation(NewRestResourceActionBase.ParamLocation.RESOURCE);
        List<RestParamsPropertyHolder.ParameterStyle> availableStyles = getParameterStyles();
        assertThat(availableStyles, hasItem(RestParamsPropertyHolder.ParameterStyle.TEMPLATE));
    }

    @Test
    public void disallowsMethodLocationForTemplateParameter() throws Exception {
        RestParamProperty prop = params.addProperty("prop");
        prop.setParamLocation(NewRestResourceActionBase.ParamLocation.RESOURCE);
        prop.setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        List<NewRestResourceActionBase.ParamLocation> availableLocations = getParameterLocations();
        assertThat(availableLocations, not(hasItem(NewRestResourceActionBase.ParamLocation.METHOD)));
    }

    @Test
    public void disallowsMethodLocationForTemplateParameterOnMethodLevel() throws Exception {
        RestParamProperty prop = params.addProperty("prop");
        prop.setParamLocation(NewRestResourceActionBase.ParamLocation.METHOD);
        prop.setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
        List<NewRestResourceActionBase.ParamLocation> availableLocations = getParameterLocations();
        assertThat(availableLocations, not(hasItem(NewRestResourceActionBase.ParamLocation.METHOD)));
    }

    @Test
    public void allowsMethodLocationForQueryParameter() throws Exception {
        RestParamProperty prop = params.addProperty("prop");
        prop.setParamLocation(NewRestResourceActionBase.ParamLocation.RESOURCE);
        prop.setStyle(RestParamsPropertyHolder.ParameterStyle.QUERY);
        List<NewRestResourceActionBase.ParamLocation> availableLocations = getParameterLocations();
        assertThat(availableLocations, hasItem(NewRestResourceActionBase.ParamLocation.METHOD));
    }

    private List<RestParamsPropertyHolder.ParameterStyle> getParameterStyles() {
        paramTable.editCellAt(0, RestParamsTableModel.STYLE_COLUMN_INDEX);
        DefaultCellEditor cellEditor = (DefaultCellEditor) paramTable.getCellEditor(0, RestParamsTableModel.STYLE_COLUMN_INDEX);
        JComboBox comboBox = (JComboBox) cellEditor.getComponent();
        return getSelectableValues(comboBox);
    }

    private List<NewRestResourceActionBase.ParamLocation> getParameterLocations() {
        paramTable.editCellAt(0, RestParamsTableModel.LOCATION_COLUMN_INDEX);
        DefaultCellEditor cellEditor = (DefaultCellEditor) paramTable.getCellEditor(0, RestParamsTableModel.LOCATION_COLUMN_INDEX);
        JComboBox comboBox = (JComboBox) cellEditor.getComponent();
        return getSelectableValues(comboBox);
    }

    private <T> List<T> getSelectableValues(JComboBox comboBox) {
        List<T> availableStyles = new ArrayList<T>();
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            availableStyles.add((T) comboBox.getItemAt(i));
        }
        return availableStyles;
    }
}
