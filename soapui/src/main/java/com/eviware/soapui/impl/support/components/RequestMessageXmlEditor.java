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

package com.eviware.soapui.impl.support.components;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.EditorViewFactory;
import com.eviware.soapui.support.editor.registry.EditorViewFactoryRegistry;
import com.eviware.soapui.support.editor.registry.InspectorFactory;
import com.eviware.soapui.support.editor.registry.InspectorRegistry;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlInspector;

/**
 * XmlEditor for the request of a WsdlRequest
 *
 * @author ole.matzura
 */

public class RequestMessageXmlEditor<T extends ModelItem, T2 extends XmlDocument> extends ModelItemXmlEditor<T, T2> {
    @SuppressWarnings("unchecked")
    public RequestMessageXmlEditor(T2 xmlDocument, T modelItem) {
        super(xmlDocument, modelItem);

        EditorViewFactory[] editorFactories = EditorViewFactoryRegistry.getInstance().getFactoriesOfType(
                RequestEditorViewFactory.class);

        for (EditorViewFactory factory : editorFactories) {
            RequestEditorViewFactory f = (RequestEditorViewFactory) factory;
            XmlEditorView editorView = (XmlEditorView) f.createRequestEditorView(this, modelItem);
            if (editorView != null) {
                addEditorView((EditorView<T2>) editorView);
            }
        }

        InspectorFactory[] inspectorFactories = InspectorRegistry.getInstance().getFactoriesOfType(
                RequestInspectorFactory.class);

        for (InspectorFactory factory : inspectorFactories) {
            RequestInspectorFactory f = (RequestInspectorFactory) factory;
            XmlInspector inspector = (XmlInspector) f.createRequestInspector(this, modelItem);
            if (inspector != null) {
                addInspector((EditorInspector<T2>) inspector);
            }
        }
    }
}
