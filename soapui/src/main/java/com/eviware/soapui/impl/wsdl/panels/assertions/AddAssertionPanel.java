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

package com.eviware.soapui.impl.wsdl.panels.assertions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.recent.RecentAssertionHandler;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.settings.AssertionDescriptionSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleForm;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class AddAssertionPanel extends SimpleDialog {
    /**
     *
     */
    private static final long serialVersionUID = 5770245094548607912L;
    //Changed categoriesList to a table to be able to disable rows from the list
    private CategoriesListTable categoriesListTable;
    private AssertionsListTable assertionsTable;
    private Assertable assertable;
    public static final String NO_PROPERTY_SELECTED = "<Select Property>";
    public static final String RESPONSE_PROPERTY = "Response";
    private AddAssertionAction addAssertionAction;
    private AssertionsListTableModel assertionsListTableModel;
    private AssertionCategoriesTableModel categoriesTableModel;

    public AssertionsListTableModel getAssertionsListTableModel() {
        return assertionsListTableModel;
    }

    //	private JPanel assertionListPanel;
    private SortedSet<AssertionListEntry> assertions;
    private ListSelectionListener selectionListener;
    private LinkedHashMap<String, SortedSet<AssertionListEntry>> categoriesAssertionsMap;
    private SimpleForm assertionsForm;
    private JCheckBox hideDescCB;
    private AssertionEntryRenderer assertionEntryRenderer = new AssertionEntryRenderer();
    private CategoryListRenderer categoriesListRenderer = new CategoryListRenderer();
    private InternalHideDescListener hideDescListener = new InternalHideDescListener();
    protected RecentAssertionHandler recentAssertionHandler = new RecentAssertionHandler();
    private AssertionListMouseAdapter mouseAdapter = new AssertionListMouseAdapter();
    private String selectedCategory;

    public AddAssertionPanel(Assertable assertable) {
        super("Add Assertion", "Select the source property and which assertion to apply below ",
                HelpUrls.ADD_ASSERTION_PANEL);
        this.assertable = assertable;
        assertionEntryRenderer.setAssertable(assertable);
        categoriesListRenderer.setAssertable(assertable);
        selectionListener = new InternalListSelectionListener();
        categoriesAssertionsMap = AssertionCategoryMapping
                .getCategoriesAssertionsMap(assertable, recentAssertionHandler);
        // load interfaces or have a issue with table and cell renderer
        WsdlProject project = (WsdlProject) ModelSupport.getModelItemProject(assertable.getModelItem());
        for (Interface inf : project.getInterfaceList()) {
            try {
                // There seems to be no good reason to load the definitions for rest interfaces
                // hence that call has been removed for the time being.
                if (inf instanceof WsdlInterface) {
                    ((WsdlInterface) inf).getWsdlContext().loadIfNecessary();
                }
            } catch (Exception e) {
                // TODO Improve this
                e.printStackTrace();
            }
        }
    }

    public RecentAssertionHandler getRecentAssertionHandler() {
        return recentAssertionHandler;
    }

    public AssertionEntryRenderer getAssertionEntryRenderer() {
        return assertionEntryRenderer;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    protected String getSelectedPropertyName() {
        return RESPONSE_PROPERTY;
    }

    public void setAssertable(Assertable assertable) {
        this.assertable = assertable;
    }

    public Assertable getAssertable() {
        return assertable;
    }

    @Override
    protected Component buildContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JSplitPane splitPane = UISupport.createHorizontalSplit(buildCategoriesList(), buildAssertionsList());
        splitPane.setDividerLocation(220);
        getAssertionsTable().setSelectable(true);
        JXToolBar toolbar = UISupport.createSmallToolbar();
        hideDescCB = new JCheckBox("Hide descriptions");
        hideDescCB.setOpaque(false);
        hideDescCB.addItemListener(hideDescListener);
        hideDescCB
                .setSelected(SoapUI.getSettings().getBoolean(AssertionDescriptionSettings.SHOW_ASSERTION_DESCRIPTION));
        toolbar.add(new JLabel("Assertions"));
        toolbar.addGlue();
        toolbar.add(hideDescCB);

        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        return mainPanel;
    }

    public AssertionListMouseAdapter getMouseAdapter() {
        return mouseAdapter;
    }

    protected Component buildAssertionsList() {
        assertionsForm = new SimpleForm();

        assertionsListTableModel = new AssertionsListTableModel();
        assertionsTable = new AssertionsListTable(assertionsListTableModel);
        int selectedRow = categoriesListTable.getSelectedRow();
        String category = (String) categoriesListTable.getModel().getValueAt(selectedRow, 0);
        if (category != null && categoriesAssertionsMap.containsKey(category)) {
            assertions = categoriesAssertionsMap.get(category);
            assertionsListTableModel.setListEntriesSet(assertions);
        }
        assertionsTable.setTableHeader(null);
        assertionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assertionsTable.getSelectionModel().addListSelectionListener(selectionListener);
        assertionsTable.setEditable(false);
        assertionsTable.setGridColor(Color.BLACK);
        assertionsTable.addMouseListener(mouseAdapter);

        assertionsTable.getColumnModel().getColumn(0).setCellRenderer(assertionEntryRenderer);
        assertionsForm.addComponent(assertionsTable);
        return new JScrollPane(assertionsForm.getPanel());
    }

    private Component buildCategoriesList() {
        JPanel panel = new JPanel(new BorderLayout());
        categoriesTableModel = new AssertionCategoriesTableModel();
        categoriesTableModel.setLisetEntriesSet(categoriesAssertionsMap.keySet());
        categoriesListTable = new CategoriesListTable(categoriesTableModel);
        categoriesListTable.setTableHeader(null);
        categoriesListTable.setEditable(false);
        categoriesListTable.setGridColor(Color.BLACK);
        categoriesListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoriesListTable.getSelectionModel().setSelectionInterval(0, 0);
        renderAssertions();
        populateSelectableCategoriesIndexes();
        categoriesListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                renderAssertionsTable();
            }
        });
        categoriesListTable.getColumnModel().getColumn(0).setCellRenderer(categoriesListRenderer);
        panel.add(new JScrollPane(categoriesListTable));
        return panel;
    }

    protected void renderAssertionsTable() {

        int selectedRow = categoriesListTable.getSelectedRow();
        if (selectedRow > -1) {
            selectedCategory = (String) categoriesListTable.getModel().getValueAt(selectedRow, 0);
            if (selectedCategory != null && categoriesAssertionsMap.containsKey(selectedCategory)) {
                assertions = categoriesAssertionsMap.get(selectedCategory);
                assertionsListTableModel.setListEntriesSet(assertions);
                renderAssertions();
                populateNonSelectableAssertionIndexes();
                assertionsListTableModel.fireTableDataChanged();
            }
        }
    }

    protected void renderCategoriesTable() {
        categoriesListRenderer.setAssertable(getAssertable());
        populateSelectableCategoriesIndexes();
        categoriesTableModel.fireTableDataChanged();
    }

    protected void renderAssertions() {
    }

    protected void populateNonSelectableAssertionIndexes() {
        getAssertionsTable().setSelectable(true);
        SortedSet<AssertionListEntry> assertionsList = getCategoriesAssertionsMap().get(getSelectedCategory());
        List<Integer> assertionsIndexList = new ArrayList<Integer>();
        for (int i = 0; i < assertionsList.size(); i++) {
            AssertionListEntry assertionListEntry = (AssertionListEntry) assertionsList.toArray()[i];
            if (!isAssertionApplicable(assertionListEntry.getTypeId())) {
                assertionsIndexList.add(i);
            }
        }
        getAssertionsTable().setNonSelectableIndexes(assertionsIndexList);
    }

    protected void populateSelectableCategoriesIndexes() {
        getCategoriesListTable().setSelectable(true);
        List<Integer> categoriesIndexList = new ArrayList<Integer>();
        Set<String> ctgs = getCategoriesAssertionsMap().keySet();
        for (int j = 0; j < ctgs.size(); j++) {
            String selCat = (String) ctgs.toArray()[j];
            SortedSet<AssertionListEntry> assertionsList = getCategoriesAssertionsMap().get(selCat);
            for (int i = 0; i < assertionsList.size(); i++) {
                AssertionListEntry assertionListEntry = (AssertionListEntry) assertionsList.toArray()[i];
                if (isAssertionApplicable(assertionListEntry.getTypeId())) {
                    categoriesIndexList.add(j);
                    break;
                }
            }
        }
        getCategoriesListTable().setSelectableIndexes(categoriesIndexList);
    }

    protected boolean isAssertionApplicable(String assertionType) {
        return TestAssertionRegistry.getInstance().canAssert(assertionType, assertable);
    }

    protected boolean isAssertionApplicable(String assertionType, ModelItem modelItem, String property) {
        try {
            //property is only used for adding assertions with selecting source and property,
            //therefore here can be empty string, but gets its meaning in Override of this method
            return TestAssertionRegistry.getInstance().canAssert(assertionType, assertable);
        } catch (Throwable t) {
            SoapUI.logError(t);
            return false;
        }
    }

    protected void enableCategoriesList(boolean enable) {
        categoriesListTable.setEnabled(enable);
    }

    @Override
    protected boolean handleOk() {
        setVisible(false);

        int selectedRow = assertionsTable.getSelectedRow();
        String selection = ((AssertionListEntry) assertionsListTableModel.getValueAt(selectedRow, 0)).getName();
        if (selection == null) {
            return false;
        }

        if (!TestAssertionRegistry.getInstance().canAddMultipleAssertions(selection, assertable)) {
            UISupport.showErrorMessage("This assertion can only be added once");
            return false;
        }

        TestAssertion assertion = assertable.addAssertion(selection);
        if (assertion == null) {
            UISupport.showErrorMessage("Failed to add assertion");
            return false;
        }

        recentAssertionHandler.add(selection);

        if (assertion.isConfigurable()) {
            assertion.configure();
            return true;
        }

        return true;
    }

    @Override
    public ActionList buildActions(String url, boolean okAndCancel) {
        DefaultActionList actions = new DefaultActionList("Actions");
        if (url != null) {
            actions.addAction(new ShowOnlineHelpAction(url));
        }

        addAssertionAction = new AddAssertionAction();
        actions.addAction(addAssertionAction);
        if (okAndCancel) {
            actions.addAction(new CancelAction());
            actions.setDefaultAction(addAssertionAction);
        }
        return actions;
    }

    protected final class AddAssertionAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = 4741995448420710392L;

        public AddAssertionAction() {
            super("Add");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            handleOk();
        }
    }

    private class InternalListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (assertionsTable.getSelectedRow() >= 0) {
                addAssertionAction.setEnabled(true);
            } else {
                addAssertionAction.setEnabled(false);
            }
        }
    }

    private class InternalHideDescListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent arg0) {
            assertionsTable.getColumnModel().getColumn(0).setCellRenderer(assertionEntryRenderer);
            assertionsListTableModel.fireTableDataChanged();
            SoapUI.getSettings().setBoolean(AssertionDescriptionSettings.SHOW_ASSERTION_DESCRIPTION,
                    arg0.getStateChange() == ItemEvent.SELECTED);
        }
    }

    public void release() {
        assertionsTable.getSelectionModel().removeListSelectionListener(selectionListener);
        assertionsTable.removeMouseListener(mouseAdapter);
        hideDescCB.removeItemListener(hideDescListener);
    }

    protected class AssertionEntryRenderer extends DefaultCellRenderer {
        /**
         *
         */
        private static final long serialVersionUID = -6843334509897580699L;
        private Assertable assertable;
        private Font boldFont;

        public void setAssertable(Assertable assertable) {
            this.assertable = assertable;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {

            boldFont = getFont().deriveFont(Font.BOLD);

            AssertionListEntry entry = (AssertionListEntry) value;
            String type = TestAssertionRegistry.getInstance().getAssertionTypeForName(entry.getName());
            boolean canAssert = false;
            boolean disable = true;
            JLabel label;
            JTextArea descText;
            JLabel disabledInfo;
            if (type != null && assertable != null && assertable.getModelItem() != null) {
                canAssert = isAssertionApplicable(type, assertable.getModelItem(), getSelectedPropertyName());
                disable = !categoriesListTable.isEnabled() || !canAssert;
            }
            String str = entry.getName();
            label = new JLabel(str);
            label.setFont(boldFont);
            descText = new JTextArea(((AssertionListEntry) value).getDescription());
            descText.setSize(new Dimension(80, 20));
            descText.setLineWrap(true);
            descText.setWrapStyleWord(true);
            disabledInfo = new JLabel("Not applicable with selected Source and Property");
            descText.setFont(disabledInfo.getFont());
            if (disable) {
                label.setForeground(Color.LIGHT_GRAY);
                descText.setForeground(Color.LIGHT_GRAY);
                disabledInfo.setForeground(Color.LIGHT_GRAY);
            }
            SimpleForm form = new SimpleForm();
            form.addComponent(label);
            if (!isHideDescriptionSelected()) {
                form.addComponent(descText);
                //				if( disable )
                //				{
                //					form.addComponent( disabledInfo );
                //				}
                getAssertionsTable().setRowHeight(70);
            } else {
                if (disable) {
                    form.addComponent(disabledInfo);
                }
                getAssertionsTable().setRowHeight(40);
            }
            if (isSelected) {
                descText.setBackground(Color.LIGHT_GRAY);
                form.getPanel().setBackground(Color.LIGHT_GRAY);
            } else {
                descText.setBackground(Color.WHITE);
                form.getPanel().setBackground(Color.WHITE);
            }
            return form.getPanel();
        }
    }

    protected class CategoryListRenderer extends DefaultCellRenderer {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private Assertable assertable;

        public void setAssertable(Assertable assertable) {
            this.assertable = assertable;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String categoryName = (String) value;
            boolean disabled = true;
            Font boldFont = getFont().deriveFont(Font.BOLD);
            SortedSet<AssertionListEntry> assertions = categoriesAssertionsMap.get(categoryName);
            for (AssertionListEntry assertionListEntry : assertions) {
                if (isAssertionApplicable(assertionListEntry.getTypeId())) {
                    disabled = false;
                    break;
                }
            }
            JLabel label = new JLabel(categoryName);
            SimpleForm form = new SimpleForm();
            form.addComponent(label);
            label.setFont(boldFont);
            if (disabled || !((CategoriesListTable) table).isSelectable(row)) {
                label.setForeground(Color.GRAY);
            }
            if (isSelected) {
                form.getPanel().setBackground(Color.LIGHT_GRAY);
            } else {
                form.getPanel().setBackground(Color.WHITE);
            }
            return form.getPanel();
        }
    }

    protected boolean isHideDescriptionSelected() {
        return hideDescCB.isSelected();
    }

    @Override
    protected void beforeShow() {
        setSize(new Dimension(650, 500));
    }

    public void setCategoriesAssertionsMap(LinkedHashMap<String, SortedSet<AssertionListEntry>> categoriesAssertionsMap) {
        this.categoriesAssertionsMap = categoriesAssertionsMap;
    }

    public LinkedHashMap<String, SortedSet<AssertionListEntry>> getCategoriesAssertionsMap() {
        return categoriesAssertionsMap;
    }

    public class AssertionListMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && !assertionsTable.getSelectionModel().isSelectionEmpty()) {
                handleOk();
            }
        }
    }

    public AssertionsListTable getAssertionsTable() {
        return assertionsTable;
    }

    public CategoriesListTable getCategoriesListTable() {
        return categoriesListTable;
    }

    public AddAssertionAction getAddAssertionAction() {
        return addAssertionAction;
    }

    public void setSelectionListener(ListSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

}
