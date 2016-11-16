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

package com.eviware.soapui.ui.support;

import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.NewMockResponseAction;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatchRegistry;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatcher;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.model.util.ModelItemNames;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.ExtendedComboBoxModel;
import com.eviware.soapui.support.swing.ModelItemListKeyListener;
import com.eviware.soapui.support.swing.ModelItemListMouseListener;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public abstract class AbstractMockOperationDesktopPanel<MockOperationType extends AbstractMockOperation>
        extends ModelItemDesktopPanel<MockOperationType> {
    private JList responseList;
    private JComboBox dispatchCombo;
    private JPanel dispatchPanel;
    private JComboBox defaultResponseCombo;
    private ResponseListModel responseListModel;
    private JComponentInspector<JComponent> dispatchInspector;
    private JInspectorPanel inspectorPanel;
    private JPanel defaultResponsePanel;

    public AbstractMockOperationDesktopPanel(MockOperationType mockOperation) {
        super(mockOperation);

        buildUI();
        setPreferredSize(new Dimension(600, 440));
    }

    private void buildUI() {
        add(buildToolbar(), BorderLayout.NORTH);

        inspectorPanel = JInspectorPanelFactory.build(buildResponseList());
        inspectorPanel.setDefaultDividerLocation(0.5F);
        dispatchInspector = new JComponentInspector<JComponent>(buildDispatchEditor(), "Dispatch ("
                + getModelItem().getDispatchStyle().toString() + ")", "Configures current dispatch style", true);
        inspectorPanel.addInspector(dispatchInspector);
        inspectorPanel.activate(dispatchInspector);

        add(inspectorPanel.getComponent(), BorderLayout.CENTER);
    }

    private JComponent buildResponseList() {
        responseListModel = new ResponseListModel();
        responseList = new JList(responseListModel);
        responseList.addKeyListener(new ModelItemListKeyListener() {
            @Override
            public ModelItem getModelItemAt(int ix) {
                return getModelItem().getMockResponseAt(ix);
            }
        });

        responseList.addMouseListener(new ModelItemListMouseListener() {

            private DefaultActionList defaultActions;

            @Override
            protected ActionList getDefaultActions() {
                if (defaultActions == null) {
                    defaultActions = new DefaultActionList();
                    defaultActions.addAction(SwingActionDelegate.createDelegate(NewMockResponseAction.SOAPUI_ACTION_ID,
                            getModelItem(), null, getAddToMockOperationIconPath()));
                }

                return defaultActions;
            }

        });
        responseList.setCellRenderer(new ResponseListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(responseList);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("MockResponses", UISupport.buildPanelWithToolbar(buildMockResponseListToolbar(), scrollPane));

        return UISupport.createTabPanel(tabs, true);
    }

    protected abstract String getAddToMockOperationIconPath();

    private JComponent buildMockResponseListToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();
        toolbar.add(UISupport.createToolbarButton(SwingActionDelegate.createDelegate(
                NewMockResponseAction.SOAPUI_ACTION_ID, getModelItem(), null, getAddToMockOperationIconPath())));

        return toolbar;
    }

    private JComponent buildDispatchEditor() {
        dispatchPanel = new JPanel(new BorderLayout());
        dispatchPanel.setOpaque(true);
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addFixed(new JLabel("Dispatch: "));
        builder.addRelatedGap();
        dispatchCombo = new JComboBox(getAvailableDispatchTypes());
        dispatchCombo.setSelectedItem(null);

        dispatchCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (dispatchPanel.getComponentCount() > 1) {
                    dispatchPanel.remove(1);
                }

                String item = (String) dispatchCombo.getSelectedItem();
                MockOperationDispatcher dispatcher = getModelItem().setDispatchStyle(item);

                dispatchPanel.add(dispatcher.getEditorComponent(), BorderLayout.CENTER);
                dispatchPanel.revalidate();
                dispatchPanel.repaint();

                if (dispatchInspector != null && item != null) {
                    dispatchInspector.setTitle("Dispatch (" + item + ")");
                }

                defaultResponsePanel.setVisible(getModelItem().getDispatcher().hasDefaultResponse());
            }
        });

        builder.addFixed(dispatchCombo);

        defaultResponsePanel = new JPanel(new BorderLayout());

        defaultResponsePanel.add(new JLabel("Default Response: "), BorderLayout.WEST);

        ModelItemNames<MockResponse> names = new ModelItemNames<MockResponse>(getModelItem().getMockResponses());
        defaultResponseCombo = new JComboBox(new ExtendedComboBoxModel(names.getNames()));
        defaultResponseCombo.setPreferredSize(new Dimension(150, 20));
        defaultResponseCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Object selectedItem = defaultResponseCombo.getSelectedItem();
                getModelItem().setDefaultResponse((String) selectedItem);
            }
        });

        defaultResponsePanel.add(defaultResponseCombo, BorderLayout.CENTER);

        builder.addUnrelatedGap();
        builder.addFixed(defaultResponsePanel);

        dispatchPanel.add(builder.getPanel(), BorderLayout.NORTH);

        // init data
        defaultResponseCombo.setSelectedItem(getModelItem().getDefaultResponse());
        dispatchCombo.setSelectedItem(getModelItem().getDispatchStyle());

        return dispatchPanel;
    }

    protected String[] getAvailableDispatchTypes() {
        return MockOperationDispatchRegistry.getDispatchTypes();
    }

    protected abstract Component buildToolbar();

    public boolean onClose(boolean canCancel) {
        responseListModel.release();

        inspectorPanel.release();

        if (getModelItem().getDispatcher() != null) {
            getModelItem().getDispatcher().releaseEditorComponent();
        }

        return release();
    }

    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == getModelItem() || modelItem == getModelItem().getMockService()
                || modelItem == getModelItem().getMockService().getProject();
    }

    public class ResponseListModel extends AbstractListModel implements ListModel, MockServiceListener,
            PropertyChangeListener {
        private java.util.List<MockResponse> responses = new ArrayList<MockResponse>();

        public ResponseListModel() {
            for (int c = 0; c < getModelItem().getMockResponseCount(); c++) {
                MockResponse mockResponse = getModelItem().getMockResponseAt(c);
                mockResponse.addPropertyChangeListener(this);

                responses.add(mockResponse);
            }

            getModelItem().getMockService().addMockServiceListener(this);
        }

        public Object getElementAt(int arg0) {
            return responses.get(arg0);
        }

        public int getSize() {
            return responses.size();
        }

        public void mockOperationAdded(MockOperation operation) {

        }

        public void mockOperationRemoved(MockOperation operation) {

        }

        public void mockResponseAdded(MockResponse response) {
            if (response.getMockOperation() != getModelItem()) {
                return;
            }

            responses.add(response);
            response.addPropertyChangeListener(this);
            fireIntervalAdded(this, responses.size() - 1, responses.size() - 1);

            defaultResponseCombo.addItem(response.getName());
        }

        public void mockResponseRemoved(MockResponse response) {
            if (response.getMockOperation() != getModelItem()) {
                return;
            }

            int ix = responses.indexOf(response);
            responses.remove(ix);
            response.removePropertyChangeListener(this);
            fireIntervalRemoved(this, ix, ix);

            defaultResponseCombo.removeItem(response.getName());
        }

        public void propertyChange(PropertyChangeEvent arg0) {
            if (arg0.getPropertyName().equals(ModelItem.NAME_PROPERTY)) {
                int ix = responses.indexOf(arg0.getSource());
                fireContentsChanged(this, ix, ix);

                ExtendedComboBoxModel model = (ExtendedComboBoxModel) defaultResponseCombo.getModel();
                model.setElementAt(arg0.getNewValue(), ix);

                if (model.getSelectedItem().equals(arg0.getOldValue())) {
                    model.setSelectedItem(arg0.getNewValue());
                }
            }
        }

        public void release() {
            for (MockResponse response : responses) {
                response.removePropertyChangeListener(this);
            }

            getModelItem().getMockService().removeMockServiceListener(this);
        }
    }

    private final static class ResponseListCellRenderer extends JLabel implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            MockResponse testStep = (MockResponse) value;
            setText(testStep.getName());
            setIcon(testStep.getIcon());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

            return this;
        }
    }

}
