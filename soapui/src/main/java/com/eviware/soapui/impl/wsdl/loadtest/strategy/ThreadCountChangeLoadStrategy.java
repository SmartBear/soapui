/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.loadtest.strategy;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTestRunner;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.ComponentBag;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * LoadStrategy allowing maximum runs and request delays
 *
 * @author Ole.Matzura
 */

public class ThreadCountChangeLoadStrategy extends AbstractLoadStrategy {
    private final static Logger log = LogManager.getLogger(ThreadCountChangeLoadStrategy.class);

    private static final int DEFAULT_END_THREAD_COUNT = 10;
    private static final int DEFAULT_START_THREAD_COUNT = 1;
    public static final String STRATEGY_TYPE = "Thread";

    private int startThreadCount = DEFAULT_START_THREAD_COUNT;
    private int endThreadCount = DEFAULT_END_THREAD_COUNT;

    private JPanel configPanel;
    private ComponentBag stateDependantComponents = new ComponentBag();

    private SpinnerNumberModel startThreadCountSpinnerNumberModel;
    private JSpinner startThreadCountSpinner;
    private SpinnerNumberModel endThreadCountSpinnerNumberModel;
    private JSpinner endThreadCountSpinner;

    public ThreadCountChangeLoadStrategy(XmlObject config, WsdlLoadTest loadTest) {
        super(STRATEGY_TYPE, loadTest);

        if (config != null) {
            XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(config);
            startThreadCount = reader.readInt("startThreadCount", DEFAULT_START_THREAD_COUNT);
            endThreadCount = reader.readInt("endThreadCount", DEFAULT_END_THREAD_COUNT);
        }
    }

    public XmlObject getConfig() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add("startThreadCount", startThreadCount);
        builder.add("endThreadCount", endThreadCount);
        return builder.finish();
    }

    public void beforeLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
        super.beforeLoadTest(loadTestRunner, context);
        stateDependantComponents.setEnabled(false);

        WsdlLoadTest wsdlLoadTest = ((WsdlLoadTest) loadTestRunner.getLoadTest());
        wsdlLoadTest.setThreadCount(startThreadCount);
    }

    public void afterLoadTest(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
        stateDependantComponents.setEnabled(true);
    }

    public boolean allowThreadCountChangeDuringRun() {
        return false;
    }

    @Override
    public void recalculate(LoadTestRunner loadTestRunner, LoadTestRunContext context) {
        // calculate thread count
        WsdlLoadTestRunner runner = (WsdlLoadTestRunner) loadTestRunner;
        float progress = runner.getProgress();
        if ((int) progress != -1) {
            WsdlLoadTest wsdlLoadTest = ((WsdlLoadTest) loadTestRunner.getLoadTest());
            synchronized (wsdlLoadTest) {
                int newThreadCount = (int) (startThreadCount + (progress * (endThreadCount - startThreadCount) + 0.5));
                if (newThreadCount != wsdlLoadTest.getThreadCount() && newThreadCount <= endThreadCount) {
                    log.debug("Changing threadcount to " + newThreadCount + ", progress = " + progress);
                    wsdlLoadTest.setThreadCount(newThreadCount);
                }
            }
        }
    }

    public JComponent getConfigurationPanel() {
        if (configPanel == null) {
            ButtonBarBuilder builder = new ButtonBarBuilder();

            startThreadCountSpinnerNumberModel = new SpinnerNumberModel(startThreadCount, 1, 10000, 1);
            startThreadCountSpinner = new JSpinner(startThreadCountSpinnerNumberModel);
            UISupport.setPreferredHeight(startThreadCountSpinner, 18);
            startThreadCountSpinner.setToolTipText("Sets the initial thread-count");
            startThreadCountSpinnerNumberModel.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    startThreadCount = startThreadCountSpinnerNumberModel.getNumber().intValue();
                    notifyConfigurationChanged();
                }
            });

            builder.addFixed(new JLabel("Start Threads"));
            builder.addRelatedGap();

            builder.addFixed(startThreadCountSpinner);
            builder.addRelatedGap();

            endThreadCountSpinnerNumberModel = new SpinnerNumberModel(endThreadCount, 1, 10000, 1);
            endThreadCountSpinner = new JSpinner(endThreadCountSpinnerNumberModel);
            UISupport.setPreferredHeight(endThreadCountSpinner, 18);
            endThreadCountSpinner.setToolTipText("Sets the final thread-count");
            endThreadCountSpinnerNumberModel.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    endThreadCount = endThreadCountSpinnerNumberModel.getNumber().intValue();
                    notifyConfigurationChanged();
                }
            });

            builder.addFixed(new JLabel("End Threads"));
            builder.addRelatedGap();
            builder.addFixed(endThreadCountSpinner);

            configPanel = builder.getPanel();

            stateDependantComponents.add(startThreadCountSpinner);
            stateDependantComponents.add(endThreadCountSpinner);
        }

        return configPanel;
    }

    /**
     * Factory for ThreadCountChangeLoadStrategy class
     *
     * @author Ole.Matzura
     */

    public static class Factory implements LoadStrategyFactory {
        public String getType() {
            return STRATEGY_TYPE;
        }

        public LoadStrategy build(XmlObject config, WsdlLoadTest loadTest) {
            return new ThreadCountChangeLoadStrategy(config, loadTest);
        }

        public LoadStrategy create(WsdlLoadTest loadTest) {
            return new ThreadCountChangeLoadStrategy(null, loadTest);
        }
    }

    public int getStartThreadCount() {
        return startThreadCount;
    }

    public int getEndThreadCount() {
        return endThreadCount;
    }

}
