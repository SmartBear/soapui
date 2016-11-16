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

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.JTableFactory;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public class MetricsPanel extends JPanel {
    private Map<String, Metric> metrics = new HashMap<String, Metric>();
    private Map<String, MetricsSection> sections = new HashMap<String, MetricsSection>();

    public MetricsPanel() {
        super(new VerticalLayout());
        setBackground(Color.WHITE);
    }

    public MetricsSection addSection(String name) {
        MetricsSection section = new MetricsSection(name);
        sections.put(name, section);
        add(section);
        return section;
    }

    public enum MetricType {
        STRING, URL
    }

    ;

    public class Metric {
        private final JLabel label;

        public Metric(JLabel label) {
            this.label = label;
        }

        public void set(String value) {
            label.setText(value);
        }

        public void set(int value) {
            set(String.valueOf(value));
        }
    }

    public class MetricsSection extends JCollapsiblePanel {
        private MetricsForm form;

        public MetricsSection(String name) {
            super(name);

            form = new MetricsForm();
            setContentPanel(form.getPanel());
        }

        public Metric addMetric(ImageIcon icon, String label, MetricType type) {
            return form.addMetric(label, icon, type == MetricType.URL);
        }

        public Metric addMetric(ImageIcon icon, String label) {
            return addMetric(icon, label, MetricType.STRING);
        }

        public Metric addMetric(String label) {
            return addMetric(null, label, MetricType.STRING);
        }

        public void finish() {
            form.finish();
        }

        public MetricsSection clear() {
            form = new MetricsForm();
            setContentPanel(form.getPanel());

            return this;
        }

        public Metric addMetric(String label, MetricType type) {
            return addMetric(null, label, type);
        }

        public JXTable addTable(TableModel model) {
            JXTable table = JTableFactory.getInstance().makeJXTable(model);
            table.setBorder(null);
            table.setShowGrid(false);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSortable(false);
            table.getColumn(0).setWidth(195);
            table.getColumn(0).setMinWidth(195);

            InternalHeaderRenderer internalHeaderRenderer = new InternalHeaderRenderer(table.getTableHeader()
                    .getBackground());
            InternalCellRenderer internalCellRenderer = new InternalCellRenderer();

            for (int c = 0; c < table.getColumnCount(); c++) {
                table.getColumn(c).setHeaderRenderer(internalHeaderRenderer);
                table.getColumn(c).setCellRenderer(internalCellRenderer);
            }

            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setBackground(Color.WHITE);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            form.addComponent(scrollPane);
            table.setPreferredScrollableViewportSize(new Dimension(100, 250));
            scrollPane.setBackground(Color.WHITE);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setOpaque(true);

            table.setBackground(Color.WHITE);
            table.setOpaque(true);

            return table;
        }
    }

    public MetricsSection getSection(String name) {
        return sections.get(name);
    }

    public boolean setMetric(String label, int value) {
        return setMetric(label, String.valueOf(value));
    }

    public boolean setMetric(String label, String value) {
        if (!hasMetric(label)) {
            return false;
        }

        metrics.get(label).set(value);
        return true;
    }

    public boolean hasMetric(String name) {
        return metrics.containsKey(name);
    }

    private class MetricsForm extends SimpleForm {
        private Dimension labelDimensions = new Dimension(200, 16);

        public MetricsForm() {
            super();

            addSpace(7);
            setRowSpacing(3);
        }

        public JPanel finish() {
            addSpace(7);

            JPanel formPanel = getPanel();
            formPanel.setBackground(Color.WHITE);
            formPanel.setOpaque(true);

            return formPanel;
        }

        public Metric addMetric(String labelText, ImageIcon icon, boolean isHyperlink) {
            return addMetric(labelText, "", icon, isHyperlink);
        }

        public Metric addMetric(String labelText, ImageIcon icon) {
            return addMetric(labelText, "", icon, false);
        }

        public Metric addMetric(String labelText, String text, ImageIcon icon, boolean isHyperlink) {
            JLabel label = new JLabel(labelText, icon, SwingConstants.LEFT);
            UISupport.setFixedSize(label, labelDimensions);
            label.setIconTextGap(5);

            label.setBorder(BorderFactory.createEmptyBorder(2, icon == null ? 16 : 14, 0, 0));

            JLabel textField = null;

            if (isHyperlink) {
                textField = append(labelText, label, new JHyperlinkLabel(text));
            } else {
                textField = append(labelText, label, new JLabel(text));
            }

            textField.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            textField.setBackground(Color.WHITE);

            Metric metric = new Metric(textField);
            metrics.put(labelText, metric);
            return metric;
        }
    }

    public static class InternalHeaderRenderer extends DefaultTableCellRenderer {
        private Font boldFont;
        private final Color color;

        public InternalHeaderRenderer(Color color) {
            super();
            this.color = color;

            setHorizontalAlignment(SwingConstants.LEFT);
            boldFont = getFont().deriveFont(Font.BOLD);
        }

        public InternalHeaderRenderer() {
            this(null);
        }

        @Override
        public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
                                                       int arg5) {
            JComponent result = (JComponent) super.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);
            setFont(boldFont);
            if (color != null) {
                setBackground(color);
            }
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder(0, 2, 1, 2)));
            return result;
        }
    }

    private class InternalCellRenderer extends DefaultTableCellRenderer {
        public InternalCellRenderer() {
            super();

            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
                                                       int arg5) {
            Component result = super.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);
            setBorder(BorderFactory.createEmptyBorder(3, 1, 3, 2));
            return result;
        }
    }
}
