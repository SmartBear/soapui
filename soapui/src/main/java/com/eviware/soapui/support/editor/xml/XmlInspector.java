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
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.EditorLocationListener;
import com.eviware.soapui.support.editor.EditorView;

/**
 * Inspectors available for the XmlDocument of a XmlEditor
 *
 * @author ole.matzura
 */

public interface XmlInspector extends EditorLocationListener<XmlDocument>, EditorInspector<XmlDocument> {
    public void init(Editor<XmlDocument> editor);

    public Editor<XmlDocument> getEditor();

    public boolean isContentHandler();

    public boolean isEnabledFor(EditorView<XmlDocument> view);
}
