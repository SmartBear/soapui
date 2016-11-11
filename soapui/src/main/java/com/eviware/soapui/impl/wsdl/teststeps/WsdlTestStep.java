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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.propertyexpansion.MutablePropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for WSDL TestCase test steps.
 *
 * @author Ole.Matzura
 */

abstract public class WsdlTestStep extends AbstractWsdlModelItem<TestStepConfig> implements TestStep {
    private final WsdlTestCase testCase;
    private final boolean forLoadTest;
    private final boolean hasEditor;

    protected WsdlTestStep(WsdlTestCase testCase, TestStepConfig config, boolean hasEditor, boolean forLoadTest) {
        super(config, testCase, null);

        this.testCase = testCase;
        this.hasEditor = hasEditor;
        this.forLoadTest = forLoadTest;
    }

    public boolean hasEditor() {
        return hasEditor;
    }

    public boolean isForLoadTest() {
        return forLoadTest;
    }

    protected PanelBuilder<?> createPanelBuilder() {
        return null;
    }

    public WsdlTestCase getTestCase() {
        return testCase;
    }

    /**
     * Called from WsdlTestCase when moving a teststep due to no move
     * functionality in xmlbeans generated arrays.
     *
     * @param config the new config to use, will be a copy of the existing one. The
     *               current will be invalid
     */

    public void resetConfigOnMove(TestStepConfig config) {
        setConfig(config);
    }

    public boolean cancel() {
        return false;
    }

    public String getLabel() {
        String name = getName();
        if (isDisabled()) {
            return name + " (disabled)";
        } else {
            return name;
        }
    }

    @Override
    public void setName(String name) {
        if (getName().equals(name)) {
            return;
        }

        UISupport.setHourglassCursor();

        try {
            List<MutablePropertyExpansion> result = new ArrayList<MutablePropertyExpansion>();
            List<MutablePropertyExpansion> properties = new ArrayList<MutablePropertyExpansion>();

            PropertyExpansion[] propertyExpansions = PropertyExpansionUtils.getPropertyExpansions(getTestCase(), true,
                    true);
            for (PropertyExpansion pe : propertyExpansions) {
                MutablePropertyExpansion mpe = (MutablePropertyExpansion) pe;
                ModelItem modelItem = mpe.getProperty().getModelItem();
                if (modelItem == this
                        || ((modelItem instanceof WsdlTestRequest && ((WsdlTestRequest) modelItem).getTestStep() == this))) {
                    properties.add(mpe);
                }
            }

            String oldLabel = getLabel();
            super.setName(name);

            String label = getLabel();
            if (!oldLabel.equals(label)) {
                notifyPropertyChanged(LABEL_PROPERTY, oldLabel, label);
            }

            for (MutablePropertyExpansion mpe : properties) {
                try {
                    mpe.update();
                    result.add(mpe);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            UISupport.resetCursor();
        }
    }

    public boolean dependsOn(AbstractWsdlModelItem<?> modelItem) {
        return false;
    }

    public String getTestStepTitle() {
        return getTestCase().getTestSuite().getName() + "#" + getTestCase().getName();
    }

    /**
     * Called after cloning for custom behaviour
     *
     * @param targetTestCase step we were cloned from
     */

    public WsdlTestStep clone(WsdlTestCase targetTestCase, String name) {
        beforeSave();
        TestStepConfig newConfig = (TestStepConfig) getConfig().copy();
        newConfig.setName(name);
        WsdlTestStep result = targetTestCase.addTestStep(newConfig);
        ModelSupport.createNewIds(result);
        return result;
    }

    public void finish(TestCaseRunner testRunner, TestCaseRunContext testRunContext) {
    }

    public void prepare(TestCaseRunner testRunner, TestCaseRunContext testRunContext) throws Exception {
    }

    public Collection<Interface> getRequiredInterfaces() {
        return new ArrayList<Interface>();
    }

    public boolean isDisabled() {
        return getConfig().getDisabled();
    }

    public void setDisabled(boolean disabled) {
        String oldLabel = getLabel();

        boolean oldDisabled = isDisabled();
        if (oldDisabled == disabled) {
            return;
        }

        if (disabled) {
            getConfig().setDisabled(disabled);
        } else if (getConfig().isSetDisabled()) {
            getConfig().unsetDisabled();
        }

        notifyPropertyChanged(DISABLED_PROPERTY, oldDisabled, disabled);

        String label = getLabel();
        notifyPropertyChanged(LABEL_PROPERTY, oldLabel, label);
    }

    public ModelItem getModelItem() {
        return this;
    }

    public String getPropertiesLabel() {
        return "Custom Properties";
    }

    /**
     * Default property to use when creating property-transfers where this step
     * is source
     */

    public String getDefaultSourcePropertyName() {
        return null;
    }

    /**
     * Default property to use when creating property-transfers where this step
     * is target
     */

    public String getDefaultTargetPropertyName() {
        return null;
    }

    public void afterCopy(WsdlTestSuite oldTestSuite, WsdlTestCase oldTestCase) {
    }
}
