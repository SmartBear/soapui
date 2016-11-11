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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * TestStep that delays execution for a number of milliseconds
 *
 * @author ole.matzura
 */

public class WsdlDelayTestStep extends WsdlTestStepWithProperties implements PropertyExpansionContainer {
    private static final String DEFAULT_DELAY = "1000";
    private static final int DELAY_CHUNK = 100;
    private int delay = 0;
    private String delayString = WsdlDelayTestStep.DEFAULT_DELAY;
    private int timeWaited = 0;
    private boolean canceled;
    private boolean running;

    public WsdlDelayTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        super(testCase, config, false, forLoadTest);

        if (!forLoadTest) {
            setIcon(UISupport.createImageIcon("/delay_step.png"));
        }

        if (config.getConfig() == null) {
            if (!forLoadTest) {
                saveDelay(config);
            }
        } else {
            readConfig(config);
        }

        addProperty(new DefaultTestStepProperty("delay", true, new DefaultTestStepProperty.PropertyHandlerAdapter() {

            @Override
            public String getValue(DefaultTestStepProperty property) {
                return getDelayString();
            }

            @Override
            public void setValue(DefaultTestStepProperty property, String value) {
                setDelayString(value);
            }
        }, this));
    }

    private void readConfig(TestStepConfig config) {
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(config.getConfig());
        delayString = reader.readString("delay", DEFAULT_DELAY);
    }

    @Override
    public String getLabel() {
        String str = running ? super.getName() + " [" + (delay - timeWaited) + "ms]" : super.getName() + " ["
                + delayString + "]";

        if (isDisabled()) {
            str += " (disabled)";
        }

        return str;
    }

    @Override
    public String getDefaultSourcePropertyName() {
        return "delay";
    }

    @Override
    public String getDefaultTargetPropertyName() {
        return "delay";
    }

    public PropertyExpansion[] getPropertyExpansions() {
        List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(this, this, "delayString"));
        return result.toArray(new PropertyExpansion[result.size()]);
    }

    private void saveDelay(TestStepConfig config) {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add("delay", delayString);
        config.setConfig(builder.finish());
    }

    @Override
    public void resetConfigOnMove(TestStepConfig config) {
        super.resetConfigOnMove(config);
        readConfig(config);
    }

    public void setDelayString(String delayString) {
        if (this.delayString.equals(delayString)) {
            return;
        }

        String oldLabel = getLabel();

        this.delayString = delayString;
        saveDelay(getConfig());
        notifyPropertyChanged(WsdlTestStep.LABEL_PROPERTY, oldLabel, getLabel());
        // FIXME This should not be hard coded
        firePropertyValueChanged("delay", oldLabel, getLabel());
    }

    public String getDelayString() {
        return delayString;
    }

    public int getDelay() {
        try {
            return Integer.parseInt(PropertyExpander.expandProperties(this, delayString));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void setDelay(int delay) {
        String oldLabel = getLabel();

        this.delayString = String.valueOf(delay);
        saveDelay(getConfig());
        notifyPropertyChanged(WsdlTestStep.LABEL_PROPERTY, oldLabel, getLabel());
        firePropertyValueChanged("delay", oldLabel, getLabel());
    }

    public TestStepResult run(TestCaseRunner testRunner, TestCaseRunContext context) {
        WsdlTestStepResult result = new WsdlTestStepResult(this);
        result.startTimer();
        String oldLabel = getLabel();

        try {
            canceled = false;
            running = true;

            try {
                delay = Integer.parseInt(PropertyExpander.expandProperties(context, delayString));
            } catch (NumberFormatException e) {
                delay = Integer.parseInt(DEFAULT_DELAY);
            }

            // sleep in chunks for canceling
            final long stopTime = System.currentTimeMillis() + delay;
            int lastUpdate = 0;
            while (!canceled && timeWaited < delay) {
                if (timeWaited - lastUpdate > 1000 && context.getProperty(TestCaseRunContext.LOAD_TEST_RUNNER) == null) {
                    String newLabel = getLabel();
                    if (SoapUI.usingGraphicalEnvironment()) {
                        final String finalOldLabel = oldLabel, finalNewLabel = newLabel;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                notifyPropertyChanged(WsdlTestStep.LABEL_PROPERTY, finalOldLabel, finalNewLabel);
                            }
                        });
                    }
                    oldLabel = newLabel;
                    lastUpdate = timeWaited;
                }

                Thread.sleep(Math.min(DELAY_CHUNK, delay - timeWaited));
                timeWaited = delay - (int) ((stopTime - System.currentTimeMillis()));
            }
        } catch (InterruptedException e) {
            SoapUI.logError(e);
        }

        result.stopTimer();
        result.setStatus(canceled ? TestStepStatus.CANCELED : TestStepStatus.OK);

        timeWaited = 0;
        running = false;

        if (context.getProperty(TestCaseRunContext.LOAD_TEST_RUNNER) == null) {
            notifyPropertyChanged(WsdlTestStep.LABEL_PROPERTY, oldLabel, getLabel());
        }

        return result;
    }

    @Override
    public boolean cancel() {
        canceled = true;
        return true;
    }
}
