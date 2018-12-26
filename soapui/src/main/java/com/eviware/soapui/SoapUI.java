/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

package com.eviware.soapui;

import com.eviware.soapui.actions.SaveAllProjectsAction;
import com.eviware.soapui.actions.ShowSystemPropertiesAction;
import com.eviware.soapui.actions.SoapUIPreferencesAction;
import com.eviware.soapui.actions.StartHermesJMSButtonAction;
import com.eviware.soapui.actions.SumbitUserInfoAction;
import com.eviware.soapui.actions.SwitchDesktopPanelAction;
import com.eviware.soapui.actions.VersionUpdateAction;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.AnalyticsHelper;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.analytics.UniqueUserIdentifier;
import com.eviware.soapui.autoupdate.SoapUIAutoUpdaterUtils;
import com.eviware.soapui.autoupdate.SoapUIUpdateProvider;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.ImportWsdlProjectAction;
import com.eviware.soapui.impl.actions.NewEmptyProjectAction;
import com.eviware.soapui.impl.actions.NewRestProjectAction;
import com.eviware.soapui.impl.actions.NewWsdlProjectAction;
import com.eviware.soapui.impl.rest.actions.project.NewRestServiceAction;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.axis1.Axis1XWSDL2JavaAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.axis2.Axis2WSDL2CodeAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.cxf.CXFAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.dotnet.DotNetWsdlAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.gsoap.GSoapAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.jaxb.JaxbXjcAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.jbossws.JBossWSConsumeAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.jbossws.WSToolsWsdl2JavaAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.oracle.OracleWsaGenProxyAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.tcpmon.TcpMonAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wscompile.WSCompileAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wsimport.WSImportAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.xfire.XFireAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.xmlbeans.XmlBeans2Action;
import com.eviware.soapui.impl.wsdl.actions.support.OpenUrlAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.environment.EnvironmentListener;
import com.eviware.soapui.model.environment.Property;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.util.PanelBuilderRegistry;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceFactory;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.settings.VersionUpdateSettings;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.dnd.DropType;
import com.eviware.soapui.support.dnd.NavigatorDragAndDropable;
import com.eviware.soapui.support.dnd.SoapUIDragAndDropHandler;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.jnlp.WebstartUtilCore;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;
import com.eviware.soapui.support.log.InspectorLog4JMonitor;
import com.eviware.soapui.support.log.JLogList;
import com.eviware.soapui.support.log.Log4JMonitor;
import com.eviware.soapui.support.log.LogDisablingTestMonitorListener;
import com.eviware.soapui.support.monitor.MonitorPanel;
import com.eviware.soapui.support.monitor.RuntimeMemoryMonitorSource;
import com.eviware.soapui.support.preferences.UserPreferences;
import com.eviware.soapui.support.swing.MenuBuilderHelper;
import com.eviware.soapui.support.swing.MenuScroller;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.tools.CmdLineRunner;
import com.eviware.soapui.ui.JDesktopPanelsList;
import com.eviware.soapui.ui.Navigator;
import com.eviware.soapui.ui.NavigatorListener;
import com.eviware.soapui.ui.URLDesktopPanel;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.desktop.DesktopRegistry;
import com.eviware.soapui.ui.desktop.NullDesktop;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;
import com.eviware.soapui.ui.desktop.standalone.StandaloneDesktop;
import com.eviware.soapui.ui.support.DesktopListenerAdapter;
import com.eviware.x.impl.swing.SwingDialogs;
import com.google.common.base.Objects;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.smartbear.analytics.AnalyticsManager;
import javafx.application.Platform;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;

import static com.eviware.soapui.analytics.SoapUIActions.CREATE_EMPTY_PROJECT_FROM_TOOLBAR;
import static com.eviware.soapui.analytics.SoapUIActions.CREATE_REST_PROJECT_FROM_TOOLBAR;
import static com.eviware.soapui.analytics.SoapUIActions.CREATE_SOAP_PROJECT_FROM_TOOLBAR;
import static com.eviware.soapui.analytics.SoapUIActions.IMPORT_PREFERENCES;
import static com.eviware.soapui.analytics.SoapUIActions.IMPORT_PROJECT_FROM_TOOLBAR;
import static com.eviware.soapui.analytics.SoapUIActions.OPEN_PREFERENCES_FROM_TOOLBAR;
import static com.eviware.soapui.analytics.SoapUIActions.SAVE_ALL_PROJECTS_FROM_TOOLBAR;
import static com.eviware.soapui.analytics.SoapUIActions.SAVE_PREFERENCES;
import static com.eviware.soapui.analytics.SoapUIActions.TURN_OFF_PROXY_FROM_TOOLBAR;
import static com.eviware.soapui.analytics.SoapUIActions.TURN_ON_PROXY_FROM_TOOLBAR;
import static com.eviware.soapui.impl.support.HttpUtils.urlEncodeWithUtf8;

/**
 * Main SoapUI entry point.
 */
public class SoapUI {
    // ------------------------------ CONSTANTS ------------------------------
    public static final String DEFAULT_DESKTOP = "Default";
    public static final String CURRENT_SOAPUI_WORKSPACE = SoapUI.class.getName() + "@workspace";
    public final static Logger log = Logger.getLogger(SoapUI.class);
    public final static String SOAPUI_VERSION = getVersion(SoapUISystemProperties.VERSION);
    public final static String PRODUCT_NAME = "SoapUI";
    public static final String DEFAULT_WORKSPACE_FILE = "default-soapui-workspace.xml";
    public static final String SOAPUI_SPLASH = "SoapUI-Spashscreen.png";
    public static final String SOAPUI_TITLE = "/branded/branded.properties";
    public static final String PROPERTIES_TAB_PANEL_NAME = "PropertiesTabPanel";
    private static final String PROXY_ENABLED_ICON = "/Proxy_Turned-on.png";
    private static final String PROXY_DISABLED_ICON = "/Proxy_Turned-off.png";
    public static final String BUILDINFO_PROPERTIES = "/buildinfo.properties";
    public static final String STARTER_PAGE_HEADER = "SoapUI Starter Page";
    public static final String STARTER_PAGE_TOOL_TIP = "Info on SoapUI";
    public static String FRAME_ICON = "/SoapUI-OS_16-16.png;/SoapUI-OS_24-24.png;/SoapUI-OS_32-32.png;/SoapUI-OS_48-48.png;/SoapUI-OS_256-256.png";

    public static String STARTER_PAGE_ERROR_URL = "file://" + System.getProperty("soapui.home", ".")
            + "/starter-page.html";

    private static final int DEFAULT_DESKTOP_ACTIONS_COUNT = 3;
    private static final int DEFAULT_MAX_THREADPOOL_SIZE = 200;
    private static final String BROWSER_DISABLED_SYSTEM_PROPERTY = "soapui.browser.disabled";

    public static final String PROJECT = "Project";
    public static final String SUITE = "Suite";
    public static final String STEP = "Step";
    public static final String CASE = "Case";

    public static final String ENABLED_PROJECT_ACTIONS = "EnabledWsdlProjectActions";
    public static final String TEST_SUITE_ACTIONS = "WsdlTestSuiteActions";
    public static final String TEST_CASE_ACTIONS = "WsdlTestCaseActions";
    public static final String TEST_STEP_ACTIONS = "WsdlTestStepActions";
    // ------------------------------ FIELDS ------------------------------

    private static List<Object> logCache = new ArrayList<Object>();
    private static SoapUICore soapUICore;
    private static Timer soapUITimer = new Timer();
    private static JFrame frame;

    private static Navigator navigator;
    private static SoapUIDesktop desktop;
    private static Workspace workspace;
    private static Log4JMonitor logMonitor;
    private static Logger errorLog = Logger.getLogger("soapui.errorlog");
    private static boolean isStandalone;
    private static boolean isCommandLine;
    private static TestMonitor testMonitor;

    private JMenu desktopMenu;
    private static JMenuBar menuBar;
    private JDesktopPanelsList desktopPanelsList;

    private static Boolean checkedGroovyLogMonitor = false;
    private static Boolean launchedTestRunner = false;

    private JPanel overviewPanel;
    private boolean saveOnExit = true;
    private InternalDesktopListener internalDesktopListener = new InternalDesktopListener();
    private JInspectorPanel mainInspector;

    private static AutoSaveTimerTask autoSaveTimerTask;
    private static String workspaceName;
    private static StringToStringMap projectOptions = new StringToStringMap();
    private static URLDesktopPanel starterPageDesktopPanel;
    private static JXToolBar mainToolbar;
    private static String[] mainArgs;
    private static GCTimerTask gcTimerTask;

    private final static ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            getMaxThreadpoolSize(), new SoapUIThreadCreator());
    private JTextField searchField;
    private static JToggleButton applyProxyButton;
    private static Logger groovyLogger;
    private static CmdLineRunner soapUIRunner;

    static {
        try {
            Platform.setImplicitExit(false);
        } catch (NoClassDefFoundError e) {
            log.warn("Could not find jfxrt.jar. Internal browser will be disabled.");
            System.setProperty(BROWSER_DISABLED_SYSTEM_PROPERTY, Boolean.TRUE.toString());
        }
    }

    // --------------------------- CONSTRUCTORS ---------------------------

    private SoapUI() {
    }

    static String getVersion(String versionPropertyName) {
        String version = System.getProperty(versionPropertyName);
        if (version != null) {
            return version;
        }
        version = com.eviware.soapui.SoapUI.class.getPackage().getImplementationVersion();
        if (version != null) {
            return version;
        }
        try {
            Properties buildInfoProperties = new Properties();
            buildInfoProperties.load(SoapUI.class.getResourceAsStream(BUILDINFO_PROPERTIES));
            version = buildInfoProperties.getProperty("version");
            if (!StringUtils.isNullOrEmpty(version)) {
                return version;
            }
        } catch (Exception exception) {
            //ignore
        }
        return "UNKNOWN VERSION";
    }

    private static int getMaxThreadpoolSize() {
        try {
            return Integer.parseInt(System.getProperty("soapui.threadpool.max"));
        } catch (Exception e) {
            return DEFAULT_MAX_THREADPOOL_SIZE;
        }
    }

    public static boolean usingGraphicalEnvironment() {
        return !UISupport.isHeadless() && !isCommandLine();
    }

    private void buildUI() {
        log.info("Used java version: " + System.getProperty("java.version"));
        frame.addWindowListener(new MainFrameWindowListener());
        UISupport.setMainFrame(frame);
        navigator = new Navigator(workspace);
        navigator.addNavigatorListener(new InternalNavigatorListener());
        desktopPanelsList = new JDesktopPanelsList(desktop);

        mainInspector = JInspectorPanelFactory.build(buildContentPanel(), SwingConstants.LEFT);
        mainInspector.addInspector(new JComponentInspector<JComponent>(buildMainPanel(), "Navigator",
                "The SoapUI Navigator", true));
        mainInspector.setCurrentInspector("Navigator");
        frame.setJMenuBar(buildMainMenu());
        frame.getContentPane().add(buildToolbar(), BorderLayout.NORTH);
        frame.getContentPane().add(mainInspector.getComponent(), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        mainInspector.setDividerLocation(250);
        mainInspector.setResizeWeight(0.1);
        navigator.selectModelItem(workspace);

        desktop.addDesktopListener(internalDesktopListener);

        ToolTipManager.sharedInstance().setInitialDelay(200);

        JTree mainTree = navigator.getMainTree();
        DragSource dragSource = DragSource.getDefaultDragSource();
        SoapUIDragAndDropHandler navigatorDragAndDropHandler = new SoapUIDragAndDropHandler(
                new NavigatorDragAndDropable(mainTree), DropType.ON + DropType.AFTER);

        dragSource.createDefaultDragGestureRecognizer(mainTree, DnDConstants.ACTION_COPY_OR_MOVE,
                navigatorDragAndDropHandler);

        desktop.init();
    }

    private JComponent buildToolbar() {
        mainToolbar = new JXToolBar();
        UISupport.setPreferredHeight(mainToolbar, JXToolBar.MAIN_COMPONENT_HEIGHT);
        mainToolbar.setFloatable(false);
        mainToolbar.setRollover(true);
        mainToolbar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
        mainToolbar.addSpace(20);
        mainToolbar.add(new NewProjectActionDelegate("/new-empty-project-icon.png", "Empty", NewEmptyProjectAction.SOAPUI_ACTION_ID, CREATE_EMPTY_PROJECT_FROM_TOOLBAR));
        mainToolbar.add(new NewProjectActionDelegate("/new-soap-project-icon.png", "SOAP", NewWsdlProjectAction.SOAPUI_ACTION_ID, CREATE_SOAP_PROJECT_FROM_TOOLBAR));
        mainToolbar.add(new NewProjectActionDelegate("/new-rest-project-icon.png", "REST", NewRestProjectAction.SOAPUI_ACTION_ID, CREATE_REST_PROJECT_FROM_TOOLBAR));
        mainToolbar.add(new ImportWsdlProjectActionDelegate());
        mainToolbar.add(new SaveAllActionDelegate());
        mainToolbar.addSpace(2);
        mainToolbar.add(new ShowOnlineHelpAction("Forum", HelpUrls.COMMUNITY_HELP_URL,
                "Opens the SoapUI Forum in a browser", "/forum.png"));
        mainToolbar.addSpace(2);
        mainToolbar.add(new ShowOnlineHelpAction("Trial", HelpUrls.TRIAL_URL, "Apply for SoapUI Pro Trial License",
                "/Trial.png"));
        mainToolbar.add(new PreferencesActionDelegate());
        applyProxyButton = (JToggleButton) mainToolbar.add(new JToggleButton(new ApplyProxyButtonAction()));
        updateProxyButtonAndTooltip();

        mainToolbar.addGlue();
        searchField = new JTextField(20) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    doCommunitySearch(searchField.getText());
                }
            }
        });

        JLabel searchLabel = new JLabel("Search Forum");
        // Extra width to avoid label to be truncated
        searchLabel.setPreferredSize(new Dimension(
                (int) (searchLabel.getPreferredSize().getWidth() * 1.1),
                (int) searchLabel.getPreferredSize().getHeight()));
        mainToolbar.addFixed(searchLabel);
        mainToolbar.addSeparator(new Dimension(3, 3));
        mainToolbar.addFixed(searchField);
        mainToolbar.add(new ToolbarForumSearchAction());
        mainToolbar.add(new ShowOnlineHelpAction(HelpUrls.USERGUIDE_HELP_URL));
        for (int i = 0; i < mainToolbar.getComponentCount(); i++) {
            if (mainToolbar.getComponent(i) instanceof JComponent) {
                ((JComponent) mainToolbar.getComponent(i)).setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
            }
        }
        mainToolbar.setBorder(BorderFactory.createEmptyBorder());
        return mainToolbar;
    }

    //TODO Replace with the community API-based search
    public static void doCommunitySearch(String text) {

        String prefix = "/t5/forums/searchpage/tab/message?include_forums=true";
        String forum = "location=board%3ASoapUI_OS";
        String suffix = "&search_type=thread&filter=labels%2Clocation";

        String searchText = "&q=" + urlEncodeWithUtf8(text.trim());

        String searchUrl = HelpUrls.COMMUNITY_SEARCH_URL + prefix + forum + searchText + suffix;

        if (StringUtils.hasContent(text)) {
            Tools.openURL(searchUrl);
        } else {
            Tools.openURL(HelpUrls.COMMUNITY_SEARCH_URL);
        }
    }


    private JMenuBar buildMainMenu() {
        menuBar = new JMenuBar() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        menuBar.setBorder(BorderFactory.createEmptyBorder());
        menuBar.add(buildFileMenu());
        menuBar.add(buildProjectMenu());
        menuBar.add(buildSuiteMenu());
        menuBar.add(buildCaseMenu());
        menuBar.add(buildStepMenu());
        menuBar.add(buildToolsMenu());
        menuBar.add(buildDesktopMenu());
        menuBar.add(buildHelpMenu());
        return menuBar;
    }

    public static ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    public static Workspace getWorkspace() {
        return workspace;
    }

    private JMenu buildDesktopMenu() {
        desktopMenu = new JMenu("Desktop");
        desktopMenu.setMnemonic(KeyEvent.VK_D);
        desktopMenu.add(new SwitchDesktopPanelAction(desktopPanelsList));
        desktopMenu.add(new MaximizeDesktopAction((InspectorLog4JMonitor) logMonitor));
        desktopMenu.addSeparator();
        ActionSupport.addActions(desktop.getActions(), desktopMenu);
        return desktopMenu;
    }

    private JMenu buildProjectMenu() {
        JMenu projectMenu = MenuBuilderHelper.buildMenuForWorkspace(new JMenu(PROJECT), ENABLED_PROJECT_ACTIONS);
        return projectMenu;
    }

    private JMenu buildSuiteMenu() {
        JMenu suiteMenu = MenuBuilderHelper.buildMenuForWorkspace(new JMenu(SUITE), TEST_SUITE_ACTIONS);
        return suiteMenu;
    }

    private JMenu buildCaseMenu() {
        JMenu caseMenu = MenuBuilderHelper.buildMenuForWorkspace(new JMenu(CASE), TEST_CASE_ACTIONS);
        return caseMenu;
    }

    private JMenu buildStepMenu() {
        JMenu stepMenu = MenuBuilderHelper.buildMenuForWorkspace(new JMenu(STEP), TEST_STEP_ACTIONS);
        return stepMenu;
    }

    private JMenu buildHelpMenu() {
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        helpMenu.add(new ShowStarterPageAction());
        helpMenu.addSeparator();
        helpMenu.add(new ShowOnlineHelpAction("API Testing Dojo", HelpUrls.API_TESTING_DOJO_HELP_URL));
        helpMenu.add(new ShowOnlineHelpAction("Getting Started", HelpUrls.GETTINGSTARTED_HELP_URL));
        helpMenu.add(new SearchForumAction());
        helpMenu.addSeparator();
        helpMenu.add(new ShowSystemPropertiesAction());
        helpMenu.addSeparator();
        helpMenu.add(new VersionUpdateAction());
        helpMenu.addSeparator();
        helpMenu.add(new ShowOnlineHelpAction("SoapUI Pro Trial", HelpUrls.TRIAL_URL,
                "Apply for SoapUI Pro Trial License", "/SoapUI-OS_16-16.png"));
        helpMenu.add(new OpenUrlAction("Privacy Policy", "http://www.soapui.org" + HelpUrls.SMARTBEAR_PRIVACY_POLICY_URL));
        helpMenu.addSeparator();
        helpMenu.add(new OpenUrlAction("soapui.org", "http://www.soapui.org"));
        helpMenu.add(new OpenUrlAction("smartbear.com", HelpUrls.SMARTBEAR_WEB_SITE_START_PAGE));
        helpMenu.addSeparator();
        helpMenu.add(new AboutAction());
        return helpMenu;
    }

    private JMenu buildToolsMenu() {
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);

        toolsMenu.add(SwingActionDelegate.createDelegate(WSToolsWsdl2JavaAction.SOAPUI_ACTION_ID));
        toolsMenu.add(SwingActionDelegate.createDelegate(JBossWSConsumeAction.SOAPUI_ACTION_ID));
        toolsMenu.addSeparator();
        toolsMenu.add(SwingActionDelegate.createDelegate(WSCompileAction.SOAPUI_ACTION_ID));
        toolsMenu.add(SwingActionDelegate.createDelegate(WSImportAction.SOAPUI_ACTION_ID));
        toolsMenu.addSeparator();
        toolsMenu.add(SwingActionDelegate.createDelegate(Axis1XWSDL2JavaAction.SOAPUI_ACTION_ID));
        toolsMenu.add(SwingActionDelegate.createDelegate(Axis2WSDL2CodeAction.SOAPUI_ACTION_ID));
        toolsMenu.add(SwingActionDelegate.createDelegate(CXFAction.SOAPUI_ACTION_ID));
        toolsMenu.add(SwingActionDelegate.createDelegate(XFireAction.SOAPUI_ACTION_ID));
        toolsMenu.add(SwingActionDelegate.createDelegate(OracleWsaGenProxyAction.SOAPUI_ACTION_ID));
        toolsMenu.addSeparator();
        toolsMenu.add(SwingActionDelegate.createDelegate(XmlBeans2Action.SOAPUI_ACTION_ID));
        toolsMenu.add(SwingActionDelegate.createDelegate(JaxbXjcAction.SOAPUI_ACTION_ID));
        toolsMenu.addSeparator();
        toolsMenu.add(SwingActionDelegate.createDelegate(DotNetWsdlAction.SOAPUI_ACTION_ID));
        toolsMenu.add(SwingActionDelegate.createDelegate(GSoapAction.SOAPUI_ACTION_ID));
        toolsMenu.addSeparator();
        toolsMenu.add(SwingActionDelegate.createDelegate(TcpMonAction.SOAPUI_ACTION_ID));
        toolsMenu.addSeparator();
        StartHermesJMSButtonAction hermesJMSButtonAction = new StartHermesJMSButtonAction();
        hermesJMSButtonAction.setEnabled(HermesUtils.isHermesJMSSupported());
        toolsMenu.add(hermesJMSButtonAction);

        return toolsMenu;
    }

    private JMenu buildFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        ActionList actions = ActionListBuilder.buildActions(workspace);
        actions.removeAction(actions.getActionCount() - 1);

        ActionSupport.addActions(actions, fileMenu);

        fileMenu.add(SoapUIPreferencesAction.getInstance());
        fileMenu.add(new SavePreferencesAction());
        fileMenu.add(new ImportPreferencesAction());

        fileMenu.addSeparator();
        fileMenu.add(buildRecentMenu());
        fileMenu.addSeparator();
        fileMenu.add(new ExitAction());
        fileMenu.add(new ExitWithoutSavingAction());

        return fileMenu;
    }

    private JMenuItem buildRecentMenu() {
        JMenu recentMenu = new JMenu("Recent");

        JMenu recentProjectsMenu = new JMenu("Projects");
        JMenu recentWorkspacesMenu = new JMenu("Workspaces");
        JMenu recentEditorsMenu = new JMenu("Editors");

        recentMenu.add(recentEditorsMenu);
        recentMenu.add(recentProjectsMenu);
        recentMenu.add(recentWorkspacesMenu);

        MenuScroller.setScrollerFor(recentEditorsMenu, 24, 125, 0, 1);
        MenuScroller.setScrollerFor(recentProjectsMenu, 24, 125, 0, 1);
        MenuScroller.setScrollerFor(recentWorkspacesMenu, 24, 125, 0, 1);

        RecentItemsListener recentItemsListener = new RecentItemsListener(recentWorkspacesMenu, recentProjectsMenu,
                recentEditorsMenu);
        workspace.addWorkspaceListener(recentItemsListener);
        desktop.addDesktopListener(recentItemsListener);

        return recentMenu;
    }

    static void addStandardPreferencesShortcutOnMac() {
        if (UISupport.isMac()) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    int modifiers = e.getModifiers();
                    if (e.getKeyChar() == ',' && (modifiers == InputEvent.META_DOWN_MASK || modifiers == InputEvent.META_MASK)) {
                        SoapUIPreferencesAction.getInstance().actionPerformed(new ActionEvent(frame, 1, "ShowPreferences"));
                    }
                    return false;
                }
            });
        }
    }

    public static JFrame getFrame() {
        return frame;
    }

    private JComponent buildMainPanel() {
        JInspectorPanel inspectorPanel = JInspectorPanelFactory.build(navigator);
        inspectorPanel.addInspector(new JComponentInspector<JComponent>(buildOverviewPanel(), "Properties",
                "Properties for the currently selected item", true));
        inspectorPanel.setDividerLocation(500);
        inspectorPanel.setResizeWeight(0.6);
        inspectorPanel.setCurrentInspector("Properties");
        return inspectorPanel.getComponent();
    }

    private JComponent buildOverviewPanel() {
        overviewPanel = new JPanel(new BorderLayout());
        return overviewPanel;
    }

    private void setOverviewPanel(Component panel) {
        if (overviewPanel.getComponentCount() == 0 && panel == null) {
            return;
        }

        overviewPanel.removeAll();
        if (panel != null) {
            overviewPanel.add(panel, BorderLayout.CENTER);
        }
        overviewPanel.revalidate();
        overviewPanel.repaint();
    }

    private JComponent buildContentPanel() {
        return buildLogPanel(true, "SoapUI log");
    }

    private JComponent buildLogPanel(boolean hasDefault, String defaultName) {
        InspectorLog4JMonitor inspectorLog4JMonitor = new InspectorLog4JMonitor(desktop.getDesktopComponent());

        JComponent monitor = initLogMonitor(hasDefault, defaultName, inspectorLog4JMonitor);

        if (!SoapUI.getSettings().getBoolean(UISettings.SHOW_LOGS_AT_STARTUP)) {
            inspectorLog4JMonitor.activate(null);
        }

        MonitorPanel monitorPanel = new MonitorPanel(new RuntimeMemoryMonitorSource());
        monitorPanel.start();
        inspectorLog4JMonitor.addInspector(new JComponentInspector<JComponent>(monitorPanel, "memory log",
                "Shows runtime memory consumption", true));

        return monitor;
    }

    public static JComponent initLogMonitor(boolean hasDefault, String defaultName, Log4JMonitor logMonitor) {
        SoapUI.logMonitor = logMonitor;
        logMonitor.addLogArea(defaultName, "com.eviware.soapui", hasDefault).setLevel(Level.DEBUG);
        logMonitor.addLogArea("http log", "org.apache.http.wire", false).setLevel(Level.DEBUG);
        logMonitor.addLogArea("jetty log", "jetty", false).setLevel(Level.INFO);
        logMonitor.addLogArea("error log", "soapui.errorlog", false).setLevel(Level.DEBUG);
        logMonitor.addLogArea("wsrm log", "wsrm", false).setLevel(Level.INFO);

        for (Object message : logCache) {
            logMonitor.logEvent(message);
        }

        return logMonitor.getComponent();
    }

    public static boolean isSelectingMostRecentlyUsedDesktopPanelOnClose() {
        return getSettings().getBoolean(UISettings.MRU_PANEL_SELECTOR, true);
    }

    // -------------------------- OTHER METHODS --------------------------

    public static synchronized void log(final Object msg) {
        if (logMonitor == null) {
            if (!isCommandLine && logCache.size() < 1000) {
                logCache.add(msg);
            }

            return;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            logMonitor.logEvent(msg);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    logMonitor.logEvent(msg);
                }
            });
        }
    }

    // -------------------------- INNER CLASSES --------------------------

    private static final class SoapUIRunner implements Runnable {
        public void run() {
            boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                    getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
            SoapUIUpdateProvider updateProvider = SoapUIAutoUpdaterUtils.getProvider();
            if (!isDebug && SoapUI.getSettings().getBoolean(VersionUpdateSettings.AUTO_CHECK_VERSION_UPDATE)) {
                updateProvider.start();
            }

            addStandardPreferencesShortcutOnMac();
            boolean isFirstLaunch = !DefaultSoapUICore.settingsFileExists();
            Properties props = new Properties();
            try {
                props.load(SoapUI.class.getResourceAsStream(SOAPUI_TITLE));
                String brandedTitleExt = props.getProperty("soapui.app.title");
                if (!StringUtils.isNullOrEmpty(brandedTitleExt)) {
                    brandedTitleExt = " - " + brandedTitleExt;
                } else {
                    brandedTitleExt = "";
                }

                startSoapUI(mainArgs, "SoapUI " + SOAPUI_VERSION + " " + brandedTitleExt,
                        new StandaloneSoapUICore(true));

                if (getSettings().getBoolean(UISettings.SHOW_STARTUP_PAGE) && !isBrowserDisabled()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            showStarterPage();
                        }
                    });
                }

                if (isCommandLine()) {
                    Analytics.trackAction(SoapUIActions.PRODUCT_STARTED_FROM_CMD);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static final class WsdlProjectCreator implements Runnable {
        private final String arg;

        public WsdlProjectCreator(String arg) {
            this.arg = arg;
        }

        public void run() {
            SoapUIAction<ModelItem> action = getActionRegistry().getAction(NewWsdlProjectAction.SOAPUI_ACTION_ID);
            if (action != null) {
                action.perform(getWorkspace(), arg);
            }
        }
    }

    private static final class RestProjectCreator implements Runnable {
        private final URL arg;

        public RestProjectCreator(URL arg) {
            this.arg = arg;
        }

        public void run() {
            try {
                WsdlProject project = (WsdlProject) getWorkspace().createProject(arg.getHost(), null);
                SoapUIAction<ModelItem> action = getActionRegistry().getAction(NewRestServiceAction.SOAPUI_ACTION_ID);
                if (action != null) {
                    action.perform(project, arg);
                }
            } catch (SoapUIException e) {
                e.printStackTrace();
            }
        }
    }


    private final class InternalDesktopListener extends DesktopListenerAdapter {
        @Override
        public void desktopPanelSelected(DesktopPanel desktopPanel) {
            ModelItem modelItem = desktopPanel.getModelItem();
            if (modelItem != null) {
                navigator.selectModelItem(modelItem);
            }
        }
    }

    private final class MainFrameWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            if (onExit()) {
                frame.dispose();
            }
        }

        @Override
        public void windowClosed(WindowEvent event) {
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(1500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
            System.out.println("exiting..");
            SoapUI.getSoapUITimer().cancel();
            System.exit(0);
        }
    }

    private static void setBackgroundsToWhite() {
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("MenuBar.background", Color.WHITE);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("TableHeader.background", Color.WHITE);
        UIManager.put("ToolBar.background", Color.WHITE);
        UIManager.put("TabbedPane.background", Color.LIGHT_GRAY);
        UIManager.put("TabbedPane.selected", Color.WHITE);
        UIManager.put("Label.background", Color.WHITE);
        UIManager.put("CheckBox.background", Color.WHITE);
        UIManager.put("Desktop.background", Color.WHITE);
        UIManager.put("ProgressBar.background", Color.WHITE);
        UIManager.put("InternalFrame.background", Color.WHITE);
        UIManager.put("SplitPane.background", Color.WHITE);
        UIManager.put("ScrollBar.background", Color.WHITE);
        UIManager.put("Spinner.background", Color.WHITE);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("ToggleButton.background", Color.WHITE);
        UIManager.put("Slider.background", Color.WHITE);
        UIManager.put("RadioButton.background", Color.WHITE);
        UIManager.put("ScrollPane.background", Color.WHITE);
    }

    public static void main(String[] args) throws Exception {
        WebstartUtilCore.init();
        setBackgroundsToWhite();
        mainArgs = args;

        SoapUIRunner soapuiRunner = new SoapUIRunner();
        SwingUtilities.invokeLater(soapuiRunner);
    }

    public static String[] getMainArgs() {
        return mainArgs;
    }

    public static SoapUI startSoapUI(String[] args, String title, SwingSoapUICore core)
            throws Exception {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SoapUI");

        frame = new JFrame(title);

        frame.setIconImages(getFrameIcons());

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        isStandalone = true;
        soapUICore = core;

        AnalyticsHelper.initializeAnalytics();
        Analytics.trackSessionStart();
        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
        if (isDebug) {
            Analytics.trackAction(SoapUIActions.PRODUCT_STARTED_IN_DEBUGGING_MODE);
        } else {
            Analytics.trackAction(SoapUIActions.PRODUCT_STARTED);
        }

        SoapUI soapUI = new SoapUI();
        Workspace workspace = null;

        org.apache.commons.cli.Options options = initSoapUIOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        if (!processCommandLineArgs(cmd)) {
            System.exit(1);
        }
        if (workspaceName != null) {
            workspace = WorkspaceFactory.getInstance().openWorkspace(workspaceName, projectOptions);
            soapUICore.getSettings().setString(CURRENT_SOAPUI_WORKSPACE, workspaceName);
        } else {
            String workspaceFile = soapUICore.getSettings().getString(CURRENT_SOAPUI_WORKSPACE,
                    System.getProperty("user.home") + File.separatorChar + DEFAULT_WORKSPACE_FILE);
            try {
                workspace = WorkspaceFactory.getInstance().openWorkspace(workspaceFile, projectOptions);
            } catch (Exception e) {
                UISupport.setDialogs(new SwingDialogs(null));
                if (UISupport
                        .confirm("Failed to open workspace: [" + e.toString() + "], create new one instead?", "Error")) {
                    new File(workspaceFile).renameTo(new File(workspaceFile + ".bak"));
                    workspace = WorkspaceFactory.getInstance().openWorkspace(workspaceFile, projectOptions);
                } else {
                    System.exit(1);
                }
            }
        }

        core.prepareUI();
        soapUI.show(workspace);
        new WindowInitializationTask().run();
        core.afterStartup(workspace);

        String[] args2 = cmd.getArgs();
        if (args2 != null && args2.length > 0) {
            String arg = args2[0];
            if (arg.toUpperCase().endsWith(".WSDL") || arg.toUpperCase().endsWith(".WADL")) {
                SwingUtilities.invokeLater(new WsdlProjectCreator(arg));
            } else {
                try {
                    URL url = new URL(arg);
                    SwingUtilities.invokeLater(new RestProjectCreator(url));
                } catch (Exception ignore) {
                }
            }
        }

        if (SoapUI.usingGraphicalEnvironment()) {
            if (workspace.isSupportInformationDialog()) {
                SumbitUserInfoAction collector = new SumbitUserInfoAction();
                collector.show();
                workspace.setSupportInformationDialog(false);
            }
        }
        return soapUI;
    }

    public static List<Image> getFrameIcons() {
        List<Image> iconList = new ArrayList<Image>();
        for (String iconPath : FRAME_ICON.split(";")) {
            iconList.add(UISupport.createImageIcon(iconPath).getImage());
        }
        return iconList;
    }

    private static boolean processCommandLineArgs(CommandLine cmd) {
        if (cmd.hasOption('w')) {
            workspaceName = cmd.getOptionValue('w');
        }

        if (cmd.hasOption('p')) {
            for (String projectNamePassword : cmd.getOptionValues('p')) {
                String[] nameAndPassword = projectNamePassword.split(":");
                projectOptions.put(nameAndPassword[0], nameAndPassword[1]);
            }
        }

        return true;
    }

    private static org.apache.commons.cli.Options initSoapUIOptions() {

        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        options.addOption("w", true, "Specified the name of the workspace xml file");
        options.addOption("p", true, "Sets project name and its password in format <project name>:<password>");

        return options;
    }

    public static SoapUICore getSoapUICore() {
        return soapUICore;
    }

    public static TestPropertyHolder getGlobalProperties() {
        return PropertyExpansionUtils.getGlobalProperties();
    }

    public static void setSoapUICore(SoapUICore soapUICore) {
        setSoapUICore(soapUICore, false);
    }

    public static void setSoapUICore(SoapUICore soapUICore, boolean isCommandLine) {
        SoapUI.soapUICore = soapUICore;
        SoapUI.isCommandLine = isCommandLine;
    }

    public static boolean isStandalone() {
        return isStandalone;
    }

    public static boolean isCommandLine() {
        return isCommandLine;
    }

    public static JMenuBar getMenuBar() {
        return menuBar;
    }

    private void show(Workspace workspace) {
        SoapUI.workspace = workspace;

        String desktopType = soapUICore.getSettings().getString(UISettings.DESKTOP_TYPE, SoapUI.DEFAULT_DESKTOP);
        desktop = DesktopRegistry.getInstance().createDesktop(desktopType, workspace);

        if (desktop == null) {
            desktop = new StandaloneDesktop(workspace);
        }

        if (testMonitor == null) {
            testMonitor = new TestMonitor();
        }

        soapUICore.getSettings().addSettingsListener(new SettingsListener() {
            public void settingChanged(String name, String newValue, String oldValue) {
                if (name.equals(UISettings.DESKTOP_TYPE)) {
                    changeDesktop(DesktopRegistry.getInstance().createDesktop(newValue, SoapUI.workspace));
                }
            }

            public void settingsReloaded() {
                // TODO Auto-generated method stub

            }
        });

        buildUI();

        testMonitor.addTestMonitorListener(new LogDisablingTestMonitorListener());
        testMonitor.init(workspace);

        initAutoSaveTimer();
        initGCTimer();
    }

    private void changeDesktop(SoapUIDesktop newDesktop) {
        desktopPanelsList.setDesktop(newDesktop);
        desktop.removeDesktopListener(internalDesktopListener);

        desktop.transferTo(newDesktop);
        desktop.release();

        desktop = newDesktop;

        if (logMonitor instanceof InspectorLog4JMonitor) {
            ((InspectorLog4JMonitor) logMonitor).setContentComponent(desktop.getDesktopComponent());
        }

        desktop.addDesktopListener(internalDesktopListener);

        while (desktopMenu.getItemCount() > DEFAULT_DESKTOP_ACTIONS_COUNT) {
            desktopMenu.remove(DEFAULT_DESKTOP_ACTIONS_COUNT);
        }

        ActionSupport.addActions(desktop.getActions(), desktopMenu);

        desktop.init();
    }

    protected boolean onExit() {
        if (saveOnExit) {
            String question = "Exit SoapUI?";

            if (getTestMonitor().hasRunningTests()) {
                question += "\n(Projects with running tests will not be saved)";
            }

            if (!UISupport.confirm(question, "Question")) {
                return false;
            }

            try {
                soapUICore.saveSettings();
                SaveStatus saveStatus = workspace.onClose();
                if (saveStatus == SaveStatus.CANCELLED || saveStatus == SaveStatus.FAILED) {
                    return false;
                }
            } catch (Exception e1) {
                SoapUI.logError(e1);
            }

            Analytics.trackAction(SoapUIActions.EXIT);
        } else {
            if (!UISupport.confirm("Exit SoapUI without saving?", "Question")) {
                saveOnExit = true;
                return false;
            }
            Analytics.trackAction(SoapUIActions.EXIT_WITHOUT_SAVE);
        }

        Analytics.trackSessionStop();
        Analytics.trackAction(AnalyticsManager.Category.MIXPANEL_PROFILE, null, UniqueUserIdentifier.getInstance().prepareUserProfile());

        shutdown();

        return true;
    }

    public static void shutdown() {
        soapUITimer.cancel();
    }

    public static void logError(Throwable e) {
        logError(e, null);
    }

    public static void logError(Throwable e, String message) {
        String msg = e.getMessage();
        if (msg == null) {
            msg = e.toString();
        }

        log.error("An error occurred [" + msg + "], see error log for details");

        try {
            if (message != null) {
                errorLog.error(message);
            }

            errorLog.error(e.toString(), e);
        } catch (OutOfMemoryError e1) {
            e1.printStackTrace();
            System.gc();
        }
        if (!isStandalone() || "true".equals(System.getProperty("soapui.stacktrace"))) {
            e.printStackTrace();
        }
    }

    public static Logger getErrorLog() {
        return errorLog;
    }

    public static Logger ensureGroovyLog() {
        synchronized (threadPool) {
            if (!checkedGroovyLogMonitor || launchedTestRunner) {
                groovyLogger = Logger.getLogger("groovy.log");

                Log4JMonitor logMonitor = getLogMonitor();
                if (logMonitor != null && !logMonitor.hasLogArea("groovy.log")) {
                    logMonitor.addLogArea("script log", "groovy.log", false);
                    checkedGroovyLogMonitor = true;
                } else if (logMonitor == null && launchedTestRunner) {
                    checkedGroovyLogMonitor = true;
                    launchedTestRunner = false;
                }
            }
        }

        return groovyLogger;
    }

    public static boolean isBrowserDisabled() {
        return Boolean.valueOf(System.getProperty(BROWSER_DISABLED_SYSTEM_PROPERTY));
    }

    public class InternalNavigatorListener implements NavigatorListener {
        private PropertyHolderTable selectedPropertyHolderTable = null;

        public void nodeSelected(SoapUITreeNode treeNode) {
            if (treeNode == null) {
                setOverviewPanel(null);
            } else {
                ModelItem modelItem = treeNode.getModelItem();

                if (selectedPropertyHolderTable != null) {
                    selectedPropertyHolderTable.release();
                    selectedPropertyHolderTable = null;
                }

                if (modelItem instanceof TestPropertyHolder) {
                    // check for closed project -> this should be solved with a
                    // separate ClosedWsdlProject modelItem
                    if (!(modelItem instanceof WsdlProject) || ((WsdlProject) modelItem).isOpen()) {
                        selectedPropertyHolderTable = new PropertyHolderTable((TestPropertyHolder) modelItem);

                        if (modelItem instanceof WsdlProject) {
                            WsdlProject project = (WsdlProject) modelItem;
                            EnvironmentListener environmentListener = new EnvironmentListener() {
                                public void propertyValueChanged(Property property) {
                                    selectedPropertyHolderTable.getPropertiesModel().fireTableDataChanged();
                                }
                            };
                            project.addEnvironmentListener(environmentListener);
                            selectedPropertyHolderTable.setEnvironmentListener(environmentListener);
                            project.addProjectListener(selectedPropertyHolderTable.getProjectListener());
                        }
                    }
                }

                PanelBuilder<ModelItem> panelBuilder = PanelBuilderRegistry.getPanelBuilder(modelItem);
                if (panelBuilder != null && panelBuilder.hasOverviewPanel()) {
                    Component overviewPanel = panelBuilder.buildOverviewPanel(modelItem);
                    if (selectedPropertyHolderTable != null) {
                        JTabbedPane tabs = new JTabbedPane();
                        tabs.setName(PROPERTIES_TAB_PANEL_NAME);
                        if (overviewPanel instanceof JPropertiesTable<?>) {
                            JPropertiesTable<?> t = (JPropertiesTable<?>) overviewPanel;
                            tabs.addTab(t.getTitle(), overviewPanel);
                            t.setTitle(null);
                        } else {
                            tabs.addTab("Overview", overviewPanel);
                        }

                        tabs.addTab(((TestPropertyHolder) modelItem).getPropertiesLabel(), selectedPropertyHolderTable);
                        overviewPanel = UISupport.createTabPanel(tabs, false);
                    }

                    setOverviewPanel(overviewPanel);
                } else {
                    setOverviewPanel(null);
                }
            }
        }
    }

    private class ExitAction extends AbstractAction {
        public ExitAction() {
            super("Exit");
            putValue(Action.SHORT_DESCRIPTION, "Saves all projects and exits SoapUI");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu Q"));
        }

        public void actionPerformed(ActionEvent e) {
            saveOnExit = true;
            WindowEvent windowEvent = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
            Analytics.trackAction(SoapUIActions.EXIT);
            frame.dispatchEvent(windowEvent);
        }
    }

    private class ApplyProxyButtonAction extends AbstractAction {

        public ApplyProxyButtonAction() {
            putValue(Action.NAME, "Proxy");
        }

        public void actionPerformed(ActionEvent e) {
            if (ProxyUtils.isProxyEnabled()) {
                SoapUI.getSettings().setBoolean(ProxySettings.ENABLE_PROXY, false);
                Analytics.trackAction(TURN_OFF_PROXY_FROM_TOOLBAR);
            } else {
                if (!ProxyUtils.isAutoProxy() && emptyManualSettings()) {
                    SoapUI.getSettings().setBoolean(ProxySettings.AUTO_PROXY, true);
                }
                SoapUI.getSettings().setBoolean(ProxySettings.ENABLE_PROXY, true);
                Analytics.trackAction(TURN_ON_PROXY_FROM_TOOLBAR);
            }

            updateProxyFromSettings();
        }

        private boolean emptyManualSettings() {
            return StringUtils.isNullOrEmpty(SoapUI.getSettings().getString(ProxySettings.HOST, ""))
                    || StringUtils.isNullOrEmpty(SoapUI.getSettings().getString(ProxySettings.PORT, ""));
        }
    }

    public static void updateProxyButtonAndTooltip() {
        if (applyProxyButton == null) {
            return;
        }
        applyProxyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        applyProxyButton.setHorizontalTextPosition(SwingConstants.CENTER);
        if (ProxyUtils.isProxyEnabled()) {
            applyProxyButton.setIcon(UISupport.createImageIcon(PROXY_ENABLED_ICON));
            if (ProxyUtils.isAutoProxy()) {
                applyProxyButton.getAction().putValue(Action.SHORT_DESCRIPTION, "Proxy Setting: Automatic");

            } else {
                applyProxyButton.getAction().putValue(Action.SHORT_DESCRIPTION, "Proxy Setting: Manual");
            }
        } else {
            applyProxyButton.setIcon(UISupport.createImageIcon(PROXY_DISABLED_ICON));
            applyProxyButton.getAction().putValue(Action.SHORT_DESCRIPTION, "Proxy Setting: None");
        }
        applyProxyButton.setSelected(ProxyUtils.isProxyEnabled());
        UIManager.put("ToggleButton.select", Color.WHITE);
        SwingUtilities.updateComponentTreeUI(applyProxyButton);
    }

    private static class ShowStarterPageAction extends AbstractAction {
        public ShowStarterPageAction() {
            super("Starter Page");
            putValue(Action.SHORT_DESCRIPTION, "Shows the starter page");
        }

        public void actionPerformed(ActionEvent e) {
            showStarterPage();
        }
    }

    private class ToolbarForumSearchAction extends AbstractAction {
        public ToolbarForumSearchAction() {
            putValue(Action.SHORT_DESCRIPTION, "Searches the Smartbear Community Forum");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/find.png"));
        }

        public void actionPerformed(ActionEvent e) {
            doCommunitySearch(searchField.getText());
        }
    }

    private class SearchForumAction extends AbstractAction {
        public SearchForumAction() {
            super("Search Forum");
            putValue(Action.SHORT_DESCRIPTION, "Searches the Smartbear Community Forum");
        }

        public void actionPerformed(ActionEvent e) {
            String text = UISupport.prompt("Search Text", "Search Community Forum", "");
            if (text == null) {
                return;
            }

            doCommunitySearch(text);
        }
    }

    public static void showStarterPage() {
        if (starterPageDesktopPanel == null || starterPageDesktopPanel.isClosed()) {
            try {
                starterPageDesktopPanel = new URLDesktopPanel(STARTER_PAGE_HEADER, STARTER_PAGE_TOOL_TIP, null);
            } catch (Exception e) {
                logError(e);
                return;
            }
        }

        UISupport.showDesktopPanel(starterPageDesktopPanel);
        starterPageDesktopPanel.navigate(HelpUrls.STARTER_PAGE_URL, STARTER_PAGE_ERROR_URL, true);
    }

    private static class AboutAction extends AbstractAction {
        private static final String COPYRIGHT = "2004-" + Calendar.getInstance().get(Calendar.YEAR) + " smartbear.com";
        private static final String SOAPUI_WEBSITE = "http://www.soapui.org";
        private static final String SMARTBEAR_WEBSITE = "http://www.smartbear.com";

        public AboutAction() {
            super("About SoapUI");
            putValue(Action.SHORT_DESCRIPTION, "Shows information on SoapUI");
        }

        public void actionPerformed(ActionEvent e) {
            URI splashURI = null;
            try {
                splashURI = UISupport.findSplash(SoapUI.SOAPUI_SPLASH).toURI();
            } catch (URISyntaxException e1) {
                SoapUI.logError(e1);
            }

            Properties buildInfoProperties = new Properties();
            try {
                buildInfoProperties.load(SoapUI.class.getResourceAsStream(BUILDINFO_PROPERTIES));
            } catch (Exception exception) {
                SoapUI.logError(exception, "Could not read build info properties");
            }


            UISupport.showExtendedInfo(
                    "About SoapUI",
                    null,
                    "<html><body><p align=center> <font face=\"Verdana,Arial,Helvetica\"><strong><img src=\"" + splashURI
                            + "\"><br>SoapUI " + SOAPUI_VERSION + "<br>"
                            + "Copyright (C) " + COPYRIGHT + "<br>"
                            + "<a href=\"" + SOAPUI_WEBSITE + "\">" + SOAPUI_WEBSITE + "</a> | "
                            + "<a href=\"" + SMARTBEAR_WEBSITE + "\">" + SMARTBEAR_WEBSITE + "</a><br>"
                            + "Build Date: " + Objects.firstNonNull(buildInfoProperties.getProperty("build.date"), "UNKNOWN BUILD DATE")
                            + "</strong></font></p></body></html>",

                    new Dimension(646, 480));   //Splash screen width + 70px, height + 175px
        }
    }

    private class ExitWithoutSavingAction extends AbstractAction {
        public ExitWithoutSavingAction() {
            super("Exit without saving");
            putValue(Action.SHORT_DESCRIPTION, "Exits SoapUI without saving");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("ctrl shift Q"));
        }

        public void actionPerformed(ActionEvent e) {
            saveOnExit = false;
            WindowEvent windowEvent = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
            frame.dispatchEvent(windowEvent);
        }
    }

    private class SavePreferencesAction extends AbstractAction {
        public SavePreferencesAction() {
            super("Save Preferences");
            putValue(Action.SHORT_DESCRIPTION, "Saves all global preferences");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                soapUICore.saveSettings();
                Analytics.trackAction(SAVE_PREFERENCES);
            } catch (Exception e1) {
                SoapUI.logError(e1, "There was an error when attempting to save your preferences");
                UISupport.showErrorMessage(e1);
            }
        }
    }

    public static TestMonitor getTestMonitor() {
        if (testMonitor == null) {
            testMonitor = new TestMonitor();
        }

        return testMonitor;
    }

    public static void setTestMonitor(TestMonitor monitor) {
        testMonitor = monitor;
    }

    public static Log4JMonitor getLogMonitor() {
        return logMonitor;
    }

    public static void setLogMonitor(Log4JMonitor monitor) {
        logMonitor = monitor;
    }

    // instance is null in Eclipse. /Lars
    // eclipse-version(s) should provide SoapUIDesktop implementation
    public static SoapUIDesktop getDesktop() {
        if (desktop == null) {
            desktop = new NullDesktop();
        }

        return desktop;
    }

    public static void setDesktop(SoapUIDesktop desktop) {
        SoapUI.desktop = desktop;
    }

    public static Navigator getNavigator() {
        return navigator;
    }

    public static SoapUIActionRegistry getActionRegistry() {
        if (soapUICore == null) {
            soapUICore = new DefaultSoapUICore();
        }

        return soapUICore.getActionRegistry();
    }

    public static void setNavigator(Navigator navigator) {
        SoapUI.navigator = navigator;
    }

    public static void setWorkspace(Workspace workspace) {
        SoapUI.workspace = workspace;
    }

    public static void setStandalone(boolean standalone) {
        SoapUI.isStandalone = standalone;
    }

    static class NewProjectActionDelegate extends AbstractAction {
        String actionId;
        private SoapUIActions analyticAction;

        public NewProjectActionDelegate(String icon, String name, String actionId) {
            this(icon, name, actionId, null);
        }

        public NewProjectActionDelegate(String icon, String name, String actionId, SoapUIActions analyticAction) {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon(icon));
            if (name.equals("Empty")) {
                putValue(Action.SHORT_DESCRIPTION, "Creates an empty project");
            } else {
                putValue(Action.SHORT_DESCRIPTION, "Creates a new " + name + " project");
            }
            putValue(Action.NAME, name);
            this.actionId = actionId;
            this.analyticAction = analyticAction;
        }

        public void setShortDescription(String description) {
            putValue(Action.SHORT_DESCRIPTION, description);
        }

        public void setName(String name) {
            putValue(Action.NAME, name);
        }

        public void actionPerformed(ActionEvent e) {
            SoapUI.getActionRegistry().getAction(actionId).perform(workspace, analyticAction);
        }
    }

    private static class ImportWsdlProjectActionDelegate extends AbstractAction {
        public ImportWsdlProjectActionDelegate() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/import_toolbar_icon.png"));
            putValue(Action.SHORT_DESCRIPTION, "Imports an existing SoapUI Project into the current workspace");
            putValue(Action.NAME, "Import");
        }

        public void actionPerformed(ActionEvent e) {
            SoapUI.getActionRegistry().getAction(ImportWsdlProjectAction.SOAPUI_ACTION_ID).perform(workspace, IMPORT_PROJECT_FROM_TOOLBAR);
        }
    }

    private static class SaveAllActionDelegate extends AbstractAction {
        public SaveAllActionDelegate() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/Save-all.png"));
            putValue(Action.SHORT_DESCRIPTION, "Saves all projects in the current workspace");
            putValue(Action.NAME, "Save All");
        }

        public void actionPerformed(ActionEvent e) {
            SoapUI.getActionRegistry().getAction(SaveAllProjectsAction.SOAPUI_ACTION_ID).perform(workspace, SAVE_ALL_PROJECTS_FROM_TOOLBAR);
        }
    }

    private class PreferencesActionDelegate extends AbstractAction {
        public PreferencesActionDelegate() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/preferences_toolbar_icon.png"));
            putValue(Action.SHORT_DESCRIPTION, "Sets Global SoapUI Preferences");
            putValue(Action.NAME, "Preferences");
        }

        public void actionPerformed(ActionEvent e) {
            SoapUIPreferencesAction.getInstance().actionPerformed(null);
            Analytics.trackAction(OPEN_PREFERENCES_FROM_TOOLBAR);
        }
    }

    public static class ImportPreferencesAction extends AbstractAction {
        public static final String IMPORT_PREFERENCES_ACTION_NAME = "Import Preferences";

        public ImportPreferencesAction() {
            super(ImportPreferencesAction.IMPORT_PREFERENCES_ACTION_NAME);
            putValue(Action.SHORT_DESCRIPTION, "Imports SoapUI Settings from another settings-file");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                // prompt for import
                File file = UISupport.getFileDialogs().open(null, ImportPreferencesAction.IMPORT_PREFERENCES_ACTION_NAME,
                        ".xml", "SoapUI Settings XML (*.xml)", null);
                if (file != null) {
                    soapUICore.importSettings(file);
                    Analytics.trackAction(IMPORT_PREFERENCES);
                }
            } catch (Exception e1) {
                UISupport.showErrorMessage(e1);
            }
        }
    }

    public static SoapUIListenerRegistry getListenerRegistry() {
        if (soapUICore == null) {
            soapUICore = DefaultSoapUICore.createDefault();
        }

        return soapUICore.getListenerRegistry();
    }

    public static SoapUIFactoryRegistry getFactoryRegistry() {
        if (soapUICore == null) {
            soapUICore = DefaultSoapUICore.createDefault();
        }

        return soapUICore.getFactoryRegistry();
    }

    public static Settings getSettings() {
        if (soapUICore == null) {
            soapUICore = DefaultSoapUICore.createDefault();
        }

        return soapUICore.getSettings();
    }

    public static void importPreferences(File file) throws Exception {
        if (soapUICore != null) {
            soapUICore.importSettings(file);
        }
    }

    public static MockEngine getMockEngine() {
        if (soapUICore == null) {
            soapUICore = DefaultSoapUICore.createDefault();
        }

        return soapUICore.getMockEngine();
    }

    public static String saveSettings() throws Exception {
        return soapUICore == null ? null : soapUICore.saveSettings();
    }

    public static void initDefaultCore() {
        if (soapUICore == null) {
            soapUICore = DefaultSoapUICore.createDefault();
        }
    }

    public class MaximizeDesktopAction extends AbstractAction {
        private JLogList lastLog;
        private int lastMainDividerLocation;
        private final InspectorLog4JMonitor log4JMonitor;
        private int lastLogDividerLocation;

        public MaximizeDesktopAction(InspectorLog4JMonitor log4JMonitor) {
            super("Maximize Desktop");
            this.log4JMonitor = log4JMonitor;

            putValue(SHORT_DESCRIPTION, "Hides/Shows the Navigator and Log tabs");
            putValue(ACCELERATOR_KEY, UISupport.getKeyStroke("menu M"));
        }

        public void actionPerformed(ActionEvent e) {
            if (mainInspector.getCurrentInspector() != null || logMonitor.getCurrentLog() != null) {
                lastMainDividerLocation = mainInspector.getDividerLocation();
                mainInspector.deactivate();

                lastLog = logMonitor.getCurrentLog();
                lastLogDividerLocation = log4JMonitor.getDividerLocation();

                log4JMonitor.deactivate();
            } else {
                mainInspector.setCurrentInspector("Navigator");
                mainInspector.setDividerLocation(lastMainDividerLocation == 0 ? 250 : lastMainDividerLocation);

                log4JMonitor.setCurrentLog(lastLog);
                log4JMonitor.setDividerLocation(lastLogDividerLocation == 0 ? 500 : lastLogDividerLocation);
            }
        }
    }

    public static void initAutoSaveTimer() {
        Settings settings = SoapUI.getSettings();
        long interval = settings.getLong(UISettings.AUTO_SAVE_INTERVAL, 0);

        if (autoSaveTimerTask != null) {
            if (interval == 0) {
                SoapUI.log("Cancelling AutoSave Timer");
            }

            autoSaveTimerTask.cancel();
            autoSaveTimerTask = null;
        }

        if (interval > 0) {
            autoSaveTimerTask = new AutoSaveTimerTask();

            SoapUI.log("Scheduling autosave every " + interval + " minutes");

            soapUITimer.schedule(autoSaveTimerTask, interval * 1000 * 60, interval * 1000 * 60);
        }
    }

    private static class AutoSaveTimerTask extends TimerTask {
        @Override
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SoapUI.log("Autosaving Workspace");
                    WorkspaceImpl workspaceImplementation = (WorkspaceImpl) SoapUI.getWorkspace();
                    if (workspaceImplementation != null) {
                        workspaceImplementation.save(false, true);
                    }
                }
            });
        }
    }

    public static void initGCTimer() {
        Settings settings = SoapUI.getSettings();
        long interval = settings.getLong(UISettings.GC_INTERVAL, 60);

        if (gcTimerTask != null) {
            if (interval == 0) {
                SoapUI.log("Cancelling GC Timer");
            }

            gcTimerTask.cancel();
            gcTimerTask = null;
        }

        if (interval > 0) {
            gcTimerTask = new GCTimerTask();
            SoapUI.log("Scheduling garbage collection every " + interval + " seconds");
            soapUITimer.schedule(gcTimerTask, interval * 1000, interval * 1000);
        }
    }

    private static class GCTimerTask extends TimerTask {
        @Override
        public void run() {
            System.gc();
        }
    }

    public static JXToolBar getToolBar() {
        return mainToolbar;
    }

    public static void setLaunchedTestRunner(Boolean launchedTestRunner) {
        SoapUI.launchedTestRunner = launchedTestRunner;
    }

    public static void updateProxyFromSettings() {
        ProxyUtils.setProxyEnabled(getSettings().getBoolean(ProxySettings.ENABLE_PROXY));
        ProxyUtils.setAutoProxy(getSettings().getBoolean(ProxySettings.AUTO_PROXY));
        ProxyUtils.setGlobalProxy(getSettings());
        updateProxyButtonAndTooltip();
    }

    public static Timer getSoapUITimer() {
        return soapUITimer;
    }

    public static void setCmdLineRunner(CmdLineRunner abstractSoapUIRunner) {
        SoapUI.soapUIRunner = abstractSoapUIRunner;
    }

    public static CmdLineRunner getCmdLineRunner() {
        return soapUIRunner;
    }

    public static boolean isAutoUpdateVersion() {
        return getSettings().getBoolean(VersionUpdateSettings.AUTO_CHECK_VERSION_UPDATE);
    }

    protected static class WindowInitializationTask implements Runnable {
        public void run() {
            expandWindow(frame);
            frame.setVisible(true);
        }

        private void expandWindow(JFrame frame) {
            UserPreferences userPreferences = new UserPreferences();
            Rectangle savedWindowBounds = userPreferences.getSoapUIWindowBounds();
            if (savedWindowBounds == null || !windowFullyVisibleOnScreen(savedWindowBounds)) {
                Rectangle availableScreenArea = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                frame.setBounds(availableScreenArea);
            } else {
                frame.setBounds(savedWindowBounds);
                if (!UISupport.isMac()) {
                    frame.setExtendedState(userPreferences.getSoapUIExtendedState());
                }
            }
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    try {
                        JFrame frame = (JFrame) event.getWindow();
                        UserPreferences userPreferences = new UserPreferences();
                        userPreferences.setSoapUIWindowBounds(frame.getBounds());
                        userPreferences.setSoapUIExtendedState(frame.getExtendedState());
                    } catch (BackingStoreException e) {
                        logError(e, "Could not save SoapUI window bounds");
                    }
                }
            });
        }

        private boolean windowFullyVisibleOnScreen(Rectangle windowBounds) {
            Rectangle bargainBounds = new Rectangle(windowBounds.x + 12, windowBounds.y + 12, windowBounds.width * 4 / 5, windowBounds.height * 4 / 5);
            for (GraphicsDevice graphicsDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                if (graphicsDevice.getDefaultConfiguration().getBounds().contains(bargainBounds)) {
                    return true;
                }
            }
            return false;
        }
    }
}
