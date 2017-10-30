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

package com.eviware.soapui.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SwingPluginSoapUICore;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.components.ConfigurationDialog;
import com.eviware.soapui.support.components.JButtonBar;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.PreviewCorner;
import com.eviware.soapui.support.components.SwingConfigurationDialogImpl;
import com.eviware.soapui.support.swing.GradientPanel;
import com.eviware.soapui.support.swing.SoapUISplitPaneUI;
import com.eviware.soapui.support.swing.SwingUtils;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;
import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XFileDialogs;
import com.eviware.x.impl.swing.SwingDialogs;
import com.eviware.x.impl.swing.SwingFileDialogs;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import org.syntax.jedit.InputHandler;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade for common UI-related tasks
 *
 * @author Ole.Matzura
 */

public class UISupport {
    public static final String IMAGES_RESOURCE_PATH = "/com/eviware/soapui/resources/images";
    public static final String TOOL_ICON_PATH = "/applications-system.png";
    public static final String OPTIONS_ICON_PATH = "/preferences-system.png";
    public static final int EXTENDED_ERROR_MESSAGE_THRESHOLD = 120;

    // This is needed in Eclipse that has strict class loader constraints.
    private static List<ClassLoader> secondaryResourceLoaders = new ArrayList<ClassLoader>();

    private static Component frame;
    private static Map<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>();
    public static Dimension TOOLBAR_BUTTON_DIMENSION;
    private static boolean isWindows = System.getProperty("os.name").contains("Windows");
    private static boolean isMac = System.getProperty("os.name").contains("Mac");

    private static XDialogs dialogs;
    private static XFileDialogs fileDialogs;
    private static UIUtils uiUtils;
    private static ToolHost toolHost;
    private static Cursor hourglassCursor;
    private static Cursor defaultCursor;
    private static Boolean isHeadless;

    public static final String DEFAULT_EDITOR_FONT = "Courier plain";
    public static final int DEFAULT_EDITOR_FONT_SIZE = 11;
    public static final Color MAC_BACKGROUND_COLOR = new Color(255, 255, 255);
    public static final Color MAC_PROGRESSBAR_BACKGROUND_COLOR = new Color(196, 196, 196);
    public static final Color MAC_PROGRESSBAR_MATTE_BORDER_COLOR = new Color(238, 238, 238);
    public static final Color MAC_PROGRESSBAR_LINE_BORDER_COLOR = new Color(166, 166, 166);

    static {
        setDialogs(new ConsoleDialogs());
        uiUtils = new SwingUtils();

        if (!isHeadless()) {
            TOOLBAR_BUTTON_DIMENSION = new Dimension(22, 21);
        }
    }

    public static ImageIcon TOOL_ICON = UISupport.createImageIcon(TOOL_ICON_PATH);
    public static ImageIcon OPTIONS_ICON = UISupport.createImageIcon(OPTIONS_ICON_PATH);
    public static ImageIcon HELP_ICON = UISupport.createImageIcon("/help-browser.png");
    private static EditorFactory editorFactory = new DefaultEditorFactory();

    /**
     * Add a classloader to find resources.
     *
     * @param loader
     * @deprecated Use {@link #addResourceClassLoader(ClassLoader)} instead
     */
    public static void addClassLoader(ClassLoader loader) {
        addResourceClassLoader(loader);
    }

    /**
     * Add a classloader to find resources.
     *
     * @param loader
     */
    public static void addResourceClassLoader(ClassLoader loader) {
        secondaryResourceLoaders.add(loader);
    }

    /**
     * Set the main frame of this application. This is only used when running
     * under Swing.
     *
     * @param frame
     */
    public static void setMainFrame(Component frame) {
        UISupport.frame = frame;
        setDialogs(new SwingDialogs(frame));
        setFileDialogs(new SwingFileDialogs(frame));
    }

    public static void setDialogs(XDialogs xDialogs) {
        dialogs = xDialogs;
    }

    public static EditorFactory getEditorFactory() {
        return editorFactory;
    }

    public static void setFileDialogs(XFileDialogs xFileDialogs) {
        fileDialogs = xFileDialogs;
    }

    public static ToolHost getToolHost() {
        return toolHost;
    }

    public static void setToolHost(ToolHost host) {
        toolHost = host;
    }

    public static Frame getMainFrame() {
        return (Frame) (frame instanceof Frame ? frame : null);
    }

    public static JComboBox addTooltipListener(JComboBox combo, String defaultTooltip) {
        combo.setToolTipText(defaultTooltip);
        combo.addItemListener(new ItemListenerImplementation(combo, defaultTooltip));

        return combo;
    }

    public static Frame getParentFrame(Component component) {
        for (Container c = component.getParent(); c != null; c = c.getParent()) {
            if (c instanceof Frame) {
                return (Frame) c;
            }
        }
        return getMainFrame();
    }

    public static XDialogs getDialogs() {
        return dialogs;
    }

    public static XFileDialogs getFileDialogs() {
        return fileDialogs;
    }

    /**
     * @deprecated use XForm related classes instead
     */

    @Deprecated
    public static ConfigurationDialog createConfigurationDialog(String name, String helpUrl, String description,
                                                                ImageIcon icon) {
        return new SwingConfigurationDialogImpl(name, helpUrl, description, icon);
    }

    /**
     * @deprecated use XForm related classes instead
     */

    @Deprecated
    public static ConfigurationDialog createConfigurationDialog(String name, String helpUrl) {
        return new SwingConfigurationDialogImpl(name, helpUrl, null, null);
    }

    /**
     * @deprecated use XForm related classes instead
     */

    @Deprecated
    public static ConfigurationDialog createConfigurationDialog(String name) {
        return new SwingConfigurationDialogImpl(name, null, null, null);
    }

    public static void showErrorMessage(final String message) {
        Analytics.trackError(new Throwable(message));
        if (message != null && message.length() > EXTENDED_ERROR_MESSAGE_THRESHOLD) {
            dialogs.showExtendedInfo("Error", "An error occurred", message, null);
        } else {
            dialogs.showErrorMessage(message);
        }
    }

    public static boolean confirm(String question, String title) {
        return dialogs.confirm(question, title);
    }

    public static boolean confirm(String question, String title, Component parent) {
        return dialogs.confirm(question, title, parent);
    }

    public static int yesYesToAllOrNo(String question, String title) {
        return dialogs.yesYesToAllOrNo(question, title);
    }

    public static String prompt(String question, String title, String value) {
        return dialogs.prompt(question, title, value);
    }

    /**
     * @deprecated use prompt(String question, String title, String value)
     *             instead
     */

    @Deprecated
    public static String prompt(String question, String title) {
        return dialogs.prompt(question, title);
    }

    public static boolean stopCellEditing(JTable table) {
        try {
            int column = table.getEditingColumn();
            if (column > -1) {
                TableCellEditor cellEditor = table.getColumnModel().getColumn(column).getCellEditor();
                if (cellEditor == null) {
                    cellEditor = table.getDefaultEditor(table.getColumnClass(column));
                }
                if (cellEditor != null) {
                    cellEditor.stopCellEditing();
                }
            }
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }

    public static JPanel createProgressBarPanel(JProgressBar progressBar, int space, boolean indeterimate) {
        JPanel panel = new JPanel(new BorderLayout());

        if (isMac()) {
            // default native progress bar on mac ignores color settings, use a custom ui to get green/red
            progressBar.setUI(new BasicProgressBarUI());
        }

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setIndeterminate(indeterimate);

        if (isMac()) {
            progressBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
            Border compound = BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(space, space, space, space, MAC_PROGRESSBAR_MATTE_BORDER_COLOR),
                    BorderFactory.createLineBorder(MAC_PROGRESSBAR_LINE_BORDER_COLOR)
            );
            panel.setBorder(compound);
            panel.setBackground(MAC_PROGRESSBAR_BACKGROUND_COLOR);
        } else {
            progressBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
            panel.setBorder(BorderFactory.createEmptyBorder(space, space, space, space));
        }
        panel.add(progressBar, BorderLayout.CENTER);

        return panel;
    }

    public static JSplitPane createHorizontalSplit() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setUI(new SoapUISplitPaneUI());
        splitPane.setDividerSize(10);
        splitPane.setOneTouchExpandable(true);
        return splitPane;
    }

    public static JSplitPane createHorizontalSplit(Component leftComponent, Component rightComponent) {
        JSplitPane splitPane = createHorizontalSplit();

        splitPane.setLeftComponent(leftComponent);
        splitPane.setRightComponent(rightComponent);
        return splitPane;
    }

    public static JSplitPane createVerticalSplit() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setUI(new SoapUISplitPaneUI());
        splitPane.setDividerSize(10);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);
        return splitPane;
    }

    public static JSplitPane createVerticalSplit(Component topComponent, Component bottomComponent) {
        JSplitPane splitPane = createVerticalSplit();

        splitPane.setLeftComponent(topComponent);
        splitPane.setRightComponent(bottomComponent);
        return splitPane;
    }

    public static void centerDialog(Window dialog) {
        centerDialog(dialog, dialog.getOwner());
    }

    public static void centerDialog(Window dialog, Window owner) {
        Dimension sz = dialog.getSize();
        Rectangle b = frame == null ? null : frame.getBounds();

        if (owner != null && owner.isVisible()) {
            b = owner.getBounds();
        } else if (b == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            b = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        }

        dialog.setLocation((int) ((b.getWidth() - sz.getWidth()) / 2) + (int) b.getX(),
                (int) ((b.getHeight() - sz.getHeight()) / 2) + (int) b.getY());
    }

    public static void showDialog(JDialog dialog) {
        centerDialog(dialog);
        dialog.setVisible(true);
    }

    public static ImageIcon createImageIcon(String path) {
        if (StringUtils.isNullOrEmpty(path)) {
            return null;
        }

        if (iconCache.containsKey(path)) {
            return iconCache.get(path);
        }

        String orgPath = path;
        java.net.URL imgURL = null;

        try {
            File file = new File(path);
            if (file.exists()) {
                imgURL = file.toURI().toURL();
            } else {
                String p = path;
                if (p.indexOf('/', 1) == -1) {
                    p = "/com/eviware/soapui/resources/images" + p;
                }

                imgURL = SoapUI.class.getResource(p);

                if (imgURL == null && path.endsWith(".gif")) {
                    imgURL = SoapUI.class.getResource(p.substring(0, p.length() - 4) + ".png");
                }
            }

            if (imgURL == null) {
                imgURL = loadFromSecondaryLoader(path);
            }
        } catch (Throwable t) {
            SoapUI.logError(t, "Failed to find icon [" + path + "]");
            return null;
        }

        if (imgURL != null) {
            try {
                ImageIcon imageIcon = new ImageIcon(imgURL);
                iconCache.put(orgPath, imageIcon);
                return imageIcon;
            } catch (Throwable e) {
                if (e instanceof NoClassDefFoundError) {
                    isHeadless = true;
                } else {
                    System.err.println("Failed to create icon: " + e);
                }

                return null;
            }
        } else {
            System.err.println("Couldn't find icon file: " + path);
            return null;
        }
    }

    public static boolean isHeadless() {
        if (isHeadless == null) {
            isHeadless = GraphicsEnvironment.isHeadless();
        }

        return isHeadless.booleanValue();
    }

    private static URL loadFromSecondaryLoader(String path) {
        for (ClassLoader loader : secondaryResourceLoaders) {
            URL url = loader.getResource(path);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public static void showInfoMessage(String message) {
        dialogs.showInfoMessage(message);
    }

    public static void showInfoMessage(String message, String title) {
        dialogs.showInfoMessage(message, title);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T prompt(String question, String title, T[] objects) {
        return (T) dialogs.prompt(question, title, objects);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T prompt(String question, String title, T[] objects, String value) {
        return (T) dialogs.prompt(question, title, objects, value);
    }

    public static JButton createToolbarButton(Action action) {
        JButton result = new JButton(action);
        if (action.getValue(Action.NAME) != null) {
            result.setName(String.valueOf(action.getValue(Action.NAME)));
        }
        result.setPreferredSize(TOOLBAR_BUTTON_DIMENSION);
        result.setText("");
        result.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        return result;
    }

    public static JButton createFormButton(Action action) {
        JButton helpButton = createToolbarButton(action);
        helpButton.setContentAreaFilled(false);
        helpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return helpButton;
    }

    public static JButton createToolbarButton(Action action, boolean enabled) {
        JButton result = createToolbarButton(action);
        result.setEnabled(enabled);
        return result;
    }

    public static JPanel createTabPanel(JTabbedPane tabs, boolean addBorder) {
        GradientPanel panel = new GradientPanel(new BorderLayout());
        panel.setForeground(Color.WHITE);
        if (tabs.getTabPlacement() == JTabbedPane.LEFT || tabs.getTabPlacement() == JTabbedPane.RIGHT) {
            panel.setDirection(GradientPanel.VERTICAL);
        }

        panel.add(tabs, BorderLayout.CENTER);

        if (addBorder) {
            if (tabs.getTabPlacement() == JTabbedPane.TOP) {
                panel.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY));
            } else {
                panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
            }
        }

        tabs.setBorder(null);

        return panel;
    }

    public static void showPopup(JPopupMenu popup, JComponent invoker, Point p) {
        popup.setInvoker(invoker);

        popup.setLocation((int) (invoker.getLocationOnScreen().getX() + p.getX()), (int) (invoker
                .getLocationOnScreen().getY() + p.getY()));
        popup.setVisible(true);
    }

    public static DesktopPanel selectAndShow(ModelItem modelItem) {
        UISupport.select(modelItem);
        return showDesktopPanel(modelItem);
    }

    public static DesktopPanel showDesktopPanel(ModelItem modelItem) {
        if (modelItem == null) {
            return null;
        }

        try {
            UISupport.setHourglassCursor();
            SoapUIDesktop desktop = SoapUI.getDesktop();
            return desktop == null ? null : desktop.showDesktopPanel(modelItem);
        } finally {
            UISupport.resetCursor();
        }
    }

    public static DesktopPanel showDesktopPanel(DesktopPanel desktopPanel) {
        try {
            UISupport.setHourglassCursor();
            SoapUIDesktop desktop = SoapUI.getDesktop();
            return desktop == null ? null : desktop.showDesktopPanel(desktopPanel);
        } finally {
            UISupport.resetCursor();
        }
    }

    public static Boolean confirmOrCancel(String question, String title) {
        return dialogs.confirmOrCancel(question, title);
    }

    public static JPanel buildPanelWithToolbar(JComponent top, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(top, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);

        return p;
    }

    public static JPanel buildPanelWithToolbarAndStatusBar(JComponent top, JComponent content, JComponent bottom) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(top, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    public static Dimension getPreferredButtonSize() {
        return TOOLBAR_BUTTON_DIMENSION;
    }

    public static void showErrorMessage(Throwable ex) {
        SoapUI.logError(ex);
        Analytics.trackError(ex);

        if (ex.toString().length() > 100) {
            dialogs.showExtendedInfo("Error", "An error of type " + ex.getClass().getSimpleName() + " occured.",
                    ex.toString(), null);
        } else {
            dialogs.showErrorMessage(ex.toString());
        }
    }

    public static Component wrapInEmptyPanel(JComponent component, Border border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(component, BorderLayout.CENTER);
        panel.setBorder(border);

        return panel;
    }

    public static boolean isWindows() {
        return isWindows;
    }

    public static boolean isMac() {
        return isMac;
    }

    public static void setHourglassCursor() {
        if (frame == null) {
            return;
        }

        if (hourglassCursor == null) {
            hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        }

        frame.setCursor(hourglassCursor);
    }

    public static void resetCursor() {
        if (frame == null) {
            return;
        }

        if (defaultCursor == null) {
            defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        }

        frame.setCursor(defaultCursor);
    }

    public static void setUIUtils(UIUtils utils) {
        UISupport.uiUtils = utils;
    }

    public static UIUtils getUIUtils() {
        return uiUtils;
    }

    public static void invokeLater(Runnable runnable) {
        uiUtils.invokeLater(runnable);
    }

    public static void invokeAndWait(Runnable runnable) throws Exception {
        uiUtils.invokeAndWait(runnable);
    }

    public static void invokeAndWaitIfNotInEDT(Runnable runnable) {
        uiUtils.invokeAndWaitIfNotInEDT(runnable);
    }

    public static JXToolBar createToolbar() {
        JXToolBar toolbar = new JXToolBar();
        toolbar.setFloatable(false);
        toolbar.addSpace(1);
        toolbar.setRollover(true);
        toolbar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        toolbar.setMinimumSize(new Dimension(20, 20));
        toolbar.setBackground(Color.WHITE);
        return toolbar;
    }

    public static JXToolBar createSmallToolbar() {
        JXToolBar toolbar = new JXToolBar();
        toolbar.setFloatable(false);
        toolbar.addSpace(1);
        toolbar.setRollover(true);
        toolbar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        toolbar.setMinimumSize(new Dimension(20, 20));
        toolbar.setBackground(Color.WHITE);
        return toolbar;
    }

    /**
     * Replaces "menu" in the keyStroke with ctrl or meta depending on
     * getMenuShortcutKeyMask
     */

    public static KeyStroke getKeyStroke(String keyStroke) {
        try {
            if (InputHandler.getMenuShortcutKeyMask() == Event.META_MASK) {
                keyStroke = keyStroke.replaceAll("menu", "meta");
            } else {
                keyStroke = keyStroke.replaceAll("menu", "ctrl");
            }
        } catch (Throwable e) {
            keyStroke = keyStroke.replaceAll("menu", "ctrl");
        }

        return KeyStroke.getKeyStroke(keyStroke);
    }

    public static DescriptionPanel buildDescription(String title, String string, ImageIcon icon) {
        return new DescriptionPanel(title, string, icon);
    }

    public static void setPreferredHeight(Component component, int heigth) {
        component.setPreferredSize(new Dimension((int) component.getPreferredSize().getWidth(), heigth));
    }

    public static JButtonBar initDialogActions(ActionList actions, final JDialog dialog) {
        return initWindowActions(actions, dialog.getRootPane(), dialog);
    }

    public static JButtonBar initFrameActions(ActionList actions, final JFrame frame) {
        return initWindowActions(actions, frame.getRootPane(), frame);
    }

    private static JButtonBar initWindowActions(ActionList actions, JRootPane rootPane, final Window dialog) {
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
        rootPane.getActionMap().put("ESCAPE", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });

        if (actions != null) {
            JButtonBar buttons = new JButtonBar();
            buttons.addActions(actions);
            rootPane.setDefaultButton(buttons.getDefaultButton());

            for (int c = 0; c < actions.getActionCount(); c++) {
                Action action = actions.getActionAt(c);
                if (action instanceof HelpActionMarker) {
                    rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
                    rootPane.getActionMap().put("HELP", action);
                    break;
                }
            }

            return buttons;
        }

        return null;
    }

    public static void initDialogActions(final JDialog dialog, Action helpAction, JButton defaultButton) {
        dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
        dialog.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });

        if (defaultButton != null) {
            dialog.getRootPane().setDefaultButton(defaultButton);
        }

        if (helpAction != null) {
            dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
            dialog.getRootPane().getActionMap().put("HELP", helpAction);
        }
    }

    public static <T extends JComponent> T addTitledBorder(T component, String title) {
        component
                .setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(3, 0, 0, 0),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title),
                                component.getBorder())));

        return component;
    }

    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T prompt(String question, String title, List<T> objects) {
        return (T) dialogs.prompt(question, title, objects.toArray());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T prompt(String question, String title, List<T> objects, String value) {
        return (T) dialogs.prompt(question, title, objects.toArray(), value);
    }

    public static void showExtendedInfo(String title, String description, String content, Dimension size) {
        dialogs.showExtendedInfo(title, description, content, size);
    }

    public static boolean confirmExtendedInfo(String title, String description, String content, Dimension size) {
        return dialogs.confirmExtendedInfo(title, description, content, size);
    }

    public static Boolean confirmOrCancelExtendedInfo(String title, String description, String content, Dimension size) {
        return dialogs.confirmOrCancleExtendedInfo(title, description, content, size);
    }

    public static void select(ModelItem modelItem) {
        if (SoapUI.getNavigator() != null) {
            SoapUI.getNavigator().selectModelItem(modelItem);
        }
    }

    public static JButton createActionButton(Action action, boolean enabled) {
        JButton button = createToolbarButton(action, enabled);
        action.putValue(Action.NAME, null);
        return button;
    }

    public static URL findSplash(String filename) {
        File file = new File(filename);
        URL url = null;

        try {
            if (!file.exists()) {
                url = SoapUI.class.getResource("/com/eviware/soapui/resources/images/" + filename);
            } else {
                url = file.toURI().toURL();
            }
        } catch (Exception e1) {
        }

        try {
            if (url == null) {
                url = new URL("http://www.soapui.org/images/" + filename);
            }
        } catch (Exception e2) {
            SoapUI.logError(e2);
        }

        return url;
    }

    public static String selectXPath(String title, String info, String xml, String xpath) {
        return dialogs.selectXPath(title, info, xml, xpath);
    }

    public static String selectJsonPath(String title, String info, String json, String jsonPath) {
        return dialogs.selectJsonPath(title, info, json, jsonPath);
    }

    public static PreviewCorner addPreviewCorner(JScrollPane scrollPane, boolean forceScrollbars) {
        ImageIcon previewIcon = UISupport.createImageIcon("/previewscroller.gif");
        PreviewCorner previewCorner = new PreviewCorner(scrollPane, previewIcon, true, JScrollPane.LOWER_RIGHT_CORNER);
        scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, previewCorner);

        if (forceScrollbars) {
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        }

        return previewCorner;
    }

    public static <T extends JComponent> T setFixedSize(T component, Dimension size) {
        component.setMinimumSize(size);
        component.setMaximumSize(size);
        component.setPreferredSize(size);
        component.setSize(size);

        return component;
    }

    public static <T extends JComponent> T setFixedSize(T component, int i, int j) {
        return setFixedSize(component, new Dimension(i, j));
    }

    public static void setFixedColumnSize(TableColumn column, int width) {
        column.setWidth(width);
        column.setPreferredWidth(width);
        column.setMaxWidth(width);
        column.setMinWidth(width);
    }

    public static JButton createToolbarButton(ImageIcon icon) {
        JButton result = new JButton(icon);
        result.setPreferredSize(TOOLBAR_BUTTON_DIMENSION);
        return result;
    }

    public static String getIconPath(ImageIcon icon) {
        String path = "";
        for (String icnPath : iconCache.keySet()) {
            ImageIcon icn = iconCache.get(icnPath);
            if (icon.equals(icn)) {
                path = icnPath;
                break;
            }
        }
        return path;
    }

    public static Font getEditorFont() {
        return getEditorFont(SoapUI.getSettings());
    }

    public static Font getEditorFont(Settings settings) {
        String editorFont = settings.getString(UISettings.EDITOR_FONT, null);
        if (StringUtils.hasContent(editorFont)) {
            return Font.decode(editorFont);
        }

        Integer fontSize = (Integer) UIManager.get("customFontSize");
        if (fontSize == null) {
            fontSize = DEFAULT_EDITOR_FONT_SIZE;
        }

        return Font.decode(DEFAULT_EDITOR_FONT + " " + fontSize);
    }

    public static <T> T findParentWithClass(Component startComponent, Class<T> expectedClass) {
        Component currentComponent = startComponent;
        while (currentComponent != null && !(expectedClass.isAssignableFrom(currentComponent.getClass()))) {
            currentComponent = currentComponent.getParent();
        }
        return (T) currentComponent;
    }

    public static char[] promptPassword(String question, String title) {
        return dialogs.promptPassword(question, title);
    }

    private static final class ItemListenerImplementation implements ItemListener {
        private final JComboBox combo;
        private final String defaultTooltip;

        public ItemListenerImplementation(JComboBox combo, String defaultTooltip) {
            this.combo = combo;
            this.defaultTooltip = defaultTooltip;
        }

        // set tooltip, property is set by model directly
        public void itemStateChanged(ItemEvent e) {
            Object item = combo.getSelectedItem();
            if (item == null) {
                combo.setToolTipText(defaultTooltip);
            } else {
                String selectedItem = item.toString();

                if (item instanceof ModelItem) {
                    selectedItem = ((ModelItem) item).getName();
                } else if (item instanceof TestProperty) {
                    selectedItem = ((TestProperty) item).getName();
                }

                combo.setToolTipText(selectedItem);
            }
        }
    }

    public static boolean isIdePlugin() {
        return SoapUI.getSoapUICore() instanceof SwingPluginSoapUICore;
    }

    public static JPanel createEmptyPanel(int top, int left, int bottom, int right) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return panel;
    }

    public static JLabel createLabelLink(final String url, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.BLUE);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Tools.openURL(url);
            }
        });
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
    }

    public static boolean isEnoughSpaceAvailableBelowComponent(Point componentLocation, int expandableDialogHeight, int componentHeight) {
        GraphicsConfiguration currentGraphicsConfiguration = getGraphicsConfigurationForPosition(componentLocation);
        if (currentGraphicsConfiguration == null) {
            return true;
        }
        double bottomYCoordinate = componentLocation.getY() + expandableDialogHeight + componentHeight;
        double bottomUsableYCoordinateOnScreen = currentGraphicsConfiguration.getBounds().getMaxY()
                - Toolkit.getDefaultToolkit().getScreenInsets(currentGraphicsConfiguration).bottom;
        return bottomYCoordinate <= bottomUsableYCoordinateOnScreen;
    }

    private static GraphicsConfiguration getGraphicsConfigurationForPosition(Point point) {
        for (GraphicsDevice graphicsDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            if (graphicsDevice.getDefaultConfiguration().getBounds().contains(point)) {
                return graphicsDevice.getDefaultConfiguration();
            }
        }
        return null;
    }
}
