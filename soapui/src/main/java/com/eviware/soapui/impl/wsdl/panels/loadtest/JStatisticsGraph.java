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

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics.Statistic;
import com.eviware.soapui.impl.wsdl.loadtest.data.StatisticsHistory.StatisticsHistoryModel;
import com.eviware.soapui.model.testsuite.TestStep;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Scrollable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * Graphical representation of testschedule statistics
 *
 * @author Ole.Matzura
 */

public class JStatisticsGraph extends JComponent implements Scrollable {
    private static final Color THREADCOUNT_COLOR = Color.GREEN.darker();
    private static final Color AVERAGE_COLOR = Color.BLUE;
    private static final Color ERRORS_COLOR = Color.RED.darker();
    private static final Color TPS_COLOR = Color.BLACK;
    private static final Color BPS_COLOR = Color.ORANGE;
    private static final Color LAST_COLOR = Color.MAGENTA.brighter();

    private static final int SCROLL_AHEAD = 50;

    @SuppressWarnings("unused")
    private final WsdlLoadTest loadTest;
    private final LoadTestStatistics statisticsModel;
    private StatisticsHistoryModel data;
    private JComponent legend;
    private InternalTableModelListener tableModelListener = new InternalTableModelListener();
    private long[] maxValues;
    private float[] scales;

    public JStatisticsGraph(WsdlLoadTest loadTest) {
        this.loadTest = loadTest;
        this.statisticsModel = loadTest.getStatisticsModel();
        this.data = statisticsModel.getHistory().getTestStepHistory(LoadTestStatistics.TOTAL);

        setAutoscrolls(true);
        addMouseMotionListener(new InternalMouseMotionListener());

        data.addTableModelListener(tableModelListener);

        initMaxValues();
        initScales();

        setBackground(Color.WHITE);
        setOpaque(true);

        addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                initScales();
            }
        });
    }

    public TableModel getModel() {
        return data;
    }

    public void release() {
        data.removeTableModelListener(tableModelListener);
    }

    public void setTestStep(TestStep testStep) {
        if (data != null) {
            data.removeTableModelListener(tableModelListener);
            data.release();
        }

        if (testStep == null) {
            data = statisticsModel.getHistory().getTestStepHistory(LoadTestStatistics.TOTAL);
        } else {
            data = statisticsModel.getHistory().getTestStepHistory(testStep.getTestCase().getIndexOfTestStep(testStep));
        }

        initMaxValues();
        initScales();

        data.addTableModelListener(tableModelListener);

        getParent().invalidate();
        revalidate();
        repaint();
    }

    public long getResolution() {
        return statisticsModel.getHistory().getResolution();
    }

    public void setResolution(long resolution) {
        statisticsModel.getHistory().setResolution(resolution);
    }

    private void initMaxValues() {
        maxValues = new long[data.getColumnCount()];

        for (int c = 0; c < data.getRowCount(); c++) {
            for (int i = 0; i < data.getColumnCount(); i++) {
                long value = (Long) data.getValueAt(c, i);
                if (value > maxValues[i]) {
                    maxValues[i] = value;
                }
            }
        }
    }

    private void initScales() {
        scales = new float[maxValues.length];

        for (int c = 0; c < maxValues.length; c++) {
            recalcScale(c);
        }
    }

    private boolean recalcScale(int index) {
        float scale = (index == 0 || maxValues[index] == 0) ? 1 : (float) (getHeight())
                / (float) (maxValues[index] + 10);
        if (scale > 1) {
            scale = 1;
        }

        if (Float.compare(scale, scales[index]) == 0) {
            return false;
        }

        scales[index] = scale;
        return true;
    }

    public void paintComponent(Graphics g) {
        g.setColor(getBackground());

        Rectangle clip = g.getClipBounds();
        g.fillRect((int) clip.getX(), (int) clip.getY(), (int) clip.getWidth(), (int) clip.getHeight());

        double right = clip.getX() + clip.getWidth();
        int rowCount = data.getRowCount();
        int height = getHeight();

        for (int c = (int) clip.getX(); c < rowCount && c < right; c++) {
            for (int i = 0; i < data.getColumnCount(); i++) {
                if (i == 0) {
                    g.setColor(THREADCOUNT_COLOR);
                } else if (i == Statistic.AVERAGE.getIndex() + 1) {
                    g.setColor(AVERAGE_COLOR);
                } else if (i == Statistic.ERRORS.getIndex() + 1) {
                    g.setColor(ERRORS_COLOR);
                } else if (i == Statistic.TPS.getIndex() + 1) {
                    g.setColor(TPS_COLOR);
                } else if (i == Statistic.BPS.getIndex() + 1) {
                    g.setColor(BPS_COLOR);
                } else {
                    continue;
                }

                int yOffset = (int) ((float) ((Long) data.getValueAt(c, i)) * scales[i]);

                if (clip.contains(c, height - yOffset - 1)) {
                    g.drawLine(c, height - yOffset - 1, c, height - yOffset - 1);
                }
            }
        }
    }

    public JComponent getLegend() {
        if (legend == null) {
            buildLegend();
        }

        return legend;
    }

    private void buildLegend() {
        ButtonBarBuilder builder = new ButtonBarBuilder();

        builder.addFixed(new JLabel("ThreadCount", createLegendIcon(THREADCOUNT_COLOR), JLabel.LEFT));
        builder.addUnrelatedGap();
        builder.addFixed(new JLabel("Average (ms)", createLegendIcon(AVERAGE_COLOR), JLabel.LEFT));
        builder.addUnrelatedGap();
        builder.addFixed(new JLabel("ErrorCount", createLegendIcon(ERRORS_COLOR), JLabel.LEFT));
        builder.addUnrelatedGap();
        builder.addFixed(new JLabel("Transaction/Sec", createLegendIcon(TPS_COLOR), JLabel.LEFT));
        builder.addUnrelatedGap();
        builder.addFixed(new JLabel("Bytes/Sec", createLegendIcon(BPS_COLOR), JLabel.LEFT));

        builder.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        legend = builder.getPanel();
    }

    private Icon createLegendIcon(Color color) {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = image.getGraphics();
        g.setColor(color);
        g.fillRect(1, 1, 8, 8);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, 10, 10);
        return new ImageIcon(image);
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        int height = getHeight();
        int width = data.getRowCount() + SCROLL_AHEAD;
        return new Dimension(width, height);
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    private final class InternalTableModelListener implements TableModelListener {
        public synchronized void tableChanged(TableModelEvent e) {
            boolean repaint = false;

            if (e.getType() == TableModelEvent.INSERT) {
                int firstRow = e.getFirstRow();
                int lastRow = e.getLastRow();
                int height = getHeight();

                for (int c = firstRow; c <= lastRow; c++) {
                    for (int i = 0; i < data.getColumnCount(); i++) {
                        long value = (Long) data.getValueAt(c, i);

                        if (value > maxValues[i]) {
                            maxValues[i] = value;
                            repaint = recalcScale(i);
                        }
                    }
                }

                if (!repaint) {
                    Rectangle rect = new Rectangle(firstRow, 0, (lastRow - firstRow) + 1, height);
                    repaint(rect);
                }

                Dimension size = getSize();
                Rectangle r = getVisibleRect();

                double x2 = r.getX() + r.getWidth();
                if (x2 >= data.getRowCount() && x2 < data.getRowCount() + SCROLL_AHEAD) {
                    scrollRectToVisible(new Rectangle(firstRow + SCROLL_AHEAD / 2, 0, (lastRow - firstRow) + 1, height));
                }

                if (!repaint && size.getWidth() < data.getRowCount() + SCROLL_AHEAD) {
                    revalidate();
                }
            } else if (e.getType() == TableModelEvent.UPDATE) {
                initMaxValues();
                initScales();

                repaint = true;
            }

            if (repaint) {
                getParent().invalidate();
                revalidate();
                repaint();
            }
        }
    }

    private class InternalMouseMotionListener implements MouseMotionListener {
        public void mouseDragged(MouseEvent e) {
            Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
            scrollRectToVisible(r);
        }

        public void mouseMoved(MouseEvent e) {
        }
    }
}
