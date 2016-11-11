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

package com.eviware.soapui.security.ui;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

@SuppressWarnings("serial")
public class XPathCellRender extends AbstractCellEditor implements TableCellEditor, MouseListener, WindowFocusListener {

    protected JPanel panel;
    JTextArea textArea;

    private JTextField textField;
    protected JFrame frame;
    private JButton resizeBtn;
    protected int mouseX;
    protected int mouseY;

    public XPathCellRender() {

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        textArea = new JTextArea(4, 5);
        textArea.setWrapStyleWord(true);

        textArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    // case KeyEvent.VK_ENTER :
                    // textField.setText( textArea.getText() );
                    // frame.setVisible( false );
                    // break;
                    case KeyEvent.VK_ESCAPE:
                        frame.setVisible(false);
                        break;
                }
            }
        });

        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(200, 100));
        panel.setMinimumSize(new Dimension(200, 100));
        JXToolBar toolbar = initToolbar(UISupport.createToolbar());
        panel.add(toolbar, BorderLayout.SOUTH);

        this.frame = new JFrame();
        this.frame.addWindowFocusListener(this);
        this.frame.setContentPane(panel);
        this.frame.setAlwaysOnTop(true);
        this.frame.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        this.frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.frame.setUndecorated(true);
        textField = new JTextField();
        textField.addMouseListener(this);

    }

    /**
     * @param jxToolBar
     * @return
     */
    protected JXToolBar initToolbar(JXToolBar toolbar) {

        resizeBtn = UISupport.createToolbarButton(UISupport.createImageIcon("/icon_resize.gif"));
        resizeBtn.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
        resizeBtn.setContentAreaFilled(false);
        resizeBtn.setBorder(null);
        resizeBtn.setToolTipText("Drag to resize...");
        resizeBtn.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                frame.setSize(frame.getWidth() - mouseX + e.getX(), frame.getHeight() - mouseY + e.getY());
            }
        });

        resizeBtn.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            ;

        });

        toolbar.add(UISupport.createToolbarButton(new SaveXPathAction()), true);
        toolbar.add(UISupport.createToolbarButton(new CancelXPathAction()), true);
        toolbar.addGlue();
        toolbar.add(resizeBtn);
        toolbar.setFloatable(false);
        return toolbar;
    }

    private class SaveXPathAction extends AbstractAction {

        public SaveXPathAction() {
            super("Save");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/disk_multiple.png"));
            putValue(Action.SHORT_DESCRIPTION, "Save XPath");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            textField.setText(textArea.getText());
            frame.setVisible(false);
        }

    }

    private class CancelXPathAction extends AbstractAction {

        public CancelXPathAction() {
            super("Cancel");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/rest_method.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Cancel Changes");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            frame.setVisible(false);
        }

    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        String val = (String) table.getModel().getValueAt(row, column);
        textField.setText(val);
        return textField;
    }

    @Override
    public Object getCellEditorValue() {
        return textField.getText();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!frame.isVisible()) {
            textArea.setText(textField.getText().replaceAll(";", ";\n"));
            Point position = textField.getLocationOnScreen();
            frame.setBounds(position.x, position.y, frame.getWidth(), frame.getHeight());
            frame.pack();
            frame.setVisible(true);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        frame.setVisible(false);
    }

}
