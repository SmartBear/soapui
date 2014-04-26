/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.rest.panels.request.views.json;

import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseMessageEditor;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeResponseMessageEditor;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;

public class JsonResponseViewFactory implements ResponseEditorViewFactory {
    public final static String VIEW_ID = "JSON Response";

    @SuppressWarnings("unchecked")
    public EditorView<?> createResponseEditorView(Editor<?> editor, ModelItem modelItem) {
        if (editor instanceof HttpResponseMessageEditor && modelItem instanceof HttpRequestInterface<?>) {
            return new JsonResponseView((HttpResponseMessageEditor) editor, (HttpRequestInterface<?>) modelItem);
        }
        if (modelItem instanceof MessageExchangeModelItem) {
            return new JsonResponseMessageExchangeView((MessageExchangeResponseMessageEditor) editor,
                    (MessageExchangeModelItem) modelItem);
        }
        return null;
    }

    public String getViewId() {
        return VIEW_ID;
    }
}
