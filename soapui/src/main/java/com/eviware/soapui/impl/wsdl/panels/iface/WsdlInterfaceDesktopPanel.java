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

package com.eviware.soapui.impl.wsdl.panels.iface;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.actions.SoapUIPreferencesAction;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.actions.iface.CreateWsdlDocumentationAction;
import com.eviware.soapui.impl.wsdl.actions.iface.ExportDefinitionAction;
import com.eviware.soapui.impl.wsdl.actions.iface.UpdateInterfaceAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wsi.WSIAnalyzeAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wsi.WSIReportPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JEditorStatusBar;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.MetricsPanel;
import com.eviware.soapui.support.components.MetricsPanel.MetricType;
import com.eviware.soapui.support.components.MetricsPanel.MetricsSection;
import com.eviware.soapui.support.components.ProgressDialog;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXTable;
import org.w3c.dom.Element;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.wsdl.BindingOperation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DesktopPanel for WsdlInterface. Loads all referenced wsdls/xsds for the
 * specified WsdlInterface and displays these in seperate tabs
 *
 * @author Ole.Matzura
 */

public class WsdlInterfaceDesktopPanel extends ModelItemDesktopPanel<WsdlInterface> {
    private final static Logger logger = LogManager.getLogger(WsdlInterfaceDesktopPanel.class);
    private JTabbedPane partTabs;
    private List<RSyntaxTextArea> editors = new ArrayList<RSyntaxTextArea>();
    private JTree tree;
    private Map<String, DefaultMutableTreeNode> groupNodes = new HashMap<String, DefaultMutableTreeNode>();
    private Map<String, TreePath> pathMap = new HashMap<String, TreePath>();
    private List<TreePath> navigationHistory = new ArrayList<TreePath>();
    private StringList targetNamespaces = new StringList();
    private int historyIndex;
    private boolean navigating;
    private JEditorStatusBar statusBar;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private InternalProjectListener projectListener;
    private final WsdlInterface iface;
    private JPanel wsiPanel;
    private WSIReportPanel reportPanel;
    private SaveWsiReportAction saveWsiReportAction;
    private MetricsPanel metrics;
    private boolean updatingInterface;
    private OperationsTableModel operationsTableModel;

    public WsdlInterfaceDesktopPanel(WsdlInterface iface) {
        super(iface);
        this.iface = iface;

        try {
            iface.getWsdlContext().loadIfNecessary();
            buildUI();
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    SoapUI.getDesktop().closeDesktopPanel(WsdlInterfaceDesktopPanel.this);
                }
            });
        }
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", buildInterfaceOverviewTab());
        tabs.addTab("Service Endpoints", buildEndpointsTab());
        tabs.addTab("WSDL Content", buildWsdlContentTab());
        tabs.addTab("WS-I Compliance", buildWSITab());

        add(UISupport.createTabPanel(tabs, true), BorderLayout.CENTER);
    }

    private Component buildWSITab() {
        wsiPanel = new JPanel(new BorderLayout());
        wsiPanel.setBackground(Color.WHITE);
        wsiPanel.setOpaque(true);

        wsiPanel.add(builderWsiToolbar(), BorderLayout.NORTH);

        return wsiPanel;
    }

    private Component builderWsiToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.addFixed(UISupport.createToolbarButton(new RunWSIAction()));
        toolbar.addFixed(UISupport.createToolbarButton(new WSIOptionsAction()));
        toolbar.addRelatedGap();
        saveWsiReportAction = new SaveWsiReportAction();
        toolbar.addFixed(UISupport.createToolbarButton(saveWsiReportAction));

        toolbar.addGlue();
        toolbar.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.WSI_COMPLIANCE_HELP_URL)));

        return toolbar;
    }

    private Component buildInterfaceOverviewTab() {
        metrics = new MetricsPanel();
        JXToolBar toolbar = UISupport.createSmallToolbar();
        toolbar.addGlue();
        toolbar.addFixed(UISupport
                .createToolbarButton(new ShowOnlineHelpAction(HelpUrls.INTERFACE_OVERVIEW_HELP_URL)));
        metrics.add(toolbar, BorderLayout.NORTH);
        MetricsSection section = metrics.addSection("WSDL Definition");

        try {
            section.addMetric("WSDL URL", MetricType.URL).set(iface.getDefinition());
            section.addMetric("Namespace").set(iface.getBindingName().getNamespaceURI());
            section.addMetric("Binding").set(iface.getBindingName().getLocalPart());
            section.addMetric("SOAP Version").set(iface.getSoapVersion().toString());
            section.addMetric("Style").set(iface.getStyle());
            section.addMetric("WS-A version").set(iface.getWsaVersion());
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }

        section.finish();

        metrics.addSection("Definition Parts");
        section = metrics.addSection("Operations");
        operationsTableModel = new OperationsTableModel();
        JXTable table = section.addTable(operationsTableModel);
        table.getColumn(1).setPreferredWidth(60);
        table.getColumn(2).setPreferredWidth(60);
        section.finish();

        table.packColumn(3, 10);
        if (table.getColumn(3).getPreferredWidth() < 250) {
            table.getColumn(3).setPreferredWidth(250);
        }

        return new JScrollPane(metrics);
    }

    private Component buildEndpointsTab() {
        return iface.getProject().getEndpointStrategy().getConfigurationPanel(iface);
    }

    private JComponent buildWsdlContentTab() {
        partTabs = new JTabbedPane();
        partTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        rootNode = new DefaultMutableTreeNode(iface.getName());
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        tree.setExpandsSelectedPaths(true);
        tree.addTreeSelectionListener(new InternalTreeSelectionListener());
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (arg0.getClickCount() > 1) {
                    TreePath selectionPath = tree.getSelectionPath();
                    if (selectionPath != null) {
                        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                        Object userObject = treeNode.getUserObject();
                        if (userObject instanceof InspectItem) {
                            InspectItem item = (InspectItem) userObject;
                            if (item != null && item.selector != null) {
                                item.selector.selectNode(item);
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tree);
        JSplitPane split = UISupport.createHorizontalSplit(scrollPane, UISupport.createTabPanel(partTabs, true));

        split.setDividerLocation(250);
        split.setResizeWeight(0.3);

        initTreeModel(iface);

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(split, BorderLayout.CENTER);
        panel.add(buildWsdlTabToolbar(), BorderLayout.PAGE_START);
        statusBar = new JEditorStatusBar();
        panel.add(statusBar, BorderLayout.PAGE_END);
        setPreferredSize(new Dimension(600, 500));

        projectListener = new InternalProjectListener();
        iface.getProject().addProjectListener(projectListener);

        return panel;
    }

    private void initTreeModel(WsdlInterface iface) {
        try {
            if (iface.getWsdlContext().loadIfNecessary()) {
                XProgressDialog progressDialog = UISupport.getDialogs().createProgressDialog("Loading Defintion", 3,
                        "Initializing definition..", true);
                Loader loader = new Loader(iface);

                if (progressDialog != null) {
                    progressDialog.setCancelLabel("Run in background");
                }

                progressDialog.run(loader);
                loader = null;
                treeModel.nodeStructureChanged(rootNode);
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    private Component buildWsdlTabToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.addFixed(UISupport.createToolbarButton(new BackwardAction()));
        toolbar.addFixed(UISupport.createToolbarButton(new ForwardAction()));
        toolbar.addUnrelatedGap();
        JButton button = UISupport.createToolbarButton(SwingActionDelegate.createDelegate(
                UpdateInterfaceAction.SOAPUI_ACTION_ID, getModelItem(), null, "/updateDefinition.gif"));
        button.setText(null);
        toolbar.addFixed(button);
        button = UISupport.createToolbarButton(SwingActionDelegate.createDelegate(
                ExportDefinitionAction.SOAPUI_ACTION_ID, getModelItem(), null, "/export.png"));
        button.setText(null);
        toolbar.addFixed(UISupport.createToolbarButton(SwingActionDelegate.createDelegate(
                CreateWsdlDocumentationAction.SOAPUI_ACTION_ID, iface, null, "/report.png")));
        toolbar.addFixed(button);
        toolbar.addGlue();
        button = UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.WSDL_CONTENT_HELP_URL));
        button.setText(null);
        toolbar.addFixed(button);

        return toolbar;
    }

    private final class InternalProjectListener extends ProjectListenerAdapter {
        @Override
        public void interfaceUpdated(Interface iface) {
            if (iface == getModelItem()) {
                updatingInterface = true;
                partTabs.removeAll();
                tree.setSelectionRow(-1);
                rootNode.removeAllChildren();
                editors.clear();
                groupNodes.clear();
                pathMap.clear();
                targetNamespaces.clear();
                initTreeModel((WsdlInterface) iface);
                operationsTableModel.fireTableDataChanged();
                updatingInterface = false;
            }
        }
    }

    private final class InternalTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            TreePath newLeadSelectionPath = e.getNewLeadSelectionPath();
            if (newLeadSelectionPath != null) {
                if (!navigating) {
                    // if we have moved back in history.. reverse before adding
                    while (historyIndex < navigationHistory.size() - 1) {
                        TreePath path = navigationHistory.remove(navigationHistory.size() - 1);
                        navigationHistory.add(historyIndex++, path);
                    }

                    navigationHistory.add(newLeadSelectionPath);
                    historyIndex = navigationHistory.size() - 1;
                }

                DefaultMutableTreeNode tn = (DefaultMutableTreeNode) newLeadSelectionPath.getLastPathComponent();
                if (tn.getUserObject() instanceof InspectItem) {
                    InspectItem item = (InspectItem) tn.getUserObject();

                    partTabs.setSelectedIndex(item.getTabIndex());
                    statusBar.setInfo(item.getDescription());

                    RSyntaxTextArea editor = editors.get(item.getTabIndex());
                    int lineNumber = item.getLineNumber();
                    try {
                        if (lineNumber > 0 && editor.getLineStartOffset(lineNumber) >= 0) {
                            editor.setCaretPosition(editor.getLineStartOffset(lineNumber));
                        } else {
                            editor.setCaretPosition(0);
                        }
                    } catch (BadLocationException e1) {
                        SoapUI.logError(e1, "Unable to reset the caret position");
                    }
                }

                tree.scrollPathToVisible(newLeadSelectionPath);
                tree.expandPath(newLeadSelectionPath);
            }
        }
    }

    private class Loader implements Worker {
        private static final String DEFINITION_PARTS_SECTION = "Definition Parts";
        private ProgressDialog progressDialog;
        private final WsdlInterface iface;
        private JProgressBar progressBar;

        public Loader(WsdlInterface iface) {
            this.iface = iface;
        }

        public Object construct(XProgressMonitor monitor) {
            MetricsSection section = metrics.getSection(DEFINITION_PARTS_SECTION);
            section.clear();

            try {
                List<InterfaceDefinitionPart> schemas = iface.getWsdlContext().getDefinitionParts();
                int tabCount = partTabs.getTabCount();

                for (InterfaceDefinitionPart part : schemas) {
                    addTab(part.getUrl(), part.getContent());
                }

                while (tabCount-- > 0) {
                    partTabs.remove(0);
                }

                return null;
            } catch (Exception e) {
                logger.error("Failed to load WSDL; " + e.getClass().getSimpleName() + "; " + e.getMessage());
                add(new JLabel("Failed to load WSDL; " + e.toString()), BorderLayout.NORTH);

                SoapUI.logError(e);

                return e;
            } finally {
                section.finish();
            }

        }

        private void addTab(String url, String content) throws Exception {
            int ix = url.startsWith("file:") ? url.lastIndexOf(File.separatorChar) : url.lastIndexOf('/');
            if (ix == -1) {
                ix = url.lastIndexOf('/');
            }

            String title = url.substring(ix + 1);

            metrics.getSection(DEFINITION_PARTS_SECTION).addMetric(title, MetricType.URL).set(url);

            if (progressBar != null) {
                progressBar.setString(title);
            } else if (progressDialog != null) {
                progressDialog.setProgress(1, title);
            }

            JPanel panel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(url);
            label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            panel.add(label, BorderLayout.NORTH);

            RSyntaxTextArea inputArea = SyntaxEditorUtil.createDefaultXmlSyntaxTextArea();
            StringWriter writer = new StringWriter();
            // XmlUtils.serializePretty( XmlObject.Factory.parse( content ), writer
            // );
            XmlUtils.serializePretty(XmlUtils.createXmlObject(content), writer);
            String xmlString = writer.toString();

            // reparse so line numbers are correct
            // XmlObject xmlObject = XmlObject.Factory.parse( xmlString, new
            // XmlOptions().setLoadLineNumbers() );
            XmlObject xmlObject = XmlUtils.createXmlObject(xmlString, new XmlOptions().setLoadLineNumbers());

            inputArea.setText(xmlString);
            inputArea.setEditable(false);
            inputArea.setFont(UISupport.getEditorFont());

            JPanel p = new JPanel(new BorderLayout());
            RTextScrollPane scrollPane = new RTextScrollPane(inputArea);
            UISupport.addPreviewCorner(scrollPane, true);
            p.add(scrollPane, BorderLayout.CENTER);
            panel.add(p, BorderLayout.CENTER);
            partTabs.addTab(title, panel);

            if (tree != null) {
                initInspectionTree(xmlObject, inputArea);
            }
        }

        private void initInspectionTree(XmlObject xmlObject, RSyntaxTextArea inputArea) {
            DefaultMutableTreeNode treeRoot = rootNode;

            targetNamespaces.add(SchemaUtils.getTargetNamespace(xmlObject));

            int tabCount = partTabs.getTabCount() - 1;
            mapTreeItems(xmlObject, treeRoot, false, tabCount, "Complex Types",
                    "declare namespace xs='http://www.w3.org/2001/XMLSchema';//xs:complexType[@name!='']", "@name", true,
                    null);

            mapTreeItems(xmlObject, treeRoot, false, tabCount, "Simple Types",
                    "declare namespace xs='http://www.w3.org/2001/XMLSchema';//xs:simpleType[@name!='']", "@name", true,
                    null);

            mapTreeItems(xmlObject, treeRoot, false, tabCount, "Anonymous Complex Types",
                    "declare namespace xs='http://www.w3.org/2001/XMLSchema';//xs:complexType[not(exists(@name))]",
                    "parent::node()/@name", true, null);

            mapTreeItems(xmlObject, treeRoot, false, tabCount, "Global Elements",
                    "declare namespace xs='http://www.w3.org/2001/XMLSchema';//xs:schema/xs:element[@name!='']", "@name",
                    true, new GlobalElementSelector());

            mapTreeItems(xmlObject, treeRoot, false, tabCount, "Schemas",
                    "declare namespace xs='http://www.w3.org/2001/XMLSchema';//xs:schema", "@targetNamespace", true, null);

            List<DefaultMutableTreeNode> messages = mapTreeItems(xmlObject, treeRoot, false, tabCount, "Messages",
                    "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';//wsdl:message", "@name", true, null);

            for (DefaultMutableTreeNode treeNode : messages) {
                mapTreeItems(
                        ((InspectItem) treeNode.getUserObject()).item,
                        treeNode,
                        false,
                        tabCount,
                        null,
                        "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';wsdl:part",
                        "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';concat('part: name=[', @name, '] type=[', @type, '] element=[', @element, ']' )",
                        true, new PartSelector());
            }

            List<DefaultMutableTreeNode> portTypes = mapTreeItems(xmlObject, treeRoot, false, tabCount, "PortTypes",
                    "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';//wsdl:portType", "@name", true, null);

            for (DefaultMutableTreeNode treeNode : portTypes) {
                List<DefaultMutableTreeNode> operationNodes = mapTreeItems(
                        ((InspectItem) treeNode.getUserObject()).item, treeNode, false, tabCount, null,
                        "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';wsdl:operation", "@name", true, null);

                for (DefaultMutableTreeNode treeNode2 : operationNodes) {
                    mapTreeItems(((InspectItem) treeNode2.getUserObject()).item, treeNode2, false, tabCount, null,
                            "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';wsdl:*",
                            "concat( @name, ' [', local-name(), '], message=[', @message, ']' )", false, new MessageSelector());
                }
            }

            List<DefaultMutableTreeNode> bindings = mapTreeItems(
                    xmlObject,
                    treeRoot,
                    false,
                    tabCount,
                    "Bindings",
                    "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';//wsdl:binding",
                    "declare namespace wsdlsoap='http://schemas.xmlsoap.org/wsdl/soap/';concat( @name, ' [style=', wsdlsoap:binding[1]/@style, ']' )",
                    true, null);

            for (DefaultMutableTreeNode treeNode : bindings) {
                List<DefaultMutableTreeNode> operationNodes = mapTreeItems(
                        ((InspectItem) treeNode.getUserObject()).item,
                        treeNode,
                        false,
                        tabCount,
                        null,
                        "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';wsdl:operation",
                        "declare namespace wsdlsoap='http://schemas.xmlsoap.org/wsdl/soap/';concat( @name, ' [soapAction=', wsdlsoap:operation/@soapAction, ']' )",
                        true, null);

                for (DefaultMutableTreeNode treeNode2 : operationNodes) {
                    mapTreeItems(((InspectItem) treeNode2.getUserObject()).item, treeNode2, false, tabCount, null,
                            "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';wsdl:*",
                            "concat( @name, ' [', local-name(), ']' )", false, new BindingOperationSelector());
                }
            }

            List<DefaultMutableTreeNode> services = mapTreeItems(xmlObject, treeRoot, false, tabCount, "Services",
                    "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';//wsdl:service", "@name", true, null);

            for (DefaultMutableTreeNode treeNode : services) {
                mapTreeItems(((InspectItem) treeNode.getUserObject()).item, treeNode, false, tabCount, null,
                        "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/';wsdl:port",
                        "concat( 'port: name=[', @name, '] binding=[', @binding, ']' )", true, new PortSelector());
            }

            tree.expandRow(0);
            editors.add(inputArea);
        }

        public void finished() {
            if (progressDialog != null) {
                progressDialog.setVisible(false);
            }

            progressDialog = null;
        }

        public boolean onCancel() {
            progressBar = new JProgressBar(0, 1);
            progressBar.setSize(new Dimension(120, 20));
            progressBar.setStringPainted(true);
            progressBar.setString("Loading Definition..");
            progressBar.setIndeterminate(true);

            ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
            builder.addGlue();
            builder.addFixed(progressBar);
            builder.addGlue();
            builder.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            partTabs.addTab("Loading.. ", builder.getPanel());
            return true;
        }
    }

    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == getModelItem() || modelItem == getModelItem().getProject();
    }

    public List<DefaultMutableTreeNode> mapTreeItems(XmlObject xmlObject, DefaultMutableTreeNode treeRoot,
                                                     boolean createEmpty, int tabIndex, String groupName, String query, String nameQuery, boolean sort,
                                                     NodeSelector selector) {
        List<DefaultMutableTreeNode> resultNodes = new ArrayList<DefaultMutableTreeNode>();

        try {
            XmlObject[] items = xmlObject.selectPath(query);
            List<DefaultMutableTreeNode> treeNodes = new ArrayList<DefaultMutableTreeNode>();

            DefaultMutableTreeNode root = treeRoot;
            if (groupName != null) {
                String groupKey = new TreePath(root.getPath()).toString() + "/" + groupName;
                root = groupNodes.get(groupKey);
                if (root == null && (items.length > 0 || createEmpty)) {
                    root = new DefaultMutableTreeNode(groupName);
                    treeRoot.add(root);
                    groupNodes.put(groupKey, root);
                } else if (root != null) {
                    Enumeration<?> children = root.children();
                    while (children.hasMoreElements()) {
                        treeNodes.add((DefaultMutableTreeNode) children.nextElement());
                    }
                }
            }

            if (items.length == 0) {
                return resultNodes;
            }

            for (XmlObject item : items) {
                XmlObject[] selectPath = item.selectPath(nameQuery);
                if (selectPath.length > 0) {
                    DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(new InspectItem(item, selectPath[0],
                            tabIndex, selector));
                    treeNodes.add(treeNode);
                    resultNodes.add(treeNode);
                }
            }

            if (sort) {
                Collections.sort(treeNodes, new Comparator<DefaultMutableTreeNode>() {

                    public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                });
            }

            root.removeAllChildren();

            for (DefaultMutableTreeNode treeNode : treeNodes) {
                root.add(treeNode);

                String path = "/" + getTreeNodeName(treeNode);
                TreePath treePath = new TreePath(treeNode.getPath());
                while (treeNode.getParent() != null) {
                    treeNode = (DefaultMutableTreeNode) treeNode.getParent();
                    path = "/" + getTreeNodeName(treeNode) + path;
                }

                pathMap.put(path, treePath);
            }
        } catch (Throwable e) {
            SoapUI.log("Failed to map items for query [" + query + "]:[" + nameQuery + "]");
            SoapUI.logError(e);
        }

        return resultNodes;
    }

    private String getTreeNodeName(DefaultMutableTreeNode treeNode) {
        Object userObject = treeNode.getUserObject();
        if (userObject instanceof InspectItem) {
            return ((InspectItem) userObject).getName();
        } else {
            return treeNode.toString();
        }
    }

    private final class InspectItem {
        private final XmlObject item;
        private String name;
        private final int tabIndex;
        private XmlLineNumber lineNumber;
        private final NodeSelector selector;

        public InspectItem(XmlObject item, XmlObject nameObj, int tabIndex, NodeSelector selector) {
            this.item = item;
            this.selector = selector;
            this.name = XmlUtils.getNodeValue(nameObj.getDomNode());
            if (name == null) {
                name = nameObj.toString();
            }
            this.tabIndex = tabIndex;

            ArrayList<?> list = new ArrayList<Object>();
            XmlCursor cursor = item.newCursor();
            cursor.getAllBookmarkRefs(list);

            for (Object o : list) {
                if (o instanceof XmlLineNumber) {
                    lineNumber = (XmlLineNumber) o;
                }
            }

            cursor.dispose();
        }

        public String getDescription() {
            return getName() + "@" + targetNamespaces.get(tabIndex);
        }

        public String getName() {
            int ix = name.indexOf(' ');
            return ix == -1 ? name : name.substring(0, ix);
        }

        public int getTabIndex() {
            return tabIndex;
        }

        public int getLineNumber() {
            return lineNumber == null ? -1 : lineNumber.getLine() - 1;
        }

        @Override
        public String toString() {
            return name;
        }

        public NodeSelector getSelector() {
            return selector;
        }

        public Element getElement() {
            return (Element) item.getDomNode();
        }
    }

    public boolean onClose(boolean canCancel) {
        if (projectListener != null) {
            getModelItem().getProject().removeProjectListener(projectListener);
        }

        return release();
    }

    private void simpleSelect(InspectItem item, String attribute, String targetGroup) {
        Element elm = item.getElement();
        String type = elm.getAttribute(attribute);
        if (type.length() > 0) {
            int ix = type.indexOf(':');
            if (ix != -1) {
                type = type.substring(ix + 1);
            }

            TreePath treePath = pathMap.get("/" + getModelItem().getName() + "/" + targetGroup + "/" + type);
            if (treePath != null) {
                tree.setSelectionPath(treePath);
            }
        }
    }

    protected interface NodeSelector {
        public void selectNode(InspectItem item);
    }

    public class PartSelector implements NodeSelector {
        public void selectNode(InspectItem item) {
            Element elm = item.getElement();
            String type = elm.getAttribute("type");
            String element = elm.getAttribute("element");
            if (type.length() > 0) {
                simpleSelect(item, "type", "Complex Types");
            } else if (element.length() > 0) {
                simpleSelect(item, "element", "Global Elements");
            }
        }
    }

    public class MessageSelector implements NodeSelector {
        public void selectNode(InspectItem item) {
            simpleSelect(item, "message", "Messages");
        }
    }

    public class GlobalElementSelector implements NodeSelector {
        public void selectNode(InspectItem item) {
            simpleSelect(item, "type", "Complex Types");
        }
    }

    public class PortSelector implements NodeSelector {
        public void selectNode(InspectItem item) {
            simpleSelect(item, "binding", "Bindings");
        }
    }

    public class BindingOperationSelector implements NodeSelector {
        public void selectNode(InspectItem item) {
            Element elm = item.getElement();
            String name = elm.getAttribute("name");

            Element operationElm = (Element) elm.getParentNode();
            Element bindingElm = (Element) operationElm.getParentNode();

            String type = bindingElm.getAttribute("type");

            if (type.length() > 0) {
                int ix = type.indexOf(':');
                if (ix != -1) {
                    type = type.substring(ix + 1);
                }

                TreePath treePath = pathMap.get("/" + getModelItem().getName() + "/PortTypes/" + type + "/"
                        + operationElm.getAttribute("name") + "/" + name);
                if (treePath != null) {
                    tree.setSelectionPath(treePath);
                }
            }
        }
    }

    private class BackwardAction extends AbstractAction {
        public BackwardAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/arrow_left.png"));
            putValue(Action.SHORT_DESCRIPTION, "Navigate to previous selection");
        }

        public void actionPerformed(ActionEvent arg0) {
            if (historyIndex > 0) {
                historyIndex--;
                navigating = true;
                tree.setSelectionPath(navigationHistory.get(historyIndex));
                navigating = false;
            }
        }
    }

    private class ForwardAction extends AbstractAction {
        public ForwardAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/arrow_right.png"));
            putValue(Action.SHORT_DESCRIPTION, "Navigate to next selection");
        }

        public void actionPerformed(ActionEvent arg0) {
            if (historyIndex < navigationHistory.size() - 1) {
                historyIndex++;
                navigating = true;
                tree.setSelectionPath(navigationHistory.get(historyIndex));
                navigating = false;
            }

        }
    }

    private class RunWSIAction extends AbstractAction {
        public RunWSIAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION, "Creates a WS-I report for this interface");
        }

        public void actionPerformed(ActionEvent arg0) {
            WSIAnalyzeAction action = new WSIAnalyzeAction() {
                @Override
                protected void showReport(File reportFile, String configFile) throws Exception {
                    reportPanel = new WSIReportPanel(reportFile, configFile, null, false);
                    reportPanel.setPreferredSize(new Dimension(600, 400));
                    if (wsiPanel.getComponentCount() > 1) {
                        wsiPanel.remove(1);
                    }

                    wsiPanel.add(reportPanel, BorderLayout.CENTER);
                    wsiPanel.revalidate();
                    saveWsiReportAction.setEnabled(true);
                }
            };

            action.perform(getModelItem(), null);
        }
    }

    private class WSIOptionsAction extends AbstractAction {
        public WSIOptionsAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/preferences.png"));
            putValue(Action.SHORT_DESCRIPTION, "Sets WS-I report creation options");
        }

        public void actionPerformed(ActionEvent arg0) {
            SoapUIPreferencesAction.getInstance().show(SoapUIPreferencesAction.WS_I_SETTINGS);
        }
    }

    private class SaveWsiReportAction extends AbstractAction {
        public SaveWsiReportAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/export.png"));
            putValue(Action.SHORT_DESCRIPTION, "Saved the current WS-I report to a file");

            setEnabled(false);
        }

        public void actionPerformed(ActionEvent arg0) {
            if (reportPanel != null) {
                reportPanel.getSaveReportAction().actionPerformed(null);
            }
        }
    }

    private class OperationsTableModel extends AbstractTableModel {
        public int getColumnCount() {
            return 4;
        }

        public int getRowCount() {
            return iface.getOperationCount();
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Use";
                case 2:
                    return "One-Way";
                case 3:
                    return "Action";
            }

            return null;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (updatingInterface) {
                return "<updating>";
            }

            WsdlOperation operation = iface.getOperationAt(rowIndex);
            BindingOperation bindingOperation = operation.getBindingOperation();

            switch (columnIndex) {
                case 0:
                    return operation.getName();
                case 1: {
                    boolean in = WsdlUtils.isInputSoapEncoded(bindingOperation);
                    boolean out = operation.isUnidirectional() ? false : WsdlUtils.isOutputSoapEncoded(bindingOperation);

                    if (out && in) {
                        return "SOAP Encoding";
                    }
                    if (!out && !in) {
                        return "Literal";
                    }

                    return "Mixed";
                }
                case 3:
                    return operation.getAction();
                case 2:
                    return Boolean.valueOf(operation.isUnidirectional());
            }

            return null;
        }
    }
}
