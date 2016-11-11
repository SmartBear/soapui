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

package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestUtils;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

/**
 * A component that displays matrix and query string parameters for a REST request and provides a popup to edit them.
 */
class ParametersField extends JPanel {

    public static final String PARAMETERS_FIELD = "ParametersField";
    private final RestRequestInterface request;
    private final JLabel textLabel;
    private final JTextField textField;
    private int lastSelectedPosition;

    ParametersField(RestRequestInterface request) {
        this.request = request;
        textLabel = new JLabel("Parameters");

        String paramsString = RestUtils.makeSuffixParameterString(request);
        textField = new JTextField(paramsString);
        textField.setEditable(false);
        textField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        textField.setBackground(Color.WHITE);
        textField.setName(PARAMETERS_FIELD);
        setToolTipText(paramsString);
        super.setLayout(new BorderLayout());
        super.add(textLabel, BorderLayout.NORTH);
        super.add(textField, BorderLayout.SOUTH);
        addListeners();
    }

    private void addListeners() {
        textField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                final ParameterFinder finder = new ParameterFinder(textField.getText());
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        openPopup(finder.findParameterAt(lastSelectedPosition));
                    }
                });
            }


        });
        textField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(final CaretEvent e) {
                lastSelectedPosition = e.getDot();
            }

        });
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
        setToolTipText(text);
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        textLabel.setToolTipText(text);
        textField.setToolTipText(text);
    }

    private void openPopup(final String selectedParameter) {
        RestParamsTable restParamsTable = new RestParamsTable(request.getParams(), false, new RestParamsTableModel(
                request.getParams(), RestParamsTableModel.Mode.MINIMAL),
                NewRestResourceActionBase.ParamLocation.RESOURCE, true, true);
        showParametersTableInWindow(restParamsTable, selectedParameter);
    }

    private void showParametersTableInWindow(RestParamsTable restParamsTable, String selectedParameter) {
        PopupWindow popupWindow = new PopupWindow(restParamsTable);
        popupWindow.pack();
        restParamsTable.focusParameter(selectedParameter);
        moveWindowBelowTextField(popupWindow);
        popupWindow.setModal(true);
        popupWindow.setVisible(true);
    }

    private void moveWindowBelowTextField(PopupWindow popupWindow) {
        try {
            Point textFieldLocation = textField.getLocationOnScreen();
            popupWindow.setLocation(textFieldLocation.x, textFieldLocation.y + textField.getHeight());
        } catch (IllegalComponentStateException ignore) {
            // this will happen when the desktop panel is being closed
        }
    }

    public void updateTextField() {
        setText(RestUtils.makeSuffixParameterString(request));
    }

    private class PopupWindow extends JDialog {

        private final JButton closeButton;
        private RestParamsTable restParamsTable;

        private PopupWindow(final RestParamsTable restParamsTable) {
            super(SoapUI.getFrame());
            setResizable(false);
            this.restParamsTable = restParamsTable;
            getContentPane().setLayout(new BorderLayout());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            closeButton = new JButton("Close");
            getRootPane().setDefaultButton(closeButton);
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    close();
                }
            });
            buttonPanel.add(closeButton);
            getContentPane().add(restParamsTable, BorderLayout.CENTER);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            closeButton.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char) KeyEvent.VK_ESCAPE), "closePopup");
            closeButton.getActionMap().put("closePopup", new CloseAction());
        }

        private void close() {
            JTable actualTable = restParamsTable.getParamsTable();
            if (actualTable.isEditing()) {
                actualTable.getCellEditor().stopCellEditing();
            }
            setVisible(false);
            dispose();
        }

        private class CloseAction implements Action {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        }
    }

}
