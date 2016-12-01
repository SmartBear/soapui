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

package com.eviware.soapui.support.editor;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.Inspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.VTextIcon;
import com.eviware.soapui.support.components.VerticalMetalTabbedPaneUI;
import com.eviware.soapui.support.components.VerticalWindowsTabbedPaneUI;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor-framework for Documents
 *
 * @author ole.matzura
 */

@SuppressWarnings("serial")
public class Editor<T extends EditorDocument> extends JPanel implements PropertyChangeListener,
        EditorLocationListener<T> {
    public final static String OUTLINE_TABLE_PROPERTY = Editor.class.getSimpleName() + "@outlineTable";
    private JTabbedPane inputTabs;
    private List<EditorView<T>> views = new ArrayList<EditorView<T>>();
    private EditorView<T> currentView;
    private T document;
    private JInspectorPanel inspectorPanel;
    private InputTabsChangeListener inputTabsChangeListener;

    public Editor(T document) {
        super(new BorderLayout());
        this.document = document;
        document.addPropertyChangeListener(EditorDocument.DOCUMENT_PROPERTY, this);

        setBackground(Color.WHITE);
        inputTabs = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

        prettifyTabbedPaneUI();

        inputTabs.setFont(inputTabs.getFont().deriveFont(8));
        inputTabsChangeListener = new InputTabsChangeListener();
        inputTabs.addChangeListener(inputTabsChangeListener);

        inspectorPanel = JInspectorPanelFactory.build(inputTabs);
        add(inspectorPanel.getComponent(), BorderLayout.CENTER);
    }

    private void prettifyTabbedPaneUI() {
        if (!UISupport.isMac()) {
            // For some reason the tabs get very wide in some L&Fs. Workaround is to replace the UI.
            if (inputTabs.getUI().getClass().getSimpleName().equals("WindowsTabbedPaneUI")) {
                inputTabs.setUI(new VerticalWindowsTabbedPaneUI());
            } else {
                inputTabs.setUI(new VerticalMetalTabbedPaneUI());
            }
        }
    }

    public void addEditorView(EditorView<T> editorView) {
        views.add(editorView);

        if (UISupport.isMac()) {
            inputTabs.addTab(editorView.getTitle(), editorView.getComponent());
        } else {
            inputTabs.addTab(null, new VTextIcon(inputTabs, editorView.getTitle(), VTextIcon.ROTATE_LEFT),
                    editorView.getComponent());
        }
        editorView.addPropertyChangeListener(this);
        editorView.addLocationListener(this);
        editorView.setDocument(document);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(EditorView.TITLE_PROPERTY)) {
            int ix = views.indexOf(evt.getSource());
            if (ix == -1) {
                return;
            }

            inputTabs.setTitleAt(ix, (String) evt.getNewValue());
        }
        if (evt.getPropertyName().equals(EditorDocument.DOCUMENT_PROPERTY)) {
            inputTabsChangeListener.refreshVisibleInspectors();
        }
    }

    public void selectView(int viewIndex) {
        inputTabs.setSelectedIndex(viewIndex);
    }

    public void selectView(String viewId) {
        for (int c = 0; c < views.size(); c++) {
            if (views.get(c).getViewId().equals(viewId)) {
                inputTabs.setSelectedIndex(c);
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void locationChanged(EditorLocation<T> location) {
        if (location != null) {
            for (Inspector inspector : inspectorPanel.getInspectors()) {
                ((EditorInspector<T>) inspector).locationChanged(location);
            }
        }
    }

    public void requestFocus() {
        if (currentView != null) {
            currentView.getComponent().requestFocus();
        }
    }

    public T getDocument() {
        return document;
    }

    public boolean hasFocus() {
        return currentView == null ? false : currentView.getComponent().hasFocus();
    }

    public final void setDocument(T document) {
        if (this.document != null) {
            this.document.release();
            this.document.removePropertyChangeListener(EditorDocument.DOCUMENT_PROPERTY, this);
        }

        this.document = document;
        this.document.addPropertyChangeListener(EditorDocument.DOCUMENT_PROPERTY, this);

        for (EditorView<T> view : views) {
            view.setDocument(document);
        }
        inputTabsChangeListener.refreshVisibleInspectors();
    }

    public final EditorView<T> getCurrentView() {
        return currentView;
    }

    public final JTabbedPane getInputTabs() {
        return inputTabs;
    }

    public final List<EditorView<T>> getViews() {
        return views;
    }

    public EditorView<T> getView(String viewId) {
        for (EditorView<T> view : views) {
            if (view.getViewId().equals(viewId)) {
                return view;
            }
        }

        return null;
    }

    public Inspector getInspector(String inspectorId) {
        return inspectorPanel.getInspector(inspectorId);
    }

    public void setEditable(boolean enabled) {
        for (EditorView<T> view : views) {
            view.setEditable(enabled);
        }
    }

    public void addInspector(EditorInspector<T> inspector) {
        inspectorPanel.addInspector(inspector);
        inspector.init(this);
        inspectorPanel
                .setInspectorVisible(inspector, currentView == null ? true : inspector.isEnabledFor(currentView));
    }

    private final class InputTabsChangeListener implements ChangeListener {
        private int lastDividerLocation;

        @SuppressWarnings("unchecked")
        public void stateChanged(ChangeEvent e) {
            int currentViewIndex = views.indexOf(currentView);

            if (currentView != null) {
                if (inputTabs.getSelectedIndex() == currentViewIndex) {
                    return;
                }

                if (!currentView.deactivate()) {
                    inputTabs.setSelectedIndex(currentViewIndex);
                    return;
                }
            }

            EditorView<T> previousView = currentView;
            int selectedIndex = inputTabs.getSelectedIndex();
            if (selectedIndex == -1) {
                currentView = null;
                return;
            }

            currentView = views.get(selectedIndex);

            if (currentView != null
                    && !currentView.activate(previousView == null ? null : previousView.getEditorLocation())) {
                inputTabs.setSelectedIndex(currentViewIndex);
                if (currentViewIndex == -1) {
                    return;
                }
            }

            refreshVisibleInspectors();

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (currentView != null) {
                        currentView.getComponent().requestFocus();
                    }
                }
            });
        }

        private void refreshVisibleInspectors() {
            EditorInspector<T> currentInspector = (EditorInspector<T>) inspectorPanel.getCurrentInspector();

            if (currentInspector != null) {
                lastDividerLocation = inspectorPanel.getDividerLocation();
            }

            for (Inspector inspector : inspectorPanel.getInspectors()) {
                inspectorPanel.setInspectorVisible(inspector,
                        ((EditorInspector<T>) inspector).isEnabledFor(currentView));
            }

            if (currentInspector != null && ((EditorInspector<T>) currentInspector).isEnabledFor(currentView)) {
                if (lastDividerLocation == 0) {
                    inspectorPanel.setResetDividerLocation();
                } else {
                    inspectorPanel.setDividerLocation(lastDividerLocation);
                }
            } else {
                currentInspector = null;
            }
        }
    }

    public void release() {
        for (EditorView<T> view : views) {
            view.release();
            view.removePropertyChangeListener(this);
        }

        views.clear();

        inputTabs.removeChangeListener(inputTabsChangeListener);
        inputTabs.removeAll();

        inspectorPanel.release();
        document.release();
    }
}
