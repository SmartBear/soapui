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

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.UISupport;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.MenuSelectionManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * This is a button which is designed to be the corner component of a
 * <code>JScrollPane</code>. It triggers a popup menu which holds a scaled image
 * of the component contained inside the <code>JScrollPane</code>.
 */

public class PreviewCorner extends JButton implements ActionListener {
    private String _corner;
    private PreviewPopup _previewPopup;

    /**
     * @param scrollPane        the <code>JScrollPane</code> to preview
     * @param zoomIcon          the icon to use for the button
     * @param doCloseAfterClick When <code>true</code> the preview popup menu is closed on mouse
     *                          click.
     * @param corner            Supposed to be one of the four corners of a
     *                          <code>JScrollPane</code>, like
     *                          <code>JScrollPane.LOWER_RIGHT_CORNER</code> for example, which
     *                          should match the position of the corner component of the scroll
     *                          pane. Note: If this parameter is not set correctly,
     *                          <code>JScrollPane.UPPER_LEFT_CORNER</code> will be used instead.
     */

    public PreviewCorner(JScrollPane scrollPane, ImageIcon zoomIcon, boolean doCloseAfterClick, String corner) {

        super(zoomIcon);
        this._corner = corner;

        // Creates the popup menu, containing the scaled image of the component.
        _previewPopup = new PreviewPopup(scrollPane, doCloseAfterClick);

        setToolTipText("Show a Panorama View of the contained editor");

        // The action listener is used to trigger the popup menu.
        addActionListener(this);
    }

    public PreviewCorner(JScrollPane scrollPane, ImageIcon zoomIcon, String corner) {
        this(scrollPane, zoomIcon, false, corner);
    }

    public void actionPerformed(ActionEvent e) {
        _previewPopup.showUpInCorner(this, _corner);
    }

    public void release() {
        _previewPopup.release();
        removeActionListener(this);
    }
}

class PreviewPopup extends JPopupMenu implements MouseListener, MouseMotionListener {

    private JScrollPane _scrollPane;
    private JViewport _viewPort;

    private JLabel _zoomWindow; // the JLabel containing the scaled image

    private JPanel _cursorLabel; // the JLabel mimicking the fake rectangle
    // cursor

    // This component will hold both JLabels _zoomWindow and _cursorLabel,
    // the latter on top of the other.
    private JLayeredPane _layeredPane;

    private int _iconWidth;
    private int _iconHeight;

    private boolean _doCloseAfterClick;

    float _ratio;

    // DELTA is the space between the scroll pane and the preview popup menu.
    private static int DELTA = 5;

    public PreviewPopup(JScrollPane scrollPane, boolean doCloseAfterClick) {
        this.setBorder(BorderFactory.createEtchedBorder());

        _doCloseAfterClick = doCloseAfterClick;

        _scrollPane = scrollPane;
        _viewPort = _scrollPane.getViewport();

        _zoomWindow = new JLabel();
        _cursorLabel = createCursor();

        _layeredPane = new JLayeredPane();

        _layeredPane.add(_zoomWindow, new Integer(0));
        _layeredPane.add(_cursorLabel, new Integer(1));

        // Creates a blank transparent cursor to be used as the cursor of
        // the popup menu.
        BufferedImage bim = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        setCursor(getToolkit().createCustomCursor(bim, (new Point(0, 0)), "PreviewCursor"));

        this.add(_layeredPane);

        // Adds the mouse input listeners to the _layeredPane to scroll the
        // viewport and to move the fake cursor (_cursorLabel).
        _layeredPane.addMouseListener(this);
        _layeredPane.addMouseMotionListener(this);
    }

    public void release() {
        if (getParent() != null) {
            getParent().remove(this);
        }

        _layeredPane.removeMouseListener(this);
        _layeredPane.removeMouseMotionListener(this);

        _scrollPane = null;
        _layeredPane = null;
        _viewPort = null;
    }

    /**
     * By default, the right corner of a popup menu is positionned at the right
     * of a mouse click. What we want is to have the preview popup menu
     * positionned <i>inside</i> the scroll pane, near the corner component. The
     * purpose of this method is to display the scaled image of the component of
     * the scroll pane, and to calculate the correct position of the preview
     * popup menu.
     */

    public void showUpInCorner(Component c, String corner) {

        if (_viewPort.getComponentCount() == 0) {
            return;
        }

        float scaleFactor = 1.1f; // _viewPort.getComponent( 0 ).getHeight() /
        // _scrollPane.getHeight() * 2;

        // if (_viewPort.getWidth() < (_viewPort.getHeight() * scaleFactor))
        // _ratio =( int ) ( _viewPort.getComponent(0).getWidth() /
        // (_viewPort.getWidth() / scaleFactor) );
        //
        // else
        // _ratio = ( int ) (( _viewPort.getComponent(0).getHeight() /
        // (_viewPort.getHeight()) / scaleFactor ));
        _ratio = (((float) _viewPort.getComponent(0).getHeight() / ((float) _viewPort.getHeight()) / scaleFactor));

        if (_ratio < 2) {
            _ratio = 2;
        }

        // System.out.println( "ratio = " + _ratio );

        int zoomWindowImageWidth = (int) (_viewPort.getComponent(0).getWidth() / _ratio);
        if (zoomWindowImageWidth < 10) {
            UISupport.showInfoMessage("Viewport too large for readable image, use scrollbar instead");
            return;
        }

        int zoomWindowImageHeight = (int) (_viewPort.getComponent(0).getHeight() / _ratio);

        // System.out.println( "ratio = " + _ratio + ", zoomWindowImageWidth = " +
        // zoomWindowImageWidth +
        // ", zoomWindowImageHeight = " + zoomWindowImageHeight);

		/*
         * Image componentImage =
		 * captureComponentViewAsBufferedImage(_viewPort.getComponent(0))
		 * .getScaledInstance( zoomWindowImageWidth, zoomWindowImageHeight,
		 * Image.SCALE_SMOOTH);
		 */
		/*
		 * Based on Shannon Hickey's comments. This is much faster way to scale
		 * instance Thanks! Shannon
		 */
        Image capture = captureComponentViewAsBufferedImage(_viewPort.getComponent(0));
        Image componentImage = new BufferedImage(zoomWindowImageWidth, zoomWindowImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) componentImage.getGraphics();
		/* if you want smoother scaling */
        if (zoomWindowImageWidth > 15) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        g2d.drawImage(capture, 0, 0, zoomWindowImageWidth, zoomWindowImageHeight, null);
        g2d.dispose();

        // Converts the Image to an ImageIcon to be used with a JLabel.
        ImageIcon componentIcon = new ImageIcon(componentImage);

        _iconWidth = componentIcon.getIconWidth();
        _iconHeight = componentIcon.getIconHeight();

        _zoomWindow.setIcon(componentIcon);

        _zoomWindow.setBounds(0, 0, _iconWidth, _iconHeight);

        int cursorWidth = (int) (_viewPort.getWidth() / _ratio);

        int cursorHeight = (int) (_viewPort.getHeight() / _ratio);

        _cursorLabel.setBounds(0, 0, cursorWidth, cursorHeight);

        _layeredPane.setPreferredSize(new Dimension(_iconWidth, _iconHeight));

        int dx = componentIcon.getIconWidth() + DELTA;
        int dy = componentIcon.getIconHeight() + DELTA;

        if (corner.equals(JScrollPane.UPPER_LEFT_CORNER)) {
            ;
        } else if (corner.equals(JScrollPane.UPPER_RIGHT_CORNER)) {
            dx = -dx;
        } else if (corner.equals(JScrollPane.LOWER_RIGHT_CORNER)) {
            dx = -dx;
            dy = -dy;
        } else if (corner.equals(JScrollPane.LOWER_LEFT_CORNER)) {
            dy = -dy;
        }

        if (dy < 0 && Math.abs(dy) > _viewPort.getHeight()) {
            dy = -_viewPort.getHeight() - 10;
        }

        // System.out.println( "Showing at " + dx + ", " + dy );

        // Shows the popup menu at the right place.
        this.show(c, dx, dy);

    }

    public JPanel createCursor() {
        JPanel label = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                Composite composite = ((Graphics2D) g).getComposite();
                ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                super.paintComponent(g);
                ((Graphics2D) g).setComposite(composite);
            }

        };
        label.setBorder(BorderFactory.createLineBorder(Color.gray));
        label.setVisible(false);
        label.setOpaque(true);
        label.setBackground(Color.orange.darker());
        return label;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        // When the mouse enters the preview popup menu, set the visibility
        // of the fake cursor to true.
        _cursorLabel.setVisible(true);
    }

    public void mouseExited(MouseEvent e) {
        // When the mouse exits the preview popup menu, set the visibility
        // of the fake cursor to false.
        _cursorLabel.setVisible(false);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        // When the mouse is released, set the visibility of the preview
        // popup menu to false only if doCloseAfterClick is set to true.
        if (_doCloseAfterClick) {
            this.setVisible(false);
            _cursorLabel.setVisible(false);
            MenuSelectionManager.defaultManager().clearSelectedPath();
            setInvoker(null);
        }
    }

    public void mouseDragged(MouseEvent e) {
        moveCursor(e.getX(), e.getY());
        scrollViewPort();
    }

    public void mouseMoved(MouseEvent e) {
        moveCursor(e.getX(), e.getY());
        scrollViewPort();
    }

    /**
     * Centers the fake cursor (_cursorLabel) position on the coordinates
     * specified in the parameters.
     */
    private void moveCursor(int x, int y) {
        int dx = x - _cursorLabel.getWidth() / 2;
        int dy = y - _cursorLabel.getHeight() / 2;
        _cursorLabel.setLocation(dx, dy);
    }

    /**
     * Scrolls the viewport according to the fake cursor position in the preview
     * popup menu.
     */
    private void scrollViewPort() {
        Point cursorLocation = _cursorLabel.getLocation();
        int dx = (int) Math.max(cursorLocation.getX(), 0);
        int dy = (int) Math.max(cursorLocation.getY(), 0);

        dx = (int) (dx * _ratio);
        dy = (int) (dy * _ratio);

        ((JComponent) _viewPort.getComponent(0)).scrollRectToVisible(new Rectangle(dx, dy, _viewPort.getWidth(),
                _viewPort.getHeight()));
    }

    /**
     * takes a java component and generates an image out of it.
     *
     * @param c the component for which image needs to be generated
     * @return the generated image
     */
    public static BufferedImage captureComponentViewAsBufferedImage(Component c) {
        Dimension size = c.getSize();
        BufferedImage bufferedImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        Graphics bufferedGraphics = bufferedImage.createGraphics();
        c.paint(bufferedGraphics);
        return bufferedImage;
    }

}
