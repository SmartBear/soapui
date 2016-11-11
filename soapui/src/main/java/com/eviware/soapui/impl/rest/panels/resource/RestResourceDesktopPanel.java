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

package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.actions.resource.NewRestMethodAction;
import com.eviware.soapui.impl.rest.panels.component.RestResourceEditor;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import org.apache.commons.lang.mutable.MutableBoolean;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;

public class RestResourceDesktopPanel extends ModelItemDesktopPanel<RestResource> {
    public static final String REST_RESOURCE_EDITOR = "rest-resource-editor";
    // package protected to facilitate unit testing
    JTextField pathTextField;

    private MutableBoolean updating = new MutableBoolean();
    private RestParamsTable paramsTable;

    public RestResourceDesktopPanel(RestResource modelItem) {
        super(modelItem);
        setName(REST_RESOURCE_EDITOR);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private Component buildContent() {
        JTabbedPane tabs = new JTabbedPane();
        paramsTable = new RestParamsTable(getModelItem().getParams(), true, ParamLocation.RESOURCE, true, false);
        tabs.addTab("Resource Parameters", paramsTable);
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
        return super.release();
    }

    private String getName(RestResource modelItem) {
        if (modelItem.getParentResource() != null) {
            return getName(modelItem.getParentResource()) + "/" + modelItem.getName();
        } else {
            return modelItem.getName();
        }
    }

    private Component buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.addFixed(createActionButton(SwingActionDelegate.createDelegate(NewRestMethodAction.SOAPUI_ACTION_ID,
                getModelItem(), null, "/create_empty_method.gif"), true));

        toolbar.addSeparator();

        pathTextField = new RestResourceEditor(getModelItem(), updating);

        toolbar.addFixed(new JLabel("Resource Path"));
        toolbar.addSeparator(new Dimension(3, 3));
        toolbar.addWithOnlyMinimumHeight(pathTextField);

        toolbar.addGlue();
        toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.RESTRESOURCEEDITOR_HELPURL)));

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
        if (evt.getPropertyName().equals("path")) {
            if (!updating.booleanValue()) {
                updating.setValue(true);
                pathTextField.setText(getModelItem().getFullPath());
                updating.setValue(false);
            }
        }
        paramsTable.refresh();
        super.propertyChange(evt);
    }
}
