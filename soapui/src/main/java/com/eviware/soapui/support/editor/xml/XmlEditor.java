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

package com.eviware.soapui.support.editor.xml;

import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;

/**
 * Editor-framework for Xml Documents
 *
 * @author ole.matzura
 */

@SuppressWarnings("serial")
public abstract class XmlEditor<T extends XmlDocument> extends Editor<T> {
    public XmlEditor(T xmlDocument) {
        super(xmlDocument);
    }

    public boolean saveDocument(boolean validate) {
        XmlEditorView<?> currentView = (XmlEditorView<?>) getCurrentView();
        return currentView == null ? true : currentView.saveDocument(validate);
    }

    @SuppressWarnings("unchecked")
    public abstract XmlSourceEditorView getSourceEditor();


}
