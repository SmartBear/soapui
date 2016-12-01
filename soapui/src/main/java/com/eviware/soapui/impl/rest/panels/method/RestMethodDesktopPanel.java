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

package com.eviware.soapui.impl.rest.panels.method;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.method.NewRestRequestAction;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;

public class RestMethodDesktopPanel extends ModelItemDesktopPanel<RestMethod> {
    public static final String REST_METHOD_EDITOR = "rest-method-editor";
    private RestParamsTable paramsTable;
    private boolean updatingRequest;
    private JComboBox methodCombo;
    private RestRepresentationsTable restRepresentationsTable;

    public RestMethodDesktopPanel(RestMethod modelItem) {
        super(modelItem);
        setName(REST_METHOD_EDITOR);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private Component buildContent() {
        JTabbedPane tabs = new JTabbedPane();

        paramsTable = new RestParamsTable(getModelItem().getParams(), true, ParamLocation.METHOD, true, false);

        tabs.addTab("Method Parameters", paramsTable);

        restRepresentationsTable = new RestRepresentationsTable(getModelItem(), new RestRepresentation.Type[]{
                RestRepresentation.Type.REQUEST, RestRepresentation.Type.RESPONSE, RestRepresentation.Type.FAULT}, false);

        tabs.addTab("Representations", restRepresentationsTable);

		/*
         * tabs.addTab("Response Representations", new RestRepresentationsTable(
		 * getModelItem(), new RestRepresentation.Type[] {
		 * RestRepresentation.Type.RESPONSE, RestRepresentation.Type.FAULT },
		 * false));
		 */

        return UISupport.createTabPanel(tabs, false);
    }

    @Override
    public String getTitle() {
        return getName(getModelItem());
    }

    public RestParamsTable getParamsTable() {
        return paramsTable;
    }

    @Override
    protected boolean release() {
        paramsTable.release();
        restRepresentationsTable.release();
        return super.release();
    }

    private String getName(RestMethod modelItem) {
        return modelItem.getName();
    }

    private Component buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        methodCombo = new JComboBox(RestRequestInterface.HttpMethod.getMethods());

        methodCombo.setSelectedItem(getModelItem().getMethod());
        methodCombo.setToolTipText("Set desired HTTP method");
        methodCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updatingRequest = true;
                getModelItem().setMethod((RestRequestInterface.HttpMethod) methodCombo.getSelectedItem());
                updatingRequest = false;
            }
        });

        toolbar.addLabeledFixed("HTTP method", methodCombo);
        toolbar.addSeparator();

        toolbar.addFixed(createActionButton(SwingActionDelegate.createDelegate(NewRestRequestAction.SOAPUI_ACTION_ID,
                getModelItem(), null, "/create_empty_request.gif"), true));

        toolbar.addSeparator();

        toolbar.addGlue();
        toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.RESTMETHODEDITOR_HELP_URL)));

        return toolbar;
    }

    @Override
    public boolean dependsOn(ModelItem modelItem) {
        return getModelItem().dependsOn(modelItem);
    }

    public boolean onClose(boolean canCancel) {
        return release();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);

        if (evt.getPropertyName().equals("method") && !updatingRequest) {
            methodCombo.setSelectedItem(evt.getNewValue());
        }

        if (paramsTable != null) {
            paramsTable.refresh();
        }
    }

}
