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
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorViewFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;

/**
 * Base XmlEditor class for editing SOAP Messages
 *
 * @author ole.matzura
 */

public abstract class ModelItemXmlEditor<T extends ModelItem, T2 extends XmlDocument> extends XmlEditor<T2> {
    private final T modelItem;

    public ModelItemXmlEditor(T2 xmlDocument, T modelItem) {
        super(xmlDocument);
        this.modelItem = modelItem;
    }

    public T getModelItem() {
        return modelItem;
    }

    public final XmlSourceEditorView getSourceEditor() {
        return (XmlSourceEditorView) getView(XmlSourceEditorViewFactory.VIEW_ID);
    }
}
