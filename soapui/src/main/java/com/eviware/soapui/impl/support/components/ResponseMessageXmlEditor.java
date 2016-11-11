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
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlInspector;

/**
 * XmlEditor for a response-message to a WsdlRequest
 *
 * @author ole.matzura
 */

public class ResponseMessageXmlEditor<T extends ModelItem, T2 extends XmlDocument> extends ModelItemXmlEditor<T, T2> {
    @SuppressWarnings("unchecked")
    public ResponseMessageXmlEditor(T2 xmlDocument, T modelItem) {
        super(xmlDocument, modelItem);

        EditorViewFactory[] editorFactories = EditorViewFactoryRegistry.getInstance().getFactoriesOfType(
                ResponseEditorViewFactory.class);

        for (EditorViewFactory factory : editorFactories) {
            ResponseEditorViewFactory f = (ResponseEditorViewFactory) factory;
            XmlEditorView editorView = (XmlEditorView) f.createResponseEditorView(this, modelItem);
            if (editorView != null) {
                addEditorView((EditorView<T2>) editorView);
            }
        }

        InspectorFactory[] inspectorFactories = InspectorRegistry.getInstance().getFactoriesOfType(
                ResponseInspectorFactory.class);

        for (InspectorFactory factory : inspectorFactories) {
            ResponseInspectorFactory f = (ResponseInspectorFactory) factory;
            XmlInspector inspector = (XmlInspector) f.createResponseInspector(this, modelItem);
            if (inspector != null) {
                addInspector((EditorInspector<T2>) inspector);
            }
        }
    }

    @Override
    public void addEditorView(EditorView<T2> editorView) {
        super.addEditorView(editorView);
    }
}
