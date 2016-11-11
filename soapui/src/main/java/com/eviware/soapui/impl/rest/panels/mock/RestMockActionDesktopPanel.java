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

package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.config.MockOperationDispatchStyleConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.panels.request.TextPanelWithTopLabel;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.AbstractMockOperationDesktopPanel;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RestMockActionDesktopPanel extends AbstractMockOperationDesktopPanel<RestMockAction> {
    public RestMockActionDesktopPanel(RestMockAction mockOperation) {
        super(mockOperation);
    }

    @Override
    protected String getAddToMockOperationIconPath() {
        return "/addToRestMockAction.gif";
    }

    @Override
    protected Component buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();
        toolbar.setLayout(new BorderLayout());

        Box methodBox = Box.createHorizontalBox();
        methodBox.add(createMethodComboBox());
        methodBox.add(Box.createHorizontalStrut(10));
        toolbar.add(methodBox, BorderLayout.WEST);

        toolbar.add(createResourcePathTextField(), BorderLayout.CENTER);

        toolbar.add(createActionButton(new ShowOnlineHelpAction(getModelItem().getHelpUrl()), true), BorderLayout.EAST);

        return toolbar;
    }

    private JComponent createResourcePathTextField() {
        final JTextField resourcePathEditor = new JTextField();
        resourcePathEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                getModelItem().setResourcePath(resourcePathEditor.getText());
            }
        });
        return new TextPanelWithTopLabel("Resource Path", getModelItem().getResourcePath(), resourcePathEditor);
    }

    private JComponent createMethodComboBox() {
        JPanel comboPanel = new JPanel(new BorderLayout());

        comboPanel.add(new JLabel("Method"), BorderLayout.NORTH);

        final JComboBox methodCombo = new JComboBox(RestRequestInterface.HttpMethod.getMethods());

        methodCombo.setSelectedItem(getModelItem().getMethod());
        methodCombo.setToolTipText("Set desired HTTP method");
        methodCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                getModelItem().setMethod((RestRequestInterface.HttpMethod) methodCombo.getSelectedItem());
            }
        });

        comboPanel.add(methodCombo, BorderLayout.SOUTH);

        return comboPanel;
    }

    protected String[] getAvailableDispatchTypes() {
        return new String[]{
                MockOperationDispatchStyleConfig.SEQUENCE.toString(),
                MockOperationDispatchStyleConfig.SCRIPT.toString()
        };
    }

}

