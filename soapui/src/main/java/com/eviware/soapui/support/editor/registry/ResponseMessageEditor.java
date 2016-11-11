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

package com.eviware.soapui.support.editor.registry;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorDocument;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.EditorView;

/**
 * XmlEditor for the request of a WsdlRequest
 *
 * @author ole.matzura
 */

public class ResponseMessageEditor<T1 extends EditorDocument, T2 extends ModelItem> extends Editor<T1> {
    private final T2 modelItem;

    @SuppressWarnings("unchecked")
    public ResponseMessageEditor(T1 xmlDocument, T2 modelItem) {
        super(xmlDocument);
        this.modelItem = modelItem;

        EditorViewFactory[] editorFactories = EditorViewFactoryRegistry.getInstance().getFactoriesOfType(
                ResponseEditorViewFactory.class);

        for (EditorViewFactory factory : editorFactories) {
            ResponseEditorViewFactory f = (ResponseEditorViewFactory) factory;
            EditorView<T1> editorView = (EditorView<T1>) f.createResponseEditorView(this, modelItem);
            if (editorView != null) {
                addEditorView(editorView);
            }
        }

        InspectorFactory[] inspectorFactories = InspectorRegistry.getInstance().getFactoriesOfType(
                ResponseInspectorFactory.class);

        for (InspectorFactory factory : inspectorFactories) {
            ResponseInspectorFactory f = (ResponseInspectorFactory) factory;
            EditorInspector<T1> inspector = (EditorInspector<T1>) f.createResponseInspector(this, modelItem);
            if (inspector != null) {
                addInspector(inspector);
            }
        }
    }

    public T2 getModelItem() {
        return modelItem;
    }
}
