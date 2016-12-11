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

import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JInspectorPanelImpl extends JPanel implements PropertyChangeListener, JInspectorPanel {
    private float defaultDividerLocation = 0.65F;

    private final JSplitPane mainSplit;
    private JPanel inspectorPanel;
    private int lastDividerLocation = 0;
    private JXToolBar inspectToolbar;
    private List<Inspector> inspectors = new ArrayList<Inspector>();
    private Map<Inspector, JToggleButton> inspectorButtons = new HashMap<Inspector, JToggleButton>();
    public Inspector currentInspector;

    private final int orientation;

    public JInspectorPanelImpl(JComponent contentComponent) {
        this(contentComponent, SwingConstants.BOTTOM);
    }

    public JInspectorPanelImpl(JComponent contentComponent, int orientation) {
        super(new BorderLayout());
        this.orientation = orientation;

        inspectorPanel = new JPanel(new CardLayout());
        inspectorPanel.setVisible(false);

        mainSplit = new JSplitPane(
                orientation == SwingConstants.LEFT || orientation == SwingConstants.RIGHT ? JSplitPane.HORIZONTAL_SPLIT
                        : JSplitPane.VERTICAL_SPLIT);
        BasicSplitPaneUI basic = (BasicSplitPaneUI) mainSplit.getUI();
        basic.getDivider().setBorder(new LineBorder(Color.WHITE, 1) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(c.getWidth() - 1, 0, c.getWidth() - 1, c.getHeight());
            }

        });
        mainSplit.setDividerSize(5);
        mainSplit.setBorder(null);
        mainSplit.setOneTouchExpandable(false);
        JXToolBar toolbar = createInspectButtons();
        if (orientation == SwingConstants.BOTTOM) {
            mainSplit.setTopComponent(contentComponent);
            mainSplit.setBottomComponent(inspectorPanel);
            mainSplit.setResizeWeight(0.8);
            toolbar.setBorder(BorderFactory.createEmptyBorder(1, 2, 3, 2));
            add(toolbar, BorderLayout.SOUTH);
        } else if (orientation == SwingConstants.LEFT) {
            mainSplit.setRightComponent(contentComponent);
            JPanel p = new JPanel(new BorderLayout());
            p.add(toolbar);
            p.setBorder(new LineBorder(Color.WHITE, 1) {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine(c.getWidth() - 1, 0, c.getWidth() - 1, c.getHeight());
                }
            });
            toolbar.setBorder(BorderFactory.createEmptyBorder(2, 3, 0, 4));
            mainSplit.setLeftComponent(inspectorPanel);
            mainSplit.setResizeWeight(0.2);
            toolbar.setOrientation(JToolBar.VERTICAL);
            add(p, BorderLayout.WEST);
        } else if (orientation == SwingConstants.RIGHT) {
            mainSplit.setLeftComponent(contentComponent);

            JPanel p = new JPanel(new BorderLayout());
            p.add(toolbar);
            toolbar.setBorder(BorderFactory.createEmptyBorder(2, 1, 0, 3));
            mainSplit.setRightComponent(inspectorPanel);
            mainSplit.setResizeWeight(0.8);
            toolbar.setOrientation(JToolBar.VERTICAL);
            add(p, BorderLayout.EAST);
        }

        add(mainSplit, BorderLayout.CENTER);
    }

    private JXToolBar createInspectButtons() {
        inspectToolbar = new JXToolBar() {
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(10, 10);
            }
        };

        inspectToolbar.setFloatable(false);
        inspectToolbar.setRollover(true);
        inspectToolbar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE);
        inspectToolbar.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        if (orientation == SwingConstants.TOP || orientation == SwingConstants.BOTTOM) {
            inspectToolbar.addSpace(10);
        }
        inspectToolbar.setBackground(Color.WHITE);
        inspectToolbar.setOpaque(true);
        return inspectToolbar;
    }

    public float getDefaultDividerLocation() {
        return defaultDividerLocation;
    }

    public void setDefaultDividerLocation(float defaultDividerLocation) {
        this.defaultDividerLocation = defaultDividerLocation;
        setResetDividerLocation();
    }

    public <T extends Inspector> T addInspector(final T inspector) {
        if (inspectors.size() > 0) {
            inspectToolbar.addSpace(5);
        }

        inspectors.add(inspector);
        inspector.addPropertyChangeListener(JInspectorPanelImpl.this);
        inspectorPanel.add(inspector.getComponent(), inspector.getInspectorId());
        JToggleButton button = new JToggleButton(new SelectInspectorAction(inspector));
        button.setName(inspector.getInspectorId());

        inspectorButtons.put(inspector, button);
        if (orientation == SwingConstants.LEFT) {
            String text = button.getText();
            button.setText(null);
            button.setPreferredSize(new Dimension(17, 10));
            button.setIcon(new VTextIcon(inspectToolbar, text, VTextIcon.ROTATE_LEFT));
            inspectToolbar.add(button);
        } else if (orientation == SwingConstants.RIGHT) {
            String text = button.getText();
            button.setText(null);
            button.setPreferredSize(new Dimension(17, 10));
            button.setIcon(new VTextIcon(inspectToolbar, text, VTextIcon.ROTATE_RIGHT));
            inspectToolbar.add(button);
        } else {
            inspectToolbar.add(button);
        }

        button.setMinimumSize(new Dimension(10, 10));

        inspectToolbar.invalidate();
        repaint();

        return inspector;
    }

    public Inspector getInspector(String inspectorId) {
        for (Inspector inspector : inspectors) {
            if (inspector.getInspectorId().equals(inspectorId)) {
                return inspector;
            }
        }

        return null;
    }

    public Inspector getInspectorByTitle(String title) {
        for (Inspector inspector : inspectors) {
            if (inspector.getTitle().equals(title)) {
                return inspector;
            }
        }

        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Inspector.ENABLED_PROPERTY)) {
            JToggleButton toggleButton = inspectorButtons.get(evt.getSource());
            if (toggleButton != null) {
                toggleButton.setEnabled((Boolean) evt.getNewValue());
            }
        }
    }

    public JComponent getComponent() {
        return this;
    }

    public class SelectInspectorAction extends AbstractAction implements PropertyChangeListener {
        private final Inspector inspector;

        public SelectInspectorAction(Inspector inspector) {
            super(inspector.getTitle());
            this.inspector = inspector;

            putValue(AbstractAction.SHORT_DESCRIPTION, inspector.getDescription());
            putValue(AbstractAction.SMALL_ICON, inspector.getIcon());
            setEnabled(inspector.isEnabled());

            inspector.addPropertyChangeListener(this);
        }

        public void actionPerformed(ActionEvent arg0) {
            JToggleButton button = inspectorButtons.get(inspector);
            if (!button.isSelected()) {
                deactivate();
                // currentInspector = null;
                // button.setBackground( inspectToolbar.getBackground() );
                // lastDividerLocation = mainSplit.getDividerLocation();
                // inspectorPanel.setVisible( false );
            } else {
                activate(inspector);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Inspector.TITLE_PROPERTY)) {
                putValue(AbstractAction.NAME, evt.getNewValue());
            } else if (evt.getPropertyName().equals(Inspector.ICON_PROPERTY)) {
                putValue(AbstractAction.SMALL_ICON, evt.getNewValue());
            } else if (evt.getPropertyName().equals(Inspector.DESCRIPTION_PROPERTY)) {
                putValue(AbstractAction.SHORT_DESCRIPTION, evt.getNewValue());
            } else if (evt.getPropertyName().equals(Inspector.ENABLED_PROPERTY)) {
                boolean enable = ((Boolean) evt.getNewValue()).booleanValue();
                setEnabled(enable);

                if (!enable && currentInspector == inspector) {
                    inspectorButtons.get(currentInspector).setSelected(false);
                }
            }
        }
    }

    public void release() {
        for (Inspector inspector : inspectors) {
            inspector.removePropertyChangeListener(this);
            inspector.release();
        }

        inspectors.clear();
        inspectorPanel.removeAll();
        mainSplit.removeAll();
    }

    public List<Inspector> getInspectors() {
        return inspectors;
    }

    public Inspector getCurrentInspector() {
        return currentInspector;
    }

    public void setInspectorsVisible(boolean b) {
        inspectorPanel.setVisible(b);
    }

    public void setInspectorVisible(Inspector inspector, boolean b) {
        if (inspectorButtons.containsKey(inspector)) {
            if (!b && inspector == currentInspector) {
                activate(null);
            }

            inspectorButtons.get(inspector).setVisible(b);
        }
    }

    public void setToolbarVisible(boolean b) {
        inspectToolbar.setVisible(b);
    }

    public double getResizeWeight() {
        return mainSplit.getResizeWeight();
    }

    public void setResizeWeight(double value) {
        mainSplit.setResizeWeight(value);
    }

    public int getDividerLocation() {
        return mainSplit.getDividerLocation();
    }

    public void setResetDividerLocation() {
        mainSplit.setDividerLocation(defaultDividerLocation);
    }

    public void setDividerLocation(int dividerLocation) {
        mainSplit.setDividerLocation(dividerLocation);
    }

    public void setCurrentInspector(String string) {
        for (Inspector inspector : inspectors) {
            if (inspector.getTitle().equals(string)) {
                activate(inspector);
                break;
            }
        }
    }

    public void deactivate() {
        activate(null);
    }

    public void activate(Inspector inspector) {
        if (inspector == currentInspector) {
            return;
        }

        if (currentInspector != null) {
            inspectorButtons.get(currentInspector).setSelected(false);
            currentInspector.deactivate();
        }

        if (inspector == null) {
            currentInspector = null;
            inspectorPanel.setVisible(false);
        } else {
            JToggleButton button = inspectorButtons.get(inspector);
            currentInspector = inspector;

            button.setSelected(true);
            button.setBackground(Color.WHITE);

            if (!inspectorPanel.isVisible()) {
                inspectorPanel.setVisible(true);
                if (lastDividerLocation == 0) {
                    mainSplit.setDividerLocation(defaultDividerLocation);
                } else {
                    mainSplit.setDividerLocation(lastDividerLocation);
                }
            }

            CardLayout cards = (CardLayout) inspectorPanel.getLayout();
            cards.show(inspectorPanel, inspector.getInspectorId());

            currentInspector.activate();
        }
    }

    public void setContentComponent(JComponent content) {
        mainSplit.setTopComponent(content);
    }

    public void removeInspector(Inspector inspector) {
        if (currentInspector == inspector) {
            deactivate();
        }

        inspector.release();
        inspectors.remove(inspector);
        JToggleButton toggleButton = inspectorButtons.get(inspector);

        int ix = inspectToolbar.getComponentIndex(toggleButton);
        if (ix > 1) {
            inspectToolbar.remove(ix - 1);
        }

        inspectToolbar.remove(toggleButton);
        inspectorPanel.remove(inspector.getComponent());
        inspectToolbar.repaint();
        inspectorButtons.remove(inspector);
    }
}
