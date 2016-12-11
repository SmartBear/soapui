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

package com.eviware.soapui.impl.rest.panels.request.inspectors.representations;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.panels.method.RestRepresentationsTable;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;

import javax.swing.JComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractRestRepresentationsInspector extends AbstractXmlInspector implements
        PropertyChangeListener {
    private RestRepresentationsTable representationTable;
    private final RestMethod restMethod;
    private List<RestRepresentation.Type> types;

    protected AbstractRestRepresentationsInspector(RestMethod restMethod, String name, String description,
                                                   RestRepresentation.Type[] types) {
        super(name, description, true, RestRepresentationsInspectorFactory.INSPECTOR_ID);
        this.restMethod = restMethod;
        this.types = Arrays.asList(types);

        restMethod.addPropertyChangeListener("representations", this);
        updateLabel();
    }

    public JComponent getComponent() {
        if (representationTable == null) {
            buildUI();
        }

        return representationTable;
    }

    protected void addToToolbar(JXToolBar toolbar) {
    }

    protected void buildUI() {
        representationTable = new RestRepresentationsTable(restMethod,
                types.toArray(new RestRepresentation.Type[]{}), true) {
            protected JXToolBar buildToolbar() {
                JXToolBar toolbar = super.buildToolbar();
                addToToolbar(toolbar);
                return toolbar;
            }
        };
    }

    public RestMethod getMethod() {
        return restMethod;
    }

    @Override
    public boolean isEnabledFor(EditorView<XmlDocument> view) {
        return !view.getViewId().equals(RawXmlEditorFactory.VIEW_ID);
    }

    public boolean beforeSubmit(Submit submit, SubmitContext context) {
        return true;
    }

    @Override
    public void release() {
        super.release();

        representationTable.release();
        restMethod.removePropertyChangeListener("representations", this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        updateLabel();
    }

    private void updateLabel() {
        int cnt = 0;
        for (RestRepresentation representation : restMethod.getRepresentations()) {
            if (types.contains(representation.getType())) {
                cnt++;
            }
        }

        setTitle("Representations (" + cnt + ")");
    }
}
