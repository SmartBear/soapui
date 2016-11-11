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

package com.eviware.soapui.support.dnd;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.dnd.handlers.InterfaceToProjectDropHandler;
import com.eviware.soapui.support.dnd.handlers.MockResponseToTestCaseDropHandler;
import com.eviware.soapui.support.dnd.handlers.MockResponseToTestStepDropHandler;
import com.eviware.soapui.support.dnd.handlers.MockResponseToTestStepsDropHandler;
import com.eviware.soapui.support.dnd.handlers.MockServiceToProjectDropHandler;
import com.eviware.soapui.support.dnd.handlers.OperationToMockServiceDropHandler;
import com.eviware.soapui.support.dnd.handlers.RequestToMockOperationDropHandler;
import com.eviware.soapui.support.dnd.handlers.RequestToTestCaseDropHandler;
import com.eviware.soapui.support.dnd.handlers.RequestToTestStepDropHandler;
import com.eviware.soapui.support.dnd.handlers.RequestToTestStepsDropHandler;
import com.eviware.soapui.support.dnd.handlers.TestCaseToProjectDropHandler;
import com.eviware.soapui.support.dnd.handlers.TestCaseToTestCaseDropHandler;
import com.eviware.soapui.support.dnd.handlers.TestCaseToTestSuiteDropHandler;
import com.eviware.soapui.support.dnd.handlers.TestStepToTestCaseDropHandler;
import com.eviware.soapui.support.dnd.handlers.TestStepToTestStepDropHandler;
import com.eviware.soapui.support.dnd.handlers.TestStepToTestStepsDropHandler;
import com.eviware.soapui.support.dnd.handlers.TestSuiteToProjectDropHandler;
import com.eviware.soapui.support.dnd.handlers.TestSuiteToTestSuiteDropHandler;

import javax.swing.Timer;
import javax.swing.ToolTipManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SoapUIDragAndDropHandler implements DragGestureListener, DragSourceListener {
    public static final int ON_RANGE = 3;
    private final SoapUIDragAndDropable<ModelItem> dragAndDropable;
    private BufferedImage _imgGhost; // The 'drag image'
    private Point _ptOffset = new Point(); // Where, in the drag image, the mouse
    private static List<ModelItemDropHandler<ModelItem>> handlers;
    private Rectangle2D _raGhost = new Rectangle2D.Float();
    private final int dropType;
    private Point _ptLast = new Point();

    static {
        handlers = new ArrayList<ModelItemDropHandler<ModelItem>>();
        SoapUIDragAndDropHandler.addDropHandler(new TestStepToTestCaseDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new TestStepToTestStepsDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new TestStepToTestStepDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new TestSuiteToProjectDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new InterfaceToProjectDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new TestCaseToProjectDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new TestCaseToTestSuiteDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new TestCaseToTestCaseDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new RequestToTestCaseDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new RequestToTestStepsDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new RequestToTestStepDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new RequestToMockOperationDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new MockServiceToProjectDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new OperationToMockServiceDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new MockResponseToTestCaseDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new MockResponseToTestStepDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new MockResponseToTestStepsDropHandler());
        SoapUIDragAndDropHandler.addDropHandler(new TestSuiteToTestSuiteDropHandler());
    }

    @SuppressWarnings("unchecked")
    public SoapUIDragAndDropHandler(SoapUIDragAndDropable target, int dropType) {
        this.dragAndDropable = target;
        this.dropType = dropType;

        // Also, make this JTree a drag target
        DropTarget dropTarget = new DropTarget(target.getComponent(), new SoapUIDropTargetListener());
        dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    @SuppressWarnings("unchecked")
    public static void addDropHandler(ModelItemDropHandler dropHandler) {
        handlers.add(dropHandler);
    }

    public void dragGestureRecognized(DragGestureEvent e) {
        Point ptDragOrigin = e.getDragOrigin();
        ModelItem modelItem = dragAndDropable.getModelItemForLocation(ptDragOrigin.x, ptDragOrigin.y);
        if (modelItem == null) {
            return;
        }

        Rectangle raPath = dragAndDropable.getModelItemBounds(modelItem);
        if (raPath == null) {
            return;
        }

        _ptOffset = new Point(ptDragOrigin.x - raPath.x, ptDragOrigin.y - raPath.y);

        Component renderer = dragAndDropable.getRenderer(modelItem);
        if (renderer != null) {
            renderer.setSize((int) raPath.getWidth(), (int) raPath.getHeight()); // <--

            // Get a buffered image of the selection for dragging a ghost image
            _imgGhost = new BufferedImage((int) raPath.getWidth(), (int) raPath.getHeight(),
                    BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g2 = _imgGhost.createGraphics();

            // Ask the cell renderer to paint itself into the BufferedImage
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
            renderer.paint(g2);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 0.5f));

            int width = dragAndDropable.getComponent().getWidth();
            g2.setPaint(new GradientPaint(0, 0, SystemColor.controlShadow, width, 0, new Color(255, 255, 255, 0)));
            g2.fillRect(0, 0, width, _imgGhost.getHeight());
            g2.dispose();
        } else {
            _imgGhost = null;
        }

        dragAndDropable.selectModelItem(modelItem); // Select this path in the
        // tree

        // Wrap the path being transferred into a Transferable object
        Transferable transferable = new ModelItemTransferable(modelItem);

        // We pass our drag image just in case it IS supported by the platform
        e.startDrag(null, _imgGhost, new Point(5, 5), transferable, this);
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        if (_raGhost != null) {
            dragAndDropable.getComponent().repaint(_raGhost.getBounds());
        }

        _ptOffset = null;
        SoapUI.getNavigator().getMainTree().setToolTipText(null);
    }

    public void dragEnter(DragSourceDragEvent dsde) {

    }

    public void dragExit(DragSourceEvent dse) {

    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    // DropTargetListener interface object...
    class SoapUIDropTargetListener implements DropTargetListener {
        // Fields...
        private ModelItem _pathLast = null;
        private Rectangle2D _raCueLine = new Rectangle2D.Float();
        private Color _colorCueLine;
        private Timer _timerHover;
        // private int _nLeftRight = 0; // Cumulative left/right mouse movement
        private String dropInfo;

        // Constructor...
        public SoapUIDropTargetListener() {
            _colorCueLine = new Color(SystemColor.controlShadow.getRed(), SystemColor.controlShadow.getGreen(),
                    SystemColor.controlShadow.getBlue(), 128);

            // Set up a hover timer, so that a node will be automatically expanded
            _timerHover = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (_ptOffset != null) {
                        dragAndDropable.toggleExpansion(_pathLast);
                    }
                }
            });
            _timerHover.setRepeats(false); // Set timer to one-shot mode
        }

        // DropTargetListener interface
        public void dragEnter(DropTargetDragEvent e) {
            int dt = getDropTypeAtPoint(e.getLocation());

            if (dt == DropType.NONE || !isDragAcceptable(e, dt)) {
                e.rejectDrag();
            } else {
                e.acceptDrag(e.getDropAction());
            }
        }

        private int getDropTypeAtPoint(Point pt) {
            ModelItem modelItem = dragAndDropable.getModelItemForLocation(pt.x, pt.y);
            if (modelItem == null) {
                return DropType.NONE;
            }

            Rectangle raPath = dragAndDropable.getModelItemBounds(modelItem);

            if (pt.y > (raPath.y + (raPath.getHeight() / 2) + ON_RANGE)) {
                return DropType.AFTER;
            } else if (pt.y < (raPath.y + (raPath.getHeight() / 2) - ON_RANGE)) {
                return DropType.BEFORE;
            } else {
                return DropType.ON;
            }
        }

        public void dragExit(DropTargetEvent e) {
            if (!DragSource.isDragImageSupported()) {
                dragAndDropable.getComponent().repaint(_raGhost.getBounds());
            }
        }

        /**
         * This is where the ghost image is drawn
         */
        public void dragOver(DropTargetDragEvent e) {
            // Even if the mouse is not moving, this method is still invoked 10
            // times per second
            Point pt = e.getLocation();
            if (pt.equals(_ptLast)) {
                return;
            }

            _ptLast = pt;

            Graphics2D g2 = (Graphics2D) dragAndDropable.getComponent().getGraphics();

            // If a drag image is not supported by the platform, then draw my own
            // drag image
            if (!DragSource.isDragImageSupported() && _imgGhost != null && _ptOffset != null) {
                dragAndDropable.getComponent().paintImmediately(_raGhost.getBounds()); // Rub
                // out
                // the
                // last ghost
                // image and cue line
                // And remember where we are about to draw the new ghost image
                _raGhost.setRect(pt.x - _ptOffset.x, pt.y - _ptOffset.y, _imgGhost.getWidth(), _imgGhost.getHeight());
                g2.drawImage(_imgGhost, AffineTransform.getTranslateInstance(_raGhost.getX(), _raGhost.getY()), null);
            } else
            // Just rub out the last cue line
            {
                dragAndDropable.getComponent().paintImmediately(_raCueLine.getBounds());
            }

            ModelItem modelItem = dragAndDropable.getModelItemForLocation(pt.x, pt.y);
            if (modelItem == null) {
                e.rejectDrag();
                return;
            }

            if (!(modelItem == _pathLast)) {
                // movement trend
                _pathLast = modelItem;
                _timerHover.restart();
            }

            // In any case draw (over the ghost image if necessary) a cue line
            // indicating where a drop will occur
            Rectangle raPath = dragAndDropable.getModelItemBounds(modelItem);

            int dt = dropType;

            if (dropType == DropType.AFTER) {
                _raCueLine.setRect(0, raPath.y + (int) raPath.getHeight() - 2, dragAndDropable.getComponent().getWidth(),
                        2);
            } else if (dropType == DropType.BEFORE) {
                _raCueLine.setRect(0, raPath.y, dragAndDropable.getComponent().getWidth(), 2);
            } else if (dropType == DropType.ON) {
                _raCueLine.setRect(0, raPath.y, dragAndDropable.getComponent().getWidth(), raPath.getHeight());
            } else {
                if (pt.y > (raPath.y + (raPath.getHeight() / 2) + ON_RANGE)) {
                    _raCueLine.setRect(0, raPath.y + (int) raPath.getHeight() - 2, dragAndDropable.getComponent()
                            .getWidth(), 2);
                    dt = DropType.AFTER;
                } else if (pt.y < (raPath.y + (raPath.getHeight() / 2) - ON_RANGE)) {
                    _raCueLine.setRect(0, raPath.y, dragAndDropable.getComponent().getWidth(), 2);
                    dt = DropType.BEFORE;
                } else {
                    _raCueLine.setRect(0, raPath.y, dragAndDropable.getComponent().getWidth(), raPath.getHeight());
                    dt = DropType.ON;
                }
            }

            boolean dragAcceptable = isDragAcceptable(e, dt);
            g2.setColor(_colorCueLine);
            g2.fill(_raCueLine);

            if (dragAcceptable) {
                dragAndDropable.setDragInfo(dropInfo);
            } else {
                dragAndDropable.setDragInfo("");
            }

            ToolTipManager.sharedInstance().mouseMoved(
                    new MouseEvent(dragAndDropable.getComponent(), 0, 0, 0, pt.x, pt.y + 10, // X-Y
                            // of
                            // the
                            // mouse
                            // for
                            // the
                            // tool
                            // tip
                            0, false));

            // And include the cue line in the area to be rubbed out next time
            _raGhost = _raGhost.createUnion(_raCueLine);

            if (!dragAcceptable) {
                e.rejectDrag();
            } else {
                e.acceptDrag(e.getDropAction());
            }
        }

        public void dropActionChanged(DropTargetDragEvent e) {
            int dt = getDropTypeAtPoint(e.getLocation());

            if (dt == DropType.NONE || !isDragAcceptable(e, dt)) {
                e.rejectDrag();
            } else {
                e.acceptDrag(e.getDropAction());
            }
        }

        public void drop(DropTargetDropEvent e) {
            int dt = getDropTypeAtPoint(e.getLocation());
            _timerHover.stop();

            if (dt == DropType.NONE || !isDropAcceptable(e, dt)) {
                e.rejectDrop();
                return;
            }

            e.acceptDrop(e.getDropAction());

            Transferable transferable = e.getTransferable();

            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                DataFlavor flavor = flavors[i];
                if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
                    try {
                        Point pt = e.getLocation();
                        ModelItem pathTarget = dragAndDropable.getModelItemForLocation(pt.x, pt.y);
                        ModelItem pathSource = (ModelItem) transferable.getTransferData(flavor);

                        for (ModelItemDropHandler<ModelItem> handler : handlers) {
                            if (handler.canDrop(pathSource, pathTarget, e.getDropAction(), dt)) {
                                // System.out.println( "Got drop handler for " +
                                // pathSource.getName() + " to " + pathTarget.getName()
                                // + "; " + handler.getClass().getSimpleName() );

                                handler.drop(pathSource, pathTarget, e.getDropAction(), dt);
                                break;
                            }
                        }

                        break; // No need to check remaining flavors
                    } catch (Exception ioe) {
                        System.out.println(ioe);
                        e.dropComplete(false);
                        return;
                    }
                }
            }

            e.dropComplete(true);
        }

        // Helpers...
        public boolean isDragAcceptable(DropTargetDragEvent e, int dt) {
            // Only accept COPY or MOVE gestures (ie LINK is not supported)
            if ((e.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
                return false;
            }

            // Only accept this particular flavor
            if (!e.isDataFlavorSupported(ModelItemTransferable.MODELITEM_DATAFLAVOR)) {
                return false;
            }

            Transferable transferable = e.getTransferable();

            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                DataFlavor flavor = flavors[i];
                if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
                    try {
                        Point pt = e.getLocation();
                        ModelItem pathTarget = dragAndDropable.getModelItemForLocation(pt.x, pt.y);
                        ModelItem pathSource = (ModelItem) transferable.getTransferData(flavor);

                        for (ModelItemDropHandler<ModelItem> handler : handlers) {
                            if (handler.canDrop(pathSource, pathTarget, e.getDropAction(), dt)) {
                                dropInfo = handler.getDropInfo(pathSource, pathTarget, e.getDropAction(), dt);
                                // System.out.println( "Got drag handler for " +
                                // pathSource.getName() + " to " + pathTarget.getName()
                                // + "; " + handler.getClass().getSimpleName() );
                                return true;
                            }
                        }

                        // System.out.println( "Missing drop handler for " +
                        // pathSource.getName() + " to " + pathTarget.getName() );

                        dropInfo = null;
                    } catch (Exception ex) {
                        SoapUI.logError(ex);
                    }
                }
            }

            return false;
        }

        public boolean isDropAcceptable(DropTargetDropEvent e, int dt) {
            // Only accept COPY or MOVE gestures (ie LINK is not supported)
            if ((e.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
                return false;
            }

            // Only accept this particular flavor
            if (!e.isDataFlavorSupported(ModelItemTransferable.MODELITEM_DATAFLAVOR)) {
                return false;
            }

            Transferable transferable = e.getTransferable();

            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                DataFlavor flavor = flavors[i];
                if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
                    try {
                        Point pt = e.getLocation();
                        ModelItem pathSource = (ModelItem) transferable.getTransferData(flavor);
                        ModelItem pathTarget = dragAndDropable.getModelItemForLocation(pt.x, pt.y);

                        for (ModelItemDropHandler<ModelItem> handler : handlers) {
                            if (handler.canDrop(pathSource, pathTarget, e.getDropAction(), dt)) {
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                        SoapUI.logError(ex);
                    }
                }
            }

            return false;
        }
    }
}
