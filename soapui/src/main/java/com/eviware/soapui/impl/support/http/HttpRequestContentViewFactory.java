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

package com.eviware.soapui.impl.support.http;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.panels.request.views.content.RestRequestContentView;
import com.eviware.soapui.impl.rest.panels.request.views.content.RestTestRequestContentView;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpRequestMessageEditor;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestInterface;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;

public class HttpRequestContentViewFactory implements RequestEditorViewFactory {
    public final static String VIEW_ID = "HTTP Content";

    public EditorView<?> createRequestEditorView(Editor<?> editor, ModelItem modelItem) {
        if (editor instanceof AbstractHttpXmlRequestDesktopPanel.HttpRequestMessageEditor && modelItem instanceof HttpRequestInterface<?>) {
            if (modelItem instanceof RestTestRequestInterface) {
                return new RestTestRequestContentView((HttpRequestMessageEditor) editor, (RestRequestInterface) modelItem);
            } else if (modelItem instanceof RestRequestInterface) {
                return new RestRequestContentView((HttpRequestMessageEditor) editor, (RestRequestInterface) modelItem);
            } else {
                return new HttpRequestContentView((HttpRequestMessageEditor) editor, (HttpRequestInterface<?>) modelItem);
            }
        }

        return null;
    }

    public String getViewId() {
        return VIEW_ID;
    }
}
