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

package com.eviware.soapui.ui;

import com.eviware.soapui.ui.desktop.DesktopListener;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * List for displaying current DesktopPanels
 *
 * @author Ole.Matzura
 */

public class JDesktopPanelsList extends JPanel {
    private DefaultListModel desktopPanels;
    private JList desktopPanelsList;
    private SoapUIDesktop desktop;
    private DesktopPanelPropertyChangeListener desktopPanelPropertyListener = new DesktopPanelPropertyChangeListener();
    private InternalDesktopListener desktopListener = new InternalDesktopListener();

    public JDesktopPanelsList(SoapUIDesktop desktop) {
        super(new BorderLayout());
        setDesktop(desktop);

        desktopPanels = new DefaultListModel();
        desktopPanelsList = new JList(desktopPanels);
        desktopPanelsList.setCellRenderer(new DesktopItemsCellRenderer());
        desktopPanelsList.setToolTipText("Open windows");
        desktopPanelsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) {
                    return;
                }

                JDesktopPanelsList.this.desktop.showDesktopPanel((DesktopPanel) desktopPanelsList.getSelectedValue());
            }
        });

        add(new JScrollPane(desktopPanelsList), BorderLayout.CENTER);
    }

    private class DesktopPanelPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            DesktopPanel desktopPanel = (DesktopPanel) evt.getSource();
            int ix = desktopPanels.indexOf(desktopPanel);
            if (ix >= 0) {
                desktopPanels.set(ix, desktopPanel);
            }
        }
    }

    private static class DesktopItemsCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            DesktopPanel desktopPanel = (DesktopPanel) value;
            String title = desktopPanel.getTitle();
            setText(title);
            setToolTipText(desktopPanel.getDescription());
            setIcon(desktopPanel.getIcon());

            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), getBorder()));

            return this;
        }
    }

    public List<DesktopPanel> getDesktopPanels() {
        List<DesktopPanel> result = new ArrayList<DesktopPanel>();

        for (int c = 0; c < desktopPanels.getSize(); c++) {
            result.add((DesktopPanel) desktopPanels.get(c));
        }

        return result;
    }

    private class InternalDesktopListener implements DesktopListener {
        public void desktopPanelSelected(DesktopPanel desktopPanel) {
            desktopPanelsList.setSelectedValue(desktopPanel, false);
        }

        public void desktopPanelCreated(DesktopPanel desktopPanel) {
            desktopPanels.addElement(desktopPanel);
            desktopPanelsList.setSelectedValue(desktopPanel, false);

            desktopPanel.addPropertyChangeListener(desktopPanelPropertyListener);
        }

        public void desktopPanelClosed(DesktopPanel desktopPanel) {
            desktopPanels.removeElement(desktopPanel);
            desktopPanel.removePropertyChangeListener(desktopPanelPropertyListener);
        }
    }

    public void setDesktop(SoapUIDesktop newDesktop) {
        if (desktop != null) {
            desktop.removeDesktopListener(desktopListener);

            while (desktopPanels.size() > 0) {
                DesktopPanel desktopPanel = (DesktopPanel) desktopPanels.getElementAt(0);
                desktopPanel.removePropertyChangeListener(desktopPanelPropertyListener);
                desktopPanels.remove(0);
            }

        }

        desktop = newDesktop;

        desktop.addDesktopListener(desktopListener);

        for (DesktopPanel desktopPanel : desktop.getDesktopPanels()) {
            desktopPanel.addPropertyChangeListener(desktopPanelPropertyListener);
            desktopPanels.addElement(desktopPanel);
        }
    }

    public JList getDesktopPanelsList() {
        return desktopPanelsList;
    }

    public int getItemsCount() {
        return desktopPanels.size();
    }
}
