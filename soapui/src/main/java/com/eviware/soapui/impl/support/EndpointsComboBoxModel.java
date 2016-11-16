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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.support.UISupport;

import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * ComboBox model for a request endpoint
 *
 * @author Ole.Matzura
 */

public class EndpointsComboBoxModel implements ComboBoxModel, PropertyChangeListener {
    public static final String ADD_NEW_ENDPOINT = "[add new endpoint..]";
    public static final String EDIT_ENDPOINT = "[edit current..]";
    private static final String DELETE_ENDPOINT = "[delete current]";

    private Set<ListDataListener> listeners = Collections.synchronizedSet(new HashSet<ListDataListener>());

    private String[] endpoints;
    private AbstractHttpRequestInterface<?> request;
    private Document textFieldDocument;

    public EndpointsComboBoxModel(AbstractHttpRequestInterface<?> request) {
        this.request = request;
        initEndpoints();
        request.addPropertyChangeListener(this);
        if (request.getOperation() != null) {
            request.getOperation().getInterface().addPropertyChangeListener(this);
        }
    }

    public AbstractHttpRequestInterface<?> getRequest() {
        return request;
    }

    public void setSelectedItem(Object anItem) {
        final String endpoint = request.getEndpoint();
        final String enteredValue = getEnteredEndpointValue();
        if (anItem != null && anItem.equals(ADD_NEW_ENDPOINT)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String value = UISupport.prompt("Add new endpoint for interface ["
                            + request.getOperation().getInterface().getName() + "]", "Add new endpoint", enteredValue);

                    if (value != null) {
                        if (request.getOperation() != null) {
                            request.getOperation().getInterface().addEndpoint(value);
                        }
                        request.setEndpoint(value);
                    } else {
                        setEditorTextTo(enteredValue);
                    }

                }
            });

        } else if (anItem != null && anItem.equals(EDIT_ENDPOINT)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String value = UISupport.prompt("Edit endpoint for interface ["
                            + request.getOperation().getInterface().getName() + "]", "Edit endpoint", enteredValue);

                    if (value != null) {
                        if (request.getOperation() != null) {
                            request.getOperation().getInterface().changeEndpoint(endpoint, value);
                        }
                        request.setEndpoint(value);
                    } else {
                        setEditorTextTo(enteredValue);
                    }
                }
            });
        } else if (anItem != null && anItem.equals(DELETE_ENDPOINT)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (UISupport.confirm("Delete endpoint [" + endpoint + "]", "Delete endpoint")) {
                        if (request.getOperation() != null) {
                            request.getOperation().getInterface().removeEndpoint(endpoint);
                        }
                        request.setEndpoint(null);
                    }
                }
            });
        } else {
            request.setEndpoint((String) anItem);
        }

        notifyContentsChanged();
    }

    private void setEditorTextTo(String enteredValue) {
        try {
            textFieldDocument.remove(0, textFieldDocument.getLength());
            textFieldDocument.insertString(0, enteredValue, null);
        } catch (BadLocationException ignore) {

        }
    }

    private String getEnteredEndpointValue() {
        try {
            return textFieldDocument.getText(0, textFieldDocument.getLength());
        } catch (BadLocationException ignore) {
            return "";
        }
    }

    public void refresh() {
        initEndpoints();
        notifyContentsChanged();
    }

    protected void initEndpoints() {
        if (request.getOperation() != null) {
            endpoints = request.getOperation().getInterface().getEndpoints();
        } else {
            endpoints = new String[0];
        }
    }

    public void setEndpoints(String[] endpoints) {
        this.endpoints = endpoints;
        notifyContentsChanged();
    }

    public String[] getEndpoints() {
        return endpoints;
    }

    protected void notifyContentsChanged() {
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
        synchronized (listeners) {
            Iterator<ListDataListener> iterator = listeners.iterator();
            while (iterator.hasNext()) {
                iterator.next().contentsChanged(e);
            }
        }
    }

    public Object getSelectedItem() {
        String endpoint = request.getEndpoint();
        return endpoint == null ? "- no endpoint set -" : endpoint;
    }

    public int getSize() {
        return endpoints.length + 3;
    }

    public Object getElementAt(int index) {
        if (index == endpoints.length) {
            return EndpointsComboBoxModel.EDIT_ENDPOINT;
        } else if (index == endpoints.length + 1) {
            return EndpointsComboBoxModel.ADD_NEW_ENDPOINT;
        } else if (index == endpoints.length + 2) {
            return EndpointsComboBoxModel.DELETE_ENDPOINT;
        } else {
            return endpoints[index];
        }
    }

    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        if (propertyName.equals(AbstractHttpRequest.ENDPOINT_PROPERTY)) {
            notifyContentsChanged();
        } else if (propertyName.equals(WsdlInterface.ENDPOINT_PROPERTY)) {
            refresh();
        }
    }

    public void release() {
        request.removePropertyChangeListener(this);
        if (request.getOperation() != null) {
            request.getOperation().getInterface().removePropertyChangeListener(this);
        }
    }

    public void listenToChangesIn(Document textFieldDocument) {
        this.textFieldDocument = textFieldDocument;
    }
}
