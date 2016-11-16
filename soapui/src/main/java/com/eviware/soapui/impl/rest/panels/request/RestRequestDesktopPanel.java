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

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseAction;
import com.eviware.soapui.impl.rest.panels.component.RestResourceEditor;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestInterface;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import org.apache.commons.lang.mutable.MutableBoolean;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class RestRequestDesktopPanel extends
        AbstractRestRequestDesktopPanel<RestRequestInterface, RestRequestInterface> {
    public static final String REST_REQUEST_EDITOR = "rest-request-editor";
    protected TextPanelWithTopLabel resourcePanel;
    protected ParametersField queryPanel;
    private JButton addToTestCaseButton;
    private MutableBoolean updating;

    public RestRequestDesktopPanel(RestRequestInterface modelItem) {
        super(modelItem, modelItem);
        setName(REST_REQUEST_EDITOR);
    }

    @Override
    protected void initializeFields() {
        String path = getRequest().getResource().getFullPath();
        updating = new MutableBoolean();
        resourcePanel = new TextPanelWithTopLabel("Resource", path, new RestResourceEditor(getRequest().getResource(), updating));
        queryPanel = new ParametersField(getRequest());
    }

    @Override
    protected void init(RestRequestInterface request) {
        addToTestCaseButton = createActionButton(SwingActionDelegate.createDelegate(
                AddRestRequestToTestCaseAction.SOAPUI_ACTION_ID, getRequest(), null, "/add_to_test_case.png"), true);

        super.init(request);
    }

    protected String getHelpUrl() {
        return HelpUrls.RESTREQUESTEDITOR_HELP_URL;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        addToTestCaseButton.setEnabled(enabled);
    }

    @Override
    protected void addTopToolbarComponents(JXToolBar toolBar) {
        addResourceAndQueryField(toolBar);
    }

    @Override
    protected void addBottomToolbar(JPanel panel) {
        //RestRequestDesktopPanel does not need a bottom tool bar
    }

    @Override
    protected void updateUiValues() {
        if (updating.booleanValue()) {
            return;
        }
        updating.setValue(true);
        resourcePanel.setText(getRequest().getResource().getFullPath());
        queryPanel.updateTextField();
        updating.setValue(false);

    }

    @Override
    protected void insertButtons(JXToolBar toolbar) {
        toolbar.add(addToTestCaseButton);

        JPanel methodPanel = addMethodCombo();
        toolbar.addWithOnlyMinimumHeight(methodPanel);
    }

    private JPanel addMethodCombo() {
        JPanel methodPanel = new JPanel(new BorderLayout());
        JComboBox methodComboBox = new JComboBox(new RestRequestMethodModel(getRequest()));
        methodComboBox.setSelectedItem(getRequest().getMethod());

        JLabel methodLabel = new JLabel("Method");
        methodPanel.add(methodLabel, BorderLayout.NORTH);
        methodPanel.add(methodComboBox, BorderLayout.SOUTH);
        methodPanel.setMinimumSize(new Dimension(75, STANDARD_TOOLBAR_HEIGHT));
        methodPanel.setMaximumSize(new Dimension(75, STANDARD_TOOLBAR_HEIGHT + 10));
        return methodPanel;
    }

    private void addResourceAndQueryField(JXToolBar toolbar) {
        if (!(getRequest() instanceof RestTestRequestInterface)) {

            toolbar.addWithOnlyMinimumHeight(resourcePanel);

            toolbar.add(Box.createHorizontalStrut(4));


            toolbar.addWithOnlyMinimumHeight(queryPanel);
        }
    }

}
