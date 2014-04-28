/*
 * Copyright 2004-2014 SmartBear Software
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
package com.eviware.soapui.impl;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.panels.project.WsdlProjectPanelBuilder;
import com.eviware.soapui.impl.wsdl.panels.teststeps.MockResponseStepPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.support.components.JPropertiesTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class GenericPanelBuilderTest {
    private static final int KEY_INDEX = 0;
    private static final int VALUE_INDEX = 1;

    private Class<? extends PanelBuilder> panelBuilderClass;
    private Class<? extends ModelItem> modelClass;

    public GenericPanelBuilderTest(Class panelBuilderClass, Class modelClass) {
        this.panelBuilderClass = panelBuilderClass;
        this.modelClass = modelClass;
    }

    @Test
    public void shouldMatchBuilderValuesWithModel() throws IllegalAccessException, InstantiationException {
        PanelBuilder builder = panelBuilderClass.newInstance();
        ModelItem model = mock(modelClass, RETURNS_MOCKS);
        JPropertiesTable table = (JPropertiesTable) builder.buildOverviewPanel(model);
        JPropertiesTable.PropertiesTableModel tableModel = table.getTableModel();

        int numberOfProperties = tableModel.getRowCount();
        for (int i = 0; i < numberOfProperties; i++) {
            assertOneProperty(builder, tableModel, i);
        }
    }

    private void assertOneProperty(PanelBuilder builder, JPropertiesTable.PropertiesTableModel tableModel, int index) {
        Object propertyValue = tableModel.getValueAt(index, VALUE_INDEX);

        // at this point I was expecting an exception but
        // the exception is swallowed and null is returned

        if (propertyValue == null) {
            Object key = tableModel.getValueAt(index, KEY_INDEX);
            fail(failureMessage(key));
        }
    }

    private String failureMessage(Object key) {
        String builderName = this.panelBuilderClass.getName();
        return "The panel builder " + builderName + " fails for the property " + key;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> panelModelCombinations() {
        return Arrays.asList(new Object[][]{
                {MockResponseStepPanelBuilder.class, WsdlMockResponseTestStep.class},
                {WsdlProjectPanelBuilder.class, WsdlProject.class}
                // add more panel builders here
        });
    }
}
