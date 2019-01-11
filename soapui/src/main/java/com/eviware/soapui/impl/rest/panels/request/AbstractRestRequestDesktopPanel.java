/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;
import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;

public abstract class AbstractRestRequestDesktopPanel<T extends ModelItem, T2 extends RestRequestInterface> extends
        AbstractHttpXmlRequestDesktopPanel<T, T2> {
    protected static final int STANDARD_TOOLBAR_HEIGHT = 45;

    private InternalTestPropertyListener testPropertyListener = new InternalTestPropertyListener();
    private RestParamPropertyChangeListener restParamPropertyChangeListener = new RestParamPropertyChangeListener();

    public AbstractRestRequestDesktopPanel(T modelItem, T2 requestItem) {
        super(modelItem, requestItem);

        addPropertyChangeListenerToResource(requestItem);

        requestItem.addTestPropertyListener(testPropertyListener);
        requestItem.getOperation().getInterface().addPropertyChangeListener(new EndpointChangeListener());

        for (TestProperty param : requestItem.getParams().getProperties().values()) {
            ((RestParamProperty) param).addPropertyChangeListener(restParamPropertyChangeListener);
        }

    }

    private void addPropertyChangeListenerToResource(T2 requestItem) {
        if (requestItem.getResource() != null) {
            requestItem.getResource().addPropertyChangeListener(this);
            requestItem.getResource().addTestPropertyListener(testPropertyListener);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        updateUiValues();
    }


    @Override
    protected Submit doSubmit() throws SubmitException {
        Analytics.trackAction(SoapUIActions.RUN_REQUEST_FROM_REQUEST_EDITOR, "Type", "REST",
                "HTTPMethod", getRequest().getMethod().name());
        Analytics.trackAction(SoapUIActions.SEND_REQUEST, "HTTPMethod", getRequest().getMethod().name());
        return getRequest().submit(new WsdlSubmitContext(getModelItem()), true);
    }

    @Override
    protected String getHelpUrl() {
        return null;
    }

    @Override
    protected JComponent buildToolbar() {
        if (getRequest().getResource() != null) {
            JPanel panel = new JPanel(new BorderLayout());

            JXToolBar topToolBar = UISupport.createToolbar();

            JComponent submitButton = super.getSubmitButton();
            topToolBar.add(submitButton);
            topToolBar.add(getCancelButton());

            // insertButtons injects different buttons for different editors. It is overridden in other subclasses
            insertButtons(topToolBar);

            JPanel endpointPanel = new JPanel(new BorderLayout());
            endpointPanel.setMinimumSize(new Dimension(75, STANDARD_TOOLBAR_HEIGHT));

            JPanel comboBoxPanel = buildEndpointPanel();

            JLabel endPointLabel = new JLabel("Endpoint");

            endpointPanel.add(endPointLabel, BorderLayout.NORTH);
            endpointPanel.add(comboBoxPanel, BorderLayout.SOUTH);

            topToolBar.add(Box.createHorizontalStrut(4));
            topToolBar.addWithOnlyMinimumHeight(endpointPanel);
            topToolBar.add(Box.createHorizontalStrut(4));

            //Hook for subclasses
            addTopToolbarComponents(topToolBar);

            topToolBar.add(Box.createHorizontalGlue());
            topToolBar.add(getTabsButton());
            topToolBar.add(getSplitButton());
            topToolBar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(getHelpUrl())));
            int maximumPreferredHeight = findMaximumPreferredHeight(topToolBar) + 6;
            topToolBar.setPreferredSize(new Dimension(600, Math.max(maximumPreferredHeight, STANDARD_TOOLBAR_HEIGHT)));

            panel.add(topToolBar, BorderLayout.NORTH);

            //Hook for subclasses
            addBottomToolbar(panel);

            return panel;
        } else {
            return super.buildToolbar();
        }
    }

    protected int findMaximumPreferredHeight(Container parent) {
        int maximum = 0;
        for (Component component : parent.getComponents()) {
            int componentPreferredHeight = component == null || component.getPreferredSize() == null ? 0 : component.getPreferredSize().height;
            maximum = Math.max(maximum, componentPreferredHeight);
        }

        return maximum;
    }


    //Hooks for subclasses
    protected abstract void addTopToolbarComponents(JXToolBar toolBar);

    protected abstract void addBottomToolbar(JPanel panel);

    protected abstract void updateUiValues();

    protected boolean release() {
        if (getRequest().getResource() != null) {
            getRequest().getResource().removePropertyChangeListener(this);
        }

        getRequest().removeTestPropertyListener(testPropertyListener);

        for (TestProperty param : getRequest().getParams().getProperties().values()) {
            ((RestParamProperty) param).removePropertyChangeListener(restParamPropertyChangeListener);
        }

        return super.release();
    }


    private class InternalTestPropertyListener extends TestPropertyListenerAdapter {
        @Override
        public void propertyValueChanged(String name, String oldValue, String newValue) {
            updateUiValues();
        }

        @Override
        public void propertyAdded(String name) {
            updateUiValues();
            RestParamProperty property = getRequest().getParams().getProperty(name);
            property.addPropertyChangeListener(restParamPropertyChangeListener);
        }

        @Override
        public void propertyRemoved(String name) {
            updateUiValues();
        }

        @Override
        public void propertyRenamed(String oldName, String newName) {
            updateUiValues();
        }
    }


    private class RestParamPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                if (evt.getPropertyName().equals(XmlBeansRestParamsTestPropertyHolder.PROPERTY_STYLE)) {
                    RestParamProperty source = (RestParamProperty) evt.getSource();
                    ((AbstractModelItem) source.getModelItem()).notifyPropertyChanged(evt.getPropertyName(),
                            evt.getOldValue(), evt.getNewValue());
                }
            } catch (XmlValueDisconnectedException exception) {
                //Do nothing, it must have been removed by another request editor instance under the same resource/method
            }
            updateUiValues();
        }

    }

    private void addPropertyToLevel(String name, String value, ParameterStyle style, ParamLocation location,
                                    String requestLevelValue) {
        RestParamsPropertyHolder paramsPropertyHolder = null;
        switch (location) {
            case METHOD:
                paramsPropertyHolder = getRequest().getRestMethod().getParams();
                break;
            case RESOURCE:
                paramsPropertyHolder = getRequest().getResource().getParams();
                break;
        }

        if (paramsPropertyHolder != null) {
            paramsPropertyHolder.addProperty(name);
            RestParamProperty addedParameter = paramsPropertyHolder.getProperty(name);
            addedParameter.addPropertyChangeListener(restParamPropertyChangeListener);
            addedParameter.setValue(value);
            addedParameter.setDefaultValue(value);
            addedParameter.setStyle(style);
            //Override the request level value as well
            getRequest().getParams().getProperty(name).setValue(requestLevelValue);
        }
        addPropertyChangeListenerToResource(getRequest());
    }

    private void removePropertyFromLevel(String propertytName, ParamLocation location) {
        switch (location) {
            case METHOD:
                getRequest().getRestMethod().removeProperty(propertytName);
                break;
            case RESOURCE:
                getRequest().getResource().removeProperty(propertytName);
                break;
        }

    }


    private class EndpointChangeListener implements PropertyChangeListener {


        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Interface.ENDPOINT_PROPERTY)) {
                Object currentEndpoint = getEndpointsModel().getSelectedItem();
                if (currentEndpoint != null && currentEndpoint.equals(evt.getOldValue())) {
                    getEndpointsModel().setSelectedItem(evt.getNewValue());
                }
            }
        }
    }
}
