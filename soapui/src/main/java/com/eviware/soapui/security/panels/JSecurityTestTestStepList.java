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

package com.eviware.soapui.security.panels;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestListener;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.actions.CloneParametersAction;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.scan.AbstractSecurityScanWithProperties;
import com.eviware.soapui.security.support.SecurityTestRunListenerAdapter;
import com.eviware.soapui.security.ui.SecurityConfigurationDialog;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.TreePathUtils;
import org.jdesktop.swingx.JXTree;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A panel showing a scrollable list of TestSteps in a SecurityTest.
 *
 * @author dragica.soldo
 */

@SuppressWarnings("serial")
public class JSecurityTestTestStepList extends JPanel implements TreeSelectionListener, MouseListener,
        SecurityTestListener {
    private SecurityTest securityTest;
    private final TestSuiteListener testSuiteListener = new InternalTestSuiteListener();
    private JXTree securityTestTree;
    private AddSecurityScanAction addSecurityScanAction;
    private ConfigureSecurityScanAction configureSecurityScanAction;
    private RemoveSecurityScanAction removeSecurityScanAction;
    private CloneParametersAction cloneParametersAction;

    private JSecurityTestRunLog securityTestLog;
    private JPopupMenu securityScanPopUp;

    private JPopupMenu securityScanWithPropertiesPopUp;
    private JPopupMenu testStepPopUp;
    private SecurityTreeCellRender cellRender;
    private SecurityScanTree treeModel;
    private InternalSecurityTestRunListener testRunListener;
    private JScrollPane scrollPane;
    private EnableDisableSecurityScan enableDisableSecurityScan;
    private JPopupMenu multySecurityScanPopUp;
    protected boolean multypopupvisible;
    private EnableSecurityScans enableSecurityScansAction;
    private DisableSecurityScans disableSecurityScansAction;
    private ShowOnlineHelpAction showOnlineHelpAction;
    private OpenTestStepEditorAction openTestStepEditorAction;

    public JSecurityTestTestStepList(SecurityTest securityTest, JSecurityTestRunLog securityTestLog) {
        this.securityTest = securityTest;
        setLayout(new BorderLayout());

        JXToolBar toolbar = initToolbar();

        securityScanPopUp = new JPopupMenu();
        securityScanPopUp.add(enableDisableSecurityScan = new EnableDisableSecurityScan());
        securityScanPopUp.add(configureSecurityScanAction);
        securityScanPopUp.addSeparator();
        securityScanPopUp.add(removeSecurityScanAction);
        showOnlineHelpAction = new ShowOnlineHelpAction(HelpUrls.RESPONSE_ASSERTIONS_HELP_URL);
        securityScanPopUp.add(showOnlineHelpAction);

        securityScanWithPropertiesPopUp = new JPopupMenu();
        securityScanWithPropertiesPopUp.add(enableDisableSecurityScan);
        securityScanWithPropertiesPopUp.add(configureSecurityScanAction);
        securityScanWithPropertiesPopUp.add(cloneParametersAction);
        securityScanWithPropertiesPopUp.addSeparator();
        securityScanWithPropertiesPopUp.add(removeSecurityScanAction);
        securityScanWithPropertiesPopUp.add(showOnlineHelpAction);

        multySecurityScanPopUp = new JPopupMenu();
        enableSecurityScansAction = new EnableSecurityScans();
        disableSecurityScansAction = new DisableSecurityScans();
        populateMultySecurityScanPopup(true, true);
        multySecurityScanPopUp.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                multypopupvisible = true;
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }
        });

        testStepPopUp = new JPopupMenu();
        initTestStepPopUpActions();
        testStepPopUp.addSeparator();
        testStepPopUp.add(new ShowOnlineHelpAction(HelpUrls.RESPONSE_ASSERTIONS_HELP_URL));

        treeModel = new SecurityScanTree(securityTest, new SecurityTreeRootNode(securityTest));
        securityTestTree = new JXTree(treeModel);
        securityTestTree.putClientProperty("JTree.lineStyle", "None");
        securityTestTree.setUI(new CustomTreeUI());
        securityTestTree.setRootVisible(false);
        securityTestTree.setLargeModel(true);
        cellRender = new SecurityTreeCellRender();
        securityTestTree.setCellRenderer(cellRender);
        securityTestTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        securityTestTree.addTreeSelectionListener(this);
        securityTestTree.addMouseListener(this);
        securityTestTree.setRowHeight(30);
        securityTestTree.setToggleClickCount(0);
        securityTestTree.setBackground(new Color(240, 240, 240));
        securityTestTree.setScrollsOnExpand(true);
        add(toolbar, BorderLayout.NORTH);
        scrollPane = new JScrollPane(securityTestTree);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        securityTest.getTestCase().getTestSuite().addTestSuiteListener(testSuiteListener);
        securityTest.addSecurityTestListener(this);
        testRunListener = new InternalSecurityTestRunListener();
        securityTest.addSecurityTestRunListener(testRunListener);
        for (int row = 0; row < securityTestTree.getRowCount(); row++) {
            securityTestTree.expandRow(row);
        }
        this.securityTestLog = securityTestLog;

    }

    private void populateMultySecurityScanPopup(boolean addEnableAction, boolean addDisableAction) {
        multySecurityScanPopUp.removeAll();
        if (addEnableAction) {
            multySecurityScanPopUp.add(enableSecurityScansAction);
        }
        if (addDisableAction) {
            multySecurityScanPopUp.add(disableSecurityScansAction);
        }
        multySecurityScanPopUp.addSeparator();
        multySecurityScanPopUp.add(removeSecurityScanAction);
        multySecurityScanPopUp.add(showOnlineHelpAction);
    }

    protected SecurityTest getSecurityTest() {
        return securityTest;
    }

    protected void setSecurityTest(SecurityTest securityTest) {
        this.securityTest = securityTest;
    }

    protected JPopupMenu getTestStepPopUp() {
        return testStepPopUp;
    }

    protected void initTestStepPopUpActions() {
        testStepPopUp.add(openTestStepEditorAction);
        testStepPopUp.add(addSecurityScanAction);
    }

    protected JPopupMenu getSecurityScanPopUp() {
        return securityScanPopUp;
    }

    private JXToolBar initToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        initToolbarLeft(toolbar);

        JButton expandActionBtn = UISupport.createToolbarButton(new ExpandTreeAction());
        expandActionBtn.setText("Expanded");
        expandActionBtn.setPreferredSize(new Dimension(80, 21));
        JButton collapsActionBtn = UISupport.createToolbarButton(new CollapsTreeAction());
        collapsActionBtn.setText("Collapsed");
        collapsActionBtn.setPreferredSize(new Dimension(80, 21));
        toolbar.addGlue();
        toolbar.add(expandActionBtn);
        toolbar.add(collapsActionBtn);

        return toolbar;
    }

    protected void initToolbarLeft(JXToolBar toolbar) {
        addSecurityScanAction = new AddSecurityScanAction();
        configureSecurityScanAction = new ConfigureSecurityScanAction();
        removeSecurityScanAction = new RemoveSecurityScanAction();
        cloneParametersAction = new CloneParametersAction();
        openTestStepEditorAction = new OpenTestStepEditorAction();

        toolbar.addFixed(UISupport.createToolbarButton(addSecurityScanAction));
        toolbar.addFixed(UISupport.createToolbarButton(configureSecurityScanAction));
        toolbar.addFixed(UISupport.createToolbarButton(removeSecurityScanAction));
        toolbar.addFixed(UISupport.createToolbarButton(cloneParametersAction));
    }

    protected JComponent buildSecurityScanInspector() {
        JPanel p = new JPanel(new BorderLayout());
        return p;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        securityTest.getTestCase().getTestSuite().addTestSuiteListener(testSuiteListener);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        securityTest.getTestCase().getTestSuite().removeTestSuiteListener(testSuiteListener);
    }

    private final class InternalTestSuiteListener extends TestSuiteListenerAdapter {
        @Override
        public void testStepAdded(TestStep testStep, int index) {
            ((SecurityScanTree) securityTestTree.getModel()).insertNodeInto(testStep);
        }

        @Override
        public void testStepRemoved(TestStep testStep, int index) {
            TestStepNode node = ((SecurityScanTree) securityTestTree.getModel()).getTestStepNode(testStep);
            for (int cnt = 0; cnt < node.getChildCount(); cnt++) {
                SecurityScanNode nodeCld = (SecurityScanNode) node.getChildAt(cnt);
                cellRender.remove(nodeCld);
                treeModel.removeNodeFromParent(nodeCld);
            }
            cellRender.remove(node);
            treeModel.removeNodeFromParent(node);
        }

        @Override
        public void testStepMoved(TestStep testStep, int index, int offset) {
            TreePath path = treeModel.moveTestStepNode(testStep, index, offset);

            securityTestTree.expandPath(path);
            securityTestTree.setSelectionPath(path);
        }
    }

    public class OpenTestStepEditorAction extends AbstractAction {

        public OpenTestStepEditorAction() {
            super("Open Editor");

            putValue(Action.SHORT_DESCRIPTION, "Opens the editor for this TestStep");
            setEnabled(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TestStepNode node = (TestStepNode) securityTestTree.getLastSelectedPathComponent();
            UISupport.selectAndShow(((TestStepNode) node).getTestStep());
        }

    }

    // toolbar actions
    public class AddSecurityScanAction extends AbstractAction {
        public AddSecurityScanAction() {
            super("Add SecurityScan");

            putValue(Action.SHORT_DESCRIPTION, "Adds a security scan to this item");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add_security_scan.gif"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            TestStepNode node = (TestStepNode) securityTestTree.getLastSelectedPathComponent();
            if (!node.getAllowsChildren()) {
                return;
            }

            TestStep testStep = node.getTestStep();

            String[] availableScanNames = SoapUI.getSoapUICore().getSecurityScanRegistry()
                    .getAvailableSecurityScansNames(testStep);
            availableScanNames = securityTest.getAvailableSecurityScanNames(testStep, availableScanNames);

            if (availableScanNames == null || availableScanNames.length == 0) {
                UISupport.showErrorMessage("No security scans available for this test step");
                return;
            }

            String name = UISupport.prompt("Specify type of security scan", "Add SecurityScan", availableScanNames);
            if (name == null || name.trim().length() == 0) {
                return;
            }

            String type = SoapUI.getSoapUICore().getSecurityScanRegistry().getSecurityScanTypeForName(name);
            if (type == null || type.trim().length() == 0) {
                return;
            }

            SecurityScan securityScan = securityTest.addNewSecurityScan(testStep, name);
            if (securityScan == null) {
                UISupport.showErrorMessage("Failed to add security scan");
                return;
            }

            securityScan.setRunOnlyOnce(true);

            securityTestTree.setSelectionPath(new TreePath(node.getPath()));

            SecurityConfigurationDialog dialog = SoapUI.getSoapUICore().getSecurityScanRegistry().getUIBuilder()
                    .buildSecurityScanConfigurationDialog((SecurityScan) securityScan);

            if (!dialog.configure()) {
                SecurityScanNode securityScanNode = (SecurityScanNode) node.getLastChild();

                securityTest.removeSecurityScan(testStep, (SecurityScan) securityScan);
                cellRender.remove(securityScanNode);
            }

            dialog.release();
        }

    }

    public class EnableDisableSecurityScan extends AbstractAction {

        EnableDisableSecurityScan() {
            super("Enable Scan");
            putValue(Action.SHORT_DESCRIPTION, "Enables/Disables Security Scan");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            SecurityScanNode node = (SecurityScanNode) securityTestTree.getLastSelectedPathComponent();
            SecurityScan securityScan = node.getSecurityScan();
            securityScan.setDisabled(!securityScan.isDisabled());
        }

        public void setText(boolean disabled) {
            if (disabled) {
                this.putValue(Action.NAME, "Enable Security Scan");
            } else {
                this.putValue(Action.NAME, "Disable Security Scan");
            }
        }

    }

    public class ConfigureSecurityScanAction extends AbstractAction {
        ConfigureSecurityScanAction() {
            super("Configure");
            putValue(Action.SHORT_DESCRIPTION, "Configures selected security scan");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/preferences.png"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            SecurityScanNode node = (SecurityScanNode) securityTestTree.getLastSelectedPathComponent();
            SecurityScan securityScan = node.getSecurityScan();

            if (securityScan.isConfigurable()) {
                SecurityScanConfig backupScanConfig = (SecurityScanConfig) securityScan.getConfig().copy();

                SecurityConfigurationDialog dialog = SoapUI.getSoapUICore().getSecurityScanRegistry().getUIBuilder()
                        .buildSecurityScanConfigurationDialog((SecurityScan) securityScan);

                if (!dialog.configure()) {
                    securityScan.copyConfig(backupScanConfig);
                }

                dialog.release();
            }
        }
    }

    public class RemoveSecurityScanAction extends AbstractAction {
        public RemoveSecurityScanAction() {
            super("Remove SecurityScan");
            putValue(Action.SHORT_DESCRIPTION, "Removes the selected security scan");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/remove_security_scan.gif"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            if (securityTest.isRunning()) {
                UISupport.showErrorMessage("Can not delete SecurityScan while the SecurityTest is running");
                return;
            }

            if (securityTestTree.getSelectionCount() == 1) {
                SecurityScanNode node = (SecurityScanNode) securityTestTree.getLastSelectedPathComponent();
                SecurityScan securityScan = node.getSecurityScan();

                TestStep testStep = ((TestStepNode) node.getParent()).getTestStep();
                if (UISupport.confirm("Remove security scan [" + securityScan.getName() + "]", "Remove SecurityScan")) {
                    securityTest.removeSecurityScan(testStep, (SecurityScan) securityScan);
                }
            } else {
                SecurityScanNode node = (SecurityScanNode) securityTestTree.getLastSelectedPathComponent();

                TestStep testStep = ((TestStepNode) node.getParent()).getTestStep();
                if (UISupport.confirm("Remove all selected security scans", "Remove SecurityScan")) {
                    for (TreePath path : securityTestTree.getSelectionPaths()) {
                        if (path.getLastPathComponent() instanceof SecurityScanNode) {
                            SecurityScan securityScan = ((SecurityScanNode) path.getLastPathComponent()).getSecurityScan();
                            securityTest.removeSecurityScan(testStep, (SecurityScan) securityScan);
                        }
                    }
                }
            }
        }
    }

    public class EnableSecurityScans extends AbstractAction {
        EnableSecurityScans() {
            super("Enable Scans");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            for (TreePath path : securityTestTree.getSelectionPaths()) {
                if (path.getLastPathComponent() instanceof SecurityScanNode) {
                    ((SecurityScanNode) path.getLastPathComponent()).getSecurityScan().setDisabled(false);
                }
            }
        }

    }

    public class DisableSecurityScans extends AbstractAction {

        public DisableSecurityScans() {
            super("Disable Scans");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (TreePath path : securityTestTree.getSelectionPaths()) {
                if (path.getLastPathComponent() instanceof SecurityScanNode) {
                    ((SecurityScanNode) path.getLastPathComponent()).getSecurityScan().setDisabled(true);
                }
            }
        }

    }

    public class ExpandTreeAction extends AbstractAction {
        public ExpandTreeAction() {
            super("Expand Tree");
            putValue(Action.SHORT_DESCRIPTION, "Expand Tree");
        }

        public void actionPerformed(ActionEvent e) {

            for (int row = 0; row < securityTestTree.getRowCount(); row++) {
                securityTestTree.expandRow(row);
            }
        }
    }

    public class CollapsTreeAction extends AbstractAction {
        public CollapsTreeAction() {
            super("Collaps Tree");
            putValue(Action.SHORT_DESCRIPTION, "Collaps Tree");
        }

        public void actionPerformed(ActionEvent e) {

            for (int row = securityTestTree.getRowCount() - 1; row >= 0; row--) {
                securityTestTree.collapseRow(row);
            }
        }
    }

    public class InternalSecurityTestRunListener extends SecurityTestRunListenerAdapter {

        @Override
        public void beforeSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                       SecurityScan securityScan) {
            securityTestTree.setSelectionRow(securityTestTree.getRowForPath(new TreePath(treeModel.getSecurityScanNode(
                    securityScan).getPath())));
        }

        @Override
        public void beforeRun(TestCaseRunner testRunner, SecurityTestRunContext runContext) {
            disableAllActions();
        }

        @Override
        public void afterRun(TestCaseRunner testRunner, SecurityTestRunContext runContext) {
            enableActionsAfterRun();
        }
    }

    // tree selection
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        enableActionsAfterRun();
    }

    /**
     *
     */
    protected void enableActionsAfterRun() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) securityTestTree.getLastSelectedPathComponent();

		/* if nothing is selected */
        if (node == null) {
            return;
        }

        if (node instanceof TestStepNode) {
            enableTestStepActions(node);
        } else if (node instanceof SecurityScanNode) {
            enableSecurityScanActions();
        }
    }

    protected void enableSecurityScanActions() {
        if (securityTest.isRunning()) {
            return;
        }
        securityTestLog.locateSecurityScan(((SecurityScanNode) securityTestTree.getLastSelectedPathComponent())
                .getSecurityScan());
        addSecurityScanAction.setEnabled(false);
        configureSecurityScanAction.setEnabled(true);
        removeSecurityScanAction.setEnabled(true);
        if (((SecurityScanNode) securityTestTree.getLastSelectedPathComponent()).getSecurityScan() instanceof AbstractSecurityScanWithProperties) {
            cloneParametersAction.setEnabled(true);
            cloneParametersAction
                    .setSecurityScan((AbstractSecurityScanWithProperties) ((SecurityScanNode) securityTestTree
                            .getLastSelectedPathComponent()).getSecurityScan());
        }
    }

    protected void enableTestStepActions(DefaultMutableTreeNode node) {
        if (securityTest.isRunning()) {
            return;
        }
        if (node.getAllowsChildren()) {
            addSecurityScanAction.setEnabled(true);
        } else {
            addSecurityScanAction.setEnabled(false);
        }
        configureSecurityScanAction.setEnabled(false);
        removeSecurityScanAction.setEnabled(false);
        cloneParametersAction.setEnabled(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) securityTestTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            return;
        }
        /* if nothing is selected */
        if (e.getClickCount() == 1) {
            if (securityTestTree.isExpanded(TreePathUtils.getPath(node)) && node instanceof TestStepNode
                    && cellRender.isOn((TestStepNode) node, e.getX(), e.getY())) {
                securityTestTree.collapseRow(securityTestTree.getRowForLocation(e.getX(), e.getY()));
            } else {
                securityTestTree.expandRow(securityTestTree.getRowForLocation(e.getX(), e.getY()));
            }
            e.consume();
            return;
        }

        if (node instanceof SecurityScanNode) {
            if (securityTest.isRunning()) {
                return;
            }
            SecurityScan securityScan = ((SecurityScanNode) securityTestTree.getLastSelectedPathComponent())
                    .getSecurityScan();

            if (securityScan.isConfigurable()) {
                SecurityScanConfig backupScanConfig = (SecurityScanConfig) securityScan.getConfig().copy();

                SecurityConfigurationDialog dialog = SoapUI.getSoapUICore().getSecurityScanRegistry().getUIBuilder()
                        .buildSecurityScanConfigurationDialog((SecurityScan) securityScan);

                if (!dialog.configure()) {
                    securityScan.copyConfig(backupScanConfig);
                }

                dialog.release();
            }
        } else {
            if (securityTestTree.isExpanded(TreePathUtils.getPath(node))) {
                UISupport.selectAndShow(((TestStepNode) node).getTestStep());
                e.consume();
            }
        }

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (securityTest.isRunning()) {
            return;
        }
        TreePath path = securityTestTree.getPathForLocation(e.getX(), e.getY());
        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK
                && (securityTestTree.getSelectionRows().length <= 1 || multypopupvisible)) {
            securityTestTree.setSelectionPath(path);
            multypopupvisible = false;
        }

        Object node = securityTestTree.getLastSelectedPathComponent();

        if (node == null) {
            return;
        }
        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            if (node instanceof SecurityScanNode) {
                // one selected
                if (securityTestTree.getSelectionRows().length == 1) {
                    SecurityScan scan = ((SecurityScanNode) node).getSecurityScan();
                    enableDisableSecurityScan.setText(scan.isDisabled());
                    if (scan instanceof AbstractSecurityScanWithProperties) {
                        securityScanWithPropertiesPopUp.show(securityTestTree, e.getX(), e.getY());
                    } else {
                        securityScanPopUp.show(securityTestTree, e.getX(), e.getY());
                    }
                } else if (securityTestTree.getSelectionRows().length > 1) {
                    // check if selected are all enabled/disabled
                    populateMultySecurityScanPopup(true, true);
                    boolean hasEnabledScans = false;
                    boolean hasDisabledScans = false;

                    for (TreePath path2 : securityTestTree.getSelectionPaths()) {
                        if (path2.getLastPathComponent() instanceof SecurityScanNode) {
                            if (((SecurityScanNode) path2.getLastPathComponent()).getSecurityScan().isDisabled()) {
                                hasDisabledScans = true;
                            } else {
                                hasEnabledScans = true;
                            }
                        }
                    }

                    if (hasEnabledScans && !hasDisabledScans) {
                        populateMultySecurityScanPopup(false, true);
                    } else if (!hasEnabledScans && hasDisabledScans) {
                        populateMultySecurityScanPopup(true, false);
                    }

                    multySecurityScanPopUp.show(securityTestTree, e.getX(), e.getY());
                }
            } else if (((TestStepNode) node).getTestStep() instanceof Securable) {
                testStepPopUp.show(securityTestTree, e.getX(), e.getY());
            }
        }
    }

    public class CustomTreeUI extends BasicTreeUI {

        public CustomTreeUI() {
            super();
            leftChildIndent = 0;
            rightChildIndent = 0;
            totalChildIndent = 0;

        }

        @Override
        public int getLeftChildIndent() {
            return 0;

        }

        @Override
        protected void installListeners() {
            super.installListeners();

            tree.addComponentListener(componentListener);
        }

        @Override
        protected void uninstallListeners() {
            tree.removeComponentListener(componentListener);

            super.uninstallListeners();
        }

        @Override
        protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
            return new NodeDimensionsHandler() {
                @Override
                public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle size) {
                    Rectangle dimensions = super.getNodeDimensions(value, row, depth, expanded, size);
                    Insets insets = tree.getInsets();
                    if (scrollPane == null) {
                        dimensions.width = tree.getWidth() - getRowX(row, depth) - insets.right;
                    } else {
                        dimensions.width = scrollPane.getViewport().getWidth() - getRowX(row, depth) - insets.right;
                    }
                    return dimensions;
                }
            };
        }

        private final ComponentListener componentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                treeState.invalidateSizes();
                tree.repaint();
            }

            ;
        };

        protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path,
                                int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
            super.paintRow(g, clipBounds, insets, new Rectangle(0, bounds.y, bounds.width + bounds.x, bounds.height),
                    path, row, isExpanded, hasBeenExpanded, isLeaf);
        }

        ;

        @Override
        protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds,
                                                TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
        }

        ;

        @Override
        protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, TreePath path) {
            // TODO Auto-generated method stub
            // super.paintVerticalPartOfLeg( g, clipBounds, insets, path );
        }
    }

    public void release() {
        cellRender.release();
        securityTest.getTestCase().getTestSuite().removeTestSuiteListener(testSuiteListener);
        securityTest.removeSecurityTestListener(this);
        securityTest.removeSecurityTestRunListener(testRunListener);
        if (treeModel != null) {
            treeModel.release();
        }
    }

    @Override
    public void securityScanAdded(SecurityScan securityScan) {
        treeModel.addSecurityScanNode(securityTestTree, securityScan);

    }

    @Override
    public void securityScanRemoved(SecurityScan securityScan) {
        cellRender.remove(treeModel.getSecurityScanNode(securityScan));
        treeModel.removeSecurityScanNode(securityScan);
    }

    /**
     *
     */
    protected void disableAllActions() {
        addSecurityScanAction.setEnabled(false);
        configureSecurityScanAction.setEnabled(false);
        removeSecurityScanAction.setEnabled(false);
        cloneParametersAction.setEnabled(false);
    }

}
