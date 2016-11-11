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

package com.eviware.soapui.impl.wsdl.panels.loadtest;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics.Statistic;
import com.eviware.soapui.impl.wsdl.loadtest.data.actions.ExportSamplesHistoryAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * DesktopPanel for StatisticsHistory Graphs
 *
 * @author Ole.Matzura
 */

public class StatisticsHistoryDesktopPanel extends DefaultDesktopPanel {
    private JPanel panel;
    private final WsdlLoadTest loadTest;
    private JStatisticsHistoryGraph historyGraph;
    private JButton exportButton;
    private JComboBox selectStatisticCombo;
    private StatisticsHistoryDesktopPanel.InternalPropertyChangeListener propertyChangeListener;
    private JComboBox resolutionCombo;

    public StatisticsHistoryDesktopPanel(WsdlLoadTest loadTest) {
        super("Statistics History for [" + loadTest.getName() + "]", null, null);
        this.loadTest = loadTest;

        propertyChangeListener = new InternalPropertyChangeListener();
        loadTest.addPropertyChangeListener(WsdlLoadTest.NAME_PROPERTY, propertyChangeListener);

        buildUI();
    }

    private void buildUI() {
        historyGraph = new JStatisticsHistoryGraph(loadTest);

        JScrollPane scrollPane = new JScrollPane(historyGraph);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        panel = UISupport.buildPanelWithToolbarAndStatusBar(buildToolbar(), scrollPane, historyGraph.getLegend());
        panel.setPreferredSize(new Dimension(600, 400));
    }

    private JComponent buildToolbar() {
        exportButton = UISupport.createToolbarButton(new ExportSamplesHistoryAction(historyGraph));

        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.addSpace(5);
        toolbar.addLabeledFixed("Select Statistic:", buildSelectStatisticCombo());
        toolbar.addUnrelatedGap();
        toolbar.addLabeledFixed("Resolution:", buildResolutionCombo());
        toolbar.addGlue();
        toolbar.addFixed(exportButton);
        toolbar.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.STATISTICSGRAPH_HELP_URL)));

        return toolbar;
    }

    private JComponent buildResolutionCombo() {
        resolutionCombo = new JComboBox(new String[]{"data", "250", "500", "1000"});
        resolutionCombo.setEditable(true);
        resolutionCombo.setToolTipText("Sets update interval of graph in milliseconds");
        long resolution = historyGraph.getResolution();
        resolutionCombo.setSelectedItem(resolution == 0 ? "data" : String.valueOf(resolution));
        resolutionCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                try {
                    String value = resolutionCombo.getSelectedItem().toString();
                    long resolution = value.equals("data") ? 0 : Long.parseLong(value);
                    if (resolution != historyGraph.getResolution()) {
                        historyGraph.setResolution(resolution);
                    }
                } catch (Exception ex) {
                    long resolution = historyGraph.getResolution();
                    resolutionCombo.setSelectedItem(resolution == 0 ? "data" : String.valueOf(resolution));
                }
            }
        });
        return resolutionCombo;
    }

    private JComponent buildSelectStatisticCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(Statistic.AVERAGE);
        model.addElement(Statistic.TPS);
        model.addElement(Statistic.ERRORS);
        model.addElement(Statistic.BPS);

        selectStatisticCombo = new JComboBox(model);
        selectStatisticCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                historyGraph.setStatistic(Statistic.valueOf(selectStatisticCombo.getSelectedItem().toString()));
            }
        });

        return selectStatisticCombo;
    }

    public JComponent getComponent() {
        return panel;
    }

    public boolean onClose(boolean canCancel) {
        loadTest.removePropertyChangeListener(WsdlLoadTest.NAME_PROPERTY, propertyChangeListener);
        historyGraph.release();

        return super.onClose(canCancel);
    }

    private final class InternalPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            setTitle("Statistics History for [" + loadTest.getName() + "]");
        }
    }
}
