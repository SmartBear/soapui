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

package com.eviware.soapui.ui.desktop.standalone;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.model.util.PanelBuilderRegistry;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.ui.desktop.AbstractSoapUIDesktop;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The default standalone SoapUI desktop using a JDesktopPane
 *
 * @author Ole.Matzura
 */

public class StandaloneDesktop extends AbstractSoapUIDesktop {
    private JDesktopPane desktop;
    private Map<ModelItem, JInternalFrame> modelItemToInternalFrameMap = new HashMap<ModelItem, JInternalFrame>();
    private Map<JInternalFrame, DesktopPanel> internalFrameToDesktopPanelMap = new HashMap<JInternalFrame, DesktopPanel>();
    private DesktopPanelPropertyChangeListener desktopPanelPropertyChangeListener = new DesktopPanelPropertyChangeListener();
    private InternalDesktopFrameListener internalFrameListener = new InternalDesktopFrameListener();
    private ActionList actions;

    private DesktopPanel currentPanel;

    private CloseCurrentAction closeCurrentAction = new CloseCurrentAction();
    private CloseOtherAction closeOtherAction = new CloseOtherAction();
    private CloseAllAction closeAllAction = new CloseAllAction();

    private static final int xOffset = 30, yOffset = 30;
    private boolean transferring;

    private List<DesktopPanel> deferredDesktopPanels = new LinkedList<DesktopPanel>();
    private JInspectorPanel inspector;
    private JPanel inspectorPanel;

    public StandaloneDesktop(Workspace workspace) {
        super(workspace);

        buildUI();

        actions = new DefaultActionList("Desktop");
        actions.addAction(closeCurrentAction);
        actions.addAction(closeOtherAction);
        actions.addAction(closeAllAction);

        // Setting Mac-like color for all platforms pending
        desktop.setBackground(UISupport.MAC_BACKGROUND_COLOR);
        enableWindowActions();
        desktop.addComponentListener(new DesktopResizeListener());

        DesktopManager originalDesktopManager = desktop.getDesktopManager();
        boolean mruSelectionChosen = SoapUI.isSelectingMostRecentlyUsedDesktopPanelOnClose();
        DesktopManager delegate = mruSelectionChosen ? new MostRecentlyUsedOrderDesktopManager(originalDesktopManager) :
                originalDesktopManager;
        desktop.setDesktopManager(new BoundsAwareDesktopManager(delegate));
    }

    private void enableWindowActions() {
        closeCurrentAction.setEnabled(currentPanel != null && internalFrameToDesktopPanelMap.size() > 0);
        closeOtherAction.setEnabled(currentPanel != null && internalFrameToDesktopPanelMap.size() > 1);
        closeAllAction.setEnabled(internalFrameToDesktopPanelMap.size() > 0);
    }

    private void buildUI() {
        desktop = new SoapUIDesktopPane();
        JScrollPane scrollPane = new JScrollPane(desktop);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        inspector = JInspectorPanelFactory.build(scrollPane, SwingConstants.RIGHT);
        inspectorPanel = new JPanel( new BorderLayout());
        inspector.addInspector(new JComponentInspector<JComponent>(inspectorPanel, "Inspector",
                "Object Inspector", true));
        inspector.setDefaultDividerLocation(0.75f);
    }

    public JComponent getDesktopComponent() {
        return inspector.getComponent();
    }

    @Override
    public void showInspector(JComponent component) {
        inspectorPanel.removeAll();
        inspectorPanel.add( component, BorderLayout.CENTER );
        inspectorPanel.repaint();

        inspector.setCurrentInspector( "Inspector" );
    }

    public boolean closeDesktopPanel(DesktopPanel desktopPanel) {
        try {
            if (desktopPanel.getModelItem() != null) {
                return closeDesktopPanel(desktopPanel.getModelItem());
            } else {
                JInternalFrame frame = getFrameForDesktopPanel(desktopPanel);
                if (frame != null) {
                    frame.doDefaultCloseAction();
                    return frame.isClosed();
                }
                // else
                // throw new RuntimeException( "Cannot close unkown DesktopPanel: "
                // + desktopPanel.getTitle() );

                return false;
            }
        } finally {
            enableWindowActions();
        }
    }

    private JInternalFrame getFrameForDesktopPanel(DesktopPanel desktopPanel) {
        for (JInternalFrame frame : internalFrameToDesktopPanelMap.keySet()) {
            if (internalFrameToDesktopPanelMap.get(frame) == desktopPanel) {
                return frame;
            }
        }

        return null;
    }

    public boolean hasDesktopPanel(ModelItem modelItem) {
        return modelItemToInternalFrameMap.containsKey(modelItem);
    }

    public DesktopPanel showDesktopPanel(ModelItem modelItem) {
        PanelBuilder<ModelItem> panelBuilder = PanelBuilderRegistry.getPanelBuilder(modelItem);
        if (modelItemToInternalFrameMap.containsKey(modelItem)) {
            JInternalFrame frame = modelItemToInternalFrameMap.get(modelItem);
            try {
                desktop.getDesktopManager().deiconifyFrame(frame);
                frame.setSelected(true);
                frame.moveToFront();
                currentPanel = internalFrameToDesktopPanelMap.get(frame);
            } catch (PropertyVetoException e) {
                SoapUI.logError(e);
            }
        } else if (panelBuilder != null && panelBuilder.hasDesktopPanel()) {
            DesktopPanel desktopPanel = panelBuilder.buildDesktopPanel(modelItem);
            if (desktopPanel == null)
                return null;

            JInternalFrame frame = createContentFrame(desktopPanel);

            desktop.add(frame);
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException e) {
                SoapUI.logError(e);
            }

            modelItemToInternalFrameMap.put(modelItem, frame);
            internalFrameToDesktopPanelMap.put(frame, desktopPanel);

            fireDesktopPanelCreated(desktopPanel);

            currentPanel = desktopPanel;
            desktopPanel.getComponent().requestFocusInWindow();
        } else
            Toolkit.getDefaultToolkit().beep();

        enableWindowActions();

        return currentPanel;
    }

    private JInternalFrame createContentFrame(DesktopPanel desktopPanel) {
        desktopPanel.addPropertyChangeListener(desktopPanelPropertyChangeListener);

        JComponent panel = desktopPanel.getComponent();

        panel.setOpaque(true);

        String title = desktopPanel.getTitle();

        JInternalFrame frame = new JInternalFrame(title, true, true, true, true);
        frame.addInternalFrameListener(internalFrameListener);
        frame.setContentPane(panel);
        frame.setLocation(xOffset * (desktop.getComponentCount() % 10), yOffset * (desktop.getComponentCount() % 10));
        Point location = frame.getLocation();
        Dimension frameSize = calculateDesktopPanelSize(panel, location);
        frame.setSize(frameSize);
        frame.setVisible(true);
        frame.setFrameIcon(desktopPanel.getIcon());
        frame.setToolTipText(desktopPanel.getDescription());
        frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        if (!SoapUI.getSettings().getBoolean(UISettings.NATIVE_LAF)) {
            // This creates an empty frame on Mac OS X native L&F.
            frame.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }
        if(!UISupport.isMac()) {
            frame.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        return frame;
    }

    private Dimension calculateDesktopPanelSize(JComponent panel, Point location) {
        Dimension frameSize;
        Dimension preferredSize = panel.getPreferredSize();
        if (desktop.getBounds().contains(new Rectangle(location, preferredSize))) {
            frameSize = preferredSize;
        } else {
            frameSize = new Dimension((int) ((desktop.getWidth() - location.x) * .95),
                    (int) ((desktop.getHeight() - location.y) * .95));
        }
        return frameSize;
    }

    public boolean closeDesktopPanel(ModelItem modelItem) {
        try {
            if (modelItemToInternalFrameMap.containsKey(modelItem)) {
                JInternalFrame frame = modelItemToInternalFrameMap.get(modelItem);
                frame.doDefaultCloseAction();
                return frame.isClosed();
            }

            return false;
        } finally {
            enableWindowActions();
        }
    }

    private class InternalDesktopFrameListener extends InternalFrameAdapter {
        public void internalFrameClosing(InternalFrameEvent e) {
            DesktopPanel desktopPanel = internalFrameToDesktopPanelMap.get(e.getInternalFrame());
            if (!transferring && !desktopPanel.onClose(true)) {
                return;
            }

            desktopPanel.removePropertyChangeListener(desktopPanelPropertyChangeListener);

            modelItemToInternalFrameMap.remove(desktopPanel.getModelItem());
            internalFrameToDesktopPanelMap.remove(e.getInternalFrame());

            // replace content frame to make sure it is released
            e.getInternalFrame().setContentPane(new JPanel());
            e.getInternalFrame().dispose();

            if (!transferring)
                fireDesktopPanelClosed(desktopPanel);

            if (currentPanel == desktopPanel)
                currentPanel = null;
        }

        public void internalFrameActivated(InternalFrameEvent e) {
            currentPanel = internalFrameToDesktopPanelMap.get(e.getInternalFrame());
            if (currentPanel != null) {
                fireDesktopPanelSelected(currentPanel);
            }

            enableWindowActions();
        }

        public void internalFrameDeactivated(InternalFrameEvent e) {
            currentPanel = null;
            enableWindowActions();
        }
    }

    public class CloseCurrentAction extends AbstractAction {
        public CloseCurrentAction() {
            super("Close Current");
            putValue(Action.SHORT_DESCRIPTION, "Closes the current window");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu F4"));

        }

        public void actionPerformed(ActionEvent e) {
            JInternalFrame frame = desktop.getSelectedFrame();
            if (frame != null)
                closeDesktopPanel(internalFrameToDesktopPanelMap.get(frame));
        }
    }

    public class CloseOtherAction extends AbstractAction {
        public CloseOtherAction() {
            super("Close Others");
            putValue(Action.SHORT_DESCRIPTION, "Closes all windows except the current one");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu alt O"));
        }

        public void actionPerformed(ActionEvent e) {
            JInternalFrame frame = desktop.getSelectedFrame();
            if (frame == null)
                return;

            JInternalFrame[] frames = internalFrameToDesktopPanelMap.keySet().toArray(
                    new JInternalFrame[internalFrameToDesktopPanelMap.size()]);
            for (JInternalFrame f : frames) {
                if (f != frame) {
                    closeDesktopPanel(internalFrameToDesktopPanelMap.get(f));
                }
            }
        }
    }

    public class CloseAllAction extends AbstractAction {
        public CloseAllAction() {
            super("Close All");
            putValue(Action.SHORT_DESCRIPTION, "Closes all windows");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu alt L"));
        }

        public void actionPerformed(ActionEvent e) {
            closeAll();
        }
    }

    public ActionList getActions() {
        return actions;
    }

    private class DesktopPanelPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            DesktopPanel desktopPanel = (DesktopPanel) evt.getSource();
            JInternalFrame frame = getFrameForDesktopPanel(desktopPanel);
            if (frame != null) {
                if (evt.getPropertyName().equals(DesktopPanel.TITLE_PROPERTY)) {
                    frame.setTitle(desktopPanel.getTitle());
                } else if (evt.getPropertyName().equals(DesktopPanel.ICON_PROPERTY)) {
                    frame.setFrameIcon(desktopPanel.getIcon());
                }
            }
        }
    }

    public DesktopPanel[] getDesktopPanels() {
        return internalFrameToDesktopPanelMap.values().toArray(new DesktopPanel[internalFrameToDesktopPanelMap.size()]);
    }

    public DesktopPanel getDesktopPanel(ModelItem modelItem) {
        for (DesktopPanel panel : internalFrameToDesktopPanelMap.values()) {
            if (panel.getModelItem() == modelItem) {
                return panel;
            }
        }
        return null;
    }

    public DesktopPanel showDesktopPanel(DesktopPanel desktopPanel) {
        if (desktop.getBounds().width == 0) {
            deferredDesktopPanels.add(desktopPanel);
            return desktopPanel;
        }
        JInternalFrame frame = getFrameForDesktopPanel(desktopPanel);
        if (frame != null) {
            try {
                desktop.getDesktopManager().deiconifyFrame(frame);
                frame.setSelected(true);
                frame.moveToFront();
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        } else {
            frame = createContentFrame(desktopPanel);
            desktop.add(frame);

            if (desktopPanel.getModelItem() != null)
                modelItemToInternalFrameMap.put(desktopPanel.getModelItem(), frame);

            internalFrameToDesktopPanelMap.put(frame, desktopPanel);
            fireDesktopPanelCreated(desktopPanel);
            frame.moveToFront();
            desktopPanel.getComponent().requestFocusInWindow();
        }

        currentPanel = desktopPanel;
        enableWindowActions();

        return desktopPanel;
    }

    class SoapUIDesktopPane extends JDesktopPane {
        Image img;
        private int imageWidth;
        private int imageHeight;

        public SoapUIDesktopPane() {
            try {
                File file = new File("soapui-background.gif");
                if (!file.exists())
                    file = new File("soapui-background.jpg");
                if (!file.exists())
                    file = new File("/soapui-background.png");

                if (file.exists()) {
                    img = javax.imageio.ImageIO.read(file);
                    imageWidth = img.getWidth(this);
                    imageHeight = img.getHeight(this);
                }
            } catch (Exception e) {
                SoapUI.logError(e, "Could not load graphics for desktop");
            }
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null)
                return;

            int x = (this.getWidth() - imageWidth) / 2;
            int y = (this.getHeight() - imageHeight) / 2;

            g.drawImage(img, x, y, imageWidth, imageHeight, this);
        }
    }

    public void transferTo(SoapUIDesktop newDesktop) {
        transferring = true;

        List<DesktopPanel> values = new ArrayList<DesktopPanel>(internalFrameToDesktopPanelMap.values());
        for (DesktopPanel desktopPanel : values) {
            closeDesktopPanel(desktopPanel);
            newDesktop.showDesktopPanel(desktopPanel);
        }

        transferring = false;
    }

    public boolean closeAll() {
        while (internalFrameToDesktopPanelMap.size() > 0) {
            Iterator<JInternalFrame> i = internalFrameToDesktopPanelMap.keySet().iterator();
            try {
                i.next().setClosed(true);
            } catch (PropertyVetoException e1) {
                SoapUI.logError(e1);
            }
        }

        internalFrameToDesktopPanelMap.clear();
        modelItemToInternalFrameMap.clear();

        JInternalFrame[] allFrames = desktop.getAllFrames();
        for (JInternalFrame frame : allFrames) {
            frame.doDefaultCloseAction();
        }

        enableWindowActions();
        return true;
    }

    public void minimize(DesktopPanel desktopPanel) {
        try {
            getFrameForDesktopPanel(desktopPanel).setIcon(true);
        } catch (PropertyVetoException e) {
            SoapUI.logError(e);
        }
    }

    public void maximize(DesktopPanel desktopPanel) {
        desktop.getDesktopManager().maximizeFrame(getFrameForDesktopPanel(desktopPanel));
    }


    /**
     * Helper class that ensures that desktop panels are displayed after a change from Tabbed to Standalone desktop.
     */
    private class DesktopResizeListener implements ComponentListener {
        @Override
        public void componentResized(ComponentEvent e) {
            Iterator<DesktopPanel> iterator = deferredDesktopPanels.iterator();
            while (iterator.hasNext()) {
                DesktopPanel nextPanel = iterator.next();
                showDesktopPanel(nextPanel);
                iterator.remove();
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    }

    /**
     * Helper class that decorates the standard desktop manager and prevents it from moving desktop panels outside
     * the desktop.
     * <p>
     * <emp>Implementation note</emp> : the width of internal frames includes their borders.  The size of borders
     * is obtained with <code>panel.getInsets()</code>.  Depending on the look and feel installed, the space occupied
     * by borders may not be selectable.  Furthermore, with Aqua l&f for example, there is an extra inside area on
     * the right of panel where mouse clicks are ignored and thus the panel cannot be dragged by a click in there.
     * Also, it is very difficult for us humans to notice a region on the screen that can be selected with the mouse
     * if that region is only a couple of pixels wide or tall.  For all these reasons, it is better to leave a
     * minimum of selectable-draggable portion of the panel visible in the desktop so the user does not loose its
     * panel outside the desktop.  That is the purpose of <code>horizontalInsetFactor</code> and <code>verticalInsetFactor</code>
     * fields of <code>BoundsAwareDesktopManager</code> : on windows and linux default l&f, the insets are small and
     * trial and error lead to a factor of 6 for computing the <emp>comfortable</emp> minimal space to leave visible
     * on the desktop.  On Aqua l&f (i.e. on Mac), the insets are bigger, leading to a factor of 3 as a <emp>comfortable</emp>
     * minimal space horizontally and a factor of 1 vertically, because the underlying UI implementation prevents
     * dragging the title bar beyond the desktop panel highest Y-boundaries..
     * </p>
     */
    private class BoundsAwareDesktopManager implements DesktopManager {

        private DesktopManager delegate;
        private int horizontalInsetFactor = 6;
        private int verticalInsetFactor = 6;
        private Dimension desktopSize;

        private BoundsAwareDesktopManager(DesktopManager delegate) {
            this.delegate = delegate;

            desktopSize = desktop.getSize();

            if (UISupport.isMac()) {
                horizontalInsetFactor = 3;
                verticalInsetFactor = 1;
            }
        }

		/* Methods enhancing the delegate with awareness of bounds */

        @Override
        public void dragFrame(JComponent f, int newX, int newY) {
            if (outsideDesktop(f, newX, newY)) {
                Point positionWherePanelReachable = findPositionWherePanelReachable(f, newX, newY);
                delegate.dragFrame(f, positionWherePanelReachable.x, positionWherePanelReachable.y);
            } else {
                delegate.dragFrame(f, newX, newY);
            }
        }

        @Override
        public void setBoundsForFrame(JComponent desktopPanel, int newX, int newY, int newWidth, int newHeight) {
            if (outsideDesktop(desktopPanel, newX, newY)) {
                Point pointInsideDesktop = findPositionInsideDesktop(desktopPanel, newX, newY);
                delegate.setBoundsForFrame(desktopPanel, pointInsideDesktop.x, pointInsideDesktop.y, newWidth, newHeight);
            } else {
                delegate.setBoundsForFrame(desktopPanel, newX, newY, newWidth, newHeight);
            }
        }

		/* Methods only delegating to the encapsulated delegate */

        @Override
        public void openFrame(JInternalFrame f) {
            delegate.openFrame(f);
        }

        @Override
        public void closeFrame(JInternalFrame f) {
            delegate.closeFrame(f);
        }

        @Override
        public void maximizeFrame(JInternalFrame f) {
            delegate.maximizeFrame(f);
        }

        @Override
        public void minimizeFrame(JInternalFrame f) {
            delegate.minimizeFrame(f);
        }

        @Override
        public void iconifyFrame(JInternalFrame f) {
            delegate.iconifyFrame(f);
        }

        @Override
        public void deiconifyFrame(JInternalFrame f) {
            delegate.deiconifyFrame(f);
        }

        @Override
        public void activateFrame(JInternalFrame f) {
            delegate.activateFrame(f);
        }

        @Override
        public void deactivateFrame(JInternalFrame f) {
            delegate.deactivateFrame(f);
        }

        @Override
        public void beginDraggingFrame(JComponent f) {
            desktopSize = desktop.getSize();
            delegate.beginDraggingFrame(f);
        }

        @Override
        public void endDraggingFrame(JComponent f) {
            delegate.endDraggingFrame(f);
        }

        @Override
        public void beginResizingFrame(JComponent f, int direction) {
            delegate.beginResizingFrame(f, direction);
        }

        @Override
        public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
            delegate.resizeFrame(f, newX, newY, newWidth, newHeight);
        }

        @Override
        public void endResizingFrame(JComponent f) {
            delegate.endResizingFrame(f);
        }

        /**
         * <p>
         * True if the coordinates (newX, newY) would put the panel outside the desktop in a manner that would make it
         * unreachable, false otherwise.
         * </p>
         * <p>
         * Parameters <code>newX</code> and <code>newY</code> are assumed to be the (left, top) origin coordinates of
         * <code>desktopPanel</code>, which is the <code>JInternalFrame</code> being dragged.  The panel's width includes
         * borders, which means that on implementations where the borders are not selectable (Mac Aqua look&feel for
         * example), the borders must be taken into account otherwise one can drag a panel into a position where it
         * would no longer be selectable, thus impossible to bring back to the visible area of the parent desktop.
         * </p>
         *
         * @param panel the panel being dragged
         * @param newX  target X-coordinate of leftmost window of desktopPanel
         * @param newY  target Y-coordinate of topmost window of desktopPanel
         * @return true if target coordinates would put desktopPanel out of reach, false otherwise
         */
        private boolean outsideDesktop(JComponent panel, int newX, int newY) {
            int smallestReachableX = -(panel.getWidth() - horizontalInsetFactor * panel.getInsets().right);
            int biggestReachableX = ((int) desktopSize.getWidth() - horizontalInsetFactor * panel.getInsets().left);
            int biggestReachableY = ((int) desktopSize.getHeight() - verticalInsetFactor * panel.getInsets().top);
            boolean xCoordinateOutside = newX > biggestReachableX || newX < smallestReachableX;
            boolean yCoordinateOutside = newY < 0 || newY > biggestReachableY;

            return xCoordinateOutside || yCoordinateOutside;
        }

        private Point findPositionWherePanelReachable(JComponent panel, int newX, int newY) {
            // at left, smallest X is a function of panel width
            // at right, highest X is a funtion of desktop width
            // at top, smalest Y is 0 (we always want to see the title bar)
            // at bottom, highest Y is a function of desktop height
            int smallestReachableX = -(panel.getWidth() - horizontalInsetFactor * panel.getInsets().right);
            int biggestReachableX = ((int) desktopSize.getWidth() - horizontalInsetFactor * panel.getInsets().left);
            int biggestReachableY = ((int) desktopSize.getHeight() - verticalInsetFactor * panel.getInsets().top);
            int boundedX, boundedY;

            boundedX = ((newX <= 0) ? Math.max(smallestReachableX, newX) : Math.min(biggestReachableX, newX));
            boundedY = ((newY <= 0) ? 0 : Math.min(biggestReachableY, newY));

            return new Point(boundedX, boundedY);

        }

        private Point findPositionInsideDesktop(JComponent f, int newX, int newY) {
            Container desktop = f.getParent();
            Dimension desktopSize = desktop.getSize();
            int boundedX = (int) Math.min(Math.max(0, newX), desktopSize.getWidth());
            int boundedY = (int) Math.min(Math.max(0, newY), desktopSize.getHeight());
            return new Point(boundedX, boundedY);
        }
    }
}
